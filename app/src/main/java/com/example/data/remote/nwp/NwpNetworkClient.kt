package com.example.data.remote.nwp

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Network Client Provider for Open-Meteo and NWP Services.
 *
 * Configured with:
 * - Moshi JSON serialization engine with Kotlin reflection fallback
 * - Resilient OkHttp connection pooling and timeout margins
 * - Logging interceptor for telemetry and diagnostics
 */
object NwpNetworkClient {

    private const val BASE_URL = "https://api.open-meteo.com/"

    val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    val apiService: OpenMeteoNwpApiService by lazy {
        retrofit.create(OpenMeteoNwpApiService::class.java)
    }

    /**
     * High-level convenience method: fetches best-match NWP forecast
     * and returns strictly normalized meteorological domain objects.
     */
    suspend fun fetchNormalizedForecast(
        latitude: Double,
        longitude: Double,
        forecastDays: Int = 3
    ): Triple<NormalizedCurrentWeather, List<NormalizedHourlyPoint>, List<NormalizedDailyPoint>> {
        val rawResponse = apiService.getBestMatchForecast(
            latitude = latitude,
            longitude = longitude,
            forecastDays = forecastDays
        )

        val normalizedCurrent = WeatherUnitNormalizer.normalizeCurrentWeather(rawResponse)
        val normalizedHourly = WeatherUnitNormalizer.normalizeHourlyForecast(rawResponse)
        val normalizedDaily = WeatherUnitNormalizer.normalizeDailyForecast(rawResponse)

        return Triple(normalizedCurrent, normalizedHourly, normalizedDaily)
    }
}
