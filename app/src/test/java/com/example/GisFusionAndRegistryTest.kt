package com.example

import com.example.data.gis.IndianGeographicRegistry
import com.example.data.gis.WeatherDataFusionEngine
import com.example.data.gis.WeatherGisLayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GisFusionAndRegistryTest {

    @Test
    fun testHeatIndexCalculationRothfusz() {
        val (hiNormal, catNormal) = WeatherDataFusionEngine.calculateHeatIndexC(24.0, 50)
        assertEquals("Normal", catNormal)
        assertEquals(24.0, hiNormal, 0.1)

        val (hiDanger, catDanger) = WeatherDataFusionEngine.calculateHeatIndexC(38.0, 75)
        assertTrue("Heat index category should be Danger or Extreme Danger", catDanger == "Danger" || catDanger == "Extreme Danger")
        assertTrue("Calculated heat index should exceed 45°C", hiDanger > 45.0)
    }

    @Test
    fun testIndianGeographicRegistryCoverage() {
        val states = IndianGeographicRegistry.states
        assertTrue("Should have multiple key states defined", states.size >= 10)

        val delhi = IndianGeographicRegistry.findStateByName("Delhi")
        assertNotNull("Delhi NCT should exist", delhi)
        assertTrue("Delhi should have districts", delhi!!.districts.isNotEmpty())

        val maharashtra = IndianGeographicRegistry.findStateByName("Maharashtra")
        assertNotNull("Maharashtra should exist", maharashtra)
        assertTrue("Should contain Mumbai", maharashtra!!.districts.any { it.name.contains("Mumbai") })
    }

    @Test
    fun testMultiSourcePointFusion() {
        val fusedDelhi = WeatherDataFusionEngine.fusePointAnalysis(
            lat = 28.6139,
            lon = 77.2090,
            baseWeather = null
        )

        assertNotNull(fusedDelhi)
        assertTrue("Locality name should resolve to Delhi area", fusedDelhi.localityName.contains("Delhi"))
        assertTrue("State should resolve to Delhi NCT", fusedDelhi.stateName.contains("Delhi"))
        assertTrue("Confidence score should be realistic", fusedDelhi.confidenceScore in 70..100)
    }
}
