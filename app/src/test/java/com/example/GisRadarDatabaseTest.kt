package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.GisRadarDao
import com.example.data.local.GisRadarPointEntity
import com.example.data.local.WeatherGPTDatabase
import com.example.data.repository.GisRadarRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GisRadarDatabaseTest {

    private lateinit var db: WeatherGPTDatabase
    private lateinit var dao: GisRadarDao
    private lateinit var repository: GisRadarRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WeatherGPTDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.gisRadarDao()
        repository = GisRadarRepository(dao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndQueryRadarPointInBoundingBox() = runBlocking {
        val point = GisRadarPointEntity(
            id = "DELHI_PVI_001",
            stationId = "DWR-DELHI",
            stationName = "Delhi Palam DWR",
            latitude = 28.5684,
            longitude = 77.0967,
            altitudeMeters = 215.0,
            reflectivityDbz = 48.5,
            rainfallRateMmH = 22.0,
            radialVelocityMps = 12.0,
            echoTopKm = 9.5,
            layerType = "RADAR",
            azimuthDeg = 240.0,
            rangeKm = 45.0,
            severity = "SEVERE",
            precipitationType = "THUNDERSTORM"
        )

        dao.insertRadarPoint(point)

        // Query within bounding box covering Delhi NCR
        val points = dao.getPointsInBoundingBox(28.0, 29.0, 76.5, 77.5).first()
        assertEquals(1, points.size)
        assertEquals("DELHI_PVI_001", points[0].id)
        assertEquals(48.5, points[0].reflectivityDbz, 0.01)
        assertEquals("DWR-DELHI", points[0].stationId)

        // Query outside bounding box (e.g. Southern India)
        val southPoints = dao.getPointsInBoundingBox(12.0, 14.0, 79.0, 81.0).first()
        assertTrue(southPoints.isEmpty())
    }

    @Test
    fun querySevereEchoes() = runBlocking {
        val lightPoint = GisRadarPointEntity(
            id = "PT_LIGHT",
            stationId = "DWR-DELHI",
            stationName = "Delhi Palam DWR",
            latitude = 28.5,
            longitude = 77.0,
            reflectivityDbz = 20.0,
            layerType = "RADAR"
        )
        val severePoint = GisRadarPointEntity(
            id = "PT_SEVERE",
            stationId = "DWR-DELHI",
            stationName = "Delhi Palam DWR",
            latitude = 28.6,
            longitude = 77.1,
            reflectivityDbz = 52.0,
            layerType = "RADAR"
        )

        dao.insertRadarPoints(listOf(lightPoint, severePoint))

        val severeEchoes = dao.getSevereEchoPoints(minDbz = 40.0).first()
        assertEquals(1, severeEchoes.size)
        assertEquals("PT_SEVERE", severeEchoes[0].id)
    }

    @Test
    fun seedDefaultOfflineRadarGrid() = runBlocking {
        assertEquals(0, repository.getCachedPointCount())

        repository.seedDefaultOfflineRadarGridIfEmpty()

        val count = repository.getCachedPointCount()
        assertTrue("Expected seeded radar points > 0, got $count", count > 0)

        val stations = repository.getDistinctStationIds().first()
        assertTrue(stations.contains("DWR-DELHI"))
        assertTrue(stations.contains("DWR-MUMBAI"))
        assertTrue(stations.contains("DWR-KOLKATA"))
        assertTrue(stations.contains("DWR-CHENNAI"))
    }
}
