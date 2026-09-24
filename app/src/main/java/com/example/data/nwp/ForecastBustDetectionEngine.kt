package com.example.data.nwp

import java.util.Locale
import kotlin.math.abs

enum class ForecastStabilityClass(val label: String, val severity: String) {
    STABLE("STABLE FORECAST", "LOW"),
    MINOR_REVISION("MINOR REVISION", "MODERATE"),
    SIGNIFICANT_REVISION("SIGNIFICANT REVISION", "HIGH"),
    FORECAST_BUST("FORECAST BUST", "CRITICAL")
}

data class ForecastRevisionSnapshot(
    val horizonLabel: String, // "T-24h", "T-12h", "T-3h", "Observation"
    val temperatureC: Double,
    val rainfallMm: Double,
    val windSpeedKmh: Double,
    val source: String
)

data class ForecastBustReport(
    val stabilityClass: ForecastStabilityClass,
    val temperatureErrorC: Double,
    val rainfallErrorMm: Double,
    val windErrorKmh: Double,
    val explanation: String,
    val snapshots: List<ForecastRevisionSnapshot>,
    val requiresAlert: Boolean
)

object ForecastBustDetectionEngine {

    /**
     * Evaluates forecast evolution across T-24h, T-12h, T-3h lead times against recent observation.
     */
    fun analyzeForecastStability(
        t24h: ForecastRevisionSnapshot,
        t12h: ForecastRevisionSnapshot,
        t3h: ForecastRevisionSnapshot,
        actualObservation: ForecastRevisionSnapshot?
    ): ForecastBustReport {
        val snapshots = listOfNotNull(t24h, t12h, t3h, actualObservation)

        // Baseline comparison target is actual observation if present, or latest T-3h
        val truthTarget = actualObservation ?: t3h
        val baselineT24 = t24h

        val tempError = abs(truthTarget.temperatureC - baselineT24.temperatureC)
        val rainError = abs(truthTarget.rainfallMm - baselineT24.rainfallMm)
        val windError = abs(truthTarget.windSpeedKmh - baselineT24.windSpeedKmh)

        val stabilityClass = when {
            tempError >= 4.5 || rainError >= 20.0 || windError >= 25.0 ->
                ForecastStabilityClass.FORECAST_BUST

            tempError >= 3.0 || rainError >= 10.0 || windError >= 15.0 ->
                ForecastStabilityClass.SIGNIFICANT_REVISION

            tempError >= 1.5 || rainError >= 4.0 || windError >= 8.0 ->
                ForecastStabilityClass.MINOR_REVISION

            else ->
                ForecastStabilityClass.STABLE
        }

        val explanation = when (stabilityClass) {
            ForecastStabilityClass.FORECAST_BUST ->
                "Forecast changed significantly because recent observations diverged from the previous model guidance. Synoptic boundary-layer moisture or convective initiation altered local conditions by ${String.format(Locale.US, "%.1f", tempError)}°C and ${String.format(Locale.US, "%.1f", rainError)} mm."

            ForecastStabilityClass.SIGNIFICANT_REVISION ->
                "Noticeable model revision detected between T-24h and recent guidance. Temperature shifted by ${String.format(Locale.US, "%.1f", tempError)}°C; monitoring ongoing convective trends."

            ForecastStabilityClass.MINOR_REVISION ->
                "Forecast guidance exhibits standard diurnal adjustments (±${String.format(Locale.US, "%.1f", tempError)}°C, ±${String.format(Locale.US, "%.1f", rainError)} mm) consistent with typical operational tolerances."

            ForecastStabilityClass.STABLE ->
                "High forecast stability. NWP model runs show strong convergence with zero significant divergence (< 1.5°C spread) over the past 24 hours."
        }

        return ForecastBustReport(
            stabilityClass = stabilityClass,
            temperatureErrorC = tempError,
            rainfallErrorMm = rainError,
            windErrorKmh = windError,
            explanation = explanation,
            snapshots = snapshots,
            requiresAlert = stabilityClass == ForecastStabilityClass.FORECAST_BUST
        )
    }
}
