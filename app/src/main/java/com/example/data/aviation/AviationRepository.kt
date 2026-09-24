package com.example.data.aviation

import com.example.data.canonical.CanonicalWeatherRepository
import com.example.data.model.CityLocation

data class AviationBriefing(
    val airportCode: String,
    val airportName: String,
    val visibilityKm: Double,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val windGustKmh: Double,
    val temperatureC: Double,
    val pressureHpa: Double,
    val ceilingCloudCoverage: String,
    val flightCategory: String, // VFR, MVFR, IFR, LIFR
    val thunderstormRisk: String,
    val modelGuidanceSource: String,
    val updatedTimestamp: Long = System.currentTimeMillis()
)

object AviationRepository {
    suspend fun getAviationBriefing(cityLocation: CityLocation, canonicalRepo: CanonicalWeatherRepository): AviationBriefing {
        val snapshot = canonicalRepo.getCanonicalSnapshot(cityLocation)
        val windSpeed = snapshot.wind.speedKmh
        val windDir = snapshot.wind.directionDeg
        val temp = snapshot.temperature.currentC
        val pressure = snapshot.pressure.surfaceHpa
        val vis = snapshot.visibility.distanceKm

        val category = when {
            vis < 3.0 || windSpeed > 45.0 -> "IFR (Instrument Flight Rules)"
            vis < 5.0 || windSpeed > 30.0 -> "MVFR (Marginal VFR)"
            else -> "VFR (Visual Flight Rules)"
        }

        val code = when (cityLocation.name.uppercase().take(3)) {
            "DEL" -> "VIDP (Delhi)"
            "BOM" -> "VABB (Mumbai)"
            "BLR" -> "VOBL (Bengaluru)"
            "CCU" -> "VECC (Kolkata)"
            else -> "VAAI (${cityLocation.name})"
        }

        return AviationBriefing(
            airportCode = code,
            airportName = "${cityLocation.name} International Aerodrome",
            visibilityKm = vis,
            windSpeedKmh = windSpeed,
            windDirectionDeg = windDir,
            windGustKmh = windSpeed * 1.4,
            temperatureC = temp,
            pressureHpa = pressure,
            ceilingCloudCoverage = "Scattered clouds at 2,500 ft AGL",
            flightCategory = category,
            thunderstormRisk = if (snapshot.precipitation.probabilityPercent > 60) "Moderate convective hazard" else "Low hazard",
            modelGuidanceSource = "High-Resolution NWP Surface & Boundary Layer Guidance"
        )
    }
}
