package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.HapticFeedbackManager
import com.example.service.HapticManager
import androidx.compose.ui.zIndex
import com.example.data.gis.GisTimelineFrame
import com.example.data.gis.WeatherGisLayer
import com.example.ui.components.ForecastTrustDetailDialog
import com.example.ui.components.ForecastVerificationDialog
import com.example.ui.components.GisMapAiAnalysisCard
import com.example.ui.components.GisPointInspectionCard
import com.example.ui.components.NearbyVillageComparisonDialog
import com.example.ui.components.ReportExportDialog
import com.example.ui.components.WeatherMapComponent
import com.example.ui.components.WhatIfSimulatorDialog
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.WeatherGPTViewModel
import com.google.android.gms.maps.model.LatLng

/**
 * RadarMapScreen
 * 
 * High-craft meteorological GIS screen featuring a Box layout where:
 * 1. Base Layer (z-index: 0f): Interactive Google Maps instance (WeatherMapComponent)
 *    centered on the Indian Subcontinent with real meteorological layer rendering.
 * 2. Floating Layer Selectors (z-index: 10f): Floating control card styled in WeatherGPT dark theme
 *    providing one-tap switching between Satellite, Radar, Rainfall, and Wind layers,
 *    plus opacity fine-tuning and layer provenance indicators.
 * 3. Floating Timeline Controller (z-index: 10f): Floating playback dock positioned at the
 *    bottom with Play/Pause, Previous/Next frame controls, relative frame badge,
 *    real timestamps, and smooth scrub slider.
 * 4. Floating Auxiliary Actions (z-index: 10f-20f): GPS Location centering, Gemini AI Synoptic
 *    Analysis trigger, Sector Jump chips, and tapped Coordinate Point Inspection overlay.
 */
@Composable
fun RadarMapScreen(
    viewModel: WeatherGPTViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val weather by viewModel.weatherData.collectAsState()
    val cachedPoints by viewModel.cachedRadarPoints.collectAsState()
    val activeLayer by viewModel.activeGisLayer.collectAsState()
    val layerOpacity by viewModel.gisLayerOpacity.collectAsState()
    val isTimelinePlaying by viewModel.isGisTimelinePlaying.collectAsState()
    val selectedTimelineIndex by viewModel.selectedTimelineIndex.collectAsState()
    val inspectedPoint by viewModel.inspectedPoint.collectAsState()
    val isMapAiAnalysisOpen by viewModel.isMapAiAnalysisOpen.collectAsState()
    val mapAiAnalysisText by viewModel.mapAiAnalysisText.collectAsState()
    val isMapAiAnalysisLoading by viewModel.isMapAiAnalysisLoading.collectAsState()
    val cycloneSystem by viewModel.activeCycloneSystem.collectAsState()
    val currentCity by viewModel.currentLocation.collectAsState()

    val villageHierarchy by viewModel.villageHierarchy.collectAsState()
    val forecastTrust by viewModel.forecastTrust.collectAsState()
    val activeSimulationScenario by viewModel.activeSimulationScenario.collectAsState()
    val simulationResult by viewModel.simulationResult.collectAsState()
    val verificationMetrics by viewModel.verificationMetrics.collectAsState()
    val nearbyVillages by viewModel.nearbyVillages.collectAsState()

    val isWhatIfSimulatorOpen by viewModel.isWhatIfSimulatorOpen.collectAsState()
    val isTrustDetailOpen by viewModel.isTrustDetailOpen.collectAsState()
    val isVerificationSheetOpen by viewModel.isVerificationSheetOpen.collectAsState()
    val isNearbyComparisonOpen by viewModel.isNearbyComparisonOpen.collectAsState()
    val isReportExportOpen by viewModel.isReportExportOpen.collectAsState()

    var mapTargetLocation by remember { mutableStateOf<LatLng?>(null) }
    var mapTargetZoom by remember { mutableStateOf<Float?>(null) }
    var userMapLocation by remember { mutableStateOf<LatLng?>(null) }
    var showOpacitySlider by remember { mutableStateOf(false) }
    var showSecondaryLayers by remember { mutableStateOf(false) }

    val currentFrame = viewModel.gisTimelineFrames.getOrElse(selectedTimelineIndex) {
        viewModel.gisTimelineFrames[3]
    }

    // ROOT BOX LAYOUT
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .testTag("radar_map_screen_root")
    ) {
        // =====================================================================
        // LAYER 0: GOOGLE MAPS INSTANCE (BASE SURFACE, z-index: 0f)
        // =====================================================================
        WeatherMapComponent(
            weather = weather,
            activeLayer = activeLayer,
            layerOpacity = layerOpacity,
            currentFrame = currentFrame,
            isTimelinePlaying = isTimelinePlaying,
            cycloneSystem = cycloneSystem,
            cachedRadarPoints = cachedPoints,
            targetLocation = mapTargetLocation,
            targetZoom = mapTargetZoom,
            userLocation = userMapLocation,
            onMapTapped = { lat, lon ->
                viewModel.inspectCoordinates(lat, lon)
            },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
                .testTag("google_maps_instance")
        )

        // =====================================================================
        // LAYER 1: FLOATING LAYER SELECTORS (TOP DOCK, z-index: 10f)
        // Satellite, Radar, Rainfall, Wind + Opacity Control
        // =====================================================================
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 0.dp)
                .zIndex(10f)
                .testTag("floating_layer_selectors_container"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FloatingPrimaryLayerSelectorCard(
                activeLayer = activeLayer,
                layerOpacity = layerOpacity,
                showOpacitySlider = showOpacitySlider,
                showSecondaryLayers = showSecondaryLayers,
                onLayerSelected = { layer ->
                    if (activeLayer != layer) {
                        HapticFeedbackManager.triggerMapViewSwitch(context)
                    }
                    viewModel.setGisLayer(layer)
                },
                onToggleOpacitySlider = {
                    showOpacitySlider = !showOpacitySlider
                },
                onOpacityChanged = { opacity ->
                    viewModel.setGisLayerOpacity(opacity)
                },
                onToggleSecondaryLayers = {
                    showSecondaryLayers = !showSecondaryLayers
                }
            )

            // Secondary Expandable Layers (Cyclone, Lightning, Temperature, etc.)
            AnimatedVisibility(
                visible = showSecondaryLayers,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                FloatingSecondaryLayersRow(
                    activeLayer = activeLayer,
                    onLayerSelected = { layer ->
                        if (activeLayer != layer) {
                            HapticFeedbackManager.triggerMapViewSwitch(context)
                        }
                        viewModel.setGisLayer(layer)
                    }
                )
            }
        }

        // =====================================================================
        // LAYER 2: FLOATING REGION JUMP BAR (z-index: 9f, Below Layer Selector)
        // Quick zoom to National (India) or key meteorological sectors
        // =====================================================================
        val sectorTopPadding = if (showOpacitySlider) 136.dp else 78.dp
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = sectorTopPadding, end = 86.dp, bottom = 0.dp)
                .horizontalScroll(rememberScrollState())
                .zIndex(9f)
                .testTag("floating_region_pills"),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FloatingSectorPill(
                label = "ALL INDIA",
                isActive = mapTargetZoom == null || mapTargetZoom!! <= 5.0f,
                onClick = {
                    HapticFeedbackManager.triggerMapViewSwitch(context)
                    mapTargetLocation = LatLng(20.5937, 78.9629)
                    mapTargetZoom = 4.8f
                }
            )
            FloatingSectorPill(
                label = "NORTH (DELHI)",
                isActive = false,
                onClick = {
                    HapticFeedbackManager.triggerMapViewSwitch(context)
                    mapTargetLocation = LatLng(28.6139, 77.2090)
                    mapTargetZoom = 7.5f
                }
            )
            FloatingSectorPill(
                label = "WEST (MUMBAI)",
                isActive = false,
                onClick = {
                    HapticFeedbackManager.triggerMapViewSwitch(context)
                    mapTargetLocation = LatLng(19.0760, 72.8777)
                    mapTargetZoom = 7.8f
                }
            )
            FloatingSectorPill(
                label = "EAST (KOLKATA/BAY)",
                isActive = false,
                onClick = {
                    HapticFeedbackManager.triggerMapViewSwitch(context)
                    mapTargetLocation = LatLng(22.5726, 88.3639)
                    mapTargetZoom = 7.2f
                }
            )
            FloatingSectorPill(
                label = "SOUTH (CHENNAI)",
                isActive = false,
                onClick = {
                    HapticFeedbackManager.triggerMapViewSwitch(context)
                    mapTargetLocation = LatLng(13.0827, 80.2707)
                    mapTargetZoom = 7.5f
                }
            )
            FloatingSectorPill(
                label = "CYCLONE ZONE",
                isActive = false,
                onClick = {
                    HapticFeedbackManager.triggerMapViewSwitch(context)
                    mapTargetLocation = LatLng(cycloneSystem.currentLat, cycloneSystem.currentLon)
                    mapTargetZoom = 6.8f
                }
            )
        }

        // =====================================================================
        // LAYER 3: FLOATING GEMINI AI "ANALYZE MAP" BUTTON (z-index: 10f, Top Right)
        // =====================================================================
        val aiButtonTopPadding = if (showOpacitySlider) 136.dp else 78.dp
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(start = 0.dp, top = aiButtonTopPadding, end = 12.dp, bottom = 0.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(ElectricCyan, ElectricTeal)
                    )
                )
                .clickable {
                    viewModel.setMapAiAnalysisOpen(true)
                }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .zIndex(10f)
                .testTag("floating_ai_analyze_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Analyze Map",
                    tint = DeepNavyBg,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "ANALYZE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 9.sp
                    ),
                    color = DeepNavyBg
                )
            }
        }

        // =====================================================================
        // LAYER 4: FLOATING GPS MY LOCATION BUTTON (z-index: 10f, Bottom Right)
        // =====================================================================
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 14.dp, bottom = 125.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(SurfaceNavy.copy(alpha = 0.94f))
                .border(1.dp, ElectricCyan, CircleShape)
                .clickable {
                    HapticManager.lightTap(context)
                    viewModel.fetchCurrentGpsLocation()
                    val latLng = LatLng(currentCity.latitude, currentCity.longitude)
                    userMapLocation = latLng
                    mapTargetLocation = latLng
                    mapTargetZoom = 12.0f
                    viewModel.inspectCoordinates(currentCity.latitude, currentCity.longitude, currentCity.name)
                }
                .zIndex(10f)
                .testTag("floating_gps_location_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "My Location",
                tint = ElectricCyan,
                modifier = Modifier.size(24.dp)
            )
        }

        // =====================================================================
        // LAYER 5: FLOATING TIMELINE CONTROLLER (z-index: 10f, Bottom Center)
        // Play/Pause, Step Frames, Timestamp, Scrub Slider, Legend Units
        // =====================================================================
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .zIndex(10f)
                .testTag("floating_timeline_controller_container")
        ) {
            FloatingTimelineControllerCard(
                frames = viewModel.gisTimelineFrames,
                currentFrameIndex = selectedTimelineIndex,
                isPlaying = isTimelinePlaying,
                activeLayer = activeLayer,
                onFrameSelected = { index ->
                    viewModel.setTimelineFrameIndex(index)
                },
                onTogglePlay = {
                    viewModel.toggleGisTimelinePlay()
                }
            )
        }

        // =====================================================================
        // LAYER 6: FLOATING POINT INSPECTION OVERLAY (z-index: 15f, Above Timeline)
        // Appears when a user taps any point on the Google Map
        // =====================================================================
        AnimatedVisibility(
            visible = inspectedPoint != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 105.dp)
                .zIndex(15f)
        ) {
            inspectedPoint?.let { point ->
                GisPointInspectionCard(
                    inspection = point,
                    onClose = { viewModel.closePointInspection() },
                    onAskWeatherGpt = { query ->
                        viewModel.sendChatMessage(query)
                    },
                    onSwitchLayer = { layerId ->
                        val matchedLayer = WeatherGisLayer.values().firstOrNull { it.id == layerId }
                        if (matchedLayer != null) {
                            if (activeLayer != matchedLayer) {
                                HapticFeedbackManager.triggerMapViewSwitch(context)
                            }
                            viewModel.setGisLayer(matchedLayer)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // =====================================================================
        // LAYER 7: SYNOPTIC AI ANALYSIS MODAL CARD (z-index: 20f)
        // =====================================================================
        AnimatedVisibility(
            visible = isMapAiAnalysisOpen,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(20f)
        ) {
            GisMapAiAnalysisCard(
                weather = weather,
                activeLayer = activeLayer,
                analysisText = mapAiAnalysisText,
                isLoading = isMapAiAnalysisLoading,
                onRefresh = { viewModel.triggerMapAiAnalysis() },
                onClose = { viewModel.setMapAiAnalysisOpen(false) },
                onOpenSimulator = { viewModel.openWhatIfSimulator(true) },
                onOpenTrust = { viewModel.openTrustDetail(true) },
                onOpenNearby = { viewModel.openNearbyComparison(true) }
            )
        }

        // =====================================================================
        // LAYER 8: HYPERLOCAL INTELLIGENCE MODALS (SIH 2026 PS-68)
        // =====================================================================
        if (isWhatIfSimulatorOpen) {
            WhatIfSimulatorDialog(
                village = villageHierarchy,
                currentScenario = activeSimulationScenario,
                simulationResult = simulationResult,
                onSelectScenario = { viewModel.setSimulationScenario(it) },
                onDismiss = { viewModel.openWhatIfSimulator(false) }
            )
        }

        if (isTrustDetailOpen) {
            ForecastTrustDetailDialog(
                trust = forecastTrust,
                village = villageHierarchy,
                onDismiss = { viewModel.openTrustDetail(false) }
            )
        }

        if (isVerificationSheetOpen) {
            ForecastVerificationDialog(
                metrics = verificationMetrics,
                village = villageHierarchy,
                onDismiss = { viewModel.openVerificationSheet(false) }
            )
        }

        if (isNearbyComparisonOpen) {
            NearbyVillageComparisonDialog(
                currentVillage = villageHierarchy,
                comparisons = nearbyVillages,
                onSelectVillage = { },
                onDismiss = { viewModel.openNearbyComparison(false) }
            )
        }

        if (isReportExportOpen) {
            ReportExportDialog(
                village = villageHierarchy,
                onGenerateReport = { format -> viewModel.generateVerifiedReport(format) },
                onDismiss = { viewModel.openReportExport(false) }
            )
        }
    }
}

/**
 * Floating Primary Layer Selector Card
 * Styled in the WeatherGPT dark theme:
 * Direct 4-pill selector for Satellite, Radar, Rainfall, and Wind
 * with active glow and optional opacity slider.
 */
@Composable
private fun FloatingPrimaryLayerSelectorCard(
    activeLayer: WeatherGisLayer,
    layerOpacity: Float,
    showOpacitySlider: Boolean,
    showSecondaryLayers: Boolean,
    onLayerSelected: (WeatherGisLayer) -> Unit,
    onToggleOpacitySlider: () -> Unit,
    onOpacityChanged: (Float) -> Unit,
    onToggleSecondaryLayers: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceNavy.copy(alpha = 0.94f))
            .border(1.dp, SurfaceBorder.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row of the 4 requested primary layers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 4 Primary Layer Buttons: Satellite, Radar, Rainfall, Wind
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryLayerTabButton(
                    label = "Satellite",
                    icon = Icons.Default.SatelliteAlt,
                    isSelected = activeLayer == WeatherGisLayer.SATELLITE,
                    onClick = { onLayerSelected(WeatherGisLayer.SATELLITE) },
                    modifier = Modifier.weight(1f)
                )
                PrimaryLayerTabButton(
                    label = "Radar",
                    icon = Icons.Default.Radar,
                    isSelected = activeLayer == WeatherGisLayer.RADAR,
                    onClick = { onLayerSelected(WeatherGisLayer.RADAR) },
                    modifier = Modifier.weight(1f)
                )
                PrimaryLayerTabButton(
                    label = "Rainfall",
                    icon = Icons.Default.Grain,
                    isSelected = activeLayer == WeatherGisLayer.RAINFALL,
                    onClick = { onLayerSelected(WeatherGisLayer.RAINFALL) },
                    modifier = Modifier.weight(1f)
                )
                PrimaryLayerTabButton(
                    label = "Wind",
                    icon = Icons.Default.Air,
                    isSelected = activeLayer == WeatherGisLayer.WIND,
                    onClick = { onLayerSelected(WeatherGisLayer.WIND) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Action: Opacity Slider Toggle
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (showOpacitySlider) ElectricCyan.copy(alpha = 0.2f) else SurfaceCard)
                    .border(
                        0.5.dp,
                        if (showOpacitySlider) ElectricCyan else SurfaceBorder,
                        CircleShape
                    )
                    .clickable { onToggleOpacitySlider() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Opacity Adjust",
                    tint = if (showOpacitySlider) ElectricCyan else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Action: Secondary Layers Expander Toggle
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (showSecondaryLayers) ElectricCyan.copy(alpha = 0.2f) else SurfaceCard)
                    .border(
                        0.5.dp,
                        if (showSecondaryLayers) ElectricCyan else SurfaceBorder,
                        CircleShape
                    )
                    .clickable { onToggleSecondaryLayers() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = "More Layers",
                    tint = if (showSecondaryLayers) ElectricCyan else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Opacity Slider Bar (Expandable)
        if (showOpacitySlider) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "OPACITY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextTertiary
                )
                Slider(
                    value = layerOpacity,
                    onValueChange = onOpacityChanged,
                    valueRange = 0.2f..1.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = SurfaceBorder
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(layerOpacity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = ElectricCyan
                )
            }
        }
    }
}

/**
 * Individual Primary Layer Tab Button
 */
@Composable
private fun PrimaryLayerTabButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) SurfaceCard else Color.Transparent
            )
            .border(
                1.dp,
                if (isSelected) ElectricCyan else SurfaceBorder.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .clickable {
                HapticManager.selectionChanged(context)
                onClick()
            }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) ElectricCyan else TextTertiary,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 10.sp
                ),
                color = if (isSelected) TextPrimary else TextSecondary,
                maxLines = 1
            )
        }
    }
}

/**
 * Secondary Expandable Layers Row
 * Allows switching to other meteorological layers (Cyclone, Lightning, Temperature, etc.)
 */
@Composable
private fun FloatingSecondaryLayersRow(
    activeLayer: WeatherGisLayer,
    onLayerSelected: (WeatherGisLayer) -> Unit
) {
    val secondaryLayers = listOf(
        WeatherGisLayer.FORECAST_CONFIDENCE,
        WeatherGisLayer.FORECAST_BUST_RISK,
        WeatherGisLayer.CYCLONE,
        WeatherGisLayer.LIGHTNING,
        WeatherGisLayer.WATERLOGGING,
        WeatherGisLayer.FLOOD_RISK,
        WeatherGisLayer.HEAT_INDEX,
        WeatherGisLayer.TEMPERATURE,
        WeatherGisLayer.HUMIDITY,
        WeatherGisLayer.PRESSURE
    )

    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceNavy.copy(alpha = 0.90f))
            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        secondaryLayers.forEach { layer ->
            val isSelected = activeLayer == layer
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else SurfaceCard)
                    .border(
                        0.5.dp,
                        if (isSelected) ElectricCyan else SurfaceBorder.copy(alpha = 0.5f),
                        RoundedCornerShape(8.dp)
                    )
                    .clickable {
                        HapticManager.selectionChanged(context)
                        onLayerSelected(layer)
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = layer.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) ElectricCyan else TextSecondary
                )
            }
        }
    }
}

/**
 * Floating Sector Quick Jump Pill
 */
@Composable
private fun FloatingSectorPill(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) ElectricCyan.copy(alpha = 0.25f) else SurfaceNavy.copy(alpha = 0.90f))
            .border(
                0.5.dp,
                if (isActive) ElectricCyan else SurfaceBorder,
                RoundedCornerShape(20.dp)
            )
            .clickable {
                HapticManager.selectionChanged(context)
                onClick()
            }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 9.5.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.3.sp
            ),
            color = if (isActive) ElectricCyan else TextSecondary
        )
    }
}

/**
 * Floating Timeline Controller Card
 * Positioned as a floating controller docked at the bottom of the map above the navigation bar.
 * Controls: Play/Pause, Step Prev, Step Next, Timeline Slider, Relative Label badge,
 * Timestamp, and physical unit legend for the active layer.
 */
@Composable
private fun FloatingTimelineControllerCard(
    frames: List<GisTimelineFrame>,
    currentFrameIndex: Int,
    isPlaying: Boolean,
    activeLayer: WeatherGisLayer,
    onFrameSelected: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (frames.isEmpty()) return
    val currentFrame = frames.getOrElse(currentFrameIndex) { frames.firstOrNull() ?: return }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceNavy.copy(alpha = 0.94f))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Controls Row: Play/Pause, Prev, Next, Frame relative label, Timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Playback Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Play / Pause Button
                IconButton(
                    onClick = {
                        HapticManager.lightTap(context)
                        onTogglePlay()
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, if (isPlaying) AlertGreen else ElectricCyan, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Timeline" else "Play Timeline",
                        tint = if (isPlaying) AlertGreen else ElectricCyan,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Previous Frame
                IconButton(
                    onClick = {
                        HapticManager.selectionChanged(context)
                        val prev = if (currentFrameIndex > 0) currentFrameIndex - 1 else frames.size - 1
                        onFrameSelected(prev)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Frame",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Next Frame
                IconButton(
                    onClick = {
                        HapticManager.selectionChanged(context)
                        val next = (currentFrameIndex + 1) % frames.size
                        onFrameSelected(next)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Frame",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Relative Label Badge (e.g. "T-3h", "NOW (LIVE)", "+6h FORECAST")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (currentFrame.isForecast) AlertOrange.copy(alpha = 0.2f)
                            else ElectricTeal.copy(alpha = 0.25f)
                        )
                        .border(
                            0.5.dp,
                            if (currentFrame.isForecast) AlertOrange else ElectricCyan,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (currentFrame.isForecast) "${currentFrame.label} FCST" else currentFrame.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (currentFrame.isForecast) AlertOrange else ElectricCyan
                    )
                }
            }

            // Real Time Label & Source Indicator
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currentFrame.formattedTime,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "${activeLayer.displayName} (${activeLayer.unit})",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                    color = ElectricCyan
                )
            }
        }

        // Scrubbable Timeline Slider
        Slider(
            value = currentFrameIndex.toFloat(),
            onValueChange = { onFrameSelected(it.toInt().coerceIn(0, frames.size - 1)) },
            valueRange = 0f..(frames.size - 1).toFloat(),
            steps = frames.size - 2,
            colors = SliderDefaults.colors(
                thumbColor = ElectricCyan,
                activeTrackColor = ElectricCyan,
                inactiveTrackColor = SurfaceBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        )

        // Active Layer Unit Legend Gradient Bar
        FloatingLayerLegendBar(activeLayer = activeLayer)
    }
}

/**
 * Compact Unit-Calibrated Gradient Legend Bar for the active layer
 */
@Composable
private fun FloatingLayerLegendBar(activeLayer: WeatherGisLayer) {
    val legendColors = when (activeLayer) {
        WeatherGisLayer.RADAR -> listOf(
            AlertGreen, AlertYellow, AlertOrange, AlertRed
        )
        WeatherGisLayer.SATELLITE -> listOf(
            Color(0xFF1E293B), Color(0xFF475569), Color(0xFF94A3B8), Color(0xFFF1F5F9)
        )
        WeatherGisLayer.RAINFALL -> listOf(
            Color(0xFF0284C7), AlertGreen, AlertYellow, AlertRed
        )
        WeatherGisLayer.WIND -> listOf(
            ElectricCyan, ElectricTeal, AlertYellow, AlertRed
        )
        WeatherGisLayer.HEAT_INDEX -> listOf(
            AlertGreen, AlertYellow, AlertOrange, AlertRed, Color(0xFF9333EA)
        )
        WeatherGisLayer.FLOOD_RISK -> listOf(
            AlertGreen, AlertYellow, AlertOrange, AlertRed
        )
        WeatherGisLayer.WATERLOGGING -> listOf(
            Color(0xFF2563EB), Color(0xFFCA8A04), Color(0xFFEA580C), Color(0xFFDC2626)
        )
        else -> listOf(
            ElectricCyan, AlertYellow, AlertRed
        )
    }

    val labels = when (activeLayer) {
        WeatherGisLayer.RADAR -> listOf("10 dBZ", "30 dBZ (Rain)", "50 dBZ (Storm)", "65+ dBZ")
        WeatherGisLayer.SATELLITE -> listOf("-80°C Top", "-40°C", "-10°C", "Surface")
        WeatherGisLayer.RAINFALL -> listOf("0.5 mm/h", "5 mm/h", "20 mm/h", "50+ mm/h")
        WeatherGisLayer.WIND -> listOf("10 km/h", "30 km/h", "60 km/h", "90+ km/h")
        WeatherGisLayer.HEAT_INDEX -> listOf("Safe (<27°)", "Caution (27-32°)", "Ext Caution (33-41°)", "Danger (42-53°)", "Ext Danger (54°+)")
        WeatherGisLayer.FLOOD_RISK -> listOf("Low Runoff", "Moderate", "High Inundation", "Critical (Underpass Flood)")
        WeatherGisLayer.WATERLOGGING -> listOf("Trace", "<15cm (Low)", "15-35cm (Mod)", ">35cm (Severe)")
        else -> listOf("Low", "Moderate", "High", "Extreme")
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.horizontalGradient(legendColors))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.5.sp),
                    color = TextTertiary
                )
            }
        }
    }
}
