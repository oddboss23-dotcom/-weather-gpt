package com.example.data.flood

import com.example.data.model.AlertSeverity
import kotlin.math.max

data class DetailedRiverRiskMetrics(
    val station: RiverBasinStation,
    val currentLevelM: Double,
    val warningLevelM: Double,
    val dangerLevelM: Double,
    val highestFloodLevelM: Double,
    val distanceToWarningLevelM: Double, // Positive if above, negative if below
    val distanceToDangerLevelM: Double,
    val rateOfRiseMetersPerHour: Double,
    val hoursToDangerLevel: Double?,
    val classification: FloodRiskCategory,
    val trend: WaterLevelTrend,
    val catchmentRainfall24hMm: Double,
    val upstreamStatusSummary: String,
    val statusDescription: String,
    val recommendedAction: String,
    val recommendedActionHindi: String,
    val calculatedAt: Long = System.currentTimeMillis()
)

/**
 * RiverFloodRiskEngine (PS-26071 + PS-68)
 * Computes deterministic river flood risk from CWC river station gauges,
 * rates of rise, distance to warning/danger thresholds, and upstream catchment status.
 */
object RiverFloodRiskEngine {

    fun computeRiverMetrics(
        station: RiverBasinStation,
        observation: RiverObservation,
        previousLevelM: Double? = null,
        deltaHours: Double = 3.0
    ): DetailedRiverRiskMetrics {
        val currentL = observation.currentWaterLevelM
        val warnL = station.warningLevelM
        val dangerL = station.dangerLevelM
        val hfl = station.highestFloodLevelM

        val distToWarn = (currentL - warnL * 100.0).toInt() / 100.0
        val distToDanger = (currentL - dangerL * 100.0).toInt() / 100.0

        val rateOfRise = if (previousLevelM != null && deltaHours > 0) {
            val rawRate = (currentL - previousLevelM) / deltaHours
            (rawRate * 100.0).toInt() / 100.0
        } else {
            when (observation.trend) {
                WaterLevelTrend.RISING -> 0.08
                WaterLevelTrend.FALLING -> -0.05
                WaterLevelTrend.STEADY -> 0.0
            }
        }

        val hoursToDanger = if (distToDanger < 0 && rateOfRise > 0.01) {
            val hours = (-distToDanger) / rateOfRise
            (hours * 10.0).toInt() / 10.0
        } else {
            null
        }

        val classification = when {
            currentL >= hfl || currentL >= (dangerL + 0.5) -> FloodRiskCategory.SEVERE_FLOOD_RISK
            currentL >= dangerL -> FloodRiskCategory.DANGER
            currentL >= warnL -> FloodRiskCategory.WARNING
            currentL >= (warnL - 0.5) && observation.trend == WaterLevelTrend.RISING -> FloodRiskCategory.WATCH
            else -> FloodRiskCategory.NORMAL
        }

        val upstreamSummary = observation.upstreamWaterLevelStatus ?: "Upstream river telemetry normal"

        val statusDesc = when (classification) {
            FloodRiskCategory.SEVERE_FLOOD_RISK -> "${station.riverName} at ${station.stationName} is in SEVERE FLOOD SITUATION. Flow is ${(distToDanger)}m above Danger Level."
            FloodRiskCategory.DANGER -> "${station.riverName} at ${station.stationName} is flowing ${(distToDanger)}m ABOVE DANGER LEVEL with a ${observation.trend.displayName.lowercase()} trend."
            FloodRiskCategory.WARNING -> "${station.riverName} at ${station.stationName} has crossed Warning Level by ${(distToWarn)}m. Rising towards Danger Level."
            FloodRiskCategory.WATCH -> "${station.riverName} at ${station.stationName} is approaching Warning Level (${distToWarn}m remaining)."
            FloodRiskCategory.NORMAL -> "${station.riverName} at ${station.stationName} is flowing safely within normal embankments."
        }

        val (recEn, recHi) = when (classification) {
            FloodRiskCategory.SEVERE_FLOOD_RISK -> Pair(
                "CRITICAL: Immediate evacuation of low-lying floodplains. Shift livestock and machinery to designated flood shelters. Avoid all riverbank dykes.",
                "अति गंभीर: निचले इलाकों से तुरंत सुरक्षित स्थान पर जाएं। मवेशियों और आवश्यक सामान को ऊंचे स्थानों पर पहुंचाएं।"
            )
            FloodRiskCategory.DANGER -> Pair(
                "DANGER: River has crossed danger mark. Halt field irrigation and secure low-lying grain stores. Monitor official CWC / SDRF announcements.",
                "खतरा: नदी खतरे के निशान से ऊपर बह रही है। निचले खेतों से मवेशी सुरक्षित करें और आधिकारिक निर्देशों का पालन करें।"
            )
            FloodRiskCategory.WARNING -> Pair(
                "WARNING: River water is rising near warning threshold. Secure loose agricultural pumps and prepare livestock evacuation routes.",
                "चेतावनी: नदी चेतावनी स्तर के पास पहुंच गई है। सिंचाई पंप और कृषि उपकरण सुरक्षित स्थानों पर रखें।"
            )
            FloodRiskCategory.WATCH -> Pair(
                "WATCH: Upstream runoff is increasing river discharge. Monitor local river ghats and weather updates.",
                "सतर्कता: नदी के जलस्तर में वृद्धि हो रही है। मौसम और नदी की स्थिति पर नज़र रखें।"
            )
            FloodRiskCategory.NORMAL -> Pair(
                "Normal river flow. Routine agricultural and navigation operations can proceed safely.",
                "नदी का प्रवाह सामान्य है। नियमित कृषि कार्य सुरक्षित रूप से किए जा सकते हैं।"
            )
        }

        return DetailedRiverRiskMetrics(
            station = station,
            currentLevelM = currentL,
            warningLevelM = warnL,
            dangerLevelM = dangerL,
            highestFloodLevelM = hfl,
            distanceToWarningLevelM = distToWarn,
            distanceToDangerLevelM = distToDanger,
            rateOfRiseMetersPerHour = rateOfRise,
            hoursToDangerLevel = hoursToDanger,
            classification = classification,
            trend = observation.trend,
            catchmentRainfall24hMm = observation.rainfallIntensityMm,
            upstreamStatusSummary = upstreamSummary,
            statusDescription = statusDesc,
            recommendedAction = recEn,
            recommendedActionHindi = recHi
        )
    }
}
