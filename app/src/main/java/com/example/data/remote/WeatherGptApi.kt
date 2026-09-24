package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class N8nLocationDto(
    @Json(name = "city") val city: String,
    @Json(name = "latitude") val latitude: Double,
    @Json(name = "longitude") val longitude: Double
)

@JsonClass(generateAdapter = true)
data class N8nChatRequest(
    @Json(name = "message") val message: String,
    @Json(name = "language") val language: String,
    @Json(name = "location") val location: N8nLocationDto,
    @Json(name = "user_type") val userType: String = "general_public",
    @Json(name = "conversation_id") val conversationId: String
)

@JsonClass(generateAdapter = true)
data class N8nChatResponse(
    @Json(name = "success") val success: Boolean? = true,
    @Json(name = "answer") val answer: String? = null,
    @Json(name = "text") val text: String? = null,
    @Json(name = "response") val response: String? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "language") val language: String? = null,
    @Json(name = "source") val source: String? = null,
    @Json(name = "conversation_id") val conversationId: String? = null,
    @Json(name = "error") val error: String? = null
) {
    fun getResolvedAnswer(): String? {
        return answer ?: text ?: response ?: message
    }
}

@JsonClass(generateAdapter = true)
data class N8nTestAlertRequest(
    @Json(name = "alert_id") val alertId: String,
    @Json(name = "source") val source: String = "IMD",
    @Json(name = "event_type") val eventType: String,
    @Json(name = "severity") val severity: String,
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String,
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

interface WeatherGptApi {
    @POST("webhook/weathergpt/chat")
    suspend fun sendChatMessage(
        @Body request: N8nChatRequest
    ): Response<N8nChatResponse>

    @POST("webhook/weathergpt/alerts")
    suspend fun sendAlert(
        @Body request: N8nTestAlertRequest
    ): Response<okhttp3.ResponseBody>
}

object WeatherGptNetwork {
    const val N8N_BASE_URL = "https://oddcsk.app.n8n.cloud/"
    const val N8N_CHAT_ENDPOINT = "https://oddcsk.app.n8n.cloud/webhook/weathergpt/chat"
    const val N8N_ALERTS_ENDPOINT = "https://oddcsk.app.n8n.cloud/webhook/weathergpt/alerts"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    val api: WeatherGptApi by lazy {
        Retrofit.Builder()
            .baseUrl(N8N_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(WeatherGptApi::class.java)
    }

    val rawHttpClient: OkHttpClient
        get() = okHttpClient
}
