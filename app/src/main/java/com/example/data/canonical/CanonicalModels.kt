package com.example.data.canonical

import com.example.data.model.AlertSeverity
import com.example.data.model.CityLocation
import com.example.data.model.DailyForecast
import com.example.data.model.HourlyForecast
import com.example.data.model.WeatherData
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ========================================================
 * CANONICAL DOMAIN SCHEMAS FOR WEATHERGPT / KRISHIGPT
 * Single Source of Truth for PS-68 & Flood Intelligence
 * ========================================================
 */

enum class GeographyType {
    COASTAL,
    INLAND,
    RIVERINE,
    FLOOD_PRONE,
    PLAIN,
    HILL,
    MOUNTAIN,
    DESERT,
    URBAN,
    RURAL
}

enum class ObservationType {
    OBSERVATION,
    FORECAST,
    MODEL,
    FUSED_ESTIMATE,
    HISTORICAL,
    AI_INFERENCE,
    CACHED,
    UNAVAILABLE
}

enum class DataFreshnessStatus(val label: String) {
    LIVE("Live Observation"),
    RECENT("Recent Update"),
    STALE("Stale Telemetry"),
    CACHED("Cached Local Snapshot"),
    UNAVAILABLE("Telemetry Unavailable")
}

enum class DataQuality {
    VALID,
    SUSPECT,
    INTERPOLATED,
    ESTIMATED,
    UNAVAILABLE
}

enum class DataSourceType(val displayName: String, val institution: String, val authoritative: Boolean) {
    OPEN_METEO("Open-Meteo High-Resolution Grid", "Open-Meteo Integration", false),
    IMD("India Meteorological Department (IMD)", "Ministry of Earth Sciences, GoI", true),
    NWP_ECMWF_GFS("NWP Multi-Model Ensemble (ECMWF/GFS/NCMRWF)", "Global Meteorological Center", true),
    RADAR_DWR("Doppler Weather Radar Network (DWR)", "IMD Radar Directorate", true),
    SATELLITE_INSAT_GPM("INSAT-3D / GPM Multi-Satellite Precipitation", "ISRO / NASA / JAXA", true),
    AWS_GROUND("Automatic Weather Station (AWS)", "IMD Surface Observation Network", true),
    ARG_RAIN_GAUGE("Automatic Rain Gauge (ARG)", "State Agromet / IMD Network", true),
    CWC_HYDROLOGY("Central Water Commission (CWC)", "Ministry of Jal Shakti, GoI", true),
    STATE_FLOOD_FMIS("State Flood Management Information System (FMIS)", "Water Resources Department", true),
    NDMA_EARLY_WARNING("National Disaster Management Authority (NDMA)", "Ministry of Home Affairs, GoI", true),
    INCOIS_OCEAN("INCOIS Ocean State Forecast", "MoES, Hyderabad", true)
}

/**
 * Authoritative Canonical Location representing a single geographical identity.
 */
data class CanonicalLocation(
    val id: String, // Deterministic canonicalLocationId e.g. "IN_DL_DELHI_28.6139_77.2090"
    val latitude: Double,
    val longitude: Double,
    val locality: String,
    val village: String,
    val panchayat: String,
    val subDistrict: String,
    val block: String,
    val tehsil: String,
    val district: String,
    val state: String,
    val country: String = "India",
    val geographyTypes: Set<GeographyType>,
    val elevation: Double?,
    val elevationSource: String?,
    val coastalDistanceKm: Double,
    val nearestRiver: String?,
    val nearestWeatherStation: String,
    val timezone: String = "Asia/Kolkata",
    val resolvedAt: Long = System.currentTimeMillis(),
    val source: String = "Authoritative Gazetteer & Reverse Geocoder"
) {
    fun toCityLocation(): CityLocation {
        val displayName = when {
            locality.isNotBlank() -> locality
            village.isNotBlank() -> village
            district.isNotBlank() -> district
            else -> "Location"
        }
        return CityLocation(
            name = displayName,
            state = state,
            latitude = latitude,
            longitude = longitude
        )
    }

    val isCoastal: Boolean
        get() = geographyTypes.contains(GeographyType.COASTAL) || coastalDistanceKm <= 50.0

    val isRiverine: Boolean
        get() = geographyTypes.contains(GeographyType.RIVERINE) || geographyTypes.contains(GeographyType.FLOOD_PRONE)

    val formattedHierarchy: String
        get() = listOf(locality.ifBlank { village }, block.ifBlank { subDistrict }, district, state)
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
}

data class SourceMetadata(
    val source: DataSourceType,
    val observationType: ObservationType,
    val timestamp: Long,
    val quality: DataQuality,
    val resolutionKm: Double? = null,
    val rawReference: String? = null
)

data class SourceObservation(
    val source: DataSourceType,
    val sourceType: String,
    val timestamp: Long,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val parameter: String,
    val value: Double?,
    val unit: String,
    val observationType: ObservationType,
    val quality: DataQuality,
    val rawReference: String? = null
)

data class WeatherObservation(
    val value: Double,
    val unit: String,
    val observationType: ObservationType,
    val source: DataSourceType,
    val timestamp: Long,
    val validTime: Long,
    val quality: DataQuality,
    val isEstimated: Boolean = false
)

data class WeatherForecast(
    val value: Double,
    val unit: String,
    val source: DataSourceType,
    val modelName: String,
    val generatedAt: Long,
    val validFrom: Long,
    val validTo: Long,
    val uncertaintyRange: Pair<Double, Double>? = null
)

data class PrecipitationState(
    val probabilityPercent: Int,
    val expectedRainfallMm: Double,
    val accumulation1hMm: Double,
    val accumulation3hMm: Double,
    val accumulation6hMm: Double,
    val accumulation24hMm: Double,
    val source: DataSourceType,
    val observationType: ObservationType,
    val isRadarCalibrated: Boolean = false,
    val validTime: Long = System.currentTimeMillis()
)

data class TemperatureState(
    val currentC: Double,
    val feelsLikeC: Double,
    val minC: Double,
    val maxC: Double,
    val source: DataSourceType,
    val observationType: ObservationType,
    val validTime: Long = System.currentTimeMillis()
)

data class HumidityState(
    val relativeHumidityPercent: Int,
    val dewPointC: Double,
    val source: DataSourceType,
    val observationType: ObservationType
)

data class WindState(
    val speedKmh: Double,
    val gustKmh: Double,
    val directionDeg: Int,
    val directionText: String,
    val source: DataSourceType,
    val observationType: ObservationType
)

data class PressureState(
    val surfaceHpa: Double,
    val seaLevelHpa: Double,
    val trend: String,
    val source: DataSourceType
)

data class VisibilityState(
    val distanceKm: Double,
    val condition: String,
    val source: DataSourceType
)

data class UvState(
    val index: Double,
    val riskLevel: String,
    val source: DataSourceType
)

data class CurrentWeatherCondition(
    val weatherCode: Int,
    val description: String,
    val iconCategory: String,
    val sunriseIso: String,
    val sunsetIso: String
)

data class CanonicalHourlyForecast(
    val timeLabel: String,
    val hourOfDay: Int,
    val validFrom: Long,
    val validTo: Long,
    val temperatureC: Double,
    val rainProbabilityPercent: Int,
    val precipitationMm: Double,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val pressureHpa: Double,
    val weatherCode: Int,
    val source: DataSourceType
) {
    fun toHourlyForecast(): HourlyForecast {
        return HourlyForecast(
            timeLabel = timeLabel,
            hourOfDay = hourOfDay,
            temperatureC = temperatureC,
            rainProbabilityPercent = rainProbabilityPercent,
            precipitationMm = precipitationMm,
            humidityPercent = humidityPercent,
            windSpeedKmh = windSpeedKmh,
            windDirectionDeg = windDirectionDeg,
            pressureHpa = pressureHpa,
            weatherCode = weatherCode
        )
    }
}

data class CanonicalDailyForecast(
    val dateLabel: String,
    val dayName: String,
    val validFrom: Long,
    val validTo: Long,
    val maxTempC: Double,
    val minTempC: Double,
    val rainProbabilityPercent: Int,
    val precipitationMm: Double,
    val conditionDescription: String,
    val weatherCode: Int,
    val source: DataSourceType
) {
    fun toDailyForecast(): DailyForecast {
        return DailyForecast(
            dateLabel = dateLabel,
            dayName = dayName,
            maxTempC = maxTempC,
            minTempC = minTempC,
            rainProbabilityPercent = rainProbabilityPercent,
            precipitationMm = precipitationMm,
            conditionDescription = conditionDescription,
            weatherCode = weatherCode
        )
    }
}

enum class RiverWaterStatus(val label: String, val severity: AlertSeverity) {
    BELOW_WARNING("Normal River Level (Below Warning)", AlertSeverity.GREEN),
    APPROACHING_WARNING("Approaching Warning Mark", AlertSeverity.YELLOW),
    WARNING("Above Warning Level", AlertSeverity.ORANGE),
    DANGER("Above Danger Level", AlertSeverity.RED),
    SEVERE_DANGER("Severe Flood (Exceeding Highest Flood Level)", AlertSeverity.RED)
}

data class RiverState(
    val riverName: String,
    val station: String,
    val currentLevel: Double,
    val warningLevel: Double,
    val dangerLevel: Double,
    val highestFloodLevel: Double,
    val trend: String, // "RISING", "FALLING", "STEADY"
    val rateOfRise: Double, // meters per hour
    val timestamp: Long,
    val source: DataSourceType = DataSourceType.CWC_HYDROLOGY,
    val status: RiverWaterStatus,
    val distanceToDangerM: Double,
    val estimatedHoursToDanger: Double? = null
)

data class HazardResult(
    val id: String,
    val hazardType: String,
    val severity: AlertSeverity,
    val location: String,
    val title: String,
    val description: String,
    val validFrom: Long,
    val validTo: Long,
    val isApplicableToLocation: Boolean,
    val source: DataSourceType,
    val confidenceScore: Double,
    val escalationState: String // "NEW", "UPDATED", "ESCALATED", "EXPIRED"
)

data class FloodRiskResult(
    val riskLevel: AlertSeverity,
    val riskScore: Double?, // null if not mathematically defensible
    val isFloodRiskActive: Boolean,
    val riverStatus: RiverState?,
    val rainfallHazardScore: Double,
    val terrainSusceptibility: Double,
    val upstreamRiskPresent: Boolean,
    val drivers: List<String>,
    val timestamp: Long,
    val sources: List<DataSourceType>,
    val uncertainty: String, // "LOW", "MEDIUM", "HIGH"
    val officialBulletinText: String
)

data class InundationResult(
    val floodProbability: Double,
    val potentialInundatedAreaKm2: Double?,
    val depthRangeMinM: Double?,
    val depthRangeMaxM: Double?,
    val estimatedOnsetTime: String?,
    val spatialResolution: String,
    val inputData: List<String>,
    val modelVersion: String,
    val uncertaintyDescription: String,
    val isAvailable: Boolean
)

data class ConfidenceResult(
    val scorePercent: Int,
    val scoreDecimal: Double,
    val basis: List<String>,
    val inputs: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

data class UncertaintyResult(
    val level: String, // "LOW", "MEDIUM", "HIGH"
    val explanation: String,
    val primaryContributingFactors: List<String>,
    val modelDisagreementSpread: Double? = null
)

data class VerificationResult(
    val rainfallMaeMm: Double?,
    val rainfallRmseMm: Double?,
    val rainfallBias: Double?,
    val brierScore: Double?,
    val floodClassificationPrecision: Double?,
    val floodClassificationRecall: Double?,
    val criticalSuccessIndexCsi: Double?,
    val floodExtentIou: Double?,
    val evaluationPairCount: Int,
    val hasGroundTruthData: Boolean,
    val statusText: String
)

data class AgricultureImpactResult(
    val cropName: String,
    val growthStage: String,
    val weatherSuitabilityScore: Int, // 0-100
    val sprayWindowStatus: String, // "SAFE", "UNSAFE - HIGH RAIN PROBABILITY", "UNSAFE - HIGH WIND"
    val irrigationAdvice: String,
    val pestDiseaseRisk: String,
    val harvestReadiness: String,
    val sources: List<DataSourceType>,
    val timestamp: Long
)

/**
 * ONE CANONICAL WEATHER SNAPSHOT.
 * Produced deterministically for: ONE LOCATION + ONE VALID TIME.
 * Consumed identically by Weather, Krishi, Radar, River, Voice, Chat, and Reports.
 */
data class WeatherSnapshot(
    val snapshotId: String, // Deterministic ID e.g. "IN_DL_DELHI_28.6139_77.2090_20260912T2230"
    val locationId: String,
    val canonicalLocation: CanonicalLocation,
    val generatedAt: Long,
    val validFrom: Long,
    val validTo: Long,
    val currentConditions: CurrentWeatherCondition,
    val hourlyForecast: List<CanonicalHourlyForecast>,
    val dailyForecast: List<CanonicalDailyForecast>,
    val precipitation: PrecipitationState,
    val temperature: TemperatureState,
    val humidity: HumidityState,
    val wind: WindState,
    val pressure: PressureState,
    val visibility: VisibilityState,
    val uv: UvState,
    val riverState: RiverState?,
    val floodRisk: FloodRiskResult,
    val hazards: List<HazardResult>,
    val agricultureImpact: AgricultureImpactResult,
    val sources: List<SourceMetadata>,
    val confidence: ConfidenceResult,
    val uncertainty: UncertaintyResult,
    val freshness: DataFreshnessStatus,
    val lastUpdatedText: String,
    val primarySourceDisplayName: String
) {
    /**
     * Converts Canonical Snapshot to UI-facing WeatherData for existing screens
     * guaranteeing 100% deterministic consistency.
     */
    fun toLegacyWeatherData(): WeatherData {
        return WeatherData(
            cityName = canonicalLocation.locality.ifBlank { canonicalLocation.district },
            stateOrRegion = canonicalLocation.state,
            country = canonicalLocation.country,
            latitude = canonicalLocation.latitude,
            longitude = canonicalLocation.longitude,
            temperatureC = temperature.currentC,
            feelsLikeC = temperature.feelsLikeC,
            tempMinC = temperature.minC,
            tempMaxC = temperature.maxC,
            humidityPercent = humidity.relativeHumidityPercent,
            windSpeedKmh = wind.speedKmh,
            windDirectionDeg = wind.directionDeg,
            windDirectionText = wind.directionText,
            pressureHpa = pressure.surfaceHpa,
            uvIndex = uv.index,
            rainProbabilityPercent = precipitation.probabilityPercent,
            expectedRainfallMm = precipitation.expectedRainfallMm,
            visibilityKm = visibility.distanceKm,
            sunriseTime = currentConditions.sunriseIso,
            sunsetTime = currentConditions.sunsetIso,
            weatherCode = currentConditions.weatherCode,
            conditionDescription = currentConditions.description,
            lastUpdated = lastUpdatedText,
            dataSource = primarySourceDisplayName,
            isRealTimeConnected = freshness == DataFreshnessStatus.LIVE || freshness == DataFreshnessStatus.RECENT
        )
    }
}
