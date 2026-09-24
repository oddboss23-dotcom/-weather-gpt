package com.example.data.climate

data class HistoricalDataPoint(
    val date: String,
    val timestamp: Long,
    val value: Double,
    val secondaryValue: Double? = null,
    val unit: String
)

data class HistoricalWeatherDataset(
    val locationName: String,
    val variable: String,
    val startDate: String,
    val endDate: String,
    val dataPoints: List<HistoricalDataPoint>,
    val averageValue: Double,
    val maxValue: Double,
    val minValue: Double,
    val trendDescription: String,
    val anomalyVsBaseline: Double,
    val dataCoveragePercent: Double,
    val source: String,
    val generatedAt: Long = System.currentTimeMillis()
)

enum class ClimateVariable(val id: String, val title: String, val unit: String) {
    TEMPERATURE("temperature", "Mean Temperature", "°C"),
    RAINFALL("rainfall", "Precipitation", "mm"),
    HUMIDITY("humidity", "Relative Humidity", "%"),
    WIND("wind", "Wind Speed", "km/h"),
    PRESSURE("pressure", "Surface Pressure", "hPa")
}
