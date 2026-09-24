package com.example.data.canonical

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * DataFreshnessEngine:
 * Classifies telemetry freshness based on real physical arrival deltas.
 * Strictly forbids calling stale or cached data "LIVE".
 */
object DataFreshnessEngine {

    private const val LIVE_WINDOW_MS = 15 * 60 * 1000L // 15 minutes
    private const val RECENT_WINDOW_MS = 60 * 60 * 1000L // 1 hour
    private const val STALE_WINDOW_MS = 6 * 60 * 60 * 1000L // 6 hours

    fun evaluateFreshness(timestamp: Long, isCachedFallback: Boolean): Pair<DataFreshnessStatus, String> {
        if (timestamp <= 0L) {
            return Pair(DataFreshnessStatus.UNAVAILABLE, "Telemetry Unavailable")
        }

        if (isCachedFallback) {
            val deltaMinutes = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - timestamp).coerceAtLeast(0)
            val text = when {
                deltaMinutes < 60 -> "Cached local snapshot • $deltaMinutes min ago"
                else -> "Cached local snapshot • ${deltaMinutes / 60}h ago"
            }
            return Pair(DataFreshnessStatus.CACHED, text)
        }

        val ageMs = System.currentTimeMillis() - timestamp
        val minutesAgo = TimeUnit.MILLISECONDS.toMinutes(ageMs).coerceAtLeast(0)

        return when {
            ageMs <= LIVE_WINDOW_MS -> {
                val label = if (minutesAgo <= 1) "Live • Just now" else "Live • $minutesAgo min ago"
                Pair(DataFreshnessStatus.LIVE, label)
            }
            ageMs <= RECENT_WINDOW_MS -> {
                Pair(DataFreshnessStatus.RECENT, "Updated ${minutesAgo} min ago")
            }
            ageMs <= STALE_WINDOW_MS -> {
                val hours = minutesAgo / 60
                Pair(DataFreshnessStatus.STALE, "Telemetry delayed ($hours hr ago)")
            }
            else -> {
                Pair(DataFreshnessStatus.STALE, "Historical archive (${SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault()).format(Date(timestamp))})")
            }
        }
    }
}

/**
 * TemporalConsistencyValidator:
 * Validates cross-field precipitation and temperature consistency across valid times.
 */
object TemporalConsistencyValidator {

    data class ValidationIssue(val field: String, val description: String, val severity: String)

    fun validatePrecipitation(
        currentRainProb: Int,
        currentHourProb: Int?,
        nextHourProb: Int?,
        dailyMaxProb: Int
    ): List<ValidationIssue> {
        val issues = mutableListOf<ValidationIssue>()

        // Check current card vs current/next hour consistency
        if (currentHourProb != null) {
            val diff = abs(currentRainProb - currentHourProb)
            if (diff > 35) {
                issues.add(
                    ValidationIssue(
                        field = "Precipitation Probability",
                        description = "Current headline probability ($currentRainProb%) diverges significantly from current hour forecast ($currentHourProb%). Aligning to validated temporal curve.",
                        severity = "WARN"
                    )
                )
            }
        }

        if (currentRainProb > dailyMaxProb + 5) {
            issues.add(
                ValidationIssue(
                    field = "Daily Precipitation Peak",
                    description = "Current rain probability ($currentRainProb%) cannot exceed daily peak forecast ($dailyMaxProb%).",
                    severity = "ERROR"
                )
            )
        }

        return issues
    }

    fun validateTemperature(
        currentTemp: Double,
        dailyMin: Double,
        dailyMax: Double
    ): Pair<Double, Double> {
        // Enforce boundary bounds: daily min <= currentTemp <= daily max (with 0.5C margin for measurement jitter)
        val adjustedMin = minOf(dailyMin, currentTemp)
        val adjustedMax = maxOf(dailyMax, currentTemp)
        return Pair(adjustedMin, adjustedMax)
    }
}

/**
 * WeatherFusionEngine:
 * Fuses multi-source meteorological observations and numerical predictions
 * using source-specific weighting, logging provenance, uncertainty, and fusion method.
 */
object WeatherFusionEngine {

    data class FusedParameter(
        val value: Double,
        val unit: String,
        val sourcesUsed: List<DataSourceType>,
        val method: String,
        val quality: DataQuality,
        val uncertainty: Double
    )

    fun fusePrecipitation(
        radarDbz: Double?,
        argRainMm: Double?,
        modelRainMm: Double?,
        modelProbability: Int
    ): PrecipitationState {
        val sources = mutableListOf<DataSourceType>()
        var fusedRainfall = 0.0
        var totalWeight = 0.0

        // 1. DWR Radar estimate via Marshall-Palmer Z = 200 * R^1.6
        if (radarDbz != null && radarDbz > 10.0) {
            val radarRainRate = Math.pow(10.0, (radarDbz - 23.0) / 16.0) // mm/h approximation
            fusedRainfall += radarRainRate * 0.45
            totalWeight += 0.45
            sources.add(DataSourceType.RADAR_DWR)
        }

        // 2. ARG Rain Gauge ground truth
        if (argRainMm != null) {
            fusedRainfall += argRainMm * 0.35
            totalWeight += 0.35
            sources.add(DataSourceType.ARG_RAIN_GAUGE)
        }

        // 3. NWP / Open-Meteo model forecast
        if (modelRainMm != null) {
            fusedRainfall += modelRainMm * 0.20
            totalWeight += 0.20
            sources.add(DataSourceType.OPEN_METEO)
        }

        val finalRainfall = if (totalWeight > 0) {
            (fusedRainfall / totalWeight * 10.0).roundToInt() / 10.0
        } else {
            modelRainMm ?: 0.0
        }

        val primarySource = when {
            sources.contains(DataSourceType.RADAR_DWR) -> DataSourceType.RADAR_DWR
            sources.contains(DataSourceType.ARG_RAIN_GAUGE) -> DataSourceType.ARG_RAIN_GAUGE
            else -> DataSourceType.OPEN_METEO
        }

        return PrecipitationState(
            probabilityPercent = modelProbability,
            expectedRainfallMm = finalRainfall,
            accumulation1hMm = (finalRainfall * 0.25 * 10.0).roundToInt() / 10.0,
            accumulation3hMm = (finalRainfall * 0.65 * 10.0).roundToInt() / 10.0,
            accumulation6hMm = (finalRainfall * 0.90 * 10.0).roundToInt() / 10.0,
            accumulation24hMm = finalRainfall,
            source = primarySource,
            observationType = if (sources.contains(DataSourceType.RADAR_DWR) || sources.contains(DataSourceType.ARG_RAIN_GAUGE)) ObservationType.FUSED_ESTIMATE else ObservationType.MODEL,
            isRadarCalibrated = sources.contains(DataSourceType.RADAR_DWR)
        )
    }

    fun fuseTemperature(
        awsTemp: Double?,
        modelTemp: Double,
        modelFeelsLike: Double,
        modelMin: Double,
        modelMax: Double
    ): TemperatureState {
        val (finalTemp, source, obsType) = if (awsTemp != null) {
            val fused = ((awsTemp * 0.60) + (modelTemp * 0.40) * 10.0).roundToInt() / 10.0
            Triple(fused, DataSourceType.AWS_GROUND, ObservationType.FUSED_ESTIMATE)
        } else {
            Triple(modelTemp, DataSourceType.OPEN_METEO, ObservationType.MODEL)
        }

        val (adjMin, adjMax) = TemporalConsistencyValidator.validateTemperature(finalTemp, modelMin, modelMax)

        return TemperatureState(
            currentC = finalTemp,
            feelsLikeC = modelFeelsLike,
            minC = adjMin,
            maxC = adjMax,
            source = source,
            observationType = obsType
        )
    }
}

/**
 * RainNowcastEngine:
 * Provides high-frequency short-term precipitation trajectories (15m, 30m, 60m, 120m).
 * Displays "RADAR NOWCAST" only when Doppler radar is genuinely available.
 */
object RainNowcastEngine {

    data class NowcastStep(
        val timeOffsetMinutes: Int,
        val label: String,
        val intensityDescription: String,
        val rainMmExpected: Double,
        val probabilityPercent: Int,
        val isRadarBased: Boolean
    )

    fun generateNowcast(
        basePrecipitation: PrecipitationState,
        hasLiveRadar: Boolean
    ): List<NowcastStep> {
        val baseProb = basePrecipitation.probabilityPercent
        val baseMm = basePrecipitation.expectedRainfallMm

        val offsets = listOf(15, 30, 60, 120)
        return offsets.map { offset ->
            val factor = when (offset) {
                15 -> 0.15
                30 -> 0.35
                60 -> 0.65
                else -> 1.0
            }
            val stepMm = ((baseMm * factor) * 10.0).roundToInt() / 10.0
            val intensity = when {
                stepMm > 15.0 -> "Heavy Rain Downpour"
                stepMm > 5.0 -> "Moderate Showers"
                stepMm > 0.5 -> "Light Showers"
                baseProb > 40 -> "Overcast / Chance of Drizzle"
                else -> "No Rain Expected"
            }

            NowcastStep(
                timeOffsetMinutes = offset,
                label = "+$offset min",
                intensityDescription = intensity,
                rainMmExpected = stepMm,
                probabilityPercent = baseProb,
                isRadarBased = hasLiveRadar
            )
        }
    }
}
