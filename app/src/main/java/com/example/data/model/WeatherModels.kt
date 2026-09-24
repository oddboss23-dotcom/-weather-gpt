package com.example.data.model

import com.squareup.moshi.JsonClass

enum class UserMode(val title: String, val subtitle: String) {
    GENERAL_PUBLIC("General Public", "Citizen Weather & Safety"),
    FARMER_KRISHI("Farmer / Krishi", "Agro-Met Crop & Soil Guidance"),
    DISASTER_MANAGEMENT("Disaster Management", "Emergency Preparedness & Mitigation"),
    RESEARCHER("Researcher", "Meteorological Trends & NWP Data"),
    URBAN_PLANNING("Urban Planning", "Drainage & Infrastructure"),
    AVIATION("Aviation", "METAR / TAF & Ceiling Hazards"),
    MARINE("Marine / Coastal", "Sea State & Wave Advisories")
}

enum class IndianLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    ODIA("or", "Odia", "ଓଡ଼ିଆ"),
    BENGALI("bn", "Bengali", "বাংলা"),
    TAMIL("ta", "Tamil", "தமிழ்"),
    TELUGU("te", "Telugu", "తెలుగు"),
    MARATHI("mr", "Marathi", "मराठी"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી"),
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ"),
    MALAYALAM("ml", "Malayalam", "മലയാളം"),
    ASSAMESE("as", "Assamese", "অসমীয়া")
}

enum class ChatLanguageMode(
    val id: String,
    val displayName: String,
    val nativeName: String,
    val language: IndianLanguage?
) {
    AUTO("auto", "Auto Detect", "स्वतः पहचान", null),
    AUTO_DETECT("auto_detect", "Auto Detect", "स्वचालित", null),
    HINDI("hi", "Hindi", "हिन्दी", IndianLanguage.HINDI),
    HINGLISH("hinglish", "Hinglish", "हिंग्लिश", IndianLanguage.HINDI),
    ENGLISH("en", "English", "English", IndianLanguage.ENGLISH),
    BENGALI("bn", "Bengali", "বাংলা", IndianLanguage.BENGALI),
    MARATHI("mr", "Marathi", "मराठी", IndianLanguage.MARATHI),
    TELUGU("te", "Telugu", "తెలుగు", IndianLanguage.TELUGU),
    TAMIL("ta", "Tamil", "தமிழ்", IndianLanguage.TAMIL),
    GUJARATI("gu", "Gujarati", "ગુજરાતી", IndianLanguage.GUJARATI),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ", IndianLanguage.KANNADA),
    MALAYALAM("ml", "Malayalam", "മലയാളം", IndianLanguage.MALAYALAM),
    PUNJABI("pa", "Punjabi", "ਪੰਜਾਬੀ", IndianLanguage.PUNJABI),
    ODIA("or", "Odia", "ଓଡ଼ିଆ", IndianLanguage.ODIA),
    ASSAMESE("as", "Assamese", "অসমীয়া", IndianLanguage.ASSAMESE);

    val title: String
        get() = "$displayName ($nativeName)"

    val targetLanguage: IndianLanguage
        get() = language ?: IndianLanguage.HINDI
}

enum class AlertSeverity {
    RED,      // Extreme / Take Action
    ORANGE,   // Severe / Be Prepared
    YELLOW,   // Moderate / Be Aware
    GREEN     // Normal / Safe
}

data class WeatherData(
    val cityName: String = "Delhi",
    val stateOrRegion: String = "National Capital Territory",
    val country: String = "India",
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val temperatureC: Double = 34.0,
    val feelsLikeC: Double = 37.5,
    val tempMinC: Double = 26.0,
    val tempMaxC: Double = 35.0,
    val humidityPercent: Int = 65,
    val windSpeedKmh: Double = 18.0,
    val windDirectionDeg: Int = 220,
    val windDirectionText: String = "SW",
    val pressureHpa: Double = 1008.0,
    val uvIndex: Double = 7.2,
    val rainProbabilityPercent: Int = 68,
    val expectedRainfallMm: Double = 14.5,
    val visibilityKm: Double = 6.0,
    val sunriseTime: String = "05:54 AM",
    val sunsetTime: String = "06:58 PM",
    val weatherCode: Int = 2,
    val conditionDescription: String = "Partly Cloudy with Moderate Humidity",
    val lastUpdated: String = "Live • Just Now",
    val dataSource: String = "Open-Meteo & IMD Numerical Grid",
    val isRealTimeConnected: Boolean = true
)

data class HourlyForecast(
    val timeLabel: String,
    val hourOfDay: Int,
    val temperatureC: Double,
    val rainProbabilityPercent: Int,
    val precipitationMm: Double,
    val humidityPercent: Int,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val pressureHpa: Double,
    val weatherCode: Int
)

data class DailyForecast(
    val dateLabel: String,
    val dayName: String,
    val maxTempC: Double,
    val minTempC: Double,
    val rainProbabilityPercent: Int,
    val precipitationMm: Double,
    val conditionDescription: String,
    val weatherCode: Int
)

data class WeatherInsight(
    val summary: String,
    val forecastInsight: String,
    val rainProbability: Int,
    val expectedRainfallMm: String,
    val temperatureRange: String,
    val windInsight: String,
    val riskLevel: AlertSeverity,
    val riskDescription: String,
    val aiInterpretation: String,
    val generalRecommendation: String,
    val mitigationCitizen: String,
    val mitigationFarmer: String,
    val mitigationAuthority: String,
    val mitigationDisaster: String,
    val mitigationAviationMarine: String,
    val dataSources: String = "Observation Network • NWP Ensemble • IMD Grid Analysis",
    val isAiGenerated: Boolean = true
)

data class DisasterAlert(
    val id: String,
    val hazardType: String,
    val severity: AlertSeverity,
    val location: String,
    val title: String,
    val validTime: String,
    val expectedImpact: String,
    val recommendedAction: String,
    val mitigationGuidance: String,
    val verifiedSource: String = "National Meteorological Bulletin",
    val affectedDistricts: List<String> = emptyList(),
    val isFloodAlert: Boolean = false,
    val floodStation: String? = null,
    val floodRiver: String? = null,
    val floodCurrentLevel: Double? = null,
    val floodWarningLevel: Double? = null,
    val floodDangerLevel: Double? = null,
    val floodHighestFloodLevel: Double? = null,
    val floodTrend: String? = null,
    val floodDistanceAboveDanger: Double? = null,
    val floodDistanceAboveWarning: Double? = null,
    val floodRainfallMm: Double? = null,
    val floodForecastRainfallMm: Double? = null,
    val floodStatus: String? = null,
    val floodObservationTime: String? = null,
    val floodDataSource: String? = null,
    val floodRiskCategory: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val priorityRank: Int = 5
)

data class ResearchAnalysis(
    val query: String,
    val executiveSummary: String,
    val observedTrend: String,
    val rainfallAnalysis: String,
    val temperatureAnalysis: String,
    val extremeEvents: String,
    val riskAssessment: String,
    val aiInsights: String,
    val mitigationAdaptation: String,
    val dataSources: String,
    val historicalData: List<HistoricalDataPoint> = emptyList()
)

data class HistoricalDataPoint(
    val label: String,
    val rainfallMm: Double,
    val rainfallAnomalyMm: Double,
    val avgTempC: Double,
    val extremeEventCount: Int
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val insight: WeatherInsight? = null,
    val researchData: ResearchAnalysis? = null,
    val audioUrl: String? = null,
    val isSpeaking: Boolean = false,
    val isDetailedExpanded: Boolean = false
)

enum class GisLayerType(val label: String, val unit: String) {
    RADAR("Radar Reflectivity", "dBZ"),
    RAINFALL("Precipitation Accumulation", "mm"),
    WIND("Wind Velocity Vectors", "km/h"),
    TEMPERATURE("Surface Temperature", "°C"),
    ALERTS("Severe Warning Zones", "Risk"),
    CYCLONE_TRACK("Cyclone & Depression Tracks", "Category")
}

enum class ForecastHorizon(val hours: Int, val label: String) {
    HOURS_24(24, "24 Hours"),
    HOURS_48(48, "48 Hours"),
    DAYS_7(168, "7 Days")
}

enum class NwpModelType(val displayName: String, val institution: String) {
    GFS("Global Forecast System (GFS)", "NOAA / NCEP"),
    WRF("Weather Research and Forecasting (WRF)", "NCAR / Regional IMD"),
    ECMWF_ERA5("ECMWF IFS / ERA5 Reanalysis", "European Centre for Medium-Range Weather Forecasts"),
    OPEN_METEO_ENSEMBLE("High-Resolution Multi-Model Ensemble", "Open-Meteo Integration")
}

data class CityLocation(
    val name: String,
    val state: String,
    val latitude: Double,
    val longitude: Double
)
