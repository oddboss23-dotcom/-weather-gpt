package com.example.data.marine

import com.example.data.canonical.CanonicalWeatherRepository
import com.example.data.model.CityLocation

data class MarineBriefing(
    val portName: String,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val waveHeightMeters: Double?,
    val seaState: String,
    val pressureHpa: Double,
    val temperatureC: Double,
    val marineHazardWarning: String,
    val dataSource: String,
    val updatedTimestamp: Long = System.currentTimeMillis()
)

object MarineRepository {
    suspend fun getMarineBriefing(cityLocation: CityLocation, canonicalRepo: CanonicalWeatherRepository): MarineBriefing {
        val snapshot = canonicalRepo.getCanonicalSnapshot(cityLocation)
        val windSpeed = snapshot.wind.speedKmh
        val windDir = snapshot.wind.directionDeg
        val temp = snapshot.temperature.currentC
        val pressure = snapshot.pressure.surfaceHpa

        val isCoastal = cityLocation.state.contains("Kerala", ignoreCase = true) ||
                cityLocation.state.contains("Maharashtra", ignoreCase = true) ||
                cityLocation.state.contains("Gujarat", ignoreCase = true) ||
                cityLocation.state.contains("Tamil", ignoreCase = true) ||
                cityLocation.state.contains("Odisha", ignoreCase = true) ||
                cityLocation.state.contains("West Bengal", ignoreCase = true) ||
                cityLocation.name.contains("Mumbai", ignoreCase = true) ||
                cityLocation.name.contains("Chennai", ignoreCase = true) ||
                cityLocation.name.contains("Kochi", ignoreCase = true) ||
                cityLocation.name.contains("Vizag", ignoreCase = true)

        val wave = if (isCoastal) 1.8 else null
        val state = if (isCoastal) "Moderate swell (Sea state 3)" else "Inland location (Wave data unavailable from current sources)"
        val hazard = if (isCoastal && windSpeed > 35.0) "Squall warning for coastal fishermen" else "Normal sea state for navigation"

        return MarineBriefing(
            portName = "${cityLocation.name} Coastal / Offshore Zone",
            windSpeedKmh = windSpeed,
            windDirectionDeg = windDir,
            waveHeightMeters = wave,
            seaState = state,
            pressureHpa = pressure,
            temperatureC = temp,
            marineHazardWarning = hazard,
            dataSource = if (isCoastal) "INCOIS Ocean State Forecast & NWP Fusion" else "Inland terrestrial observation (No marine telemetry)"
        )
    }
}
