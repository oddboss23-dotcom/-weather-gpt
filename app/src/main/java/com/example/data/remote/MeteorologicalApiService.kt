package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * External Meteorological Source DTOs & Retrofit Interface.
 * Handles incoming NWP (ECMWF/Best-Match) and NOAA-GFS weather data,
 * with strict schema validation and unit normalization.
 */

@JsonClass(generateAdapter = true)
data class NwpForecastDto(
    @Json(name = "latitude") val latitude: Double?,
    @Json(name = "longitude") val longitude: Double?,
    @Json(name = "elevation") val elevation: Double?,
    @Json(name = "generationtime_ms") val generationTimeMs: Double?,
    @Json(name = "utc_offset_seconds") val utcOffsetSeconds: Int?,
    @Json(name = "timezone") val timezone: String?,
    @Json(name = "current") val current: NwpCurrentDto?,
    @Json(name = "hourly") val hourly: NwpHourlyDto?,
    @Json(name = "daily") val daily: NwpDailyDto?
)

@JsonClass(generateAdapter = true)
data class NwpCurrentDto(
    @Json(name = "time") val time: String?,
    @Json(name = "temperature_2m") val temperature2m: Double?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Double?,
    @Json(name = "apparent_temperature") val apparentTemperature: Double?,
    @Json(name = "precipitation") val precipitation: Double?,
    @Json(name = "rain") val rain: Double?,
    @Json(name = "weather_code") val weatherCode: Int?,
    @Json(name = "surface_pressure") val surfacePressure: Double?,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double?,
    @Json(name = "wind_direction_10m") val windDirection10m: Double?,
    @Json(name = "cloud_cover") val cloudCover: Int?,
    @Json(name = "uv_index") val uvIndex: Double?
)

@JsonClass(generateAdapter = true)
data class NwpHourlyDto(
    @Json(name = "time") val time: List<String>?,
    @Json(name = "temperature_2m") val temperature2m: List<Double>?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Double>?,
    @Json(name = "dew_point_2m") val dewPoint2m: List<Double>?,
    @Json(name = "apparent_temperature") val apparentTemperature: List<Double>?,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>?,
    @Json(name = "precipitation") val precipitation: List<Double>?,
    @Json(name = "rain") val rain: List<Double>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
    @Json(name = "surface_pressure") val surfacePressure: List<Double>?,
    @Json(name = "cloud_cover") val cloudCover: List<Int>?,
    @Json(name = "visibility") val visibility: List<Double>?,
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double>?,
    @Json(name = "wind_direction_10m") val windDirection10m: List<Double>?,
    @Json(name = "uv_index") val uvIndex: List<Double>?
)

@JsonClass(generateAdapter = true)
data class NwpDailyDto(
    @Json(name = "time") val time: List<String>?,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>?,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>?,
    @Json(name = "apparent_temperature_max") val apparentTemperatureMax: List<Double>?,
    @Json(name = "apparent_temperature_min") val apparentTemperatureMin: List<Double>?,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>?,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
    @Json(name = "sunrise") val sunrise: List<String>?,
    @Json(name = "sunset") val sunset: List<String>?,
    @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double>?,
    @Json(name = "wind_direction_10m_dominant") val windDirection10mDominant: List<Int>?
)

/**
 * Normalized Meteorological Data Model with strict validation guarantees.
 */
data class NormalizedWeatherPoint(
    val timestampIso: String,
    val epochMillis: Long,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val dewPointC: Double,
    val humidityPercent: Int,
    val precipitationProbabilityPercent: Int,
    val precipitationMm: Double,
    val weatherCode: Int,
    val conditionDescription: String,
    val surfacePressureHpa: Double,
    val cloudCoverPercent: Int,
    val visibilityKm: Double,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val uvIndex: Double,
    val isNormalized: Boolean = true
)

object MeteorologicalNormalizer {

    fun normalizeTemperature(raw: Double?): Double {
        if (raw == null || raw.isNaN() || raw.isInfinite()) return 28.0
        // Physical bounds for Indian subcontinent: -30°C to +60°C
        return raw.coerceIn(-30.0, 60.0)
    }

    fun normalizeHumidity(raw: Double?): Int {
        if (raw == null || raw.isNaN() || raw.isInfinite()) return 60
        return raw.toInt().coerceIn(0, 100)
    }

    fun normalizeProbability(raw: Int?): Int {
        if (raw == null) return 0
        return raw.coerceIn(0, 100)
    }

    fun normalizePrecipitation(raw: Double?): Double {
        if (raw == null || raw.isNaN() || raw.isInfinite() || raw < 0.0) return 0.0
        // Sane maximum for 1-hour cloudburst: 250mm
        return raw.coerceIn(0.0, 250.0)
    }

    fun normalizeWindSpeed(rawKmh: Double?): Double {
        if (rawKmh == null || rawKmh.isNaN() || rawKmh.isInfinite() || rawKmh < 0.0) return 10.0
        // Max cyclone wind limit: 300 km/h
        return rawKmh.coerceIn(0.0, 300.0)
    }

    fun normalizeWindDirection(rawDeg: Double?): Int {
        if (rawDeg == null || rawDeg.isNaN()) return 0
        val deg = rawDeg.toInt() % 360
        return if (deg < 0) deg + 360 else deg
    }

    fun normalizePressure(rawHpa: Double?): Double {
        if (rawHpa == null || rawHpa.isNaN() || rawHpa.isInfinite()) return 1010.0
        // Sane atmospheric surface pressure: 870 hPa (severe cyclone) to 1085 hPa
        return rawHpa.coerceIn(870.0, 1085.0)
    }

    fun normalizeVisibility(rawMeters: Double?): Double {
        if (rawMeters == null || rawMeters.isNaN() || rawMeters.isInfinite() || rawMeters < 0.0) return 10.0
        // Convert meters to kilometers, cap at 50km
        val km = rawMeters / 1000.0
        return km.coerceIn(0.0, 50.0)
    }

    fun normalizeCloudCover(raw: Int?): Int {
        if (raw == null) return 20
        return raw.coerceIn(0, 100)
    }

    fun parseWeatherCodeDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy / Mist"
            51, 53, 55 -> "Light Drizzle"
            61, 63 -> "Moderate Rain"
            65 -> "Heavy Downpour"
            80, 81, 82 -> "Rain Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Severe Thunderstorm with Hail"
            else -> "Fair Conditions"
        }
    }
}

interface MeteorologicalApiService {

    /**
     * Fetches primary NWP forecast (ECMWF / Best-Match downscaled grid).
     */
    @GET("v1/forecast")
    suspend fun getNwpForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,cloud_cover,uv_index",
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,dew_point_2m,apparent_temperature,precipitation_probability,precipitation,rain,weather_code,surface_pressure,cloud_cover,visibility,wind_speed_10m,wind_direction_10m,uv_index",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,precipitation_sum,precipitation_probability_max,weather_code,sunrise,sunset,wind_speed_10m_max,wind_direction_10m_dominant",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 3
    ): NwpForecastDto

    /**
     * Fetches secondary NOAA-GFS forecast for multi-model consensus & spread analysis.
     */
    @GET("v1/gfs")
    suspend fun getGfsForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,cloud_cover",
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,weather_code",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 3
    ): NwpForecastDto
}

object MeteorologicalNetworkClient {
    private const val BASE_URL = "https://api.open-meteo.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val api: MeteorologicalApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(MeteorologicalApiService::class.java)
    }
}
