package com.example.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.example.data.local.RuralOfflineCacheEntity
import com.example.data.local.WeatherGPTDatabase
import com.example.data.model.DisasterAlert
import com.example.data.model.HourlyForecast
import com.example.data.model.WeatherData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DataMode(val badgeLabel: String, val description: String) {
    LIVE("LIVE DATA", "Real-time IMD AWS telemetry & live satellite sync"),
    CACHED("CACHED DATA", "Verified offline cache for rural continuity"),
    ESTIMATED("ESTIMATED DATA", "Interpolated from adjacent station / 4km NWP grid")
}

data class RuralOfflineState(
    val dataMode: DataMode = DataMode.LIVE,
    val isOnline: Boolean = true,
    val lastSyncTimestamp: Long = System.currentTimeMillis(),
    val cacheAgeMinutes: Long = 0,
    val cachedHierarchyPath: String = "",
    val cachedStationInfo: String = "",
    val isAutoSyncActive: Boolean = false
) {
    val statusDisplayText: String
        get() = when (dataMode) {
            DataMode.LIVE -> "● LIVE DATA (Realtime)"
            DataMode.CACHED -> {
                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(lastSyncTimestamp))
                "● CACHED DATA (Sync at $timeStr • Offline Rural Mode)"
            }
            DataMode.ESTIMATED -> "● ESTIMATED DATA (From nearest station grid)"
        }
}

class OfflineRuralManager(
    private val context: Context,
    private val db: WeatherGPTDatabase,
    private val scope: CoroutineScope
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _ruralState = MutableStateFlow(RuralOfflineState())
    val ruralState: StateFlow<RuralOfflineState> = _ruralState

    private var onNetworkRestoredCallback: (() -> Unit)? = null

    init {
        checkInitialConnectivity()
        registerNetworkCallback()
    }

    fun setOnNetworkRestoredListener(listener: () -> Unit) {
        onNetworkRestoredCallback = listener
    }

    private fun checkInitialConnectivity() {
        val activeNetwork = connectivityManager?.activeNetwork
        val caps = connectivityManager?.getNetworkCapabilities(activeNetwork)
        val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _ruralState.value = _ruralState.value.copy(
            isOnline = isConnected,
            dataMode = if (isConnected) DataMode.LIVE else DataMode.CACHED
        )
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager?.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                scope.launch(Dispatchers.Main) {
                    val wasOffline = !_ruralState.value.isOnline
                    _ruralState.value = _ruralState.value.copy(
                        isOnline = true,
                        dataMode = DataMode.LIVE,
                        isAutoSyncActive = true
                    )
                    if (wasOffline) {
                        onNetworkRestoredCallback?.invoke()
                    }
                }
            }

            override fun onLost(network: Network) {
                scope.launch(Dispatchers.Main) {
                    _ruralState.value = _ruralState.value.copy(
                        isOnline = false,
                        dataMode = DataMode.CACHED
                    )
                }
            }
        })
    }

    fun markDataAsEstimated(stationName: String, distanceKm: Double) {
        _ruralState.value = _ruralState.value.copy(
            dataMode = DataMode.ESTIMATED,
            cachedStationInfo = "$stationName (${String.format(Locale.US, "%.1f", distanceKm)} km)"
        )
    }

    fun markDataAsLive() {
        if (_ruralState.value.isOnline) {
            _ruralState.value = _ruralState.value.copy(dataMode = DataMode.LIVE)
        }
    }

    fun persistRuralCache(
        locationKey: String,
        weather: WeatherData,
        hourlyList: List<HourlyForecast>,
        alerts: List<DisasterAlert>,
        krishiSummary: String,
        localLangAdvice: String,
        hierarchyPath: String,
        isEstimated: Boolean,
        stationName: String,
        stationDistanceKm: Double
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val now = System.currentTimeMillis()
                val entity = RuralOfflineCacheEntity(
                    locationKey = locationKey,
                    cityName = weather.cityName,
                    forecast48hSummary = "Temp: ${weather.temperatureC}°C, Max: ${weather.tempMaxC}°C, Min: ${weather.tempMinC}°C, Rain: ${weather.expectedRainfallMm} mm, RainProb: ${weather.rainProbabilityPercent}%, Wind: ${weather.windSpeedKmh} km/h",
                    disasterAlertsJson = alerts.joinToString(";;") { "${it.title}|${it.severity}|${it.expectedImpact}|${it.recommendedAction}" },
                    krishiAdvisoryJson = krishiSummary,
                    localLanguageAdvice = localLangAdvice,
                    savedHierarchyPath = hierarchyPath,
                    isEstimatedFromGrid = isEstimated,
                    nearbyStationName = stationName,
                    stationDistanceKm = stationDistanceKm,
                    cacheTimestamp = now
                )
                db.weatherDao().cacheRuralOfflineData(entity)
                _ruralState.value = _ruralState.value.copy(
                    lastSyncTimestamp = now,
                    cacheAgeMinutes = 0,
                    cachedHierarchyPath = hierarchyPath,
                    cachedStationInfo = "$stationName (${String.format(Locale.US, "%.1f", stationDistanceKm)} km)",
                    isAutoSyncActive = false
                )
            } catch (_: Exception) {}
        }
    }

    suspend fun loadRuralCache(locationKey: String): RuralOfflineCacheEntity? {
        return try {
            val cache = db.weatherDao().getRuralOfflineData(locationKey)
            if (cache != null) {
                val ageMins = ((System.currentTimeMillis() - cache.cacheTimestamp) / (1000 * 60)).coerceAtLeast(0)
                _ruralState.value = _ruralState.value.copy(
                    dataMode = if (cache.isEstimatedFromGrid) DataMode.ESTIMATED else DataMode.CACHED,
                    lastSyncTimestamp = cache.cacheTimestamp,
                    cacheAgeMinutes = ageMins,
                    cachedHierarchyPath = cache.savedHierarchyPath,
                    cachedStationInfo = "${cache.nearbyStationName} (${String.format(Locale.US, "%.1f", cache.stationDistanceKm)} km)"
                )
            }
            cache
        } catch (_: Exception) {
            null
        }
    }
}
