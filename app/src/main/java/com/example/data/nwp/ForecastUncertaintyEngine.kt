package com.example.data.nwp

import java.util.Locale
import kotlin.math.abs

data class UncertaintyAssessment(
    val confidencePercent: Int?,
    val confidenceCategory: String, // "High", "Moderate", "Low", or "Confidence unavailable"
    val uncertaintyTempRangeC: Double,
    val uncertaintyPrecipRangeMm: Double,
    val summaryText: String,
    val contributingSignals: List<String>,
    val modelAgreementText: String
)

object ForecastUncertaintyEngine {

    /**
     * Mathematically derives forecast confidence and uncertainty bounds from genuine physical signals.
     */
    fun evaluateUncertainty(
        horizonHours: Int,
        modelSpreadC: Double?, // e.g. GFS vs ECMWF difference, null if only single model
        hasObsAgreement: Boolean,
        dataAgeMinutes: Long,
        radarCoverageAvailable: Boolean,
        isBustRiskDetected: Boolean
    ): UncertaintyAssessment {
        val signals = mutableListOf<String>()

        // 1. Model Agreement Signal
        val modelAgreementText = if (modelSpreadC != null) {
            when {
                modelSpreadC <= 1.2 -> {
                    signals.add("Multi-model guidance shows high convergence (spread ±${String.format(Locale.US, "%.1f", modelSpreadC)}°C)")
                    "Multi-model convergence: High"
                }
                modelSpreadC <= 2.8 -> {
                    signals.add("Moderate model spread (±${String.format(Locale.US, "%.1f", modelSpreadC)}°C)")
                    "Multi-model spread: Moderate"
                }
                else -> {
                    signals.add("Elevated model divergence (spread ±${String.format(Locale.US, "%.1f", modelSpreadC)}°C)")
                    "Multi-model divergence: Elevated"
                }
            }
        } else {
            signals.add("Single-model NWP guidance (no secondary ensemble run)")
            "Single NWP guidance"
        }

        // 2. Base Confidence by Lead Time Horizon
        // Lead-time physics: confidence diminishes naturally with horizon
        var baseScore = when {
            horizonHours <= 3 -> 90
            horizonHours <= 12 -> 84
            horizonHours <= 24 -> 76
            horizonHours <= 36 -> 68
            else -> 60
        }

        // 3. Adjust based on Model Spread
        if (modelSpreadC != null) {
            if (modelSpreadC <= 1.0) baseScore += 5
            else if (modelSpreadC >= 3.0) baseScore -= 12
        }

        // 4. Adjust based on Observation Agreement
        if (hasObsAgreement) {
            baseScore += 5
            signals.add("NWP background aligns with surface observations")
        } else {
            baseScore -= 6
            signals.add("Slight variance between NWP baseline and station telemetry")
        }

        // 5. Adjust for Data Freshness
        if (dataAgeMinutes <= 30) {
            signals.add("Telemetry is fresh (< 30 min)")
        } else if (dataAgeMinutes > 180) {
            baseScore -= 8
            signals.add("Telemetry is aging (> 3h old)")
        }

        // 6. Radar Availability
        if (radarCoverageAvailable) {
            baseScore += 3
            signals.add("Within Doppler Radar coverage radius")
        }

        // 7. Bust Risk
        if (isBustRiskDetected) {
            baseScore -= 15
            signals.add("Caution: Recent revision detected across forecast cycles")
        }

        val finalScore = baseScore.coerceIn(35, 94)

        val category = when {
            finalScore >= 78 -> "High"
            finalScore >= 60 -> "Moderate"
            else -> "Low"
        }

        // Uncertainty bounds calculated from horizon and spread
        val horizonFactor = horizonHours / 24.0
        val baseSpread = modelSpreadC ?: 1.2
        val tempUncertainty = (baseSpread * 0.6 + horizonFactor * 0.7).coerceIn(0.6, 4.5)
        val precipUncertainty = (1.0 + horizonFactor * 2.0).coerceIn(0.8, 8.0)

        val summary = String.format(
            Locale.US,
            "±%.1f°C temperature, ±%.1f mm precipitation",
            tempUncertainty, precipUncertainty
        )

        return UncertaintyAssessment(
            confidencePercent = finalScore,
            confidenceCategory = category,
            uncertaintyTempRangeC = tempUncertainty,
            uncertaintyPrecipRangeMm = precipUncertainty,
            summaryText = summary,
            contributingSignals = signals,
            modelAgreementText = modelAgreementText
        )
    }
}
