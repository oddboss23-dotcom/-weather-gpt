package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.canonical.CanonicalLocationResolver
import com.example.data.hyperlocal.TerrainType
import com.example.data.model.CityLocation
import com.example.service.DistrictBlockResolution
import com.example.service.HyperlocalWeatherContext
import com.example.service.LocationManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LocationManagerHyperlocalTest {

    @Test
    fun testDistrictBlockResolutionFormatting() {
        val resolution = DistrictBlockResolution(
            state = "Uttar Pradesh",
            district = "Varanasi",
            block = "Harhua",
            subDistrictOrTehsil = "Pindra",
            gramPanchayat = "Harhua Gram Panchayat",
            villageOrLocality = "Rampur Harhua",
            postalCode = "221105",
            country = "India",
            resolutionSource = "Gazetteer Proximity Engine"
        )

        assertEquals("Harhua Block, Varanasi District", resolution.districtAndBlockDisplay)
        assertTrue(resolution.fullAdministrativeHierarchy.contains("Harhua"))
        assertTrue(resolution.fullAdministrativeHierarchy.contains("Varanasi"))
        assertTrue(resolution.fullAdministrativeHierarchy.contains("Uttar Pradesh"))
    }

    @Test
    fun testHyperlocalWeatherContextPromptGeneration() {
        val context = HyperlocalWeatherContext(
            latitude = 25.4215,
            longitude = 82.9124,
            altitudeMeters = 81.0,
            accuracyMeters = 5.0f,
            state = "Uttar Pradesh",
            district = "Varanasi",
            block = "Harhua",
            locality = "Rampur Harhua",
            postalCode = "221105",
            terrainType = TerrainType.RIVER_BASIN_LOWLAND,
            nearestImdStation = "IMD-BABATPUR-AIRPORT-AWS",
            radarStationCode = "DWR-LUCKNOW",
            isDirectObservationStation = false,
            resolutionDescription = "Hyperlocal GPS Resolution: Harhua Block, Varanasi District"
        )

        val prompt = context.toHyperlocalPromptContext()
        assertTrue(prompt.contains("25.4215°N"))
        assertTrue(prompt.contains("82.9124°E"))
        assertTrue(prompt.contains("81 m MSL"))
        assertTrue(prompt.contains("Harhua Block, Varanasi District"))
        assertTrue(prompt.contains("River Basin"))
        assertTrue(prompt.contains("DWR-LUCKNOW"))
    }

    @Test
    fun testCanonicalResolutionForKnownLocations() {
        // Test Patna (Ganga River basin)
        val patna = CanonicalLocationResolver.resolve(
            CityLocation(name = "Patna", state = "Bihar", latitude = 25.5941, longitude = 85.1376)
        )
        assertEquals("Bihar", patna.state)
        assertEquals("Ganga", patna.nearestRiver)

        // Test Shimla (High altitude)
        val shimla = CanonicalLocationResolver.resolve(
            CityLocation(name = "Shimla", state = "Himachal Pradesh", latitude = 31.1048, longitude = 77.1734)
        )
        assertEquals("Himachal Pradesh", shimla.state)
        assertTrue((shimla.elevation ?: 0.0) > 1500.0)

        // Test Mumbai (Coastal)
        val mumbai = CanonicalLocationResolver.resolve(
            CityLocation(name = "Mumbai", state = "Maharashtra", latitude = 18.9220, longitude = 72.8347)
        )
        assertEquals("Maharashtra", mumbai.state)
        assertTrue(mumbai.coastalDistanceKm < 20.0)
    }

    @Test
    fun testLocationManagerHyperlocalWeatherContextResolution() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val locationManager = LocationManager(app)

        // Test resolving context for Delhi coordinates
        val delhiContext = locationManager.resolveHyperlocalWeatherContext(
            latitude = 28.6139,
            longitude = 77.2090,
            accuracyMeters = 8.5f
        )

        assertEquals("DWR-PALAM", delhiContext.radarStationCode)
        assertTrue(delhiContext.latitude in 28.6..28.62)
        assertNotNull(delhiContext.district)
        assertNotNull(delhiContext.block)
        assertTrue(delhiContext.resolutionDescription.isNotBlank())
    }
}
