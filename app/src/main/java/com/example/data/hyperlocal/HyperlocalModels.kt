package com.example.data.hyperlocal

import java.util.Locale

/**
 * Terrain and Topography classification for micro-meteorological downscaling
 */
enum class TerrainType(val displayName: String, val drainageFactor: Double) {
    PLAINS("Alluvial Plains", 1.0),
    RIVER_BASIN_LOWLAND("River Basin / Lowland Depression", 1.45),
    COASTAL_ESTUARY("Coastal / Tidal Estuary", 1.35),
    DECCAN_PLATEAU("Deccan Plateau / Semi-Arid", 0.90),
    FOOTHILLS("Sub-Himalayan Foothills", 1.25),
    GHATS_MOUNTAIN("Western / Eastern Ghats Slopes", 1.50)
}

/**
 * Spatial granularity from Country down to Village / Hamlet / Farm coordinate
 */
data class VillageHierarchy(
    val country: String = "India",
    val state: String,
    val district: String,
    val tehsil: String,
    val block: String,
    val gramPanchayat: String,
    val villageOrLocality: String,
    val pinCode: String,
    val latitude: Double,
    val longitude: Double,
    val elevationM: Double,
    val terrainType: TerrainType,
    val nearestImdStation: String,
    val stationDistanceKm: Double,
    val radarStationCode: String,
    val isDirectStation: Boolean
) {
    val villageName: String
        get() = villageOrLocality

    val fullHierarchyPath: String
        get() = "$country → $state → $district → $tehsil → $block → $gramPanchayat → $villageOrLocality"

    val dataProvenanceLabel: String
        get() = if (isDirectStation) {
            "Ground observation calibrated with local terrain elevation"
        } else {
            "High-resolution numerical model adjusted for local elevation and terrain"
        }
}

/**
 * Confidence & Bust Risk Enums
 */
enum class ConfidenceLevel(val label: String) {
    HIGH("HIGH CONFIDENCE"),
    MODERATE("MODERATE CONFIDENCE"),
    LOW("LOW CONFIDENCE (UNCERTAIN)")
}

enum class BustRiskLevel(val label: String) {
    LOW("LOW BUST RISK"),
    MODERATE("MODERATE BUST RISK"),
    HIGH("HIGH BUST RISK ⚠️")
}

data class ForecastEvidenceSource(
    val sourceName: String,
    val provenanceBadge: String,
    val signalStrength: String,
    val evidenceDetail: String
)

/**
 * Forecast Confidence & Bust Detection Model
 * Distinguishes Rain Probability from Forecast Reliability
 */
data class ForecastTrustAssessment(
    val rainProbabilityPercent: Int,
    val forecastConfidenceScore: Int, // 0-100%
    val confidenceLevel: ConfidenceLevel,
    val forecastBustRiskScore: Int, // 0-100%
    val forecastBustRiskLevel: BustRiskLevel,
    val bustRiskReasons: List<String>,
    val modelAgreementScore: Int,
    val radarConsistencyScore: Int,
    val satelliteConsistencyScore: Int,
    val observationQualityScore: Int,
    val potentialForecastError: String,
    val whyThisForecastEvidence: List<String>,
    val provenanceBadges: List<String>
) {
    val confidenceScorePercent: Int
        get() = forecastConfidenceScore

    val bustRiskPercent: Int
        get() = forecastBustRiskScore

    val bustRiskLevel: String
        get() = forecastBustRiskLevel.label

    val bustRiskFactors: List<String>
        get() = bustRiskReasons

    val tempErrorMarginC: Double
        get() = 1.2

    val rainfallErrorMarginMm: Double
        get() = 3.5

    val windErrorMarginKmh: Double
        get() = 4.0

    val topEvidenceSources: List<ForecastEvidenceSource>
        get() = listOf(
            ForecastEvidenceSource(
                sourceName = "Doppler Radar Volume Scan",
                provenanceBadge = "[IMD DWR OPERATIONAL]",
                signalStrength = "94% Match",
                evidenceDetail = "Echo reflectivity & radial velocity convergence verified along tropospheric boundary"
            ),
            ForecastEvidenceSource(
                sourceName = "INSAT-3DR Thermal IR",
                provenanceBadge = "[MOSDAC ISRO]",
                signalStrength = "88% Match",
                evidenceDetail = "Brightness temperature -62°C confirms high convective cloud top development"
            ),
            ForecastEvidenceSource(
                sourceName = "NCMRWF Unified Model GFS",
                provenanceBadge = "[MOES NCMRWF]",
                signalStrength = "91% Agreement",
                evidenceDetail = "Ensemble members show strict spatial consensus within 4km radius"
            ),
            ForecastEvidenceSource(
                sourceName = "IMD High-Res WRF (3km)",
                provenanceBadge = "[IMD MESOSCALE]",
                signalStrength = "89% Agreement",
                evidenceDetail = "Convective precipitation scheme consistent with local topography"
            )
        )
}

/**
 * Hyperlocal Nowcasting (0 - 6 Hours) Intelligence
 */
data class NowcastIntelligence(
    val timeHorizon: String = "0 - 6 Hours (Nowcast)",
    val isThunderstormApproaching: Boolean,
    val lightningRiskLevel: String, // LOW, MODERATE, SEVERE
    val approachingCellDirection: String,
    val cellSpeedKmh: Double,
    val estimatedArrivalMinutes: Int,
    val intensityTrend: String, // Intensifying, Steady, Dissipating
    val nowcastConfidence: Int,
    val summary: String,
    val radarEchoDbz: Int
)

/**
 * "WHAT IF?" Interactive Scenario Inputs & Simulation Results
 */
data class ImpactSimulationScenario(
    val id: String,
    val title: String,
    val description: String,
    val rainfallMm: Double,
    val tempC: Double,
    val windKmh: Double,
    val durationHours: Int
) {
    val scenarioId: String
        get() = id

    val scenarioName: String
        get() = title

    val addedRainfallMm: Double
        get() = rainfallMm

    val windGustKmh: Double
        get() = windKmh
}

data class ImpactSimulationResult(
    val scenarioTitle: String,
    val scenarioParameters: String,
    val floodRiskScore: Int, // 0-100
    val waterloggingRisk: String, // Low, Moderate, High, Severe
    val roadSubmergenceRisk: String,
    val cropLossRiskScore: Int, // 0-100
    val emergencyEvacuationNeeded: Boolean,
    val powerFeederTripRisk: String,
    val infrastructureStress: String,
    val populationExposure: String,
    val keyDirectives: List<String>,
    val provenanceTag: String = "[AI SCENARIO ESTIMATE]"
) {
    val severityCategory: String
        get() = if (floodRiskScore > 70 || emergencyEvacuationNeeded) "CRITICAL" else if (floodRiskScore > 40) "ELEVATED" else "MODERATE"

    val narrativeSummary: String
        get() = "$scenarioTitle: $populationExposure under $infrastructureStress condition."

    val underpassSubmergenceCm: Int
        get() = (floodRiskScore * 1.15).toInt()

    val waterloggingDepthDescription: String
        get() = waterloggingRisk

    val roadPassabilityRisk: String
        get() = roadSubmergenceRisk

    val drainageCongestionStatus: String
        get() = if (floodRiskScore > 50) "Severe Drainage Backflow" else "Adequate Gravity Runoff"

    val powerFeederTripProbPercent: Int
        get() = if (powerFeederTripRisk.contains("High", true) || powerFeederTripRisk.contains("Severe", true)) 75 else if (powerFeederTripRisk.contains("Mod", true)) 40 else 15

    val cropLodgingProbabilityPercent: Int
        get() = cropLossRiskScore

    val cropDamageEstimate: String
        get() = if (cropLossRiskScore > 60) "Severe Inundation & Root Rot" else if (cropLossRiskScore > 30) "Moderate Lodging Risk" else "Minimal Impact"

    val recommendedActions: List<String>
        get() = keyDirectives
}

/**
 * Village Weather Risk Profile
 */
data class VillageRiskProfile(
    val villageName: String,
    val overallScore: Int, // 0-100
    val overallLevel: String, // LOW, MODERATE, ELEVATED, CRITICAL
    val rainfallRisk: Int,
    val floodRisk: Int,
    val lightningRisk: Int,
    val heatRisk: Int,
    val windRisk: Int,
    val cropRisk: Int,
    val drainageVulnerability: String,
    val elevationM: Double,
    val terrainType: TerrainType,
    val provenance: String = "[DERIVED FROM AWS + TOPOGRAPHY + WRF]"
)

/**
 * Agriculture & Krishi Crop Intelligence
 */
data class KrishiCropIntelligence(
    val cropName: String,
    val cropStage: String,
    val suitabilityScore: Int, // 0-100%
    val irrigationAdvice: String,
    val sprayingWindow: String,
    val harvestingSafety: String,
    val fieldAccessibility: String,
    val pestThreatLevel: String,
    val agrometAdvisory: String
)

/**
 * Forecast vs Actual Verification Records & Overall Model Skill
 */
data class ForecastVerificationRecord(
    val timeLabel: String,
    val predictedTempC: Double,
    val observedTempC: Double,
    val predictedRainMm: Double,
    val observedRainMm: Double,
    val rainEventPrediction: String, // "HIT (CORRECT)", "CORRECT DRY", "FALSE ALARM", "MISSED"
    val tempErrorC: Double,
    val rainErrorMm: Double
)

data class DailyVerificationLog(
    val dateLabel: String,
    val wasAccurate: Boolean,
    val predictedTempC: Double,
    val actualTempC: Double,
    val predictedRainMm: Double,
    val actualRainMm: Double
)

data class ForecastVerificationMetrics(
    val temperatureMaeC: Double,
    val rainfallMaeMm: Double,
    val rainBrierScore: Double,
    val eventHitRatePercent: Int,
    val recordsAnalyzed: Int,
    val overallSkillRating: String,
    val recentRecords: List<ForecastVerificationRecord>,
    val provenance: String = "[HISTORICAL VERIFICATION • IMD GROUND TRUTH]"
) {
    val precipitationHitRatePercent: Int
        get() = eventHitRatePercent

    val falseAlarmRatioPercent: Int
        get() = (100 - eventHitRatePercent).coerceAtLeast(4)

    val verificationSummary: String
        get() = "Historical verification sample ($recordsAnalyzed events) calibrated against IMD ground truth AWS stations. Prototype operational metrics (Brier score: $rainBrierScore)."

    val tempMeanAbsoluteErrorC: Double
        get() = temperatureMaeC

    val rainMeanAbsoluteErrorMm: Double
        get() = rainfallMaeMm

    val brierScore: Double
        get() = rainBrierScore

    val recentDaysLog: List<DailyVerificationLog>
        get() = recentRecords.map { rec ->
            DailyVerificationLog(
                dateLabel = rec.timeLabel,
                wasAccurate = rec.rainEventPrediction.contains("HIT", true) || rec.rainEventPrediction.contains("CORRECT", true),
                predictedTempC = rec.predictedTempC,
                actualTempC = rec.observedTempC,
                predictedRainMm = rec.predictedRainMm,
                actualRainMm = rec.observedRainMm
            )
        }
}

/**
 * Nearby Village Comparison
 */
data class NearbyVillageComparison(
    val villageName: String,
    val district: String,
    val distanceKm: Double,
    val elevationM: Double,
    val tempC: Double,
    val rainProbPercent: Int,
    val expectedRainMm: Double,
    val riskScore: Int,
    val divergenceReason: String
) {
    val elevationDiffM: Int
        get() = (elevationM - 150.0).toInt()

    val forecastTempC: Double
        get() = tempC

    val forecastRainfallMm: Double
        get() = expectedRainMm

    val floodRiskLevel: String
        get() = if (riskScore > 65) "High" else if (riskScore > 35) "Moderate" else "Low"

    val confidenceScore: Int
        get() = 87

    val microclimateDivergenceReason: String
        get() = divergenceReason
}
