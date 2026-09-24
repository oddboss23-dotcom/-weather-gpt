package com.example.service

import com.example.data.gis.MeteorologicalStation
import com.example.data.model.WeatherData
import com.example.ui.components.GisMarkerIconFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

/**
 * SmartCityLayerManager
 *
 * Dedicated GIS overlay controller that dynamically adds and manages smart-city
 * meteorological spatial vectors on the existing Google Maps implementation:
 * - Urban Waterlogging Vulnerability (arterial roads, underpasses, drainage pinch points)
 * - Basin Runoff & Urban Flood Inundation Zones
 * - Biometeorological Heat Stress & Urban Heat Island (UHI) thermal contours
 *
 * Controlled dynamically via Map Settings Panel toggles without adding new UI navigation.
 */
class SmartCityLayerManager(
    private val map: GoogleMap
) {
    private val waterloggingMarkers = mutableListOf<Marker>()
    private val floodCircles = mutableListOf<Circle>()
    private val floodMarkers = mutableListOf<Marker>()
    private val heatStressCircles = mutableListOf<Circle>()
    private val heatStressMarkers = mutableListOf<Marker>()

    /**
     * Synchronize smart city GIS overlays according to current toggle states and opacity.
     */
    fun updateSmartCityOverlays(
        stations: List<MeteorologicalStation>,
        currentWeather: WeatherData,
        showWaterlogging: Boolean,
        showFloodRisk: Boolean,
        showHeatStress: Boolean,
        layerOpacity: Float
    ) {
        updateWaterloggingLayer(stations, currentWeather, showWaterlogging, layerOpacity)
        updateFloodRiskLayer(stations, showFloodRisk, layerOpacity)
        updateHeatStressLayer(stations, showHeatStress, layerOpacity)
    }

    private fun updateWaterloggingLayer(
        stations: List<MeteorologicalStation>,
        currentWeather: WeatherData,
        enabled: Boolean,
        opacity: Float
    ) {
        waterloggingMarkers.forEach { it.remove() }
        waterloggingMarkers.clear()

        if (!enabled) return

        stations.forEach { st ->
            // Compute realistic localized urban drainage waterlogging level
            val rainIntensity = Math.max(st.rainfallRateMmH, currentWeather.expectedRainfallMm)
            val (severity, depthCm) = when {
                st.floodRiskLevel.equals("Critical", ignoreCase = true) || rainIntensity >= 35.0 -> "Severe" to ((rainIntensity * 1.2).toInt().coerceIn(30, 80))
                st.floodRiskLevel.equals("High", ignoreCase = true) || rainIntensity >= 15.0 -> "Moderate" to ((rainIntensity * 0.9).toInt().coerceIn(15, 35))
                st.floodRiskLevel.equals("Moderate", ignoreCase = true) || rainIntensity >= 5.0 -> "Low" to ((rainIntensity * 0.6).toInt().coerceIn(5, 18))
                else -> "Trace" to 2
            }

            // Offset slightly from station center to represent urban arterial drainage low-points
            val underpassLat = st.latitude - 0.015
            val underpassLon = st.longitude + 0.015

            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(underpassLat, underpassLon))
                    .icon(GisMarkerIconFactory.getWaterloggingMarker(severity, depthCm))
                    .title("${st.name} • Urban Waterlogging: ${depthCm}cm")
                    .snippet("Status: $severity Inundation | Arterial low-points & underpasses | Municipal drainage threshold exceeded")
                    .alpha(opacity)
            )
            marker?.let { waterloggingMarkers.add(it) }
        }
    }

    private fun updateFloodRiskLayer(
        stations: List<MeteorologicalStation>,
        enabled: Boolean,
        opacity: Float
    ) {
        floodCircles.forEach { it.remove() }
        floodCircles.clear()
        floodMarkers.forEach { it.remove() }
        floodMarkers.clear()

        if (!enabled) return

        val baseAlpha = (opacity * 50).toInt().coerceIn(12, 90)
        val strokeAlpha = (opacity * 110).toInt().coerceIn(25, 160)

        stations.forEach { st ->
            val (fillColor, strokeColor) = when (st.floodRiskLevel.uppercase()) {
                "VERY HIGH", "CRITICAL" -> Pair(
                    android.graphics.Color.argb(baseAlpha, 239, 68, 68),
                    android.graphics.Color.argb(strokeAlpha, 239, 68, 68)
                )
                "HIGH" -> Pair(
                    android.graphics.Color.argb(baseAlpha, 249, 115, 22),
                    android.graphics.Color.argb(strokeAlpha, 249, 115, 22)
                )
                "MODERATE" -> Pair(
                    android.graphics.Color.argb(baseAlpha, 234, 179, 8),
                    android.graphics.Color.argb(strokeAlpha, 234, 179, 8)
                )
                else -> Pair(
                    android.graphics.Color.argb(baseAlpha, 16, 185, 129),
                    android.graphics.Color.argb(strokeAlpha, 16, 185, 129)
                )
            }

            val floodRadius = when (st.floodRiskLevel.uppercase()) {
                "VERY HIGH", "CRITICAL" -> 60000.0
                "HIGH" -> 45000.0
                "MODERATE" -> 30000.0
                else -> 18000.0
            }

            val floodCircle = map.addCircle(
                CircleOptions()
                    .center(LatLng(st.latitude, st.longitude))
                    .radius(floodRadius)
                    .fillColor(fillColor)
                    .strokeColor(strokeColor)
                    .strokeWidth(2.0f)
            )
            floodCircles.add(floodCircle)

            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(st.latitude, st.longitude))
                    .icon(GisMarkerIconFactory.getFloodRiskMarker(st.floodRiskLevel))
                    .title("${st.name} • Runoff Risk: ${st.floodRiskLevel}")
                    .snippet("CWC Basin Discharge & Flash Flood Runoff Susceptibility")
                    .alpha(opacity)
            )
            marker?.let { floodMarkers.add(it) }
        }
    }

    private fun updateHeatStressLayer(
        stations: List<MeteorologicalStation>,
        enabled: Boolean,
        opacity: Float
    ) {
        heatStressCircles.forEach { it.remove() }
        heatStressCircles.clear()
        heatStressMarkers.forEach { it.remove() }
        heatStressMarkers.clear()

        if (!enabled) return

        val baseAlpha = (opacity * 45).toInt().coerceIn(10, 80)
        val strokeAlpha = (opacity * 95).toInt().coerceIn(20, 140)

        stations.forEach { st ->
            val (fillColor, strokeColor) = when (st.heatIndexCategory) {
                "Extreme Danger" -> Pair(
                    android.graphics.Color.argb(baseAlpha, 147, 51, 234),
                    android.graphics.Color.argb(strokeAlpha, 147, 51, 234)
                )
                "Danger" -> Pair(
                    android.graphics.Color.argb(baseAlpha, 239, 68, 68),
                    android.graphics.Color.argb(strokeAlpha, 239, 68, 68)
                )
                "Extreme Caution" -> Pair(
                    android.graphics.Color.argb(baseAlpha, 249, 115, 22),
                    android.graphics.Color.argb(strokeAlpha, 249, 115, 22)
                )
                "Caution" -> Pair(
                    android.graphics.Color.argb(baseAlpha, 234, 179, 8),
                    android.graphics.Color.argb(strokeAlpha, 234, 179, 8)
                )
                else -> Pair(
                    android.graphics.Color.argb(baseAlpha, 16, 185, 129),
                    android.graphics.Color.argb(strokeAlpha, 16, 185, 129)
                )
            }

            val heatCircle = map.addCircle(
                CircleOptions()
                    .center(LatLng(st.latitude, st.longitude))
                    .radius(85000.0)
                    .fillColor(fillColor)
                    .strokeColor(strokeColor)
                    .strokeWidth(2.0f)
            )
            heatStressCircles.add(heatCircle)

            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(st.latitude, st.longitude))
                    .icon(GisMarkerIconFactory.getHeatIndexMarker(st.feelsLikeC, st.heatIndexCategory))
                    .title("${st.name} • Heat Index: ${st.feelsLikeC}°C")
                    .snippet("Category: ${st.heatIndexCategory} | Rothfusz Biometeorological Calculation")
                    .alpha(opacity)
            )
            marker?.let { heatStressMarkers.add(it) }
        }
    }

    /**
     * Clear all smart city overlay resources when destroyed or toggled off.
     */
    fun clear() {
        waterloggingMarkers.forEach { it.remove() }
        waterloggingMarkers.clear()
        floodCircles.forEach { it.remove() }
        floodCircles.clear()
        floodMarkers.forEach { it.remove() }
        floodMarkers.clear()
        heatStressCircles.forEach { it.remove() }
        heatStressCircles.clear()
        heatStressMarkers.forEach { it.remove() }
        heatStressMarkers.clear()
    }
}
