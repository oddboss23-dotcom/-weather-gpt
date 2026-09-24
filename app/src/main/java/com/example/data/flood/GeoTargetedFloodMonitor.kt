package com.example.data.flood

import com.example.data.model.AlertSeverity
import com.example.data.model.CityLocation
import com.example.data.model.DisasterAlert
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Geo-Targeted Flood Monitoring Engine.
 *
 * Monitors:
 * - Current GPS Location
 * - Selected Active Location
 * - Saved Locations
 * - District / Block / Village hierarchy
 *
 * Correlates locations with nearby monitored river stations (within 55km river basin radius
 * or matching district) and generates structured, prioritized disaster alerts.
 */
class GeoTargetedFloodMonitor(
    private val floodDataService: AuthoritativeFloodDataService = AuthoritativeFloodDataService()
) {

    /**
     * Finds nearby river stations for a given coordinate or district name.
     */
    fun findRelevantStations(
        locationName: String,
        districtName: String?,
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 55.0
    ): List<RiverBasinStation> {
        val directDistrictMatches = AuthoritativeHydrologyRegistry.stations.filter { st ->
            (districtName != null && st.district.contains(districtName, ignoreCase = true)) ||
            st.district.contains(locationName, ignoreCase = true) ||
            locationName.contains(st.district, ignoreCase = true)
        }

        if (directDistrictMatches.isNotEmpty()) {
            return directDistrictMatches
        }

        // Distance-based matching
        return AuthoritativeHydrologyRegistry.stations.filter { st ->
            haversineDistanceKm(latitude, longitude, st.latitude, st.longitude) <= radiusKm
        }
    }

    /**
     * Evaluates flood alerts for the user's location and produces structured DisasterAlerts.
     */
    suspend fun evaluateLocationFloodAlerts(
        locationName: String,
        districtName: String?,
        stateName: String,
        latitude: Double,
        longitude: Double
    ): List<DisasterAlert> {
        val candidateStations = findRelevantStations(locationName, districtName, latitude, longitude)
        val floodAlerts = mutableListOf<DisasterAlert>()

        for (station in candidateStations) {
            val evalResult = floodDataService.evaluateStationFloodRisk(station)
            evalResult.onSuccess { assessment ->
                // Generate alerts for WATCH, WARNING, DANGER, and SEVERE FLOOD RISK
                if (assessment.classification != FloodRiskCategory.NORMAL) {
                    floodAlerts.add(mapAssessmentToDisasterAlert(assessment, locationName, districtName ?: station.district))
                }
            }
        }

        return floodAlerts
    }

    /**
     * Maps calculated numeric assessment into the official DisasterAlert schema.
     * ZERO LLM intervention in triggering logic.
     */
    fun mapAssessmentToDisasterAlert(
        assessment: FloodRiskAssessment,
        userLocation: String,
        userDistrict: String
    ): DisasterAlert {
        val st = assessment.station
        val obs = assessment.observation

        val titlePrefix = when (assessment.classification) {
            FloodRiskCategory.SEVERE_FLOOD_RISK -> "🚨 CRITICAL FLOOD ALERT"
            FloodRiskCategory.DANGER -> "🚨 FLOOD WARNING (DANGER LEVEL EXCEEDED)"
            FloodRiskCategory.WARNING -> "⚠️ FLOOD ADVISORY (ABOVE WARNING LEVEL)"
            FloodRiskCategory.WATCH -> "🟡 FLOOD WATCH (RIVER DISCHARGE SWELLING)"
            FloodRiskCategory.NORMAL -> "🟢 SAFE RIVER CONDITIONS"
        }

        val title = "$titlePrefix: ${st.riverName} River at ${st.stationName} Station"

        val impactSummary = when (assessment.classification) {
            FloodRiskCategory.SEVERE_FLOOD_RISK ->
                "Extreme inundation across low-lying riparian tracts of $userDistrict. River is flowing at ${obs.currentWaterLevelM}m (+${assessment.distanceAboveDangerM}m above Danger Level). Highest Flood Level (${st.highestFloodLevelM}m) threatened. Severe threat to kutcha embankments."
            FloodRiskCategory.DANGER ->
                "River water level at ${obs.currentWaterLevelM}m has crossed the official Danger Level (${st.dangerLevelM}m) by +${assessment.distanceAboveDangerM}m. Trend is ${obs.trend.displayName.uppercase()}. Inundation of riverbank agricultural lands and rural approach roads."
            FloodRiskCategory.WARNING ->
                "River water level at ${obs.currentWaterLevelM}m is flowing above Warning Level (${st.warningLevelM}m) by +${assessment.distanceAboveWarningM}m. Catchment rainfall is ${obs.rainfallIntensityMm} mm with 48h forecast rainfall of ${obs.forecastRainfallMm} mm."
            FloodRiskCategory.WATCH ->
                "Water level at ${obs.currentWaterLevelM}m is approaching Warning Level (${st.warningLevelM}m). Water level trend is ${obs.trend.displayName.uppercase()} due to upstream rainfall."
            FloodRiskCategory.NORMAL ->
                "River water levels normal at ${obs.currentWaterLevelM}m."
        }

        val mitigation = "CWC Hydro-Meteorological Cell active. Bihar FMIS 24x7 control room monitoring barrage discharge. NDRF and SDRF battalions alerted. District Magistrate Emergency Operations Center (DEOC) on standby."

        return DisasterAlert(
            id = "flood_${st.stationId}_${obs.timestamp}",
            hazardType = "Riverine Flood & Inundation (${st.riverName} Basin)",
            severity = assessment.urgencyLevel,
            location = "$userLocation ($userDistrict, ${st.state})",
            title = title,
            validTime = "Observed: ${obs.observationTimeFormatted} • Real-time CWC / FMIS Feed",
            expectedImpact = impactSummary,
            recommendedAction = assessment.recommendedAction,
            mitigationGuidance = mitigation,
            verifiedSource = "Central Water Commission (CWC) & Bihar Flood Management Information System (FMIS)",
            affectedDistricts = listOf(st.district, userDistrict),
            isFloodAlert = true,
            floodStation = st.stationName,
            floodRiver = st.riverName,
            floodCurrentLevel = obs.currentWaterLevelM,
            floodWarningLevel = obs.warningLevelM,
            floodDangerLevel = obs.dangerLevelM,
            floodHighestFloodLevel = obs.highestFloodLevelM,
            floodTrend = obs.trend.displayName,
            floodDistanceAboveDanger = assessment.distanceAboveDangerM,
            floodDistanceAboveWarning = assessment.distanceAboveWarningM,
            floodRainfallMm = obs.rainfallIntensityMm,
            floodForecastRainfallMm = obs.forecastRainfallMm,
            floodStatus = assessment.statusText,
            floodObservationTime = obs.observationTimeFormatted,
            floodDataSource = "CWC / Bihar WRD FMIS & IMD Telemetry",
            floodRiskCategory = assessment.classification.name,
            latitude = st.latitude,
            longitude = st.longitude,
            priorityRank = assessment.priorityRank
        )
    }

    private fun haversineDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
