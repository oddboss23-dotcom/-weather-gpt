package com.example.data.model

typealias AviationWeather = AirportWeather

data class AirportWeather(
    val icaoCode: String,
    val airportName: String,
    val cityName: String,
    val stateName: String,
    val visibilityKm: Double,
    val cloudCeilingFt: Int,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val crosswindKmh: Double,
    val thunderstormRisk: String, // "Low", "Moderate", "High"
    val turbulenceIndication: String, // "Light", "Moderate", "Severe"
    val flightWeatherRisk: String, // "LOW", "MODERATE", "ELEVATED"
    val metarRaw: String,
    val tafRaw: String,
    val aiBriefing: String,
    val source: String = "AAI / IMD Aviation Met & AWS",
    val updatedAt: String = "Just now",
    val confidenceScore: Int = 94
)

data class MarineWeather(
    val coastalZoneName: String,
    val seaArea: String, // "Arabian Sea", "Bay of Bengal", "Indian Ocean"
    val waveHeightM: Double,
    val swellHeightM: Double,
    val swellPeriodSec: Int,
    val windSpeedKmh: Double,
    val windDirection: String,
    val seaSurfaceTempC: Double,
    val visibilityKm: Double,
    val stormSurgeRisk: String, // "Low", "Moderate", "High"
    val cycloneInfluence: String, // "Distant Swell", "Active Band", "None"
    val fishingSafety: String, // "SAFE", "CAUTION ADVISED", "RESTRICTED", "NO SAILING"
    val aiAdvisory: String,
    val source: String = "INCOIS Coastal Buoys & IMD Marine Met",
    val updatedAt: String = "Just now",
    val confidenceScore: Int = 92
)

data class NwpModelComparison(
    val locationName: String,
    val gfsRainMm: Double,
    val wrfRainMm: Double,
    val imdRainMm: Double,
    val gfsTempC: Double,
    val wrfTempC: Double,
    val imdTempC: Double,
    val gfsWindKmh: Double,
    val wrfWindKmh: Double,
    val imdWindKmh: Double,
    val gfsHumidity: Int,
    val wrfHumidity: Int,
    val imdHumidity: Int,
    val modelAgreementPercent: Int,
    val forecastConfidence: String, // "HIGH", "MODERATE", "UNCERTAIN"
    val uncertaintySummary: String,
    val aiComparisonAnalysis: String,
    val satelliteStatus: String = "INSAT-3DR Geostationary: Active (IR Brightness 218K)",
    val radarStatus: String = "Doppler Weather Radar (DWR): Active (Reflectivity 32 dBZ)",
    val isDisagreementHigh: Boolean = false,
    val uncertaintyWarning: String = "Forecast uncertainty HIGH (Spread > 5.0 mm / 3.0°C across models)",
    val source: String = "NCMRWF-GFS (12km) + IMD-WRF (3km) + INSAT-3DR + DWR",
    val updatedAt: String = "15 min ago"
)

data class ClimateTrendData(
    val cityName: String,
    val parameter: String, // "Temperature", "Rainfall", "Humidity", "Extreme Events"
    val timeRangeYears: Int, // 5, 10, 20, 50
    val historicalBaseline: Double,
    val currentPeriodAverage: Double,
    val anomalyValue: Double,
    val trendDirection: String, // "+0.68°C / decade warming", "+14% monsoon intensity"
    val heatwaveFrequencyIncrease: String,
    val rainfallVariability: String,
    val aiClimateExplanation: String,
    val trendPoints: List<Pair<String, Double>>,
    val source: String = "IMD Gridded Climate Data (1971–2025)",
    val confidenceScore: Int = 96
)

data class SmartCityWeatherRisk(
    val cityName: String,
    val urbanFloodRisk: String, // "LOW", "MODERATE", "HIGH", "CRITICAL"
    val waterloggingRisk: String,
    val heatRisk: String,
    val visibilityImpact: String,
    val windHazard: String,
    val trafficImpact: String,
    val drainageLoadPercent: Int,
    val criticalInfrastructureRisk: String,
    val vulnerableZones: List<String>,
    val actionAdvisory: String,
    val source: String = "IMD Urban Radar + Smart City C4i Center",
    val updatedAt: String = "8 min ago"
)

data class DisasterImpactAssessment(
    val eventType: String, // "Cyclone", "Urban Flood", "Lightning", "Heatwave"
    val title: String,
    val severity: String, // "MODERATE", "HIGH", "VERY HIGH", "EXTREME"
    val affectedRegion: String,
    val riskRadiusKm: Double,
    val populationExposure: String,
    val agricultureExposure: String,
    val infrastructureExposure: String,
    val roadsAtRisk: String,
    val recommendedAction: String,
    val timeWindow: String,
    val windRangeKmh: String,
    val rainfallRangeMm: String,
    val source: String = "NDRF / IMD Early Warning Framework",
    val confidenceScore: Int = 93
)

data class UserCustomAlertRule(
    val id: String,
    val ruleName: String,
    val conditionType: String, // "Rainfall", "Temperature", "Lightning", "Cyclone", "Flood", "Wind"
    val thresholdValue: String,
    val channel: String, // "App Notification", "Voice Alert", "SMS Broadcast", "Webhook"
    val isEnabled: Boolean = true
)
