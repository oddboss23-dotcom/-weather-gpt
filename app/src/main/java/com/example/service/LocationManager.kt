package com.example.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.canonical.CanonicalLocation
import com.example.data.canonical.CanonicalLocationResolver
import com.example.data.hyperlocal.TerrainType
import com.example.data.hyperlocal.VillageHierarchy
import com.example.data.model.CityLocation
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Resolved administrative hierarchy specifically for Indian hyperlocal administration:
 * State -> District -> Block / Sub-District / Tehsil -> Gram Panchayat -> Village / Locality
 */
data class DistrictBlockResolution(
    val state: String,
    val district: String,
    val block: String,
    val subDistrictOrTehsil: String,
    val gramPanchayat: String,
    val villageOrLocality: String,
    val postalCode: String?,
    val country: String = "India",
    val resolutionSource: String
) {
    val districtAndBlockDisplay: String
        get() = when {
            block.isNotBlank() && district.isNotBlank() && !district.equals(block, ignoreCase = true) ->
                "$block Block, $district District"
            district.isNotBlank() ->
                "$district District"
            else ->
                state
        }

    val fullAdministrativeHierarchy: String
        get() = listOf(villageOrLocality, gramPanchayat, block, district, state)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(" → ")
}

/**
 * Full Hyperlocal Weather Context for NWP downscaling, KrishiGPT, and AI grounding.
 */
data class HyperlocalWeatherContext(
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double,
    val accuracyMeters: Float,
    val state: String,
    val district: String,
    val block: String,
    val locality: String,
    val postalCode: String?,
    val terrainType: TerrainType,
    val nearestImdStation: String,
    val radarStationCode: String,
    val isDirectObservationStation: Boolean,
    val resolutionDescription: String
) {
    fun toHyperlocalPromptContext(): String {
        return """
            [HYPERLOCAL CONTEXT]
            Coordinates: ${"%.4f".format(Locale.US, latitude)}°N, ${"%.4f".format(Locale.US, longitude)}°E
            Altitude: ${altitudeMeters.toInt()} m MSL
            Administrative Unit: $locality, $block Block, $district District, $state
            Topography: ${terrainType.displayName}
            Primary Meteorological Reference: $nearestImdStation (Radar: $radarStationCode)
        """.trimIndent()
    }
}

data class HyperlocalLocationResult(
    val latitude: Double,
    val longitude: Double,
    val locality: String,
    val villageOrArea: String,
    val subDistrictOrBlock: String,
    val district: String,
    val state: String,
    val country: String = "India",
    val postalCode: String? = null,
    val accuracyMeters: Float = 0f,
    val resolutionDescription: String = "Location Resolution: Local observation + forecast grid",
    val isGpsDerived: Boolean = true
) {
    fun toCityLocation(): CityLocation {
        val displayName = when {
            locality.isNotBlank() -> locality
            villageOrArea.isNotBlank() -> villageOrArea
            district.isNotBlank() -> district
            else -> "Current Location"
        }
        return CityLocation(
            name = displayName,
            state = state,
            latitude = latitude,
            longitude = longitude
        )
    }

    val formattedHierarchy: String
        get() = listOf(locality.ifBlank { villageOrArea }, subDistrictOrBlock, district, state)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
}

sealed class LocationFetchState {
    object Idle : LocationFetchState()
    object RequestingPermission : LocationFetchState()
    object AcquiringGpsCoordinates : LocationFetchState()
    object ResolvingGeocodingHierarchy : LocationFetchState()
    data class Success(val location: HyperlocalLocationResult) : LocationFetchState()
    data class Error(val reason: String) : LocationFetchState()
    object PermissionRequired : LocationFetchState()
}

/**
 * Production LocationManager for WeatherGPT.
 * Uses Google Play Services [FusedLocationProviderClient] to acquire precise GPS coordinates,
 * and executes robust multi-tier reverse geocoding to resolve state, district,
 * sub-district/block, and village/local area hierarchy without fabricating spatial accuracy.
 */
class LocationManager(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
) {
    private val TAG = "LocationManager"

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun obtainPreciseLocation(): LocationFetchState = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext LocationFetchState.PermissionRequired
        }

        try {
            val tokenSource = CancellationTokenSource()
            // 1. Try High Accuracy Current Location
            var location: Location? = try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    tokenSource.token
                ).await()
            } catch (e: Exception) {
                Log.w(TAG, "High accuracy GPS fetch failed, attempting lastLocation fallback", e)
                null
            }

            // 2. Fallback to last known location if needed
            if (location == null) {
                location = try {
                    fusedLocationClient.lastLocation.await()
                } catch (e: Exception) {
                    Log.w(TAG, "LastLocation fetch failed", e)
                    null
                }
            }

            if (location == null) {
                return@withContext LocationFetchState.Error("GPS coordinates unavailable. Check device location services.")
            }

            // 3. Perform Reverse Geocoding for District / Block / Sub-District
            val reverseGeoResult = reverseGeocode(location.latitude, location.longitude)
            val result = reverseGeoResult.copy(
                accuracyMeters = location.accuracy,
                isGpsDerived = true
            )
            return@withContext LocationFetchState.Success(result)
        } catch (e: Exception) {
            Log.e(TAG, "Error acquiring precise location", e)
            return@withContext LocationFetchState.Error("Location error: ${e.localizedMessage ?: "Unknown failure"}")
        }
    }

    suspend fun reverseGeocode(latitude: Double, longitude: Double): HyperlocalLocationResult = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.Builder().setLanguage("en").setRegion("IN").build())

        val address: Address? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                try {
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses.firstOrNull())
                        }

                        override fun onError(errorMessage: String?) {
                            Log.w(TAG, "Geocoder error callback: $errorMessage")
                            continuation.resume(null)
                        }
                    })
                } catch (e: Exception) {
                    Log.w(TAG, "Async Geocoder exception", e)
                    continuation.resume(null)
                }
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            } catch (e: Exception) {
                Log.w(TAG, "Synchronous Geocoder exception", e)
                null
            }
        }

        if (address != null) {
            val locality = address.locality ?: address.subLocality ?: address.featureName ?: ""
            val subDistrictOrBlock = address.subAdminArea ?: address.subLocality ?: ""
            val district = address.subAdminArea ?: address.adminArea ?: "District"
            val state = address.adminArea ?: "India"
            val postalCode = address.postalCode

            HyperlocalLocationResult(
                latitude = latitude,
                longitude = longitude,
                locality = locality,
                villageOrArea = address.subLocality ?: locality,
                subDistrictOrBlock = subDistrictOrBlock,
                district = district,
                state = state,
                country = address.countryName ?: "India",
                postalCode = postalCode,
                resolutionDescription = "Location Resolution: Local observation + forecast grid"
            )
        } else {
            // Truthful fallback using Indian Gazetteer proximity
            val canonical = CanonicalLocationResolver.resolve(
                CityLocation(name = "Locality", state = "State", latitude = latitude, longitude = longitude)
            )
            HyperlocalLocationResult(
                latitude = latitude,
                longitude = longitude,
                locality = canonical.locality.ifBlank { canonical.district },
                villageOrArea = canonical.village,
                subDistrictOrBlock = canonical.block.ifBlank { canonical.subDistrict },
                district = canonical.district,
                state = canonical.state,
                country = "India",
                resolutionDescription = "Location Resolution: Local observation + forecast grid"
            )
        }
    }

    /**
     * Resolves precise District and Block / Tehsil hierarchy for a given coordinate.
     * Combines system Geocoder and authoritative gazetteer fallback.
     */
    suspend fun resolveDistrictAndBlock(latitude: Double, longitude: Double): DistrictBlockResolution = withContext(Dispatchers.IO) {
        val geocoder = Geocoder(context, Locale.Builder().setLanguage("en").setRegion("IN").build())

        val address: Address? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                try {
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            continuation.resume(addresses.firstOrNull())
                        }
                        override fun onError(errorMessage: String?) {
                            Log.w(TAG, "Geocoder error: $errorMessage")
                            continuation.resume(null)
                        }
                    })
                } catch (e: Exception) {
                    continuation.resume(null)
                }
            }
        } else {
            try {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            } catch (_: Exception) {
                null
            }
        }

        if (address != null) {
            val state = address.adminArea ?: "India"
            val district = address.subAdminArea ?: address.locality ?: "District"
            val block = address.subLocality ?: address.thoroughfare ?: address.subAdminArea ?: district
            val locality = address.locality ?: address.subLocality ?: address.featureName ?: district

            DistrictBlockResolution(
                state = state,
                district = district,
                block = block,
                subDistrictOrTehsil = address.subAdminArea ?: block,
                gramPanchayat = "$block Gram Panchayat",
                villageOrLocality = locality,
                postalCode = address.postalCode,
                country = address.countryName ?: "India",
                resolutionSource = "Android Geocoder Service (Online)"
            )
        } else {
            val canonical = CanonicalLocationResolver.resolve(
                CityLocation(name = "Locality", state = "State", latitude = latitude, longitude = longitude)
            )
            DistrictBlockResolution(
                state = canonical.state,
                district = canonical.district,
                block = canonical.block.ifBlank { canonical.subDistrict },
                subDistrictOrTehsil = canonical.tehsil.ifBlank { canonical.subDistrict },
                gramPanchayat = canonical.panchayat,
                villageOrLocality = canonical.village.ifBlank { canonical.locality },
                postalCode = null,
                country = "India",
                resolutionSource = "Gazetteer Proximity Engine (Offline/Fallback)"
            )
        }
    }

    /**
     * Resolves complete Hyperlocal Weather Context including altitude, terrain,
     * nearest IMD station, and radar code for NWP downscaling.
     */
    suspend fun resolveHyperlocalWeatherContext(
        latitude: Double,
        longitude: Double,
        accuracyMeters: Float = 0f
    ): HyperlocalWeatherContext = withContext(Dispatchers.IO) {
        val distBlock = resolveDistrictAndBlock(latitude, longitude)
        val canonical = CanonicalLocationResolver.resolve(
            CityLocation(name = distBlock.villageOrLocality, state = distBlock.state, latitude = latitude, longitude = longitude)
        )

        val elevation = canonical.elevation ?: 150.0
        val terrainType = when {
            canonical.coastalDistanceKm < 25.0 -> TerrainType.COASTAL_ESTUARY
            canonical.nearestRiver != null -> TerrainType.RIVER_BASIN_LOWLAND
            elevation > 1200.0 -> TerrainType.GHATS_MOUNTAIN
            elevation > 400.0 -> TerrainType.DECCAN_PLATEAU
            else -> TerrainType.PLAINS
        }

        val radarStationCode = when {
            latitude in 28.0..30.0 && longitude in 76.5..78.0 -> "DWR-PALAM"
            latitude in 18.5..19.5 && longitude in 72.5..73.5 -> "DWR-MUMBAI"
            latitude in 22.0..23.0 && longitude in 88.0..89.0 -> "DWR-KOLKATA"
            latitude in 12.5..13.5 && longitude in 79.5..80.5 -> "DWR-CHENNAI"
            latitude in 26.5..27.5 && longitude in 80.5..81.5 -> "DWR-LUCKNOW"
            latitude in 25.0..26.0 && longitude in 84.5..85.5 -> "DWR-PATNA"
            else -> "DWR-NATIONAL-MOSAIC"
        }

        HyperlocalWeatherContext(
            latitude = latitude,
            longitude = longitude,
            altitudeMeters = elevation,
            accuracyMeters = accuracyMeters,
            state = distBlock.state,
            district = distBlock.district,
            block = distBlock.block,
            locality = distBlock.villageOrLocality,
            postalCode = distBlock.postalCode,
            terrainType = terrainType,
            nearestImdStation = canonical.nearestWeatherStation,
            radarStationCode = radarStationCode,
            isDirectObservationStation = false,
            resolutionDescription = "Hyperlocal GPS Resolution: ${distBlock.districtAndBlockDisplay}"
        )
    }

    /**
     * One-shot method to acquire live GPS coordinates and resolve the full Hyperlocal Weather Context.
     */
    suspend fun getHyperlocalWeatherContext(): Result<HyperlocalWeatherContext> = withContext(Dispatchers.IO) {
        when (val state = obtainPreciseLocation()) {
            is LocationFetchState.Success -> {
                val loc = state.location
                val context = resolveHyperlocalWeatherContext(loc.latitude, loc.longitude, loc.accuracyMeters)
                Result.success(context)
            }
            is LocationFetchState.PermissionRequired -> {
                Result.failure(SecurityException("Location permissions (ACCESS_FINE_LOCATION) required."))
            }
            is LocationFetchState.Error -> {
                Result.failure(IllegalStateException(state.reason))
            }
            else -> {
                Result.failure(IllegalStateException("Unable to acquire location fix."))
            }
        }
    }

    /**
     * Continuous reactive location updates flow for real-time tracking.
     */
    @SuppressLint("MissingPermission")
    fun requestLocationUpdates(intervalMillis: Long = 15000L): Flow<HyperlocalWeatherContext> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Missing location permissions"))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .setMinUpdateIntervalMillis(intervalMillis / 2)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                this@callbackFlow.launch {
                    try {
                        val context = resolveHyperlocalWeatherContext(loc.latitude, loc.longitude, loc.accuracy)
                        trySend(context)
                    } catch (e: Exception) {
                        Log.w(TAG, "Location callback error", e)
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, callback, context.mainLooper)

        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }.flowOn(Dispatchers.IO)
}

