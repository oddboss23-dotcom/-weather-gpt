package com.example.data.flood

import com.example.data.model.AlertSeverity
import com.example.data.model.DisasterAlert

/**
 * Strict Priority Ordering Engine:
 * 1. CRITICAL FLOOD
 * 2. SEVERE WEATHER
 * 3. FLOOD WARNING
 * 4. HEAVY RAIN
 * 5. THUNDERSTORM
 * 6. NORMAL WEATHER
 */
object AlertPriorityManager {

    fun determinePriorityRank(alert: DisasterAlert): Int {
        return when {
            // Priority 1: CRITICAL FLOOD
            alert.isFloodAlert && (alert.severity == AlertSeverity.RED ||
                    alert.floodRiskCategory == FloodRiskCategory.SEVERE_FLOOD_RISK.name ||
                    alert.floodRiskCategory == FloodRiskCategory.DANGER.name ||
                    alert.floodStatus?.contains("ABOVE DANGER", ignoreCase = true) == true ||
                    alert.floodStatus?.contains("EXCEEDING DANGER", ignoreCase = true) == true) -> 1

            // Priority 2: SEVERE WEATHER (Cyclone, High Heatwave, Violent Gales)
            alert.severity == AlertSeverity.RED ||
                    alert.hazardType.contains("Cyclone", ignoreCase = true) ||
                    alert.hazardType.contains("Storm Track", ignoreCase = true) ||
                    (alert.hazardType.contains("Heat", ignoreCase = true) && alert.severity == AlertSeverity.ORANGE) -> 2

            // Priority 3: FLOOD WARNING (Orange severity river water level alerts)
            alert.isFloodAlert && (alert.severity == AlertSeverity.ORANGE ||
                    alert.floodRiskCategory == FloodRiskCategory.WARNING.name ||
                    alert.floodStatus?.contains("WARNING") == true) -> 3

            // Priority 4: HEAVY RAIN (Orange/Yellow precipitation, waterlogging)
            alert.hazardType.contains("Rain", ignoreCase = true) ||
                    alert.hazardType.contains("Monsoon", ignoreCase = true) ||
                    alert.hazardType.contains("Waterlogging", ignoreCase = true) ||
                    (alert.isFloodAlert && alert.floodRiskCategory == FloodRiskCategory.WATCH.name) -> 4

            // Priority 5: THUNDERSTORM / LIGHTNING
            alert.hazardType.contains("Thunderstorm", ignoreCase = true) ||
                    alert.hazardType.contains("Lightning", ignoreCase = true) ||
                    alert.hazardType.contains("Squall", ignoreCase = true) -> 5

            // Priority 6: NORMAL WEATHER / SAFE
            else -> 6
        }
    }

    /**
     * Sorts a list of alerts strictly following the mandated priority rules.
     */
    fun sortAlertsByPriority(alerts: List<DisasterAlert>): List<DisasterAlert> {
        return alerts.sortedWith(
            compareBy<DisasterAlert> { determinePriorityRank(it) }
                .thenByDescending { it.severity == AlertSeverity.RED }
                .thenByDescending { it.severity == AlertSeverity.ORANGE }
        )
    }
}
