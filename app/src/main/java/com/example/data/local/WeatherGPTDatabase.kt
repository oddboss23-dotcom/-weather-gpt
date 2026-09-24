package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "weather_cache")
data class WeatherCacheEntity(
    @PrimaryKey val locationKey: String,
    val cityName: String,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val tempMinC: Double,
    val tempMaxC: Double,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: String,
    val pressure: Double,
    val uvIndex: Double,
    val rainProb: Int,
    val expectedRainfall: Double,
    val condition: String,
    val weatherCode: Int,
    val timestamp: Long
)

@Entity(tableName = "chat_history")
data class ChatHistoryEntity(
    @PrimaryKey val id: String,
    val queryText: String,
    val answerText: String,
    val isUser: Boolean,
    val timestamp: Long,
    val personaMode: String,
    val languageCode: String
)

@Entity(tableName = "rural_offline_cache")
data class RuralOfflineCacheEntity(
    @PrimaryKey val locationKey: String,
    val cityName: String,
    val forecast48hSummary: String,
    val disasterAlertsJson: String,
    val krishiAdvisoryJson: String,
    val localLanguageAdvice: String,
    val savedHierarchyPath: String,
    val isEstimatedFromGrid: Boolean,
    val nearbyStationName: String,
    val stationDistanceKm: Double,
    val cacheTimestamp: Long
)

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheWeather(cache: WeatherCacheEntity)

    @Query("SELECT * FROM weather_cache WHERE locationKey = :key LIMIT 1")
    suspend fun getCachedWeather(key: String): WeatherCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheRuralOfflineData(cache: RuralOfflineCacheEntity)

    @Query("SELECT * FROM rural_offline_cache WHERE locationKey = :key LIMIT 1")
    suspend fun getRuralOfflineData(key: String): RuralOfflineCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatHistoryEntity)

    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllChatHistory(): Flow<List<ChatHistoryEntity>>

    @Query("DELETE FROM chat_history")
    suspend fun clearChatHistory()
}

@Database(
    entities = [
        WeatherCacheEntity::class,
        ChatHistoryEntity::class,
        GisRadarPointEntity::class,
        RuralOfflineCacheEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class WeatherGPTDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun gisRadarDao(): GisRadarDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherGPTDatabase? = null

        fun getDatabase(context: Context): WeatherGPTDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WeatherGPTDatabase::class.java,
                    "weathergpt_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
