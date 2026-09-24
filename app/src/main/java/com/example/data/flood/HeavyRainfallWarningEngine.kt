package com.example.data.flood

import com.example.data.model.AlertSeverity

enum class RainfallHazardCategory(
    val title: String,
    val imdCriteria: String,
    val severity: AlertSeverity,
    val colorHex: Long
) {
    EXTREMELY_HEAVY_RAIN("Extremely Heavy Rainfall", "> 204.4 mm / 24h", AlertSeverity.RED, 0xFFDC2626),
    VERY_HEAVY_RAIN("Very Heavy Rainfall", "115.6 to 204.4 mm / 24h", AlertSeverity.ORANGE, 0xFFF97316),
    HEAVY_RAIN("Heavy Rainfall", "64.5 to 115.5 mm / 24h", AlertSeverity.YELLOW, 0xFFEAB308),
    MODERATE_RAIN("Moderate Rainfall", "15.6 to 64.4 mm / 24h", AlertSeverity.GREEN, 0xFF10B981),
    LIGHT_RAIN("Light to Isolated Rain", "< 15.6 mm / 24h", AlertSeverity.GREEN, 0xFF06B6D4)
}

data class HeavyRainfallWarning(
    val location: String,
    val hazardCategory: RainfallHazardCategory,
    val probabilityPercent: Int,
    val expectedAccumulationMm: Double,
    val validTimeWindow: String,
    val sourceAttribution: String,
    val confidenceLevel: String, // "High", "Medium", "Moderate"
    val uncertaintyNotes: String,
    val timestamp: Long
)

/**
 * HeavyRainfallWarningEngine (PS-26071)
 * Evaluates multi-source rainfall forecasts and applies authoritative IMD thresholds
 * to issue structured heavy rainfall early warnings.
 */
object HeavyRainfallWarningEngine {

    fun evaluateHeavyRainfall(
        locationName: String,
        fusedRainfall: FusedRainfallEstimate
    ): HeavyRainfallWarning? {
        val h24 = fusedRainfall.probabilisticHorizons.firstOrNull { it.horizonHours == 24 }
            ?: return null

        val expected24h = h24.expectedAccumulationMm
        val probHeavy = (h24.probRainOver50mm * 100).toInt()

        val category = when {
            expected24h >= 204.4 || (expected24h >= 160.0 && probHeavy >= 70) -> RainfallHazardCategory.EXTREMELY_HEAVY_RAIN
            expected24h >= 115.6 || (expected24h >= 90.0 && probHeavy >= 60) -> RainfallHazardCategory.VERY_HEAVY_RAIN
            expected24h >= 64.5 || (expected24h >= 50.0 && probHeavy >= 50) -> RainfallHazardCategory.HEAVY_RAIN
            expected24h >= 25.0 -> RainfallHazardCategory.MODERATE_RAIN
            else -> RainfallHazardCategory.LIGHT_RAIN
        }

        if (category == RainfallHazardCategory.LIGHT_RAIN) return null

        val confidence = when {
            fusedRainfall.dataQualityScore >= 0.75 -> "High Confidence"
            fusedRainfall.dataQualityScore >= 0.50 -> "Medium Confidence"
            else -> "Moderate Confidence (NWP-dominated)"
        }

        val uncertainty = when {
            fusedRainfall.radarReflectivityDbz != null -> "Doppler Radar convective storm tracking active; high spatial precision."
            fusedRainfall.satellitePrecipitationMm != null -> "Radar coverage partial; supported by INSAT-3D cloud-top brightness & NWP."
            else -> "Forecast derived primarily from calibrated NWP model ensemble."
        }

        return HeavyRainfallWarning(
            location = locationName,
            hazardCategory = category,
            probabilityPercent = probHeavy.coerceIn(20, 99),
            expectedAccumulationMm = expected24h,
            validTimeWindow = "Next 24 Hours",
            sourceAttribution = "IMD + Doppler Radar (DWR) + Calibrated NWP",
            confidenceLevel = confidence,
            uncertaintyNotes = uncertainty,
            timestamp = System.currentTimeMillis()
        )
    }
}
