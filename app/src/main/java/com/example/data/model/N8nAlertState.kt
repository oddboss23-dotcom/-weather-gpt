package com.example.data.model

/**
 * State representation for n8n emergency alert webhook operations.
 */
sealed interface N8nAlertState {
    /**
     * Initial/idle state before an alert dispatch is triggered.
     */
    object Idle : N8nAlertState

    /**
     * Dispatch in progress state.
     */
    data class Loading(
        val message: String = "Sending alert to n8n emergency automation workflow...",
        val alertId: String? = null
    ) : N8nAlertState

    /**
     * Successfully received and processed by n8n workflow (HTTP 200/201/202).
     */
    data class Success(
        val alertId: String,
        val statusCode: Int,
        val responseBody: String,
        val userMessage: String = "✓ Alert accepted by n8n emergency automation workflow."
    ) : N8nAlertState

    /**
     * Error state with distinction for HTTP 404 (workflow inactive/missing),
     * timeouts, server errors, and network issues.
     */
    data class Error(
        val alertId: String? = null,
        val statusCode: Int? = null,
        val is404: Boolean = false,
        val userMessage: String,
        val technicalMessage: String
    ) : N8nAlertState
}
