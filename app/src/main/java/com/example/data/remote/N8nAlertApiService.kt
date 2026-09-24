package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * n8n Emergency Alert Webhook Request DTO.
 * Target URL: https://oddcsk.app.n8n.cloud/webhook/weathergpt/alerts
 */
@JsonClass(generateAdapter = true)
data class N8nAlertRequest(
    @Json(name = "alert_id") val alertId: String,
    @Json(name = "source") val source: String = "IMD",
    @Json(name = "event_type") val eventType: String,
    @Json(name = "severity") val severity: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
    @Json(name = "issued_at") val issuedAt: String,
    @Json(name = "expires_at") val expiresAt: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double,
    @Json(name = "affected_area") val affectedArea: String,
    @Json(name = "language") val language: String = "en",
    @Json(name = "citizen_advisory") val citizenAdvisory: String? = null,
    @Json(name = "farmer_advisory") val farmerAdvisory: String? = null,
    @Json(name = "authority_advisory") val authorityAdvisory: String? = null,
    @Json(name = "aviation_advisory") val aviationAdvisory: String? = null,
    @Json(name = "short_notification") val shortNotification: String? = null,
    @Json(name = "recommended_action") val recommendedAction: String? = null
)

/**
 * Retrofit interface for n8n emergency alerts.
 */
interface N8nAlertApiService {

    @Headers("Content-Type: application/json", "User-Agent: WeatherGPT-Android-EarlyWarning/1.0")
    @POST("webhook/weathergpt/alerts")
    suspend fun postAlert(
        @Body request: N8nAlertRequest
    ): Response<ResponseBody>

    companion object {
        const val BASE_URL = "https://oddcsk.app.n8n.cloud/"
        const val WEBHOOK_ENDPOINT = "https://oddcsk.app.n8n.cloud/webhook/weathergpt/alerts"

        /**
         * Factory function to create configured Retrofit client with timeouts and interceptors.
         */
        fun create(
            connectTimeoutSeconds: Long = 15,
            readTimeoutSeconds: Long = 20,
            writeTimeoutSeconds: Long = 20
        ): N8nAlertApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .retryOnConnectionFailure(true)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(N8nAlertApiService::class.java)
        }
    }
}
