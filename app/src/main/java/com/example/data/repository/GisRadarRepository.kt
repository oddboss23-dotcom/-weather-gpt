package com.example.data.repository

import com.example.data.local.GisRadarDao
import com.example.data.local.GisRadarPointEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repository abstracting local GIS radar data point caching and offline mapping coordinate queries.
 * Adheres to Clean Architecture and MVVM patterns.
 */
class GisRadarRepository(
    private val gisRadarDao: GisRadarDao
) {

    /**
     * Stream radar data points located within the spatial viewport bounding box.
     */
    fun getPointsInBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        layerType: String? = null
    ): Flow<List<GisRadarPointEntity>> {
        return if (layerType.isNullOrBlank()) {
            gisRadarDao.getPointsInBoundingBox(minLat, maxLat, minLon, maxLon)
        } else {
            gisRadarDao.getPointsInBoundingBoxAndLayer(minLat, maxLat, minLon, maxLon, layerType)
        }
    }

    /**
     * Synchronously load radar points for high-performance offline canvas drawing passes.
     */
    suspend fun getPointsInBoundingBoxSync(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        layerType: String = "RADAR"
    ): List<GisRadarPointEntity> = withContext(Dispatchers.IO) {
        gisRadarDao.getPointsInBoundingBoxSync(minLat, maxLat, minLon, maxLon, layerType)
    }

    /**
     * Stream radar points around a specific city coordinate.
     */
    fun getPointsNearCoordinate(
        lat: Double,
        lon: Double,
        delta: Double = 1.5,
        limit: Int = 100
    ): Flow<List<GisRadarPointEntity>> {
        return gisRadarDao.getPointsNearCoordinate(lat, lon, delta, delta, limit)
    }

    /**
     * Stream radar points for a specific Doppler Weather Radar station ID.
     */
    fun getPointsByStation(stationId: String): Flow<List<GisRadarPointEntity>> {
        return gisRadarDao.getPointsByStation(stationId)
    }

    /**
     * Stream all severe weather radar echoes exceeding the specified reflectivity threshold.
     */
    fun getSevereWeatherEchoes(minDbz: Double = 40.0): Flow<List<GisRadarPointEntity>> {
        return gisRadarDao.getSevereEchoPoints(minDbz)
    }

    /**
     * Stream all cached radar points for a given GIS layer.
     */
    fun getAllPointsByLayer(layerType: String): Flow<List<GisRadarPointEntity>> {
        return gisRadarDao.getAllPointsByLayer(layerType)
    }

    /**
     * Stream all cached points.
     */
    fun getAllCachedPoints(): Flow<List<GisRadarPointEntity>> {
        return gisRadarDao.getAllCachedPoints()
    }

    /**
     * Get distinct station IDs.
     */
    fun getDistinctStationIds(): Flow<List<String>> {
        return gisRadarDao.getDistinctStationIds()
    }

    /**
     * Total number of cached radar data points in Room.
     */
    suspend fun getCachedPointCount(): Int = withContext(Dispatchers.IO) {
        gisRadarDao.getCachedPointCount()
    }

    /**
     * Cache a single radar data point.
     */
    suspend fun cachePoint(point: GisRadarPointEntity) = withContext(Dispatchers.IO) {
        gisRadarDao.insertRadarPoint(point)
    }

    /**
     * Batch cache multiple radar data points.
     */
    suspend fun cachePoints(points: List<GisRadarPointEntity>) = withContext(Dispatchers.IO) {
        gisRadarDao.insertRadarPoints(points)
    }

    /**
     * Delete expired points older than the specified timestamp.
     */
    suspend fun pruneExpired(olderThan: Long = System.currentTimeMillis() - (4 * 3600 * 1000L)): Int = withContext(Dispatchers.IO) {
        gisRadarDao.pruneExpiredPoints(olderThan)
    }

    /**
     * Clear all cached GIS radar points.
     */
    suspend fun clearAllCache(): Int = withContext(Dispatchers.IO) {
        gisRadarDao.clearAllRadarCache()
    }

    /**
     * Pre-populates the local database with offline Doppler Weather Radar (DWR) network points
     * across India (Delhi, Mumbai, Kolkata, Chennai, Bhubaneswar, Patna, Nagpur, Hyderabad, etc.)
     * if the database is currently empty.
     */
    suspend fun seedDefaultOfflineRadarGridIfEmpty() = withContext(Dispatchers.IO) {
        val existingCount = gisRadarDao.getCachedPointCount()
        if (existingCount > 0) return@withContext

        val now = System.currentTimeMillis()
        val ttl = now + (24 * 3600 * 1000L) // 24h validity for offline fallback

        val seedStations = listOf(
            RadarStationInfo("DWR-DELHI", "Delhi Palam DWR", 28.5684, 77.0967, 215.0),
            RadarStationInfo("DWR-MUMBAI", "Mumbai Colaba DWR", 18.9067, 72.8147, 45.0),
            RadarStationInfo("DWR-KOLKATA", "Kolkata Alipore DWR", 22.5317, 88.3308, 12.0),
            RadarStationInfo("DWR-CHENNAI", "Chennai Port DWR", 13.0827, 80.2707, 16.0),
            RadarStationInfo("DWR-BHUBANESWAR", "Bhubaneswar Coastal DWR", 20.2961, 85.8245, 45.0),
            RadarStationInfo("DWR-HYDERABAD", "Hyderabad Begumpet DWR", 17.4475, 78.4722, 535.0),
            RadarStationInfo("DWR-NAGPUR", "Nagpur Sonegaon DWR", 21.0922, 79.0608, 310.0),
            RadarStationInfo("DWR-PATNA", "Patna Airport DWR", 25.5941, 85.1376, 52.0),
            RadarStationInfo("DWR-JAIPUR", "Jaipur Sanganer DWR", 26.8289, 75.8056, 390.0),
            RadarStationInfo("DWR-KOCHI", "Kochi Naval Base DWR", 9.9312, 76.2673, 10.0)
        )

        val seedPoints = mutableListOf<GisRadarPointEntity>()

        for (st in seedStations) {
            // Station center point
            seedPoints.add(
                GisRadarPointEntity(
                    id = "${st.id}_CENTER",
                    stationId = st.id,
                    stationName = st.name,
                    latitude = st.lat,
                    longitude = st.lon,
                    altitudeMeters = st.alt,
                    reflectivityDbz = 18.0,
                    rainfallRateMmH = 0.5,
                    radialVelocityMps = 3.5,
                    echoTopKm = 4.2,
                    layerType = "RADAR",
                    azimuthDeg = 0.0,
                    rangeKm = 0.0,
                    severity = "LIGHT",
                    precipitationType = "RAIN",
                    timestamp = now,
                    validUntil = ttl,
                    dataSource = "IMD Doppler Weather Radar Network"
                )
            )

            // Outer range rings (50km, 100km, 150km, 200km)
            val ranges = listOf(40.0, 80.0, 140.0, 200.0)
            val azimuths = listOf(0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0)

            for (range in ranges) {
                for (az in azimuths) {
                    val rad = Math.toRadians(az)
                    // ~111 km per degree latitude
                    val dLat = (range * Math.cos(rad)) / 111.0
                    val dLon = (range * Math.sin(rad)) / (111.0 * Math.cos(Math.toRadians(st.lat)))

                    val ptLat = st.lat + dLat
                    val ptLon = st.lon + dLon

                    // Simulated realistic Doppler reflectivity pattern
                    val dbz = when {
                        range <= 80.0 && (az in 180.0..270.0) -> 48.0 // Convective monsoonal core
                        range <= 140.0 -> 32.0 // Moderate rainband
                        else -> 15.0 // Light drizzle / stratiform echo
                    }

                    val rainRate = when {
                        dbz >= 45.0 -> 24.5
                        dbz >= 35.0 -> 8.0
                        dbz >= 20.0 -> 2.2
                        else -> 0.4
                    }

                    val severity = when {
                        dbz >= 50.0 -> "EXTREME"
                        dbz >= 40.0 -> "SEVERE"
                        dbz >= 30.0 -> "MODERATE"
                        else -> "LIGHT"
                    }

                    val precipType = when {
                        dbz >= 50.0 -> "THUNDERSTORM"
                        dbz >= 40.0 -> "HEAVY_SHOWERS"
                        dbz >= 25.0 -> "RAIN"
                        else -> "CLEAR"
                    }

                    seedPoints.add(
                        GisRadarPointEntity(
                            id = "${st.id}_R${range.toInt()}_A${az.toInt()}",
                            stationId = st.id,
                            stationName = st.name,
                            latitude = Math.round(ptLat * 10000.0) / 10000.0,
                            longitude = Math.round(ptLon * 10000.0) / 10000.0,
                            altitudeMeters = st.alt,
                            reflectivityDbz = dbz,
                            rainfallRateMmH = rainRate,
                            radialVelocityMps = 8.5,
                            echoTopKm = if (dbz >= 40.0) 11.5 else 5.5,
                            layerType = "RADAR",
                            azimuthDeg = az,
                            rangeKm = range,
                            severity = severity,
                            precipitationType = precipType,
                            timestamp = now,
                            validUntil = ttl,
                            dataSource = "IMD Doppler Weather Radar Network"
                        )
                    )
                }
            }
        }

        gisRadarDao.insertRadarPoints(seedPoints)
    }

    private data class RadarStationInfo(
        val id: String,
        val name: String,
        val lat: Double,
        val lon: Double,
        val alt: Double
    )
}
