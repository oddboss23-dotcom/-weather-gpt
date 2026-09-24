package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Database Entity representing a local GIS radar data point and weather mapping coordinate.
 * Indexed by coordinates (latitude, longitude), stationId, layerType, and reflectivity
 * for ultra-fast spatial bounding-box lookups and offline GIS rendering.
 */
@Entity(
    tableName = "gis_radar_cache",
    indices = [
        Index(value = ["latitude", "longitude"]),
        Index(value = ["stationId"]),
        Index(value = ["layerType"]),
        Index(value = ["timestamp"]),
        Index(value = ["reflectivityDbz"])
    ]
)
data class GisRadarPointEntity(
    @PrimaryKey
    val id: String,
    val stationId: String,
    val stationName: String,
    val latitude: Double,
    val longitude: Double,
    val altitudeMeters: Double = 0.0,
    val reflectivityDbz: Double,
    val rainfallRateMmH: Double = 0.0,
    val radialVelocityMps: Double = 0.0,
    val echoTopKm: Double = 0.0,
    val layerType: String, // e.g. "RADAR", "RAINFALL", "WIND", "TEMPERATURE", "ALERTS", "CYCLONE_TRACK"
    val azimuthDeg: Double = 0.0,
    val rangeKm: Double = 0.0,
    val severity: String = "MODERATE", // NONE, LIGHT, MODERATE, SEVERE, EXTREME
    val precipitationType: String = "RAIN", // CLEAR, RAIN, HEAVY_SHOWERS, THUNDERSTORM, HAIL
    val timestamp: Long = System.currentTimeMillis(),
    val validUntil: Long = System.currentTimeMillis() + (30 * 60 * 1000L), // 30 mins TTL
    val dataSource: String = "IMD Doppler Weather Radar Network"
)
