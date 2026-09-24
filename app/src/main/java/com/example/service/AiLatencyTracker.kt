package com.example.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.ceil
import kotlin.math.roundToInt

data class AiLatencyRecord(
    val id: String,
    val timestampMs: Long,
    val retrievalMs: Long,
    val aiProcessingMs: Long,
    val totalMs: Long
)

data class AiLatencyStats(
    val sampleSize: Int,
    val averageSec: Double,
    val medianSec: Double,
    val p95Sec: Double,
    val lastRetrievalMs: Long,
    val lastAiProcessingMs: Long,
    val lastTotalSec: Double
)

/**
 * High-precision Internal Timing Instrumentation for AI Response Pipeline.
 * Accurately measures:
 * - Request Start
 * - Data Retrieval
 * - AI Processing
 * - Response Received
 * - Total Latency
 *
 * Real empirical statistics (Average, Median, P95) calculated strictly from
 * observed operational samples.
 */
object AiLatencyTracker {

    private val samples = CopyOnWriteArrayList<AiLatencyRecord>()

    private val _latencyStats = MutableStateFlow<AiLatencyStats?>(null)
    val latencyStats: StateFlow<AiLatencyStats?> = _latencyStats.asStateFlow()

    fun recordSample(
        id: String,
        retrievalMs: Long,
        aiProcessingMs: Long,
        totalMs: Long
    ) {
        val record = AiLatencyRecord(
            id = id,
            timestampMs = System.currentTimeMillis(),
            retrievalMs = retrievalMs,
            aiProcessingMs = aiProcessingMs,
            totalMs = totalMs
        )
        samples.add(record)
        recomputeStats(record)
    }

    private fun recomputeStats(lastRecord: AiLatencyRecord) {
        val size = samples.size
        if (size == 0) {
            _latencyStats.value = null
            return
        }

        val totalMsList = samples.map { it.totalMs.toDouble() }.sorted()
        val avgSec = (totalMsList.sum() / size / 1000.0 * 100.0).roundToInt() / 100.0

        val medianSec = if (size % 2 == 1) {
            (totalMsList[size / 2] / 1000.0 * 100.0).roundToInt() / 100.0
        } else {
            val mid = size / 2
            ((totalMsList[mid - 1] + totalMsList[mid]) / 2.0 / 1000.0 * 100.0).roundToInt() / 100.0
        }

        // P95 calculation
        val p95Index = (ceil(0.95 * size).toInt() - 1).coerceIn(0, size - 1)
        val p95Sec = (totalMsList[p95Index] / 1000.0 * 100.0).roundToInt() / 100.0

        val lastSec = (lastRecord.totalMs / 1000.0 * 100.0).roundToInt() / 100.0

        _latencyStats.value = AiLatencyStats(
            sampleSize = size,
            averageSec = avgSec,
            medianSec = medianSec,
            p95Sec = p95Sec,
            lastRetrievalMs = lastRecord.retrievalMs,
            lastAiProcessingMs = lastRecord.aiProcessingMs,
            lastTotalSec = lastSec
        )
    }

    fun clearSamples() {
        samples.clear()
        _latencyStats.value = null
    }
}
