package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) providing high-performance offline queries for
 * cached GIS radar data points and weather mapping coordinates.
 */
@Dao
interface GisRadarDao {

    /**
     * Efficient bounding-box query for mapping viewports.
     * Uses composite index (latitude, longitude) to return radar points within viewport bounds.
     */
    @Query("""
        SELECT * FROM gis_radar_cache 
        WHERE latitude BETWEEN :minLat AND :maxLat 
          AND longitude BETWEEN :minLon AND :maxLon
        ORDER BY timestamp DESC
    """)
    fun getPointsInBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Flow<List<GisRadarPointEntity>>

    /**
     * Bounding-box query filtered by specific GIS layer (e.g. RADAR, RAINFALL, WIND).
     */
    @Query("""
        SELECT * FROM gis_radar_cache 
        WHERE latitude BETWEEN :minLat AND :maxLat 
          AND longitude BETWEEN :minLon AND :maxLon
          AND layerType = :layerType
        ORDER BY timestamp DESC
    """)
    fun getPointsInBoundingBoxAndLayer(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        layerType: String
    ): Flow<List<GisRadarPointEntity>>

    /**
     * Synchronous bounding-box lookup for offline canvas rendering passes.
     */
    @Query("""
        SELECT * FROM gis_radar_cache 
        WHERE latitude BETWEEN :minLat AND :maxLat 
          AND longitude BETWEEN :minLon AND :maxLon
          AND layerType = :layerType
        ORDER BY timestamp DESC
    """)
    suspend fun getPointsInBoundingBoxSync(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double,
        layerType: String
    ): List<GisRadarPointEntity>

    /**
     * Query radar points around a center coordinate with a specified coordinate delta.
     */
    @Query("""
        SELECT * FROM gis_radar_cache
        WHERE latitude BETWEEN (:centerLat - :latDelta) AND (:centerLat + :latDelta)
          AND longitude BETWEEN (:centerLon - :lonDelta) AND (:centerLon + :lonDelta)
        ORDER BY reflectivityDbz DESC
        LIMIT :limit
    """)
    fun getPointsNearCoordinate(
        centerLat: Double,
        centerLon: Double,
        latDelta: Double = 1.5,
        lonDelta: Double = 1.5,
        limit: Int = 100
    ): Flow<List<GisRadarPointEntity>>

    /**
     * Retrieve all cached radar observations for a specific Doppler Weather Radar (DWR) station.
     */
    @Query("SELECT * FROM gis_radar_cache WHERE stationId = :stationId ORDER BY timestamp DESC")
    fun getPointsByStation(stationId: String): Flow<List<GisRadarPointEntity>>

    /**
     * Retrieve observations for a station filtered by layer type.
     */
    @Query("""
        SELECT * FROM gis_radar_cache 
        WHERE stationId = :stationId AND layerType = :layerType 
        ORDER BY timestamp DESC
    """)
    fun getPointsByStationAndLayer(stationId: String, layerType: String): Flow<List<GisRadarPointEntity>>

    /**
     * List all distinct radar stations currently cached locally.
     */
    @Query("SELECT DISTINCT stationId FROM gis_radar_cache ORDER BY stationId ASC")
    fun getDistinctStationIds(): Flow<List<String>>

    /**
     * Stream all cached points for a given GIS layer.
     */
    @Query("SELECT * FROM gis_radar_cache WHERE layerType = :layerType ORDER BY timestamp DESC")
    fun getAllPointsByLayer(layerType: String): Flow<List<GisRadarPointEntity>>

    /**
     * Stream severe weather echoes exceeding the specified dBZ threshold (e.g. 40 dBZ for heavy rain/hail).
     */
    @Query("SELECT * FROM gis_radar_cache WHERE reflectivityDbz >= :minDbz ORDER BY reflectivityDbz DESC")
    fun getSevereEchoPoints(minDbz: Double = 40.0): Flow<List<GisRadarPointEntity>>

    /**
     * Compute maximum reflectivity in an area (used for rapid threat assessment).
     */
    @Query("""
        SELECT MAX(reflectivityDbz) FROM gis_radar_cache 
        WHERE latitude BETWEEN :minLat AND :maxLat 
          AND longitude BETWEEN :minLon AND :maxLon
    """)
    suspend fun getMaxReflectivityInArea(
        minLat: Double,
        maxLat: Double,
        minLon: Double,
        maxLon: Double
    ): Double?

    /**
     * Look up a single radar coordinate point by its primary key ID.
     */
    @Query("SELECT * FROM gis_radar_cache WHERE id = :id LIMIT 1")
    suspend fun getPointById(id: String): GisRadarPointEntity?

    /**
     * Total number of cached radar coordinates available offline.
     */
    @Query("SELECT COUNT(*) FROM gis_radar_cache")
    suspend fun getCachedPointCount(): Int

    /**
     * Stream all cached radar points in the database.
     */
    @Query("SELECT * FROM gis_radar_cache ORDER BY timestamp DESC")
    fun getAllCachedPoints(): Flow<List<GisRadarPointEntity>>

    /**
     * Insert or update a single GIS radar point.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRadarPoint(point: GisRadarPointEntity)

    /**
     * Batch insert multiple GIS radar points (for rapid offline grid caching).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRadarPoints(points: List<GisRadarPointEntity>)

    /**
     * Evict expired radar data points older than the given timestamp.
     */
    @Query("DELETE FROM gis_radar_cache WHERE timestamp < :olderThanTimestamp")
    suspend fun pruneExpiredPoints(olderThanTimestamp: Long): Int

    /**
     * Delete cached points for a specific station.
     */
    @Query("DELETE FROM gis_radar_cache WHERE stationId = :stationId")
    suspend fun deletePointsForStation(stationId: String): Int

    /**
     * Delete cached points for a given GIS layer.
     */
    @Query("DELETE FROM gis_radar_cache WHERE layerType = :layerType")
    suspend fun deletePointsForLayer(layerType: String): Int

    /**
     * Clear all cached radar data.
     */
    @Query("DELETE FROM gis_radar_cache")
    suspend fun clearAllRadarCache(): Int
}
