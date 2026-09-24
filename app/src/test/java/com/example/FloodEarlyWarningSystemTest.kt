package com.example

import com.example.data.flood.AlertPriorityManager
import com.example.data.flood.AuthoritativeFloodDataService
import com.example.data.flood.AuthoritativeHydrologyRegistry
import com.example.data.flood.FloodRiskCategory
import com.example.data.flood.GeoTargetedFloodMonitor
import com.example.data.flood.RiverObservation
import com.example.data.flood.WaterLevelTrend
import com.example.data.model.AlertSeverity
import com.example.data.model.DisasterAlert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FloodEarlyWarningSystemTest {

    @Test
    fun `authoritative hydrology registry contains valid bihar river stations`() {
        val stations = AuthoritativeHydrologyRegistry.stations
        assertTrue("Hydrology registry must contain river stations", stations.isNotEmpty())

        // Verify key Bihar flood stations exist
        val gopalganjStation = stations.find { it.district.equals("Gopalganj", ignoreCase = true) }
        assertNotNull("Gopalganj station must exist in registry", gopalganjStation)
        assertEquals("Gandak", gopalganjStation?.riverName)
        assertEquals("Dumariaghat", gopalganjStation?.stationName)
        assertTrue("Danger level must be greater than warning level", gopalganjStation!!.dangerLevelM > gopalganjStation.warningLevelM)
        assertTrue("HFL must be greater than or equal to danger level", gopalganjStation.highestFloodLevelM >= gopalganjStation.dangerLevelM)

        val patnaStation = stations.find { it.district.equals("Patna", ignoreCase = true) }
        assertNotNull("Patna station must exist in registry", patnaStation)
        assertEquals("Ganga", patnaStation?.riverName)

        val madhubaniStation = stations.find { it.district.equals("Madhubani", ignoreCase = true) }
        assertNotNull("Madhubani station must exist in registry", madhubaniStation)
        assertEquals("Kamla Balan", madhubaniStation?.riverName)

        val supaulStation = stations.find { it.district.equals("Supaul", ignoreCase = true) }
        assertNotNull("Supaul station must exist in registry", supaulStation)
        assertEquals("Kosi", supaulStation?.riverName)

        val buxarStation = stations.find { it.district.equals("Buxar", ignoreCase = true) }
        assertNotNull("Buxar station must exist in registry", buxarStation)

        val bhagalpurStation = stations.find { it.district.equals("Bhagalpur", ignoreCase = true) }
        assertNotNull("Bhagalpur station must exist in registry", bhagalpurStation)

        val westChamparanStation = stations.find { it.district.equals("West Champaran", ignoreCase = true) }
        assertNotNull("West Champaran station must exist in registry", westChamparanStation)
    }

    @Test
    fun `flood risk engine evaluates critical flood above danger level deterministically`() {
        val service = AuthoritativeFloodDataService()
        val dumariaghat = AuthoritativeHydrologyRegistry.findStationById("CWC_DUMARIAGHAT_01")
        assertNotNull(dumariaghat)

        // Dumariaghat: Warning = 62.24m, Danger = 63.24m, HFL = 64.25m
        // Simulate real observation 63.59m (> Danger Level by 0.35m) with heavy catchment rainfall
        val observation = RiverObservation(
            stationId = dumariaghat!!.stationId,
            timestamp = System.currentTimeMillis(),
            observationTimeFormatted = "12 Sep 14:30 IST",
            currentWaterLevelM = 63.59,
            warningLevelM = dumariaghat.warningLevelM,
            dangerLevelM = dumariaghat.dangerLevelM,
            highestFloodLevelM = dumariaghat.highestFloodLevelM,
            trend = WaterLevelTrend.RISING,
            rainfallIntensityMm = 75.0,
            forecastRainfallMm = 95.0,
            source = "Central Water Commission & Bihar WRD FMIS"
        )

        val assessment = service.computeRiskAssessment(dumariaghat, observation)
        assertNotNull(assessment)
        assertEquals(FloodRiskCategory.SEVERE_FLOOD_RISK, assessment.classification)
        assertEquals(AlertSeverity.RED, assessment.urgencyLevel)
        assertEquals(1, assessment.priorityRank)
        assertTrue("Distance above danger must be positive", assessment.distanceAboveDangerM > 0)
        assertEquals(0.35, assessment.distanceAboveDangerM, 0.01)
        assertEquals("Flood warning issued for your selected location.", assessment.voiceAnnouncementEnglish)
        assertEquals("आपके चुने हुए स्थान के लिए बाढ़ की चेतावनी जारी की गई है।", assessment.voiceAnnouncementHindi)
    }

    @Test
    fun `geo targeted monitor finds relevant bihar river stations for flood prone districts`() {
        val monitor = GeoTargetedFloodMonitor()

        // 1. Gopalganj (Latitude: 26.4670, Longitude: 84.4360) -> Dumariaghat
        val gopalganjStations = monitor.findRelevantStations("Gopalganj", "Gopalganj", 26.4670, 84.4360)
        assertTrue("Gopalganj must match nearby Gandak station", gopalganjStations.isNotEmpty())
        assertTrue(gopalganjStations.any { it.riverName == "Gandak" && it.district == "Gopalganj" })

        // 2. West Champaran (Latitude: 27.1500, Longitude: 84.4000) -> Valmikinagar
        val westChamparanStations = monitor.findRelevantStations("West Champaran", "West Champaran", 27.1500, 84.4000)
        assertTrue("West Champaran must match Valmikinagar station", westChamparanStations.isNotEmpty())
        assertTrue(westChamparanStations.any { it.riverName == "Gandak" })

        // 3. Patna (Latitude: 25.6139, Longitude: 85.1410) -> Digha Ghat / Gandhi Ghat
        val patnaStations = monitor.findRelevantStations("Patna", "Patna", 25.6139, 85.1410)
        assertTrue("Patna must match Ganga river station", patnaStations.isNotEmpty())
        assertTrue(patnaStations.any { it.riverName == "Ganga" })

        // 4. Madhubani (Latitude: 26.3500, Longitude: 86.0700) -> Kamla Balan Jhanjharpur
        val madhubaniStations = monitor.findRelevantStations("Madhubani", "Madhubani", 26.3500, 86.0700)
        assertTrue("Madhubani must match Kamla Balan station", madhubaniStations.isNotEmpty())
        assertTrue(madhubaniStations.any { it.riverName == "Kamla Balan" })

        // 5. Supaul (Latitude: 26.1260, Longitude: 86.6056) -> Kosi River
        val supaulStations = monitor.findRelevantStations("Supaul", "Supaul", 26.1260, 86.6056)
        assertTrue("Supaul must match Kosi station", supaulStations.isNotEmpty())
        assertTrue(supaulStations.any { it.riverName == "Kosi" })

        // 6. Buxar (Latitude: 25.5647, Longitude: 83.9777) -> Ganga River
        val buxarStations = monitor.findRelevantStations("Buxar", "Buxar", 25.5647, 83.9777)
        assertTrue("Buxar must match Ganga station", buxarStations.isNotEmpty())
        assertTrue(buxarStations.any { it.riverName == "Ganga" })

        // 7. Bhagalpur (Latitude: 25.2425, Longitude: 86.9842) -> Ganga River
        val bhagalpurStations = monitor.findRelevantStations("Bhagalpur", "Bhagalpur", 25.2425, 86.9842)
        assertTrue("Bhagalpur must match Ganga station", bhagalpurStations.isNotEmpty())
        assertTrue(bhagalpurStations.any { it.riverName == "Ganga" })

        // Non-flood location (e.g. desert far away)
        val jaisalmerStations = monitor.findRelevantStations("Jaisalmer", "Jaisalmer", 26.9157, 70.9083, radiusKm = 55.0)
        assertTrue("Jaisalmer must not match Bihar river stations", jaisalmerStations.isEmpty())
    }

    @Test
    fun `alert priority manager strictly prioritizes critical flood over ordinary alerts`() {
        val criticalFlood = DisasterAlert(
            id = "flood_crit_1",
            hazardType = "Riverine Inundation",
            severity = AlertSeverity.RED,
            location = "Gopalganj",
            title = "CRITICAL FLOOD WARNING",
            validTime = "Next 24h",
            expectedImpact = "Inundation",
            recommendedAction = "Evacuate",
            mitigationGuidance = "Move to higher ground",
            isFloodAlert = true,
            floodStatus = "ABOVE DANGER LEVEL",
            floodDistanceAboveDanger = 0.35,
            priorityRank = 1
        )

        val severeWeather = DisasterAlert(
            id = "severe_weather_1",
            hazardType = "Severe Squall",
            severity = AlertSeverity.RED,
            location = "Gopalganj",
            title = "SEVERE SQUALL WARNING",
            validTime = "Next 6h",
            expectedImpact = "High wind",
            recommendedAction = "Stay indoors",
            mitigationGuidance = "Avoid unreinforced structures",
            isFloodAlert = false,
            priorityRank = 2
        )

        val floodWarning = DisasterAlert(
            id = "flood_warn_1",
            hazardType = "River Flood Advisory",
            severity = AlertSeverity.ORANGE,
            location = "Supaul",
            title = "FLOOD WARNING ADVISORY",
            validTime = "Next 48h",
            expectedImpact = "Waterlogging",
            recommendedAction = "Move cattle",
            mitigationGuidance = "Secure pumps",
            isFloodAlert = true,
            floodStatus = "APPROACHING DANGER LEVEL",
            floodDistanceAboveDanger = -0.15,
            priorityRank = 3
        )

        val heavyRain = DisasterAlert(
            id = "heavy_rain_1",
            hazardType = "Heavy Rainfall",
            severity = AlertSeverity.ORANGE,
            location = "Patna",
            title = "HEAVY RAINFALL ADVISORY",
            validTime = "Today",
            expectedImpact = "Puddles",
            recommendedAction = "Carry umbrella",
            mitigationGuidance = "Clear drains",
            isFloodAlert = false,
            priorityRank = 4
        )

        val thunderstorm = DisasterAlert(
            id = "thunderstorm_1",
            hazardType = "Thunderstorm",
            severity = AlertSeverity.YELLOW,
            location = "Delhi",
            title = "THUNDERSTORM WATCH",
            validTime = "Evening",
            expectedImpact = "Lightning",
            recommendedAction = "Avoid open fields",
            mitigationGuidance = "Seek safe shelter",
            isFloodAlert = false,
            priorityRank = 5
        )

        val normalWeather = DisasterAlert(
            id = "normal_1",
            hazardType = "General",
            severity = AlertSeverity.GREEN,
            location = "Jaipur",
            title = "NORMAL WEATHER NOTICE",
            validTime = "All Day",
            expectedImpact = "None",
            recommendedAction = "Enjoy",
            mitigationGuidance = "Routine activity",
            isFloodAlert = false,
            priorityRank = 6
        )

        // Feed in shuffled/reversed order
        val unorderedList = listOf(thunderstorm, normalWeather, heavyRain, criticalFlood, severeWeather, floodWarning)
        val sortedList = AlertPriorityManager.sortAlertsByPriority(unorderedList)

        // Strict hierarchy check:
        // Priority 1: CRITICAL FLOOD
        // Priority 2: SEVERE WEATHER
        // Priority 3: FLOOD WARNING
        // Priority 4: HEAVY RAIN
        // Priority 5: THUNDERSTORM
        // Priority 6: NORMAL WEATHER
        assertEquals("CRITICAL FLOOD must be priority #1", "flood_crit_1", sortedList[0].id)
        assertEquals("SEVERE WEATHER must be priority #2", "severe_weather_1", sortedList[1].id)
        assertEquals("FLOOD WARNING must be priority #3", "flood_warn_1", sortedList[2].id)
        assertEquals("HEAVY RAIN must be priority #4", "heavy_rain_1", sortedList[3].id)
        assertEquals("THUNDERSTORM must be priority #5", "thunderstorm_1", sortedList[4].id)
        assertEquals("NORMAL WEATHER must be priority #6", "normal_1", sortedList[5].id)
    }
}
