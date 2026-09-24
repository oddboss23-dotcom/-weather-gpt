package com.example.data.repository

import com.example.data.model.AlertSeverity
import com.example.data.model.DisasterAlert
import com.example.data.model.IndianLanguage
import com.example.data.model.N8nAlertState
import com.example.data.remote.N8nAlertApiService
import com.example.data.remote.N8nAlertRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Repository interface for n8n emergency alerts.
 */
interface IN8nAlertRepository {
    suspend fun dispatchAlert(request: N8nAlertRequest): N8nAlertState
    fun dispatchAlertFlow(request: N8nAlertRequest): Flow<N8nAlertState>
    suspend fun dispatchDisasterAlert(
        alert: DisasterAlert,
        language: IndianLanguage = IndianLanguage.ENGLISH,
        latitude: Double = 28.6139,
        longitude: Double = 77.2090
    ): N8nAlertState
    suspend fun sendTestRedAlert(): N8nAlertState
    suspend fun checkHealth(): Pair<Boolean, String>
}

/**
 * Concrete Repository implementation interacting with Retrofit [N8nAlertApiService].
 * Implements connection timeout handling, HTTP 404 detection (workflow inactive/offline),
 * safe response parsing, and state emission (Loading, Success, Error).
 */
class N8nAlertRepository(
    private val apiService: N8nAlertApiService = N8nAlertApiService.create()
) : IN8nAlertRepository {

    override suspend fun dispatchAlert(request: N8nAlertRequest): N8nAlertState = withContext(Dispatchers.IO) {
        try {
            val response = apiService.postAlert(request)
            val statusCode = response.code()
            val rawBody = response.body()?.string() ?: ""
            val errorBody = response.errorBody()?.string() ?: ""

            when (statusCode) {
                200, 201, 202 -> {
                    N8nAlertState.Success(
                        alertId = request.alertId,
                        statusCode = statusCode,
                        responseBody = if (rawBody.isNotBlank()) rawBody else "Alert processed successfully by n8n workflow.",
                        userMessage = "✓ Alert accepted by n8n emergency automation workflow (HTTP $statusCode)."
                    )
                }
                404 -> {
                    N8nAlertState.Error(
                        alertId = request.alertId,
                        statusCode = 404,
                        is404 = true,
                        userMessage = "n8n webhook is unavailable. Make sure the workflow is ACTIVE and the production webhook URL (${N8nAlertApiService.WEBHOOK_ENDPOINT}) is being used.",
                        technicalMessage = "HTTP 404 Not Found: $errorBody"
                    )
                }
                400 -> {
                    N8nAlertState.Error(
                        alertId = request.alertId,
                        statusCode = 400,
                        is404 = false,
                        userMessage = "Invalid alert payload format. Please verify request parameters.",
                        technicalMessage = "HTTP 400 Bad Request: $errorBody"
                    )
                }
                408 -> {
                    N8nAlertState.Error(
                        alertId = request.alertId,
                        statusCode = 408,
                        is404 = false,
                        userMessage = "n8n connection timed out. Please check network and retry.",
                        technicalMessage = "HTTP 408 Request Timeout"
                    )
                }
                500, 502, 503, 504 -> {
                    N8nAlertState.Error(
                        alertId = request.alertId,
                        statusCode = statusCode,
                        is404 = false,
                        userMessage = "Automation service temporarily unavailable (HTTP $statusCode).",
                        technicalMessage = "HTTP $statusCode Server Error: $errorBody"
                    )
                }
                else -> {
                    N8nAlertState.Error(
                        alertId = request.alertId,
                        statusCode = statusCode,
                        is404 = false,
                        userMessage = "Automation responded with HTTP $statusCode.",
                        technicalMessage = "HTTP $statusCode: $errorBody"
                    )
                }
            }
        } catch (e: SocketTimeoutException) {
            N8nAlertState.Error(
                alertId = request.alertId,
                statusCode = 408,
                is404 = false,
                userMessage = "n8n connection timed out. Please check network and retry.",
                technicalMessage = "SocketTimeoutException: ${e.message}"
            )
        } catch (e: UnknownHostException) {
            N8nAlertState.Error(
                alertId = request.alertId,
                statusCode = null,
                is404 = false,
                userMessage = "Unable to reach n8n server. Please verify your internet connection.",
                technicalMessage = "UnknownHostException: ${e.message}"
            )
        } catch (e: ConnectException) {
            N8nAlertState.Error(
                alertId = request.alertId,
                statusCode = null,
                is404 = false,
                userMessage = "Failed to connect to n8n server. Please verify server host and connection.",
                technicalMessage = "ConnectException: ${e.message}"
            )
        } catch (e: HttpException) {
            val code = e.code()
            if (code == 404) {
                N8nAlertState.Error(
                    alertId = request.alertId,
                    statusCode = 404,
                    is404 = true,
                    userMessage = "n8n webhook is unavailable. Make sure the workflow is ACTIVE and the production webhook URL is being used.",
                    technicalMessage = "HttpException 404: ${e.message()}"
                )
            } else {
                N8nAlertState.Error(
                    alertId = request.alertId,
                    statusCode = code,
                    is404 = false,
                    userMessage = "Automation service error (HTTP $code).",
                    technicalMessage = "HttpException: ${e.message()}"
                )
            }
        } catch (e: IOException) {
            N8nAlertState.Error(
                alertId = request.alertId,
                statusCode = null,
                is404 = false,
                userMessage = "Network communication failure while contacting n8n.",
                technicalMessage = "IOException: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        } catch (e: Exception) {
            N8nAlertState.Error(
                alertId = request.alertId,
                statusCode = null,
                is404 = false,
                userMessage = "Failed to dispatch alert to n8n: ${e.localizedMessage ?: "Unknown error"}",
                technicalMessage = "Exception: ${e.localizedMessage ?: e.javaClass.simpleName}"
            )
        }
    }

    override fun dispatchAlertFlow(request: N8nAlertRequest): Flow<N8nAlertState> = flow {
        emit(N8nAlertState.Loading("Sending alert to n8n emergency automation workflow...", request.alertId))
        val result = dispatchAlert(request)
        emit(result)
    }.flowOn(Dispatchers.IO)

    override suspend fun dispatchDisasterAlert(
        alert: DisasterAlert,
        language: IndianLanguage,
        latitude: Double,
        longitude: Double
    ): N8nAlertState {
        val request = mapDisasterAlertToRequest(alert, language, latitude, longitude)
        return dispatchAlert(request)
    }

    override suspend fun sendTestRedAlert(): N8nAlertState {
        val testRequest = buildTestRedAlertRequest()
        return dispatchAlert(testRequest)
    }

    override suspend fun checkHealth(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val testRequest = buildTestRedAlertRequest()
            val state = dispatchAlert(testRequest)
            when (state) {
                is N8nAlertState.Success -> {
                    Pair(true, "🟢 n8n Automation Connected (HTTP ${state.statusCode})")
                }
                is N8nAlertState.Error -> {
                    if (state.is404) {
                        Pair(false, "🔴 Automation Offline (Workflow Inactive - 404)")
                    } else {
                        Pair(false, "🟠 Automation Offline (${state.userMessage})")
                    }
                }
                else -> {
                    Pair(false, "🟠 Automation Offline")
                }
            }
        } catch (e: Exception) {
            Pair(false, "🟠 Automation Offline")
        }
    }

    companion object {
        fun getIsoTimestamp(millis: Long): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date(millis))
        }

        fun buildTestRedAlertRequest(): N8nAlertRequest {
            return N8nAlertRequest(
                alertId = "TEST-RED-001",
                source = "IMD",
                eventType = "CYCLONE",
                severity = "RED",
                title = "Severe Cyclone Warning",
                description = "A severe cyclonic storm is approaching the coast with wind gusts exceeding 110 km/h.",
                issuedAt = getIsoTimestamp(System.currentTimeMillis()),
                expiresAt = getIsoTimestamp(System.currentTimeMillis() + 8 * 3600 * 1000L),
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

        fun mapDisasterAlertToRequest(
            alert: DisasterAlert,
            language: IndianLanguage = IndianLanguage.ENGLISH,
            lat: Double = 28.6139,
            lon: Double = 77.2090
        ): N8nAlertRequest {
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

            return N8nAlertRequest(
                alertId = alert.id,
                source = alert.verifiedSource.ifBlank { "IMD" },
                severity = severityStr,
                eventType = alert.hazardType,
                title = alert.title,
                description = alert.expectedImpact,
                issuedAt = getIsoTimestamp(System.currentTimeMillis()),
                expiresAt = getIsoTimestamp(System.currentTimeMillis() + 12 * 3600 * 1000L),
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
    }
}
