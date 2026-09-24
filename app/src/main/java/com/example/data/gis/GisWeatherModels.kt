package com.example.data.gis

import com.example.data.model.AlertSeverity

/**
 * Supported Weather & Meteorological GIS Layers
 */
enum class WeatherGisLayer(
    val id: String,
    val displayName: String,
    val unit: String,
    val defaultOpacity: Float,
    val primarySource: String,
    val productType: String,
    val description: String
) {
    SATELLITE(
        id = "satellite",
        displayName = "Satellite",
        unit = "IR / VIS",
        defaultOpacity = 0.85f,
        primarySource = "INSAT-3DR / MOSDAC",
        productType = "SATELLITE-DERIVED",
        description = "INSAT-3DR Geostationary infrared cloud imagery & moisture fields"
    ),
    RADAR(
        id = "radar",
        displayName = "Radar",
        unit = "dBZ",
        defaultOpacity = 0.80f,
        primarySource = "IMD Doppler Radar Network",
        productType = "RADAR",
        description = "Doppler Weather Radar (DWR) composite reflectivity & echo tops"
    ),
    RAINFALL(
        id = "rainfall",
        displayName = "Rainfall",
        unit = "mm/h",
        defaultOpacity = 0.70f,
        primarySource = "INSAT-3DR QPE & IMD Gauge Grid",
        productType = "SATELLITE-DERIVED",
        description = "Quantitative Precipitation Estimation (QPE) rate & accumulation"
    ),
    WIND(
        id = "wind",
        displayName = "Wind Streamlines",
        unit = "km/h",
        defaultOpacity = 0.75f,
        primarySource = "INSAT-3DR CMV & NWP WRF",
        productType = "NWP MODEL",
        description = "Cloud Motion Vectors (CMV) & animated low-level wind circulation"
    ),
    TEMPERATURE(
        id = "temperature",
        displayName = "Temperature",
        unit = "°C",
        defaultOpacity = 0.65f,
        primarySource = "IMD AWS & NWP GFS Grid",
        productType = "OBSERVATION",
        description = "Surface 2m atmospheric temperature gradient thermal heatmap"
    ),
    HUMIDITY(
        id = "humidity",
        displayName = "Humidity",
        unit = "%",
        defaultOpacity = 0.65f,
        primarySource = "INSAT-3DR UTH & Surface Obs",
        productType = "SATELLITE-DERIVED",
        description = "Relative humidity & Upper Tropospheric Humidity (UTH) saturation"
    ),
    CLOUD_COVER(
        id = "cloud_cover",
        displayName = "Cloud Cover",
        unit = "%",
        defaultOpacity = 0.75f,
        primarySource = "INSAT-3DR / MOSDAC",
        productType = "SATELLITE-DERIVED",
        description = "Optical cloud fraction & convective cloud top temperature"
    ),
    LIGHTNING(
        id = "lightning",
        displayName = "Lightning",
        unit = "flashes/km²",
        defaultOpacity = 0.90f,
        primarySource = "IITM Damini & IMD Lightning Grid",
        productType = "OBSERVATION",
        description = "Real-time atmospheric electrostatic discharge & convective strike density"
    ),
    CYCLONE(
        id = "cyclone",
        displayName = "Cyclone Track",
        unit = "Category",
        defaultOpacity = 0.95f,
        primarySource = "IMD RSMC Tropical Cyclones",
        productType = "OFFICIAL RSMC BULLETIN",
        description = "Observed vortex center, past positions, cone of uncertainty & forecast track"
    ),
    HEAT_INDEX(
        id = "heat_index",
        displayName = "Heat Index",
        unit = "°C",
        defaultOpacity = 0.70f,
        primarySource = "WeatherGPT Meteorological Engine",
        productType = "DERIVED AI/RISK",
        description = "Biometeorological apparent heat stress computed via Rothfusz equation"
    ),
    FOG(
        id = "fog",
        displayName = "Fog & Smog",
        unit = "Index",
        defaultOpacity = 0.75f,
        primarySource = "INSAT-3DR Night Microphysics",
        productType = "SATELLITE-DERIVED",
        description = "Dual-channel brightness temperature difference (BTD) radiation fog"
    ),
    PRESSURE(
        id = "pressure",
        displayName = "Pressure (MSLP)",
        unit = "hPa",
        defaultOpacity = 0.60f,
        primarySource = "IMD Synoptic Charts & NWP GFS",
        productType = "NWP MODEL",
        description = "Mean Sea Level Pressure isobars & synoptic low/trough axes"
    ),
    VISIBILITY(
        id = "visibility",
        displayName = "Visibility",
        unit = "km",
        defaultOpacity = 0.65f,
        primarySource = "IMD METAR & MOSDAC AOD",
        productType = "OBSERVATION",
        description = "Horizontal runway & surface visual range"
    ),
    FLOOD_RISK(
        id = "flood_risk",
        displayName = "Flood Inundation",
        unit = "Risk Level",
        defaultOpacity = 0.75f,
        primarySource = "CWC Flood Cell & WeatherGPT ML",
        productType = "DERIVED AI/RISK",
        description = "Basin discharge runoff anomaly & urban flash-flood susceptibility"
    ),
    WATERLOGGING(
        id = "waterlogging",
        displayName = "Waterlogging",
        unit = "Inundation cm",
        defaultOpacity = 0.85f,
        primarySource = "Urban Drainage & Municipal GIS",
        productType = "DERIVED AI/RISK",
        description = "Arterial road, underpass water accumulation & municipal drainage overflow"
    ),
    FORECAST_CONFIDENCE(
        id = "forecast_confidence",
        displayName = "Confidence",
        unit = "%",
        defaultOpacity = 0.75f,
        primarySource = "WeatherGPT Trust Engine",
        productType = "DERIVED AI/RISK",
        description = "Multi-model ensemble consensus & radar consistency confidence score"
    ),
    FORECAST_BUST_RISK(
        id = "bust_risk",
        displayName = "Bust Risk",
        unit = "Risk Level",
        defaultOpacity = 0.80f,
        primarySource = "WeatherGPT Bust Detector",
        productType = "DERIVED AI/RISK",
        description = "Atmospheric instability & forecast bust vulnerability level"
    )
}

/**
 * Data Freshness Classification
 */
enum class DataFreshness(val label: String, val badgeColorHex: Long) {
    LIVE("LIVE (<15m)", 0xFF10B981),        // AlertGreen
    RECENT("RECENT (15-60m)", 0xFF00E5FF),  // ElectricCyan
    AGING("AGING (1-6h)", 0xFFF59E0B),      // AlertYellow
    STALE("STALE (>6h)", 0xFFEF4444)        // AlertRed
}

/**
 * Data Provenance Classification
 */
enum class DataProvenance(val tag: String) {
    SATELLITE_DERIVED("SATELLITE-DERIVED"),
    OBSERVATION("OBSERVATION"),
    RADAR("RADAR"),
    NWP_MODEL("NWP MODEL"),
    WEATHER_API("WEATHER API"),
    DERIVED_AI_RISK("DERIVED AI/RISK"),
    OFFICIAL_BULLETIN("OFFICIAL RSMC BULLETIN")
}

/**
 * Comprehensive point inspection model when the user taps ANY location on the map
 */
data class GisPointInspection(
    val latitude: Double,
    val longitude: Double,
    val localityName: String,
    val districtName: String,
    val stateName: String,
    val temperatureC: Double?,
    val feelsLikeC: Double?,
    val humidityPercent: Int?,
    val rainfallRateMmH: Double?,
    val rainProbabilityPercent: Int?,
    val windSpeedKmh: Double?,
    val windDirectionDeg: Int?,
    val windDirectionText: String?,
    val pressureHpa: Double?,
    val cloudCoverPercent: Int?,
    val visibilityKm: Double?,
    val uvIndex: Double?,
    val lightningRisk: String?, // "Low", "Moderate", "High", "Critical" or null
    val heatIndexCategory: String?, // "Normal", "Caution", "Extreme Caution", "Danger"
    val timestamp: Long,
    val timeLabel: String,
    val primarySource: String,
    val provenance: DataProvenance,
    val freshness: DataFreshness,
    val confidencePercent: Int,
    val weatherGptAnalysis: String,
    val isOfflineCached: Boolean = false
)

/**
 * Represents an animated weather frame along the temporal slider
 */
data class GisTimelineFrame(
    val frameIndex: Int,
    val offsetHours: Int, // e.g. -3, -2, -1, 0, 1, 2, 3, 6
    val label: String,    // "T-3h", "T-1h", "NOW", "+2h", "+6h"
    val formattedTime: String, // "08 Sep 14:20 IST"
    val isForecast: Boolean,
    val satelliteCloudOpacityMultiplier: Float = 1.0f,
    val stormShiftX: Float = 0f,
    val stormShiftY: Float = 0f,
    val cycloneCenterLat: Double = 16.4,
    val cycloneCenterLon: Double = 84.8
)

/**
 * Cyclone track storm system model
 */
data class GisCycloneSystem(
    val systemId: String,
    val name: String,
    val classification: String, // e.g. "Severe Cyclonic Storm (VSCS)"
    val currentLat: Double,
    val currentLon: Double,
    val centralPressureHpa: Double,
    val maxSustainedWindKmh: Double,
    val gustKmh: Double,
    val movementDirection: String,
    val movementSpeedKmh: Double,
    val radiusOfGaleWindKm: Double,
    val issueTimeIST: String,
    val validUntilIST: String,
    val source: String = "IMD RSMC New Delhi Tropical Cyclone Advisory",
    val pastPoints: List<CycloneTrackPoint>,
    val forecastPoints: List<CycloneTrackPoint>,
    val warningDistricts: List<String>
)

data class CycloneTrackPoint(
    val latitude: Double,
    val longitude: Double,
    val timeLabel: String,
    val intensityKmh: Double,
    val category: String
)

/**
 * Regional hierarchy level for dynamic semantic zoom
 */
enum class MapZoomDetailLevel(val levelNumber: Int, val title: String) {
    LEVEL_1_INDIA(1, "LEVEL 1: India Subcontinent Overview"),
    LEVEL_2_STATE(2, "LEVEL 2: State Regional View"),
    LEVEL_3_DISTRICT(3, "LEVEL 3: District Basin View"),
    LEVEL_4_CITY(4, "LEVEL 4: City & Local Grid View"),
    LEVEL_5_INSPECTION(5, "LEVEL 5: Precision Coordinate Inspection")
}
