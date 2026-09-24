package com.example

import com.example.data.canonical.CanonicalLocationResolver
import com.example.data.canonical.CheckStatus
import com.example.data.canonical.ConfidenceEngine
import com.example.data.canonical.DataFreshnessEngine
import com.example.data.canonical.DataFreshnessStatus
import com.example.data.canonical.DataSourceType
import com.example.data.canonical.GeographyType
import com.example.data.canonical.HazardEligibilityEngine
import com.example.data.canonical.SourceIntegrityValidator
import com.example.data.canonical.TemporalConsistencyValidator
import com.example.data.canonical.WeatherGPTIntegrityCheck
import com.example.data.model.CityLocation
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Master Automated Data-Integrity Test Suite for WeatherGPT / KrishiGPT.
 * Enforces the non-negotiable architectural mandates:
 * - Deterministic Location & Snapshot identity
 * - Geospatial Hazard Isolation (Inland vs Coastal)
 * - Riverine Hydrology & CWC Benchmarks
 * - Provenance & Source Integrity (No false claims)
 * - Defensible Confidence Methodology (Zero hardcoded constants)
 */
class CanonicalDataIntegrityTest {

    @Test
    fun testDeterministicLocationIdentity() {
        val city1 = CityLocation("Delhi", "National Capital Territory", 28.6139123, 77.2090456)
        val city2 = CityLocation("Delhi", "National Capital Territory", 28.6139499, 77.2090111)

        val loc1 = CanonicalLocationResolver.resolve(city1)
        val loc2 = CanonicalLocationResolver.resolve(city2)

        // Verifies 4-decimal coordinate normalization eliminates float jitter
        assertEquals("Canonical IDs must be deterministic", loc1.id, loc2.id)
        assertTrue("Location ID must contain administrative identity", loc1.id.contains("DELHI"))
        assertEquals("Normalized latitude must match", 28.6139, loc1.latitude, 0.0001)
        assertEquals("Normalized longitude must match", 77.2090, loc1.longitude, 0.0001)
    }

    @Test
    fun testGeospatialHazardIsolation_DelhiVsMumbai() {
        val delhi = CanonicalLocationResolver.resolve(CityLocation("Delhi", "National Capital Territory", 28.6139, 77.2090))
        val mumbai = CanonicalLocationResolver.resolve(CityLocation("Mumbai", "Maharashtra", 19.0760, 72.8777))

        // Negative test: Delhi must NEVER receive marine alerts
        assertTrue("Delhi coastal distance must be inland", delhi.coastalDistanceKm > 500.0)
        assertFalse("Delhi must NOT be coastal", delhi.isCoastal)
        assertFalse("Delhi must NOT be eligible for marine hazard", HazardEligibilityEngine.isMarineHazardEligible(delhi))
        assertFalse("Delhi must NOT be eligible for storm surge", HazardEligibilityEngine.isStormSurgeEligible(delhi))

        // Positive test: Mumbai MUST be eligible for coastal hazards
        assertTrue("Mumbai must be coastal", mumbai.isCoastal)
        assertTrue("Mumbai coastal distance must be <= 50km", mumbai.coastalDistanceKm <= 50.0)
        assertTrue("Mumbai MUST be eligible for marine hazard", HazardEligibilityEngine.isMarineHazardEligible(mumbai))
        assertTrue("Mumbai MUST be eligible for storm surge", HazardEligibilityEngine.isStormSurgeEligible(mumbai))
    }

    @Test
    fun testRiverineAndFloodProneClassification_Gopalganj() {
        val gopalganj = CanonicalLocationResolver.resolve(CityLocation("Gopalganj", "Bihar", 26.4674, 84.4447))

        assertTrue("Gopalganj must be classified as RIVERINE", gopalganj.geographyTypes.contains(GeographyType.RIVERINE))
        assertTrue("Gopalganj must be classified as FLOOD_PRONE", gopalganj.geographyTypes.contains(GeographyType.FLOOD_PRONE))
        assertTrue("Gopalganj must be eligible for river flood evaluation", HazardEligibilityEngine.isRiverFloodEligible(gopalganj))
        assertNotNull("Gopalganj nearest river must be mapped", gopalganj.nearestRiver)
        assertEquals("Gandak", gopalganj.nearestRiver)
    }

    @Test
    fun testSourceIntegrityValidator_HonestAttribution() {
        // Test IMD claim when no IMD observation exists
        val (attribution, isVerified) = SourceIntegrityValidator.validateSourceClaim(DataSourceType.IMD, emptyList())
        assertFalse("Unverified IMD claim must NOT be reported as authoritative", isVerified)
        assertTrue("Unverified claim must be safely downgraded to model estimate", attribution.contains("estimate"))
    }

    @Test
    fun testDataFreshnessEngine_PhysicalDeltas() {
        val now = System.currentTimeMillis()

        // 5 minutes old
        val (liveStatus, liveText) = DataFreshnessEngine.evaluateFreshness(now - 5 * 60 * 1000L, isCachedFallback = false)
        assertEquals(DataFreshnessStatus.LIVE, liveStatus)
        assertTrue(liveText.contains("min ago") || liveText.contains("Just now"))

        // Cached fallback
        val (cachedStatus, cachedText) = DataFreshnessEngine.evaluateFreshness(now - 45 * 60 * 1000L, isCachedFallback = true)
        assertEquals(DataFreshnessStatus.CACHED, cachedStatus)
        assertTrue(cachedText.contains("Cached local snapshot"))
    }

    @Test
    fun testConfidenceEngine_DefensibleMathematicalScoring() {
        val (conf, uncert) = ConfidenceEngine.computeConfidence(
            freshness = DataFreshnessStatus.LIVE,
            hasRiverStation = true,
            hasLiveRadar = true,
            hasNwpAgreement = true,
            hasElevationDem = true
        )

        assertTrue("Confidence score must be in defensible bounds (25-95%)", conf.scorePercent in 25..95)
        assertTrue("Basis must document explicit contributing factors", conf.basis.isNotEmpty())
        assertEquals("Uncertainty must be LOW when all telemetry inputs align", "LOW", uncert.level)
    }

    @Test
    fun testFullPreDemoIntegrityAudit() = runBlocking {
        val report = WeatherGPTIntegrityCheck.runFullSystemIntegrityAudit()

        println("INTEGRITY AUDIT REPORT: ${report.overallStatus} (${report.passedCount}/${report.totalTests} tests passed)")
        for (item in report.items) {
            println("  [${item.status}] ${item.category} - ${item.testName}: ${item.evidence}")
        }

        assertTrue("Integrity Audit must not FAIL", report.overallStatus != CheckStatus.FAIL)
        assertTrue("Must pass at least 9 critical architectural tests", report.passedCount >= 9)
    }
}
