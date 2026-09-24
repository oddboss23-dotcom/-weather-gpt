package com.example.data.flood

data class DynamicFloodConfidence(
    val overallConfidencePercent: Int,
    val dataFreshnessScore: Int, // 0-100
    val radarCoverageScore: Int,
    val stationDensityScore: Int,
    val nwpEnsembleAgreementScore: Int,
    val demResolutionScore: Int,
    val positiveFactors: List<String>,
    val uncertaintyFactors: List<String>,
    val explanationSummary: String
)

/**
 * FloodConfidenceEngine
 * Dynamically evaluates prediction confidence from data freshness, radar availability,
 * river gauge density, NWP ensemble agreement, and DEM resolution.
 * Invariant: Never hardcodes 95% or fixed static confidence scores.
 */
object FloodConfidenceEngine {

    fun computeConfidence(
        hasRiverGauge: Boolean,
        hasRadarCoverage: Boolean,
        hasSatelliteData: Boolean,
        hasHighResDem: Boolean,
        nwpSpreadMm: Double,
        minutesSinceLastObservation: Long
    ): DynamicFloodConfidence {
        // 1. Data freshness
        val freshness = when {
            minutesSinceLastObservation <= 60 -> 95
            minutesSinceLastObservation <= 180 -> 80
            minutesSinceLastObservation <= 360 -> 65
            else -> 45
        }

        // 2. Sensor coverage
        val radar = if (hasRadarCoverage) 90 else 40
        val station = if (hasRiverGauge) 95 else 35
        val nwpAgreement = when {
            nwpSpreadMm <= 5.0 -> 90
            nwpSpreadMm <= 15.0 -> 75
            nwpSpreadMm <= 30.0 -> 55
            else -> 40
        }
        val dem = if (hasHighResDem) 85 else 50

        // Weighted confidence score
        val overall = (station * 0.35 + freshness * 0.20 + radar * 0.20 + nwpAgreement * 0.15 + dem * 0.10).toInt().coerceIn(25, 95)

        val positive = mutableListOf<String>()
        val uncertainty = mutableListOf<String>()

        if (hasRiverGauge) positive.add("Authoritative CWC river gauge level active") else uncertainty.add("No direct river gauge; using hydro-catchment interpolation")
        if (freshness >= 80) positive.add("Recent station rainfall observations (< 3h)") else uncertainty.add("Station observation telemetry delayed")
        if (hasRadarCoverage) positive.add("Doppler Weather Radar (DWR) active coverage") else uncertainty.add("Outside primary Doppler Radar radius; relying on INSAT-3D satellite & NWP")
        if (nwpAgreement >= 75) positive.add("Strong agreement across multi-model NWP ensemble") else uncertainty.add("Moderate divergence in NWP precipitation accumulation models")
        if (hasHighResDem) positive.add("Standard 30m Digital Elevation Model") else uncertainty.add("Coarse terrain data (regional floodplain approximation)")

        val summary = "Flood confidence is $overall% based on ${positive.size} verified physical inputs and ${uncertainty.size} uncertainty constraints."

        return DynamicFloodConfidence(
            overallConfidencePercent = overall,
            dataFreshnessScore = freshness,
            radarCoverageScore = radar,
            stationDensityScore = station,
            nwpEnsembleAgreementScore = nwpAgreement,
            demResolutionScore = dem,
            positiveFactors = positive,
            uncertaintyFactors = uncertainty,
            explanationSummary = summary
        )
    }
}
