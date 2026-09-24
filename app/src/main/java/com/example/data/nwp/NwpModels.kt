package com.example.data.nwp

/**
 * Structured NWP & Forecast Data Models for WeatherGPT.
 * Single source of truth for 24H operational and 48H planning forecasts.
 */

enum class ForecastEventMarkerType(val emoji: String, val label: String) {
    RAIN_WINDOW("🌧", "Rain Window"),
    THUNDERSTORM_RISK("⛈", "Thunderstorm Risk"),
    HEAT_STRESS("🔥", "Heat Stress"),
    STRONG_WIND("💨", "Strong Wind"),
    CLEAR_WINDOW("☀️", "Clear Window"),
    VISIBILITY_RISK("🌫", "Visibility Risk")
}

data class ForecastEventMarker(
    val type: ForecastEventMarkerType,
    val title: String,
    val description: String,
    val supportingData: String,
    val severity: String = "INFO" // INFO, CAUTION, SEVERE
)

data class ForecastPoint(
    val timestamp: Long,
    val timeLabel: String, // e.g. "14:00" or "NOW"
    val temperature: Double,
    val feelsLike: Double,
    val precipProbability: Int,
    val precipAmount: Double,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val pressure: Double,
    val cloudCover: Int,
    val visibility: Double,
    val condition: String,
    val weatherCode: Int,
    val source: String,
    val forecastHorizon: Int, // lead time in hours
    val confidence: Int,
    val uncertainty: String,
    val eventMarkers: List<ForecastEventMarker> = emptyList()
)

data class Forecast24H(
    val points: List<ForecastPoint>,
    val minTemp: Double,
    val maxTemp: Double,
    val totalRainMm: Double,
    val peakRainHour: String?,
    val activeEvents: List<ForecastEventMarker>,
    val issuedAt: String,
    val validRange: String,
    val source: String,
    val confidenceScore: Int,
    val confidenceCategory: String,
    val uncertaintyTempRangeC: Double,
    val uncertaintyPrecipRangeMm: Double,
    val whyThisForecastBulletPoints: List<String>
)

enum class TrendDirection(val symbol: String, val description: String) {
    RISING("↑", "Increasing"),
    FALLING("↓", "Decreasing"),
    STEADY("→", "Stable")
}

data class DiurnalPeriod(
    val periodName: String, // "Morning", "Afternoon", "Evening", "Night"
    val timeRange: String,  // "06:00–12:00", etc.
    val tempAvg: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val rainProbability: Int,
    val expectedRainMm: Double,
    val windSpeed: Double,
    val humidity: Int,
    val condition: String,
    val weatherCode: Int
)

data class ForecastDaySummary(
    val dayName: String, // "TODAY" / "TOMORROW"
    val dateLabel: String,
    val maxTemp: Double,
    val minTemp: Double,
    val rainProbability: Int,
    val expectedRainMm: Double,
    val windSpeed: Double,
    val humidity: Int,
    val confidenceCategory: String, // "High", "Moderate", "Low"
    val periods: List<DiurnalPeriod>
)

data class Forecast48H(
    val next24Hours: ForecastDaySummary,
    val hours24To48: ForecastDaySummary,
    val tempTrend: TrendDirection,
    val tempTrendDeltaC: Double,
    val rainTrend: TrendDirection,
    val rainTrendDeltaPercent: Int,
    val windTrend: TrendDirection,
    val windTrendDeltaKmh: Double,
    val humidityTrend: TrendDirection,
    val humidityTrendDeltaPercent: Int,
    val weatherTransitions: List<String>,
    val hazardWindows: List<String>,
    val modelAgreement: String, // "High", "Moderate", or "Single NWP guidance"
    val confidenceScore: Int,
    val confidenceCategory: String,
    val uncertaintyRange: String,
    val forecastBasis: List<String>
)

/**
 * Feature vector representation for NWP post-processing and ML calibration.
 */
data class FeatureVector(
    val latitude: Double,
    val longitude: Double,
    val elevationM: Double?,
    val forecastLeadTimeHours: Int,
    val nwpTemperatureC: Double,
    val nwpPrecipitationMm: Double,
    val nwpHumidityPercent: Int,
    val nwpWindKmh: Double,
    val nwpPressureHpa: Double,
    val nwpCloudCoverPercent: Int,
    val recentObservedTempC: Double?,
    val recentObservedPrecipMm: Double?,
    val recentObservedHumidityPercent: Int?,
    val radarRainfallMm: Double?,
    val satelliteDerivedCloudInfo: String?,
    val hourOfDay: Int,
    val dayOfYear: Int,
    val previousForecastErrorC: Double? = null
)

/**
 * Historical validation metrics. Only populated when real verification pairs exist.
 */
data class HistoricalWeatherDataset(
    val datasetName: String,
    val samplePairsCount: Int,
    val temperatureMaeC: Double?,
    val temperatureRmseC: Double?,
    val temperatureBiasC: Double?,
    val precipitationMaeMm: Double?,
    val brierScore: Double?,
    val rawNwpErrorC: Double?,
    val calibratedErrorC: Double?,
    val improvementPercent: Double?,
    val isSufficientForTraining: Boolean,
    val statusMessage: String
)
