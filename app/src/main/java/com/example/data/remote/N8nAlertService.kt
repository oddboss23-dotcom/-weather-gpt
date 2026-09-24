package com.example.data.remote

import com.example.data.model.AlertSeverity
import com.example.data.model.DisasterAlert
import com.example.data.model.IndianLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class N8nAlertPayload(
    val alertId: String,
    val source: String = "IMD",
    val severity: String,
    val eventType: String,
    val title: String,
    val description: String,
    val issuedAt: String = getIsoTimestamp(System.currentTimeMillis()),
    val expiresAt: String = getIsoTimestamp(System.currentTimeMillis() + 8 * 3600 * 1000L),
    val latitude: Double = 28.6139,
    val longitude: Double = 77.2090,
    val affectedArea: String,
    val language: String = "en",
    val citizenAdvisory: String? = null,
    val farmerAdvisory: String? = null,
    val authorityAdvisory: String? = null,
    val aviationAdvisory: String? = null,
    val shortNotification: String? = null,
    val recommendedAction: String? = null
) {
    fun toJsonString(): String {
        val json = JSONObject()
        json.put("alert_id", alertId)
        json.put("source", source)
        json.put("event_type", eventType)
        json.put("severity", severity)
        json.put("title", title)
        json.put("description", description)
        json.put("issued_at", issuedAt)
        json.put("expires_at", expiresAt)
        json.put("latitude", latitude)
        json.put("longitude", longitude)
        json.put("affected_area", affectedArea)
        json.put("language", language)
        citizenAdvisory?.let { json.put("citizen_advisory", it) }
        farmerAdvisory?.let { json.put("farmer_advisory", it) }
        authorityAdvisory?.let { json.put("authority_advisory", it) }
        aviationAdvisory?.let { json.put("aviation_advisory", it) }
        shortNotification?.let { json.put("short_notification", it) }
        recommendedAction?.let { json.put("recommended_action", it) }
        return json.toString()
    }

    companion object {
        fun getIsoTimestamp(millis: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date(millis))
        }
    }
}

data class N8nDispatchResult(
    val success: Boolean,
    val statusCode: Int = 0,
    val message: String,
    val userFriendlyMessage: String,
    val alertId: String
)

object N8nAlertService {
    const val WEBHOOK_URL = WeatherGptNetwork.N8N_ALERTS_ENDPOINT
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    suspend fun dispatchAlert(payload: N8nAlertPayload): N8nDispatchResult = withContext(Dispatchers.IO) {
        try {
            val jsonBody = payload.toJsonString()
            val requestBody = jsonBody.toRequestBody(JSON_MEDIA_TYPE)
            val request = Request.Builder()
                .url(WEBHOOK_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "WeatherGPT-Android-EarlyWarning/1.0")
                .build()

            WeatherGptNetwork.rawHttpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                val code = response.code
                when (code) {
                    200, 201 -> {
                        N8nDispatchResult(
                            success = true,
                            statusCode = code,
                            message = if (responseBody.isNotBlank()) responseBody else "Alert processed successfully by n8n.",
                            userFriendlyMessage = "✓ Alert accepted by n8n emergency automation workflow.",
                            alertId = payload.alertId
                        )
                    }
                    400 -> {
                        N8nDispatchResult(
                            success = false,
                            statusCode = code,
                            message = "Invalid alert payload (HTTP 400): $responseBody",
                            userFriendlyMessage = "Invalid alert payload format.",
                            alertId = payload.alertId
                        )
                    }
                    404 -> {
                        N8nDispatchResult(
                            success = false,
                            statusCode = code,
                            message = "Webhook not found or workflow inactive (HTTP 404): $responseBody",
                            userFriendlyMessage = "Automation is not active. Please activate the n8n workflow and retry.",
                            alertId = payload.alertId
                        )
                    }
                    408 -> {
                        N8nDispatchResult(
                            success = false,
                            statusCode = code,
                            message = "n8n connection timed out (HTTP 408)",
                            userFriendlyMessage = "n8n connection timed out. Please check connection and retry.",
                            alertId = payload.alertId
                        )
                    }
                    500, 502, 503, 504 -> {
                        N8nDispatchResult(
                            success = false,
                            statusCode = code,
                            message = "n8n server error (HTTP $code): $responseBody",
                            userFriendlyMessage = "Automation service temporarily unavailable (HTTP $code).",
                            alertId = payload.alertId
                        )
                    }
                    else -> {
                        N8nDispatchResult(
                            success = false,
                            statusCode = code,
                            message = "n8n returned HTTP $code: $responseBody",
                            userFriendlyMessage = "Automation responded with HTTP $code.",
                            alertId = payload.alertId
                        )
                    }
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            N8nDispatchResult(
                success = false,
                statusCode = 408,
                message = "SocketTimeoutException: ${e.message}",
                userFriendlyMessage = "n8n connection timed out. Please check connection and retry.",
                alertId = payload.alertId
            )
        } catch (e: java.net.UnknownHostException) {
            N8nDispatchResult(
                success = false,
                statusCode = -1,
                message = "UnknownHostException: ${e.message}",
                userFriendlyMessage = "Unable to reach n8n server. Please verify your internet connection.",
                alertId = payload.alertId
            )
        } catch (e: Exception) {
            N8nDispatchResult(
                success = false,
                statusCode = -1,
                message = "Exception: ${e.localizedMessage ?: e.javaClass.simpleName}",
                userFriendlyMessage = "Failed to dispatch alert to n8n: ${e.localizedMessage ?: "Network error"}",
                alertId = payload.alertId
            )
        }
    }

    suspend fun checkHealth(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val testPayload = buildTestRedAlert()
            val res = dispatchAlert(testPayload)
            if (res.success) {
                Pair(true, "🟢 n8n Automation Connected (HTTP ${res.statusCode})")
            } else if (res.statusCode == 404) {
                Pair(false, "🔴 Automation Offline (Workflow Inactive)")
            } else {
                Pair(false, "🟠 Automation Offline (${res.userFriendlyMessage})")
            }
        } catch (e: Exception) {
            Pair(false, "🟠 Automation Offline")
        }
    }

    fun buildPayloadFromAlert(
        alert: DisasterAlert,
        language: IndianLanguage = IndianLanguage.ENGLISH,
        lat: Double = 28.6139,
        lon: Double = 77.2090
    ): N8nAlertPayload {
        val langCode = when (language) {
            IndianLanguage.HINDI -> "hi"
            IndianLanguage.ODIA -> "or"
            IndianLanguage.BENGALI -> "bn"
            IndianLanguage.MARATHI -> "mr"
            IndianLanguage.GUJARATI -> "gu"
            IndianLanguage.PUNJABI -> "pa"
            IndianLanguage.TAMIL -> "ta"
            IndianLanguage.TELUGU -> "te"
            else -> "en"
        }

        val severityStr = when (alert.severity) {
            AlertSeverity.RED -> "RED"
            AlertSeverity.ORANGE -> "ORANGE"
            AlertSeverity.YELLOW -> "YELLOW"
            AlertSeverity.GREEN -> "GREEN"
        }

        val shortNote = "[🚨 ${severityStr} ALERT] ${alert.title}. ${alert.recommendedAction.take(120)}"

        val farmerAdvice = when (alert.severity) {
            AlertSeverity.RED, AlertSeverity.ORANGE -> "Protect harvested crops in covered warehouses. Clear farm drainage channels. Postpone pesticide/fertilizer spraying."
            AlertSeverity.YELLOW -> "Monitor soil moisture. Postpone light irrigation if heavy showers occur."
            AlertSeverity.GREEN -> "Normal farm operations and irrigation schedule may continue."
        }

        val authorityAdvice = when (alert.severity) {
            AlertSeverity.RED -> "Activate District Emergency Operations Center. Deploy NDRF/SDRF units to coastal/low-lying catchments. Ensure drainage pump operationality."
            AlertSeverity.ORANGE -> "Inspect flood underpasses and pump stations. Keep civic emergency teams on standby."
            AlertSeverity.YELLOW -> "Routine watch and traffic advisory for slippery roadways."
            AlertSeverity.GREEN -> "Standard baseline monitoring."
        }

        val aviationAdvice = when (alert.severity) {
            AlertSeverity.RED -> "Severe convective gusts and storm surge. Restrict coastal and low-altitude flight sorties."
            AlertSeverity.ORANGE -> "Moderate crosswind shear and thunderstorm activity in terminal control area."
            AlertSeverity.YELLOW -> "Maintain watch for isolated convective cloud cells."
            AlertSeverity.GREEN -> "VFR and IFR flight operations normal."
        }

        return N8nAlertPayload(
            alertId = alert.id,
            source = alert.verifiedSource.ifBlank { "IMD" },
            severity = severityStr,
            eventType = alert.hazardType,
            title = alert.title,
            description = alert.expectedImpact,
            issuedAt = N8nAlertPayload.getIsoTimestamp(System.currentTimeMillis()),
            expiresAt = N8nAlertPayload.getIsoTimestamp(System.currentTimeMillis() + 12 * 3600 * 1000L),
            latitude = lat,
            longitude = lon,
            affectedArea = alert.location + if (alert.affectedDistricts.isNotEmpty()) " (${alert.affectedDistricts.joinToString(", ")})" else "",
            language = langCode,
            citizenAdvisory = alert.recommendedAction,
            farmerAdvisory = farmerAdvice,
            authorityAdvisory = authorityAdvice,
            aviationAdvisory = aviationAdvice,
            shortNotification = shortNote,
            recommendedAction = alert.recommendedAction
        )
    }

    fun buildTestRedAlert(): N8nAlertPayload {
        return N8nAlertPayload(
            alertId = "TEST-RED-001",
            source = "IMD",
            eventType = "CYCLONE",
            severity = "RED",
            title = "Severe Cyclone Warning",
            description = "A severe cyclonic storm is approaching the coast with wind gusts exceeding 110 km/h.",
            issuedAt = N8nAlertPayload.getIsoTimestamp(System.currentTimeMillis()),
            expiresAt = N8nAlertPayload.getIsoTimestamp(System.currentTimeMillis() + 8 * 3600 * 1000L),
            latitude = 19.0760,
            longitude = 72.8777,
            affectedArea = "Mumbai Coastal Region",
            language = "en",
            citizenAdvisory = "Evacuate coastal sectors, stay indoors away from windows, secure loose outdoor objects.",
            farmerAdvisory = "Move livestock to elevated shelters, harvest mature crops immediately, open drainage culverts.",
            authorityAdvisory = "Deploy NDRF coastal battalions, initiate shelter relief protocols, sound warning sirens.",
            aviationAdvisory = "Airport ground stop in effect for coastal sectors; severe convective wind shear.",
            shortNotification = "[🚨 RED ALERT] Severe Cyclone Warning for Mumbai Coastal Region. Take immediate shelter.",
            recommendedAction = "Take immediate structural shelter and follow official civil defense directives."
        )
    }
}
