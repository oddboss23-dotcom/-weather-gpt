package com.example.data.flood

enum class WaterloggingRiskLevel(
    val title: String,
    val severityLabel: String,
    val colorHex: Long
) {
    SEVERE("Severe Waterlogging (Traffic Gridlock / Basement Inundation)", "CRITICAL", 0xFFDC2626),
    HIGH("High Waterlogging Risk (Low-lying Road Inundation)", "HIGH", 0xFFF97316),
    MODERATE("Moderate Waterlogging (Slow Drainage / Minor Puddling)", "MODERATE", 0xFFEAB308),
    LOW("Low / Normal Urban Drainage", "LOW", 0xFF10B981)
}

data class UrbanWaterloggingReport(
    val cityOrWard: String,
    val riskLevel: WaterloggingRiskLevel,
    val rainfallIntensityMmPerHour: Double,
    val municipalDrainageCapacityMmPerHour: Double?,
    val imperviousSurfacePercent: Int,
    val drainageModelAvailable: Boolean,
    val estimatedClearanceTimeHours: Double?,
    val vulnerableRoadSectors: List<String>,
    val advisoryAction: String,
    val timestamp: Long
)

/**
 * UrbanWaterloggingEngine (PS-26071)
 * Models municipal urban surface runoff, impervious area ratios,
 * and drainage throughput to assess street-level waterlogging.
 */
object UrbanWaterloggingEngine {

    // Known municipal drainage profiles
    private val urbanDrainageProfiles = mapOf(
        "Patna" to Triple(18.0, 78, listOf("Rajendra Nagar", "Kankarbagh", "Boring Road Underpass", "Saidpur Nala Catchment")),
        "Muzaffarpur" to Triple(14.0, 65, listOf("Motijheel", "Brahmpura", "Maripur Road")),
        "Gaya" to Triple(15.0, 60, listOf("Swarajpuri Road", "Station Underpass")),
        "Bhagalpur" to Triple(16.0, 62, listOf("Tilkamanjhi Chowk", "Adampur"))
    )

    fun evaluateUrbanWaterlogging(
        cityName: String,
        rainfallIntensityMmPerHour: Double,
        accumulatedRainfall24hMm: Double
    ): UrbanWaterloggingReport {
        val profile = urbanDrainageProfiles[cityName]
        val now = System.currentTimeMillis()

        if (profile == null) {
            val isSevereRain = rainfallIntensityMmPerHour >= 30.0 || accumulatedRainfall24hMm >= 100.0
            val isHighRain = rainfallIntensityMmPerHour >= 15.0 || accumulatedRainfall24hMm >= 60.0
            val risk = when {
                isSevereRain -> WaterloggingRiskLevel.HIGH
                isHighRain -> WaterloggingRiskLevel.MODERATE
                else -> WaterloggingRiskLevel.LOW
            }
            return UrbanWaterloggingReport(
                cityOrWard = cityName,
                riskLevel = risk,
                rainfallIntensityMmPerHour = rainfallIntensityMmPerHour,
                municipalDrainageCapacityMmPerHour = null,
                imperviousSurfacePercent = 50,
                drainageModelAvailable = false,
                estimatedClearanceTimeHours = null,
                vulnerableRoadSectors = emptyList(),
                advisoryAction = "Urban drainage model unavailable for $cityName. Exercise caution around natural depressions and underpasses during heavy downpours.",
                timestamp = now
            )
        }

        val (drainageCapacity, imperviousPercent, hotspots) = profile
        val runoffExcessMm = rainfallIntensityMmPerHour - drainageCapacity

        val risk = when {
            runoffExcessMm >= 15.0 || (rainfallIntensityMmPerHour >= 35.0) -> WaterloggingRiskLevel.SEVERE
            runoffExcessMm >= 5.0 || (accumulatedRainfall24hMm >= 80.0) -> WaterloggingRiskLevel.HIGH
            runoffExcessMm >= -5.0 && rainfallIntensityMmPerHour >= 10.0 -> WaterloggingRiskLevel.MODERATE
            else -> WaterloggingRiskLevel.LOW
        }

        val clearanceTime = when (risk) {
            WaterloggingRiskLevel.SEVERE -> 6.0 + (accumulatedRainfall24hMm / 30.0)
            WaterloggingRiskLevel.HIGH -> 3.5
            WaterloggingRiskLevel.MODERATE -> 1.5
            WaterloggingRiskLevel.LOW -> 0.5
        }

        val action = when (risk) {
            WaterloggingRiskLevel.SEVERE -> "AVOID low-lying underpasses (${hotspots.take(2).joinToString(", ")}). Expect severe traffic delays and underground basement water entry."
            WaterloggingRiskLevel.HIGH -> "Caution on arterial roads. Slow drainage expected near ${hotspots.firstOrNull() ?: "major intersections"}."
            WaterloggingRiskLevel.MODERATE -> "Minor surface water pooling. Standard urban traffic clearance."
            WaterloggingRiskLevel.LOW -> "Municipal storm drains operating normally within capacity."
        }

        return UrbanWaterloggingReport(
            cityOrWard = cityName,
            riskLevel = risk,
            rainfallIntensityMmPerHour = rainfallIntensityMmPerHour,
            municipalDrainageCapacityMmPerHour = drainageCapacity,
            imperviousSurfacePercent = imperviousPercent,
            drainageModelAvailable = true,
            estimatedClearanceTimeHours = (clearanceTime * 10.0).toInt() / 10.0,
            vulnerableRoadSectors = hotspots,
            advisoryAction = action,
            timestamp = now
        )
    }
}
