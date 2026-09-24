package com.example.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.data.model.CityLocation
import com.example.data.model.LocationState
import com.example.data.model.UserLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

/**
 * LocationRepository uses [FusedLocationProviderClient] to fetch real-time GPS coordinates,
 * performs reverse geocoding to retrieve postal address/locality details, and ensures
 * proper runtime permission handling.
 */
class LocationRepository(
    private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
) {

    val indianCities = listOf(
        CityLocation("Delhi", "National Capital Territory", 28.6139, 77.2090),
        CityLocation("Mumbai", "Maharashtra", 19.0760, 72.8777),
        CityLocation("Bengaluru", "Karnataka", 12.9716, 77.5946),
        CityLocation("Kolkata", "West Bengal", 22.5726, 88.3639),
        CityLocation("Chennai", "Tamil Nadu", 13.0827, 80.2707),
        CityLocation("Hyderabad", "Telangana", 17.3850, 78.4867),
        CityLocation("Ahmedabad", "Gujarat", 23.0225, 72.5714),
        CityLocation("Pune", "Maharashtra", 18.5204, 73.8567),
        CityLocation("Jaipur", "Rajasthan", 26.9124, 75.7873),
        CityLocation("Lucknow", "Uttar Pradesh", 26.8467, 80.9462),
        CityLocation("Patna", "Bihar", 25.5941, 85.1376),
        CityLocation("Gopalganj", "Bihar", 26.4674, 84.4447),
        CityLocation("West Champaran", "Bihar", 26.8028, 84.5167),
        CityLocation("Vaishali", "Bihar", 25.6858, 85.2146),
        CityLocation("Madhubani", "Bihar", 26.3562, 86.0718),
        CityLocation("Supaul", "Bihar", 26.1260, 86.6044),
        CityLocation("Buxar", "Bihar", 25.5647, 83.9777),
        CityLocation("Bhagalpur", "Bihar", 25.2425, 86.9842),
        CityLocation("Bhopal", "Madhya Pradesh", 23.2599, 77.4126),
        CityLocation("Guwahati", "Assam", 26.1445, 91.7362),
        CityLocation("Bhubaneswar", "Odisha", 20.2961, 85.8245),
        CityLocation("Kochi", "Kerala", 9.9312, 76.2673),
        CityLocation("Chandigarh", "Punjab / Haryana", 30.7333, 76.7794),
        CityLocation("Srinagar", "Jammu & Kashmir", 34.0837, 74.7973),
        CityLocation("Shimla", "Himachal Pradesh", 31.1048, 77.1734),
        CityLocation("Dehradun", "Uttarakhand", 30.3165, 78.0322),
        CityLocation("Ranchi", "Jharkhand", 23.3441, 85.3096)
    )

    /**
     * Checks if either FINE or COARSE location permission is granted.
     */
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

    /**
     * Fetches current location using FusedLocationProviderClient and reverse geocodes the address.
     * Returns [LocationState] representing Success, PermissionRequired, or Error.
     */
    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): LocationState = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) {
            return@withContext LocationState.PermissionRequired
        }

        try {
            val cancellationTokenSource = CancellationTokenSource()

            // 1. Try High Accuracy Current Location
            var location: Location? = try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                ).await()
            } catch (e: Exception) {
                null
            }

            // 2. Fallback to last known location if current is null
            if (location == null) {
                location = try {
                    fusedLocationClient.lastLocation.await()
                } catch (e: Exception) {
                    null
                }
            }

            if (location == null) {
                return@withContext LocationState.Error("Unable to acquire GPS fix. Please ensure location services are enabled.")
            }

            val userLocation = reverseGeocodeLocation(location)
            LocationState.Success(userLocation)
        } catch (e: SecurityException) {
            LocationState.PermissionRequired
        } catch (e: Exception) {
            LocationState.Error(e.localizedMessage ?: "Failed to fetch location")
        }
    }

    /**
     * Exposes location fetching as a reactive Flow stream.
     */
    fun getLocationFlow(): Flow<LocationState> = flow {
        emit(LocationState.Loading)
        val result = fetchCurrentLocation()
        emit(result)
    }.flowOn(Dispatchers.IO)

    /**
     * Performs reverse geocoding on GPS coordinates to extract detailed address, locality, and district.
     */
    private suspend fun reverseGeocodeLocation(location: Location): UserLocation = withContext(Dispatchers.IO) {
        var locality = ""
        var subAdminArea = ""
        var adminArea = "India"
        var postalCode = ""
        var fullAddress = ""
        var country = "India"

        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())

                val addresses: List<Address>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        ) { resultAddresses ->
                            continuation.resume(resultAddresses)
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    geocoder.getFromLocation(location.latitude, location.longitude, 1)
                }

                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    locality = addr.locality ?: addr.subLocality ?: addr.subAdminArea ?: ""
                    subAdminArea = addr.subAdminArea ?: ""
                    adminArea = addr.adminArea ?: "India"
                    postalCode = addr.postalCode ?: ""
                    country = addr.countryName ?: "India"

                    val addressLines = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }
                    fullAddress = addressLines.joinToString(", ")
                }
            }
        } catch (e: Exception) {
            // Reverse geocoding network/IO error; keep default empty fields
        }

        // Fallback display locality
        val resolvedLocality = when {
            locality.isNotBlank() -> locality
            subAdminArea.isNotBlank() -> subAdminArea
            adminArea.isNotBlank() -> adminArea
            else -> "My Location"
        }

        val resolvedAddress = when {
            fullAddress.isNotBlank() -> fullAddress
            locality.isNotBlank() && adminArea.isNotBlank() -> "$locality, $adminArea"
            else -> "${String.format(Locale.US, "%.4f", location.latitude)}°N, ${String.format(Locale.US, "%.4f", location.longitude)}°E"
        }

        UserLocation(
            latitude = location.latitude,
            longitude = location.longitude,
            address = resolvedAddress,
            locality = resolvedLocality,
            subAdminArea = subAdminArea,
            adminArea = adminArea,
            postalCode = postalCode,
            country = country,
            timestamp = location.time.takeIf { it > 0 } ?: System.currentTimeMillis()
        )
    }

    /**
     * Backward-compatible helper to get CityLocation for weather queries
     */
    suspend fun getCurrentGpsLocation(): CityLocation? {
        val state = fetchCurrentLocation()
        return if (state is LocationState.Success) {
            state.location.toCityLocation()
        } else {
            null
        }
    }
}
