package com.example.data.provider

import com.example.data.model.CityLocation
import com.example.data.model.DisasterAlert
import com.example.data.model.ForecastHorizon
import com.example.data.model.HourlyForecast
import com.example.data.model.NwpModelType
import com.example.data.model.ResearchAnalysis
import com.example.data.model.WeatherData
import kotlinx.coroutines.flow.Flow

/**
 * PHASE 1 ARCHITECTURAL PROVIDER ABSTRACTIONS
 * WeatherGPT strictly decouples UI & Business Logic from specific meteorological APIs.
 */

interface WeatherProvider {
    val providerName: String
    suspend fun getCurrentWeather(latitude: Double, longitude: Double): WeatherData
}

interface ForecastProvider {
    val providerName: String
    suspend fun getForecast(latitude: Double, longitude: Double, horizon: ForecastHorizon): List<HourlyForecast>
}

interface AlertProvider {
    val providerName: String
    suspend fun getActiveAlerts(latitude: Double, longitude: Double): List<DisasterAlert>
    fun observeRealtimeAlerts(): Flow<DisasterAlert>
}

interface NWPProvider {
    val modelType: NwpModelType
    suspend fun fetchModelGrid(latitude: Double, longitude: Double): Map<String, Any>
}

interface GISProvider {
    val layerName: String
    suspend fun fetchGeoJsonAlertPolygons(latitude: Double, longitude: Double, radiusKm: Double): String
}

interface VoiceProvider {
    val isAvailable: Boolean
    fun startSpeechToText(languageCode: String, onResult: (String) -> Unit)
    fun stopSpeechToText()
    fun speakText(text: String, languageCode: String)
}
