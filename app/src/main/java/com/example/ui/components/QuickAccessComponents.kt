package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AirportWeather
import com.example.data.model.ClimateTrendData
import com.example.data.model.DisasterImpactAssessment
import com.example.data.model.MarineWeather
import com.example.data.model.NwpModelComparison
import com.example.data.model.SmartCityWeatherRisk
import com.example.data.model.WeatherData
import com.example.service.ImpactGptEngine
import com.example.service.ImpactSector
import com.example.service.QuickAccessWeatherEngine
import com.example.service.SectorImpactAssessment
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanLight
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

enum class QuickAccessTab(val title: String, val icon: ImageVector) {
    NONE("None", Icons.Default.Close),
    AVIATION("Aviation", Icons.Default.Flight),
    MARINE("Marine", Icons.Default.Sailing),
    NWP_MODELS("Model Center", Icons.Default.Science),
    CLIMATE("Climate", Icons.Default.History),
    SMART_CITY("Smart City", Icons.Default.Apartment),
    DISASTER_IMPACT("ImpactGPT ⚡", Icons.Default.Warning)
}

@Composable
fun QuickAccessStrip(
    selectedTab: QuickAccessTab,
    onTabSelected: (QuickAccessTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "QUICK ACCESS DOMAIN INTELLIGENCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        fontSize = 10.5.sp
                    ),
                    color = CyanLight
                )
            }
            if (selectedTab != QuickAccessTab.NONE) {
                Text(
                    text = "Close panel ✕",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    ),
                    color = AlertOrange,
                    modifier = Modifier.clickable { onTabSelected(QuickAccessTab.NONE) }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                QuickAccessTab.AVIATION,
                QuickAccessTab.MARINE,
                QuickAccessTab.NWP_MODELS,
                QuickAccessTab.CLIMATE,
                QuickAccessTab.SMART_CITY,
                QuickAccessTab.DISASTER_IMPACT
            )

            tabs.forEach { tab ->
                val isSelected = selectedTab == tab
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else SurfaceCard)
                        .border(
                            width = if (isSelected) 1.2.dp else 0.6.dp,
                            color = if (isSelected) ElectricCyan else SurfaceBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            if (isSelected) onTabSelected(QuickAccessTab.NONE) else onTabSelected(tab)
                        }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = null,
                        tint = if (isSelected) ElectricCyan else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) Color.White else TextHighlight
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAccessPanel(
    selectedTab: QuickAccessTab,
    currentWeather: WeatherData,
    cityName: String,
    onClose: () -> Unit,
    onNavigateToGisRadar: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = selectedTab != QuickAccessTab.NONE) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(SurfaceNavy)
                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = selectedTab.icon,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = selectedTab.title.uppercase(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 13.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "Specialized Domain Intelligence Module",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextTertiary
                            )
                        }
                    }

                    IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                when (selectedTab) {
                    QuickAccessTab.AVIATION -> AviationWeatherModule(currentWeather = currentWeather, cityName = cityName)
                    QuickAccessTab.MARINE -> MarineWeatherModule(currentWeather = currentWeather)
                    QuickAccessTab.NWP_MODELS -> NwpModelCenterModule(currentWeather = currentWeather, cityName = cityName)
                    QuickAccessTab.CLIMATE -> ClimateIntelligenceModule(cityName = cityName)
                    QuickAccessTab.SMART_CITY -> SmartCityWeatherModule(
                        currentWeather = currentWeather,
                        cityName = cityName,
                        onOpenGisMap = onNavigateToGisRadar
                    )
                    QuickAccessTab.DISASTER_IMPACT -> DisasterImpactModule(
                        currentWeather = currentWeather,
                        cityName = cityName,
                        onOpenGisMap = onNavigateToGisRadar
                    )
                    QuickAccessTab.NONE -> {}
                }
            }
        }
    }
}

@Composable
fun AviationWeatherCard(
    currentWeather: WeatherData,
    cityName: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        AviationWeatherModule(currentWeather = currentWeather, cityName = cityName)
    }
}

@Composable
fun AviationWeatherModule(
    currentWeather: WeatherData,
    cityName: String
) {
    var selectedAirportCode by remember { mutableStateOf("DEL") }
    val airportWeather = remember(selectedAirportCode, currentWeather) {
        QuickAccessWeatherEngine.computeAirportWeather(selectedAirportCode, currentWeather, cityName)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Airport Selector Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickAccessWeatherEngine.airports.forEach { (code, fullName) ->
                val isSelected = selectedAirportCode == code
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ElectricCyan else SurfaceCard)
                        .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        .clickable { selectedAirportCode = code }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = code,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (isSelected) DeepNavyBg else TextHighlight
                    )
                }
            }
        }

        Text(
            text = airportWeather.airportName,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp),
            color = Color.White
        )

        // Metrics Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DomainMetricPill(label = "VISIBILITY", value = "${airportWeather.visibilityKm} km", sub = "Runway Visual", modifier = Modifier.weight(1f))
            DomainMetricPill(label = "CLOUD CEILING", value = "${airportWeather.cloudCeilingFt} ft", sub = "SCT Layer", modifier = Modifier.weight(1f))
            DomainMetricPill(label = "CROSSWIND", value = "${airportWeather.crosswindKmh.toInt()} km/h", sub = "Runway Axis", modifier = Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DomainMetricPill(
                label = "THUNDERSTORM",
                value = airportWeather.thunderstormRisk,
                sub = "Convective",
                valueColor = if (airportWeather.thunderstormRisk == "High") AlertRed else AlertGreen,
                modifier = Modifier.weight(1f)
            )
            DomainMetricPill(label = "TURBULENCE", value = airportWeather.turbulenceIndication, sub = "Boundary Layer", modifier = Modifier.weight(1f))
            DomainMetricPill(
                label = "FLIGHT RISK",
                value = airportWeather.flightWeatherRisk,
                sub = "Status",
                valueColor = if (airportWeather.flightWeatherRisk == "LOW") AlertGreen else AlertOrange,
                modifier = Modifier.weight(1f)
            )
        }

        // AI Briefing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
                .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "AI AVIATION BRIEFING",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                    color = CyanLight
                )
                Text(
                    text = airportWeather.aiBriefing,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                    color = TextHighlight
                )
            }
        }

        // Raw METAR & TAF toggle
        var showRaw by remember { mutableStateOf(false) }
        Text(
            text = if (showRaw) "Hide Raw METAR / TAF ▲" else "View Raw METAR / TAF ▼",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
            color = CyanAccent,
            modifier = Modifier.clickable { showRaw = !showRaw }
        )
        if (showRaw) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepNavyBg)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(text = airportWeather.metarRaw, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextSecondary)
                Text(text = airportWeather.tafRaw, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary)
            }
        }

        // Data Provenance
        ProvenanceFooter(source = airportWeather.source, updated = airportWeather.updatedAt, confidence = "${airportWeather.confidenceScore}%")
    }
}

@Composable
private fun MarineWeatherModule(currentWeather: WeatherData) {
    var selectedZone by remember { mutableStateOf(QuickAccessWeatherEngine.coastalZones[0].first) }
    val marineWeather = remember(selectedZone, currentWeather) {
        QuickAccessWeatherEngine.computeMarineWeather(selectedZone, currentWeather)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Coastal Zone Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuickAccessWeatherEngine.coastalZones.forEach { (zone, _) ->
                val isSelected = selectedZone == zone
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) CyanAccent else SurfaceCard)
                        .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        .clickable { selectedZone = zone }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = zone.substringAfter(" - "),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                        color = if (isSelected) DeepNavyBg else TextHighlight
                    )
                }
            }
        }

        Text(
            text = "${marineWeather.coastalZoneName} (${marineWeather.seaArea})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
            color = Color.White
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DomainMetricPill(label = "WAVE HEIGHT", value = "${marineWeather.waveHeightM} m", sub = "Significant", modifier = Modifier.weight(1f))
            DomainMetricPill(label = "SWELL", value = "${marineWeather.swellHeightM} m", sub = "${marineWeather.swellPeriodSec}s Period", modifier = Modifier.weight(1f))
            DomainMetricPill(label = "SEA TEMP", value = "${marineWeather.seaSurfaceTempC}°C", sub = "Surface SST", modifier = Modifier.weight(1f))
        }

        // Fishing Safety Banner
        val (safetyColor, safetyBg) = when {
            marineWeather.fishingSafety.contains("SAFE") -> Pair(AlertGreen, AlertGreen.copy(alpha = 0.15f))
            marineWeather.fishingSafety.contains("CAUTION") -> Pair(AlertOrange, AlertOrange.copy(alpha = 0.15f))
            else -> Pair(AlertRed, AlertRed.copy(alpha = 0.15f))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(safetyBg)
                .border(1.dp, safetyColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "FISHING & MARINE SAFETY", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), color = safetyColor)
                    Text(text = marineWeather.fishingSafety, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = safetyColor)
                }
                Text(
                    text = marineWeather.aiAdvisory,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                    color = TextHighlight
                )
            }
        }

        ProvenanceFooter(source = marineWeather.source, updated = marineWeather.updatedAt, confidence = "${marineWeather.confidenceScore}%")
    }
}

@Composable
private fun NwpModelCenterModule(
    currentWeather: WeatherData,
    cityName: String
) {
    val modelData = remember(cityName, currentWeather) {
        QuickAccessWeatherEngine.computeNwpModelComparison(cityName, currentWeather)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Disagreement Warning Banner if spread is high
        if (modelData.isDisagreementHigh || modelData.modelAgreementPercent < 70) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AlertOrange.copy(alpha = 0.15f))
                    .border(1.dp, AlertOrange.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = AlertOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "⚠️ Forecast uncertainty HIGH",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            color = AlertOrange
                        )
                        Text(
                            text = "Ensemble models diverge on precipitation boundary-layer flux. Exercise caution for critical operations.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "ENSEMBLE MODEL AGREEMENT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = CyanLight)
            Text(
                text = "${modelData.modelAgreementPercent}% Consensus",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = if (modelData.isDisagreementHigh) AlertOrange else AlertGreen
            )
        }

        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "PARAMETER", modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), color = TextTertiary)
            Text(text = "GFS (12km)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), color = CyanAccent)
            Text(text = "WRF (3km)", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), color = ElectricTeal)
            Text(text = "IMD AWS", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), color = ElectricCyan)
        }

        // Table Rows
        ModelComparisonRow(param = "Rainfall (24h)", gfs = "${modelData.gfsRainMm} mm", wrf = "${modelData.wrfRainMm} mm", imd = "${modelData.imdRainMm} mm")
        ModelComparisonRow(param = "Temperature", gfs = "${modelData.gfsTempC}°C", wrf = "${modelData.wrfTempC}°C", imd = "${modelData.imdTempC}°C")
        ModelComparisonRow(param = "Wind Velocity", gfs = "${modelData.gfsWindKmh.toInt()} km/h", wrf = "${modelData.wrfWindKmh.toInt()} km/h", imd = "${modelData.imdWindKmh.toInt()} km/h")
        ModelComparisonRow(param = "Rel Humidity", gfs = "${modelData.gfsHumidity}%", wrf = "${modelData.wrfHumidity}%", imd = "${modelData.imdHumidity}%")

        // Observation & Remote Sensing Verification
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "REMOTE SENSING & RADAR OBSERVATION", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold), color = CyanAccent)
            Text(text = "🛰️ ${modelData.satelliteStatus}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color.White)
            Text(text = "📡 ${modelData.radarStatus}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextHighlight)
        }

        // AI Comparison Analysis
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "AI MODEL DISCREPANCY ANALYSIS", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold), color = CyanLight)
                Text(text = modelData.aiComparisonAnalysis, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp), color = TextHighlight)
            }
        }

        ProvenanceFooter(source = modelData.source, updated = modelData.updatedAt, confidence = modelData.forecastConfidence)
    }
}

@Composable
private fun ModelComparisonRow(param: String, gfs: String, wrf: String, imd: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = param, modifier = Modifier.weight(1.2f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium, fontSize = 11.sp), color = TextPrimary)
        Text(text = gfs, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextHighlight)
        Text(text = wrf, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextHighlight)
        Text(text = imd, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = Color.White)
    }
}

@Composable
private fun ClimateIntelligenceModule(cityName: String) {
    var selectedParam by remember { mutableStateOf("Temperature") }
    var selectedYears by remember { mutableStateOf(10) }

    val climateData = remember(cityName, selectedParam, selectedYears) {
        QuickAccessWeatherEngine.computeClimateTrend(cityName, selectedParam, selectedYears)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Param Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Temperature", "Rainfall", "Extreme Events").forEach { p ->
                val isSelected = selectedParam == p
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) ElectricCyan else SurfaceCard)
                        .clickable { selectedParam = p }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = p,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                        color = if (isSelected) DeepNavyBg else TextHighlight
                    )
                }
            }
        }

        // Years Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "HISTORICAL TIME HORIZON:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(5, 10, 20, 50).forEach { y ->
                    val isSel = selectedYears == y
                    Text(
                        text = "${y}Y",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal, fontSize = 10.5.sp),
                        color = if (isSel) ElectricCyan else TextSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSel) ElectricCyan.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { selectedYears = y }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Metrics Summary
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val sign = if (climateData.anomalyValue > 0) "+" else ""
            DomainMetricPill(label = "ANOMALY", value = "$sign${climateData.anomalyValue}", sub = "vs Baseline", valueColor = AlertOrange, modifier = Modifier.weight(1f))
            DomainMetricPill(label = "TREND", value = climateData.trendDirection.take(14), sub = "Decadal Rate", modifier = Modifier.weight(1.3f))
        }

        // Interactive Historical Trend Graph (Compose Recharts-style interactive charting)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceCard)
                .border(0.5.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            HistoricalTemperaturePatternChart(
                trendPoints = climateData.trendPoints,
                baselineTemp = climateData.historicalBaseline,
                parameterName = selectedParam,
                unit = if (selectedParam == "Temperature") "°C" else if (selectedParam == "Rainfall") "mm" else "ev"
            )
        }

        // AI Explanation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard)
                .padding(10.dp)
        ) {
            Text(
                text = climateData.aiClimateExplanation,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                color = TextHighlight
            )
        }

        ProvenanceFooter(source = climateData.source, updated = "Climatological Normal", confidence = "${climateData.confidenceScore}%")
    }
}

@Composable
private fun SmartCityWeatherModule(
    currentWeather: WeatherData,
    cityName: String,
    onOpenGisMap: () -> Unit
) {
    val smartCity = remember(cityName, currentWeather) {
        QuickAccessWeatherEngine.computeSmartCityRisk(cityName, currentWeather)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DomainMetricPill(
                label = "WATERLOGGING",
                value = smartCity.urbanFloodRisk,
                sub = "Underpass Status",
                valueColor = if (smartCity.urbanFloodRisk == "HIGH") AlertRed else AlertGreen,
                modifier = Modifier.weight(1f)
            )
            DomainMetricPill(label = "DRAINAGE LOAD", value = "${smartCity.drainageLoadPercent}%", sub = "Capacity Used", modifier = Modifier.weight(1f))
            DomainMetricPill(label = "TRAFFIC IMPACT", value = smartCity.trafficImpact.take(8), sub = "Arterial Delay", modifier = Modifier.weight(1f))
        }

        // Action advisory
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard)
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "MUNICIPAL COMMAND ACTION ADVISORY", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold), color = AlertYellow)
                Text(text = smartCity.actionAdvisory, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp), color = TextHighlight)
            }
        }

        // View Risk Map Button (reusing existing GIS map)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ElectricCyan.copy(alpha = 0.15f))
                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .clickable { onOpenGisMap() }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                Text(text = "VIEW URBAN FLOOD & DRAINAGE GIS LAYER", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = CyanLight)
            }
        }

        ProvenanceFooter(source = smartCity.source, updated = smartCity.updatedAt, confidence = "Operational C4i")
    }
}

@Composable
private fun DisasterImpactModule(
    currentWeather: WeatherData,
    cityName: String,
    onOpenGisMap: () -> Unit
) {
    var activeSubMode by remember { mutableStateOf("ImpactGPT") } // "ImpactGPT" or "Disaster"
    var selectedSector by remember { mutableStateOf(ImpactSector.ROADS) }
    val sectorAssessments = remember(cityName, currentWeather) {
        ImpactGptEngine.evaluateSectorImpacts(currentWeather)
    }
    val currentSectorAssessment = remember(selectedSector, sectorAssessments) {
        sectorAssessments.firstOrNull { it.sector == selectedSector } ?: sectorAssessments.first()
    }

    var selectedEvent by remember { mutableStateOf("Cyclone") }
    val impact = remember(selectedEvent, cityName, currentWeather) {
        QuickAccessWeatherEngine.computeDisasterImpact(selectedEvent, cityName, currentWeather)
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Mode Switcher: ImpactGPT Sectors vs Disaster Scenarios
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard)
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeSubMode == "ImpactGPT") ElectricCyan.copy(alpha = 0.2f) else Color.Transparent)
                    .border(if (activeSubMode == "ImpactGPT") 1.dp else 0.dp, if (activeSubMode == "ImpactGPT") ElectricCyan else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { activeSubMode = "ImpactGPT" }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IMPACTGPT ⚡ (9 SECTORS)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                    color = if (activeSubMode == "ImpactGPT") ElectricCyan else TextTertiary
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (activeSubMode == "Disaster") AlertRed.copy(alpha = 0.2f) else Color.Transparent)
                    .border(if (activeSubMode == "Disaster") 1.dp else 0.dp, if (activeSubMode == "Disaster") AlertRed else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { activeSubMode = "Disaster" }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "DISASTER SCENARIOS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                    color = if (activeSubMode == "Disaster") AlertRed else TextTertiary
                )
            }
        }

        if (activeSubMode == "ImpactGPT") {
            // Sector Selector Chips
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ImpactSector.values().forEach { sec ->
                    val isSel = selectedSector == sec
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) CyanLight.copy(alpha = 0.25f) else SurfaceCard)
                            .border(1.dp, if (isSel) CyanLight else SurfaceBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedSector = sec }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = sec.icon,
                                contentDescription = null,
                                tint = if (isSel) CyanLight else TextTertiary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = sec.titleEn,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = if (isSel) Color.White else TextHighlight
                            )
                        }
                    }
                }
            }

            // Impact Decision Card: WEATHER SIGNAL → RISK → AFFECTED SECTOR → ACTION
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceNavy)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = currentSectorAssessment.sector.icon,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${currentSectorAssessment.sector.titleEn.uppercase()} IMPACT ANALYSIS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = CyanLight
                            )
                        }
                        val riskColor = when (currentSectorAssessment.riskSeverity) {
                            "CRITICAL" -> AlertRed
                            "HIGH" -> AlertOrange
                            "MODERATE" -> AlertYellow
                            else -> AlertGreen
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(riskColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "RISK: ${currentSectorAssessment.riskSeverity}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                                color = riskColor
                            )
                        }
                    }

                    // Structured Chain Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCard)
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "WEATHER SIGNAL:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), color = TextTertiary)
                            Text(text = currentSectorAssessment.weatherSignal, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "→ RISK:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), color = AlertOrange)
                            Text(text = "${currentSectorAssessment.riskSeverity} (${currentSectorAssessment.riskDescription})", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextHighlight)
                        }
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "→ AFFECTED SECTOR:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), color = ElectricCyan)
                            Text(text = "${currentSectorAssessment.sector.titleEn} • ${currentSectorAssessment.affectedAreaOrEntity}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "→ ACTION:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp), color = AlertGreen)
                            Text(text = currentSectorAssessment.actionableDirective, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold), color = AlertGreen)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Confidence: ${currentSectorAssessment.confidenceScore}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = AlertGreen
                        )
                        Text(
                            text = "Prov: C4i Impact Engine • IMD / NDMA Protocols",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = TextTertiary
                        )
                    }
                }
            }
        } else {
            // Disaster Scenarios (Cyclone, Urban Flood, Lightning)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Cyclone", "Urban Flood", "Lightning").forEach { ev ->
                    val isSel = selectedEvent == ev
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) AlertRed.copy(alpha = 0.25f) else SurfaceCard)
                            .border(1.dp, if (isSel) AlertRed else SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable { selectedEvent = ev }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ev,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                            color = if (isSel) Color.White else TextHighlight
                        )
                    }
                }
            }

            Text(text = impact.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = Color.White)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DomainMetricPill(label = "SEVERITY", value = impact.severity, sub = "NDRF Category", valueColor = AlertRed, modifier = Modifier.weight(1f))
                DomainMetricPill(label = "RISK RADIUS", value = "${impact.riskRadiusKm.toInt()} km", sub = "Exposure Zone", modifier = Modifier.weight(1f))
                DomainMetricPill(label = "TIME WINDOW", value = impact.timeWindow, sub = "Action Window", modifier = Modifier.weight(1.3f))
            }

            // Detailed Exposure Rows
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceCard)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(text = "POTENTIAL IMPACT & EXPOSURE ASSESSMENT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold), color = AlertOrange)
                ImpactSubRow(label = "Population:", value = impact.populationExposure)
                ImpactSubRow(label = "Agriculture:", value = impact.agricultureExposure)
                ImpactSubRow(label = "Critical Infra:", value = impact.infrastructureExposure)
                ImpactSubRow(label = "Highways / Roads:", value = impact.roadsAtRisk)
            }

            // Action Recommendation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AlertRed.copy(alpha = 0.12f))
                    .border(0.8.dp, AlertRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(text = "RECOMMENDED CIVIL PROTECTION ACTION", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold), color = AlertRed)
                    Text(text = impact.recommendedAction, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp), color = Color.White)
                }
            }

            // View on GIS Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .clickable { onOpenGisMap() }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Text(text = "VIEW DISASTER FOOTPRINT ON GIS RADAR", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = CyanLight)
                }
            }

            ProvenanceFooter(source = impact.source, updated = "Real-Time Early Warning", confidence = "${impact.confidenceScore}%")
        }
    }
}

@Composable
private fun ImpactSubRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp), color = TextTertiary)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp), color = TextHighlight)
    }
}

@Composable
private fun DomainMetricPill(
    label: String,
    value: String,
    sub: String,
    valueColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(8.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = TextTertiary)
            Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp), color = valueColor)
            Text(text = sub, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp), color = TextSecondary)
        }
    }
}

@Composable
private fun ProvenanceFooter(source: String, updated: String, confidence: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(DeepNavyBg)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "SOURCE: $source", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp), color = TextTertiary)
        Text(text = "CONF: $confidence", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold), color = ElectricCyan)
    }
}
