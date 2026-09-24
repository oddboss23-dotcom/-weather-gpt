package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.CityLocation
import com.example.data.model.UserMode
import com.example.service.HapticFeedbackManager
import com.example.ui.components.AiInsightCard
import com.example.ui.components.AuthoritativeFloodCard
import com.example.ui.components.CurrentWeatherHeroCard
import com.example.ui.components.FarmerAdvisoryCard
import com.example.ui.components.ForecastTrustDetailDialog
import com.example.ui.components.ForecastVerificationDialog
import com.example.ui.components.HorizontalHourlyForecastStrip
import com.example.ui.components.HourlyRainfallBarChart
import com.example.ui.components.HourlyTemperatureLineChart
import com.example.ui.components.HyperlocalWeatherModule
import com.example.ui.components.NearbyVillageComparisonDialog
import com.example.ui.components.QuickAccessPanel
import com.example.ui.components.QuickAccessStrip
import com.example.ui.components.QuickAccessTab
import com.example.ui.components.ReportExportDialog
import com.example.ui.components.RuralOfflineModeBanner
import com.example.ui.components.WhatIfSimulatorDialog
import com.example.ui.components.WhyThisForecastCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextHighlight
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.WeatherGPTViewModel

@Composable
fun WeatherHomeScreen(
    viewModel: WeatherGPTViewModel,
    onNavigateToChatWithQuery: (String) -> Unit,
    onNavigateToGisRadar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weather by viewModel.weatherData.collectAsState()
    val canonicalSnapshot by viewModel.canonicalSnapshot.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val hourlyForecast by viewModel.hourlyForecast.collectAsState()
    val dailyForecast by viewModel.dailyForecast.collectAsState()
    val insight by viewModel.weatherInsight.collectAsState()
    val userMode by viewModel.selectedUserMode.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val activeQuickAccessTab by viewModel.activeQuickAccessTab.collectAsState()

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

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Trigger differentiated tactile vibration patterns for flood warnings and weather alerts
    LaunchedEffect(alerts) {
        val criticalFlood = alerts.firstOrNull { it.isFloodAlert && (it.severity == AlertSeverity.RED || it.severity == AlertSeverity.ORANGE) }
        val redAlert = alerts.firstOrNull { it.severity == AlertSeverity.RED }
        val floodWarning = alerts.firstOrNull { it.isFloodAlert }
        val weatherWarning = alerts.firstOrNull { it.severity == AlertSeverity.ORANGE || it.severity == AlertSeverity.YELLOW }

        when {
            criticalFlood != null -> HapticFeedbackManager.triggerFloodWarning(context, isCritical = true)
            redAlert != null -> HapticFeedbackManager.triggerSevereAlert(context)
            floodWarning != null -> HapticFeedbackManager.triggerFloodWarning(context, isCritical = false)
            weatherWarning != null -> HapticFeedbackManager.triggerWeatherAlert(context, weatherWarning.severity)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Low-Bandwidth Rural Offline Indicator & Sync Bar (Feature 6)
            RuralOfflineModeBanner(viewModel = viewModel)

            // PRIORITIZED DISASTER ALERTS:
            // Flood alerts and severe weather alerts strictly take priority over ordinary weather cards.
            // Priority: CRITICAL FLOOD > SEVERE WEATHER > FLOOD WARNING > HEAVY RAIN > THUNDERSTORM > NORMAL
            val activeFloodAlerts = alerts.filter { it.isFloodAlert }
            if (activeFloodAlerts.isNotEmpty()) {
                activeFloodAlerts.forEach { floodAlert ->
                    AuthoritativeFloodCard(
                        alert = floodAlert,
                        onListenAudio = { viewModel.speakFloodAlert(floodAlert) },
                        onViewMap = onNavigateToGisRadar
                    )
                }
            }

            // Sleek Hero Current Weather Section
            CurrentWeatherHeroCard(
                weather = weather,
                confidencePercent = canonicalSnapshot?.confidence?.scorePercent ?: 88,
                confidenceBasis = canonicalSnapshot?.confidence?.basis ?: emptyList()
            )

            // Horizontal Hourly Forecast Strip (immediate next 12 hours)
            if (hourlyForecast.isNotEmpty()) {
                HorizontalHourlyForecastStrip(hourlyList = hourlyForecast)
            }

            // Quick Access Domain Intelligence Strip
            QuickAccessStrip(
                selectedTab = activeQuickAccessTab,
                onTabSelected = { tab -> viewModel.setActiveQuickAccessTab(tab) }
            )

            // Dynamic Contextual Quick Access Panel (Aviation, Marine, Model Center, Climate, Smart City, Disaster Impact)
            QuickAccessPanel(
                selectedTab = activeQuickAccessTab,
                currentWeather = weather,
                cityName = currentLocation.name,
                onClose = { viewModel.setActiveQuickAccessTab(QuickAccessTab.NONE) },
                onNavigateToGisRadar = onNavigateToGisRadar
            )

            // Hyperlocal Weather & Micro-Panchayat Spatial Hierarchy (Feature 4)
            HyperlocalWeatherModule(
                weather = weather,
                hourlyForecast = hourlyForecast,
                dailyForecast = dailyForecast,
                currentLocation = currentLocation,
                onSelectHierarchyLocation = { hierarchy ->
                    viewModel.loadWeatherForCity(
                        CityLocation(
                            name = hierarchy.district,
                            state = hierarchy.state,
                            latitude = hierarchy.latitude,
                            longitude = hierarchy.longitude
                        )
                    )
                }
            )

            // AI "Why This Forecast?" & Confidence Gauge Card
            WhyThisForecastCard(
                weather = weather,
                snapshot = canonicalSnapshot,
                trustAssessment = forecastTrust,
                onOpenTrust = { viewModel.openTrustDetail(true) },
                onOpenSimulator = { viewModel.openWhatIfSimulator(true) },
                onOpenVerification = { viewModel.openVerificationSheet(true) },
                onOpenNearby = { viewModel.openNearbyComparison(true) },
                onOpenExport = { viewModel.openReportExport(true) }
            )

            // Sleek AI Quick Intelligence Query Chips
            QuickPromptStrip(onPromptClick = onNavigateToChatWithQuery)

            // AI Insight & Decision Support Card
            if (insight != null) {
                AiInsightCard(
                    insight = insight!!,
                    onSpeakClick = {
                        viewModel.speakText("${insight!!.summary}. ${insight!!.aiInterpretation}. ${insight!!.generalRecommendation}")
                    },
                    onActionClick = { action ->
                        when (action) {
                            "Crop Advisory" -> onNavigateToChatWithQuery("Detailed Crop Advisory & Farm Protection Guidance for current weather")
                            "Detailed Forecast" -> onNavigateToChatWithQuery("Provide a comprehensive meteorological breakdown for the next 24-48 hours")
                            else -> onNavigateToChatWithQuery("What is the action plan for $action based on current weather?")
                        }
                    }
                )
            }

            // Farmer Advisory Card if Farmer mode OR general awareness
            if (userMode == UserMode.FARMER_KRISHI || userMode == UserMode.GENERAL_PUBLIC) {
                FarmerAdvisoryCard(weather = weather)
            }

            // 24H / 48H Atmospheric Forecast Charts
            if (hourlyForecast.isNotEmpty()) {
                var selectedHorizonHours by remember { mutableStateOf(24) }
                val displayHourlyList = remember(hourlyForecast, selectedHorizonHours) {
                    hourlyForecast.take(selectedHorizonHours)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceNavy)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // 24H | 48H Horizon Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FORECAST TIMELINE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                color = TextTertiary
                            )

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(SurfaceCard)
                                    .border(0.5.dp, SurfaceBorder, RoundedCornerShape(50))
                                    .padding(2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(24, 48).forEach { hours ->
                                    val isSelected = selectedHorizonHours == hours
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(if (isSelected) ElectricCyan else Color.Transparent)
                                            .clickable { selectedHorizonHours = hours }
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${hours}H",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 10.sp
                                            ),
                                            color = if (isSelected) DeepNavyBg else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        HourlyTemperatureLineChart(hourlyList = displayHourlyList)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceNavy)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    HourlyRainfallBarChart(hourlyList = displayHourlyList)
                }
            }

            // GIS Radar Quick Launcher Tile
            GisRadarLauncherBanner(onOpenRadar = onNavigateToGisRadar)

            Spacer(modifier = Modifier.height(36.dp))
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DeepNavyBg.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ElectricCyan)
            }
        }

        // =====================================================================
        // HYPERLOCAL INTELLIGENCE MODALS (SIH 2026 PS-68)
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

@Composable
private fun QuickPromptStrip(
    onPromptClick: (String) -> Unit
) {
    val prompts = listOf(
        "Will it rain in the next 6 hours?",
        "Aviation: Is flight departure clear of turbulence?",
        "Marine: Wave height and sea state conditions",
        "NWP Model: Compare GFS vs WRF consensus",
        "Is it safe to travel outdoors today?",
        "Farmer: Is today safe for pesticide spraying?",
        "Check urban flood & waterlogging risk",
        "Heat index & UV safety advisory"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "AI INTELLIGENCE QUICK PROMPTS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                fontSize = 10.sp
            ),
            color = TextTertiary
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            prompts.forEach { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SurfaceCard)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(50))
                        .clickable { onPromptClick(prompt) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = prompt,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = TextHighlight
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GisRadarLauncherBanner(
    onOpenRadar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceNavy)
            .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            .clickable { onOpenRadar() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "GIS Radar",
                        tint = ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Interactive GIS Radar & Satellite",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Live reflectivity sweep, Doppler winds & cyclones",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = CyanAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
