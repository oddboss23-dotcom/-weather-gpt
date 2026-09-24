package com.example.data.remote

import com.example.data.model.AlertSeverity
import com.example.data.model.DisasterAlert
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * High-performance, lifecycle-aware WebSocket Manager for Disaster Alerts and Live Telemetry.
 * Ensures:
 * 1. Strictly SINGLE active connection (no duplicate websockets).
 * 2. Exponential backoff reconnection strategy on failure (2s -> 4s -> 8s ... max 30s).
 * 3. Lazy on-demand connection.
 * 4. Clean disconnect and resource disposal.
 */
object WebSocketManager {

    private val isConnecting = AtomicBoolean(false)
    private val isConnected = AtomicBoolean(false)

    private val _connectionStatus = MutableStateFlow("DISCONNECTED")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _liveAlertStream = MutableStateFlow<List<DisasterAlert>>(emptyList())
    val liveAlertStream: StateFlow<List<DisasterAlert>> = _liveAlertStream.asStateFlow()

    private var activeWebSocket: WebSocket? = null
    private var reconnectJob: Job? = null
    private var connectionScope = CoroutineScope(Dispatchers.IO + Job())
    private var backoffDelayMs = 2000L
    private const val MAX_BACKOFF_MS = 30000L

    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .pingInterval(25, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    /**
     * Connect only when required. Prevents multiple simultaneous connections.
     */
    @Synchronized
    fun connect(url: String = "wss://echo.websocket.org", cityName: String = "Delhi") {
        if (isConnected.get() || isConnecting.get()) {
            return // Already active or in progress
        }

        isConnecting.set(true)
        _connectionStatus.value = "CONNECTING"
        reconnectJob?.cancel()

        connectionScope.launch {
            try {
                val request = Request.Builder()
                    .url(url)
                    .build()

                activeWebSocket = client.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        isConnecting.set(false)
                        isConnected.set(true)
                        backoffDelayMs = 2000L // reset backoff
                        _connectionStatus.value = "CONNECTED"
                        webSocket.send("{\"action\":\"subscribe\",\"city\":\"$cityName\",\"channel\":\"imd_disaster_alerts\"}")
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        // Live feed incoming
                    }

                    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                        isConnected.set(false)
                        _connectionStatus.value = "CLOSING"
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        isConnected.set(false)
                        isConnecting.set(false)
                        _connectionStatus.value = "DISCONNECTED"
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        isConnected.set(false)
                        isConnecting.set(false)
                        _connectionStatus.value = "FAILED"
                        scheduleReconnect(url, cityName)
                    }
                })
            } catch (e: Exception) {
                isConnecting.set(false)
                isConnected.set(false)
                _connectionStatus.value = "ERROR"
                scheduleReconnect(url, cityName)
            }
        }
    }

    private fun scheduleReconnect(url: String, cityName: String) {
        reconnectJob?.cancel()
        reconnectJob = connectionScope.launch {
            delay(backoffDelayMs)
            if (isActive && !isConnected.get() && !isConnecting.get()) {
                backoffDelayMs = (backoffDelayMs * 2).coerceAtMost(MAX_BACKOFF_MS)
                connect(url, cityName)
            }
        }
    }

    @Synchronized
    fun disconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
        try {
            activeWebSocket?.close(1000, "Client disposed")
            activeWebSocket?.cancel()
        } catch (_: Exception) {}
        activeWebSocket = null
        isConnected.set(false)
        isConnecting.set(false)
        _connectionStatus.value = "DISCONNECTED"
    }
}
