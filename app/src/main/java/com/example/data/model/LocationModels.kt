package com.example.data.model

import java.util.Locale

/**
 * Encapsulates detailed GPS coordinates and reverse-geocoded postal/locality metadata.
 */
data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String = "",
    val locality: String = "",
    val subAdminArea: String = "",
    val adminArea: String = "",
    val postalCode: String = "",
    val country: String = "India",
    val timestamp: Long = System.currentTimeMillis()
) {
    /**
     * Converts UserLocation to CityLocation format for weather forecasting engines.
     */
    fun toCityLocation(): CityLocation {
        val cityName = when {
            locality.isNotBlank() -> locality
            subAdminArea.isNotBlank() -> subAdminArea
            adminArea.isNotBlank() -> adminArea
            else -> "GPS Location"
        }
        val stateName = when {
            adminArea.isNotBlank() -> adminArea
            country.isNotBlank() -> country
            else -> "India"
        }
        return CityLocation(
            name = cityName,
            state = stateName,
            latitude = latitude,
            longitude = longitude
        )
    }

    fun formattedCoordinates(): String {
        return "${String.format(Locale.US, "%.4f", latitude)}°N, ${String.format(Locale.US, "%.4f", longitude)}°E"
    }
}

/**
 * Sealed class representing the reactive lifecycle of location acquisition.
 */
sealed class LocationState {
    object Idle : LocationState()
    object Loading : LocationState()
    data class Success(val location: UserLocation) : LocationState()
    object PermissionRequired : LocationState()
    data class Error(val message: String) : LocationState()
}
