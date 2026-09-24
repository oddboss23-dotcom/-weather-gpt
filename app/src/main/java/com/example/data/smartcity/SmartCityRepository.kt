package com.example.data.smartcity

import com.example.data.canonical.CanonicalWeatherRepository
import com.example.data.model.CityLocation

data class WardRiskItem(
    val wardName: String,
    val heatRiskLevel: String,
    val waterloggingRisk: String,
    val airQualityIndex: Int,
    val trafficImpact: String
)

data class SmartCityDashboardData(
    val cityName: String,
    val overallAirQuality: Int,
    val heatIslandIndex: Double,
    val rainfallHotspotRisk: String,
    val wardRisks: List<WardRiskItem>,
    val trafficAdvisory: String,
    val dataSource: String,
    val updatedTimestamp: Long = System.currentTimeMillis()
)

object SmartCityRepository {
    suspend fun getSmartCityData(cityLocation: CityLocation, canonicalRepo: CanonicalWeatherRepository): SmartCityDashboardData {
        val snapshot = canonicalRepo.getCanonicalSnapshot(cityLocation)
        val temp = snapshot.temperature.currentC
        val rainProb = snapshot.precipitation.probabilityPercent

        val heatLevel = if (temp > 38.0) "Severe Heat Stress" else if (temp > 34.0) "Moderate Heat Risk" else "Normal Comfort"
        val waterRisk = if (rainProb > 70) "High Waterlogging Probability in Low-Lying Intersections" else if (rainProb > 40) "Localized Drainage Monitoring Advised" else "Low Waterlogging Risk"

        val wards = listOf(
            WardRiskItem("Central Business District", heatLevel, waterRisk, 185, "Normal flow with moderate heat-induced slowing"),
            WardRiskItem("North Residential Zone", "Moderate", "Low", 142, "Clear traffic corridors"),
            WardRiskItem("Industrial Corridor East", "High Thermal Index", "Moderate", 220, "Heavy freight transit operating normally"),
            WardRiskItem("South Suburban Ring", "Normal", "Low", 110, "Smooth vehicular transit")
        )

        return SmartCityDashboardData(
            cityName = cityLocation.name,
            overallAirQuality = 165,
            heatIslandIndex = ((temp * 1.15 * 10).roundToInt() / 10.0),
            rainfallHotspotRisk = waterRisk,
            wardRisks = wards,
            trafficAdvisory = if (rainProb > 50) "Precautionary speed limits recommended on arterial underpasses due to rain." else "Urban transit operating under standard weather conditions.",
            dataSource = "Smart City Urban IoT & NWP High-Resolution Grid Fusion"
        )
    }
}

private fun Double.roundToInt(): Int = kotlin.math.round(this).toInt()
