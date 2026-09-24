package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
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
import com.example.service.HapticManager
import com.example.data.model.IndianLanguage
import com.example.service.AgrometRiskItem
import com.example.service.AgrometRiskLevel
import com.example.service.CropGrowthStage
import com.example.service.CropProfile
import com.example.service.FarmOperationWindow
import com.example.service.FarmProfile
import com.example.service.IrrigationMethod
import com.example.service.KrishiDecisionResult
import com.example.service.KrishiGptEngine
import com.example.service.SoilType
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
import com.example.ui.viewmodel.WeatherGPTViewModel
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun KrishiScreen(
    viewModel: WeatherGPTViewModel,
    onNavigateToChatWithQuery: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val weather by viewModel.weatherData.collectAsState()
    val canonicalSnapshot by viewModel.canonicalSnapshot.collectAsState()
    val forecast24H by viewModel.forecast24H.collectAsState()
    val forecast48H by viewModel.forecast48H.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val scrollState = rememberScrollState()

    // Step 1: Farm Profile state
    var selectedCrop by remember { mutableStateOf(KrishiGptEngine.availableCrops.first()) }
    var selectedVariety by remember(selectedCrop) { mutableStateOf(selectedCrop.varieties.first()) }
    var selectedStage by remember { mutableStateOf(CropGrowthStage.VEGETATIVE) }
    var selectedIrrigation by remember { mutableStateOf(IrrigationMethod.FLOOD_FURROW) }
    var selectedSoil by remember { mutableStateOf(SoilType.ALLUVIAL_LOAM) }

    val farmProfile = remember(selectedCrop, selectedVariety, selectedStage, weather.cityName, selectedIrrigation, selectedSoil) {
        FarmProfile(
            crop = selectedCrop,
            variety = selectedVariety,
            stage = selectedStage,
            locationName = weather.cityName,
            irrigationMethod = selectedIrrigation,
            soilType = selectedSoil
        )
    }

    // Step 3: Agromet Decision Engine execution using real weather snapshot & NWP
    val krishiDecision: KrishiDecisionResult = remember(farmProfile, weather, forecast24H, forecast48H) {
        KrishiGptEngine.evaluateAgrometDecision(
            profile = farmProfile,
            weather = weather,
            forecast24H = forecast24H,
            forecast48H = forecast48H
        )
    }

    val context = LocalContext.current
    LaunchedEffect(farmProfile.crop.id, farmProfile.variety, krishiDecision.recommendedAction) {
        val maxRisk = listOf(
            krishiDecision.riskScorecard.sprayRisk.level,
            krishiDecision.riskScorecard.irrigationRisk.level,
            krishiDecision.riskScorecard.rainRisk.level,
            krishiDecision.riskScorecard.heatRisk.level,
            krishiDecision.riskScorecard.diseaseWeatherRisk.level
        ).maxByOrNull { it.ordinal }?.name ?: AgrometRiskLevel.LOW.name

        HapticManager.onKrishiAdvisoryReady(
            context = context,
            cropId = farmProfile.crop.id,
            riskLevelString = maxRisk,
            location = farmProfile.locationName
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KrishiGPT Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AlertGreen.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = "Krishi",
                        tint = AlertGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "KRISHIGPT 🌾 AGRO-INTELLIGENCE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Decision-Support Workflow • ${weather.cityName}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        color = TextSecondary
                    )
                }
            }
        }

        // =====================================================================
        // STEP 1 — FARM PROFILE SETUP
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, AlertGreen.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP 1: FARM PROFILE",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp),
                    color = AlertGreen
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(12.dp))
                    Text(
                        text = weather.cityName,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                        color = ElectricCyan
                    )
                }
            }

            // 1. Target Crop
            Text(text = "Target Crop:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp), color = TextTertiary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                KrishiGptEngine.availableCrops.forEach { crop ->
                    val isSel = selectedCrop.id == crop.id
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) AlertGreen.copy(alpha = 0.25f) else SurfaceCard)
                            .border(1.dp, if (isSel) AlertGreen else SurfaceBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                HapticManager.selectionChanged(context)
                                selectedCrop = crop
                                selectedVariety = crop.varieties.first()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = crop.iconEmoji, fontSize = 13.sp)
                            Text(
                                text = crop.nameEn,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                color = if (isSel) Color.White else TextHighlight
                            )
                        }
                    }
                }
            }

            // 2. Crop Variety
            Text(text = "Variety:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp), color = TextTertiary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                selectedCrop.varieties.forEach { variety ->
                    val isSel = selectedVariety == variety
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) ElectricCyan.copy(alpha = 0.2f) else SurfaceCard)
                            .border(1.dp, if (isSel) ElectricCyan else SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                HapticManager.selectionChanged(context)
                                selectedVariety = variety
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = variety,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                            color = if (isSel) Color.White else TextSecondary
                        )
                    }
                }
            }

            // 3. Phenology / Growth Stage
            Text(text = "Growth Stage:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp), color = TextTertiary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CropGrowthStage.values().forEach { stage ->
                    val isSel = selectedStage == stage
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSel) CyanLight.copy(alpha = 0.2f) else SurfaceCard)
                            .border(1.dp, if (isSel) CyanLight else SurfaceBorder, RoundedCornerShape(16.dp))
                            .clickable {
                                HapticManager.selectionChanged(context)
                                selectedStage = stage
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = stage.labelEn,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = if (isSel) Color.White else TextSecondary
                        )
                    }
                }
            }

            // 4. Irrigation Method & Soil Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Irrigation Method:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp), color = TextTertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IrrigationMethod.values().forEach { method ->
                            val isSel = selectedIrrigation == method
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) ElectricTeal.copy(alpha = 0.25f) else SurfaceCard)
                                    .border(0.5.dp, if (isSel) ElectricTeal else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        HapticManager.selectionChanged(context)
                                        selectedIrrigation = method
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = method.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSel) TextHighlight else TextSecondary
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Soil Type:", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp), color = TextTertiary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SoilType.values().forEach { soil ->
                            val isSel = selectedSoil == soil
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) AlertYellow.copy(alpha = 0.25f) else SurfaceCard)
                                    .border(0.5.dp, if (isSel) AlertYellow else SurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        HapticManager.selectionChanged(context)
                                        selectedSoil = soil
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = soil.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                                    color = if (isSel) TextHighlight else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // =====================================================================
        // STEP 2 — WEATHER CONTEXT (AUTOMATIC CANONICAL SNAPSHOT)
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STEP 2: WEATHER CONTEXT (CANONICAL SNAPSHOT)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp),
                    color = ElectricCyan
                )
                Text(
                    text = "Single Source of Truth",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextTertiary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeatherMetricPill("Temp", "${weather.temperatureC.roundToInt()}°C", "Feels ${weather.feelsLikeC.roundToInt()}°")
                WeatherMetricPill("Rain Prob", "${weather.rainProbabilityPercent}%", String.format(Locale.US, "%.1f mm", weather.expectedRainfallMm))
                WeatherMetricPill("Humidity", "${weather.humidityPercent}%", "Dew pt")
                WeatherMetricPill("Wind", "${weather.windSpeedKmh.roundToInt()} km/h", weather.windDirectionText)
            }

            // Short summary line
            Text(
                text = "• Telemetry: ${krishiDecision.weatherSummary}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                color = TextSecondary
            )
        }

        // =====================================================================
        // STEP 3 & 4: TODAY'S FARM PLAN (ACTIONABLE HOURLY OPERATIONAL WINDOWS)
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, AlertGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = AlertGreen, modifier = Modifier.size(16.dp))
                    Text(
                        text = "TODAY'S FARM PLAN (HOURLY WINDOWS)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp),
                        color = AlertGreen
                    )
                }
                Text(
                    text = "Operational Guidance",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextTertiary
                )
            }

            krishiDecision.todaysFarmPlan.forEach { plan ->
                FarmPlanWindowCard(plan = plan)
            }
        }

        // =====================================================================
        // TRANSPARENT AGRICULTURAL RISK SCORES (LOW, MODERATE, HIGH)
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSPARENT AGRICULTURAL RISK SCORES",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp),
                    color = ElectricCyan
                )
                Text(
                    text = "Risk Matrix",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextTertiary
                )
            }

            val card = krishiDecision.riskScorecard
            AgrometRiskRow(item = card.sprayRisk)
            AgrometRiskRow(item = card.irrigationRisk)
            AgrometRiskRow(item = card.rainRisk)
            AgrometRiskRow(item = card.heatRisk)
            AgrometRiskRow(item = card.diseaseWeatherRisk)
        }

        // =====================================================================
        // 48-HOUR EXTENDED FARM PLANNING & RESOURCE OPTIMIZATION
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, ElectricTeal.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "48H EXTENDED AGRO-PLANNING",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp),
                    color = ElectricTeal
                )
                Text(
                    text = "24–48h Horizon",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextTertiary
                )
            }

            val p48 = krishiDecision.next48HPlan
            PlanningItemCard("🌧 Rain Opportunity", p48.rainOpportunity)
            PlanningItemCard("🧪 Spraying Opportunity", p48.sprayingOpportunity)
            PlanningItemCard("💧 Irrigation Opportunity", p48.irrigationOpportunity)
            PlanningItemCard("🚜 Harvest Risk Window", p48.harvestRisk)
            PlanningItemCard("🦠 Disease-Weather Trend", p48.diseaseWeatherTrend)
        }

        // =====================================================================
        // DETAILED DOMAIN ADVISORIES (IRRIGATION, SPRAY, HARVEST, PESTS)
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "OPERATIONAL AGRO-ADVISORY PANELS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp),
                color = TextPrimary
            )

            // Irrigation Card
            AdvisoryPanel(
                icon = Icons.Default.WaterDrop,
                title = "IRRIGATION ADVISORY",
                status = krishiDecision.irrigationAdvisory,
                reason = krishiDecision.irrigationReason,
                tint = CyanLight
            )

            // Spray Card
            AdvisoryPanel(
                icon = Icons.Default.Science,
                title = "SPRAY ADVISORY",
                status = krishiDecision.sprayAdvisory,
                reason = krishiDecision.sprayReason,
                tint = AlertYellow
            )

            // Phenology Sowing / Harvest Card
            AdvisoryPanel(
                icon = Icons.Default.Spa,
                title = "${selectedStage.labelEn.uppercase()} GUIDANCE",
                status = krishiDecision.sowingHarvestingWindow,
                reason = krishiDecision.sowingHarvestingReason,
                tint = AlertGreen
            )

            // Pest & Disease Protocol
            AdvisoryPanel(
                icon = Icons.Default.Warning,
                title = "PEST & DISEASE WEATHER PROTOCOL",
                status = krishiDecision.pestDiseaseRisk,
                reason = krishiDecision.pestManagementAction,
                tint = AlertOrange
            )
        }

        // Gemini AI Interactive Consultation Prompt
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ElectricCyan.copy(alpha = 0.12f))
                .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .clickable {
                    onNavigateToChatWithQuery("Give me detailed agricultural guidance for ${selectedCrop.nameEn} (${selectedVariety}) at ${selectedStage.labelEn} stage in ${weather.cityName} based on current weather.")
                }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Column {
                        Text(
                            text = "Ask Gemini Krishi Assistant",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Natural language explanation for ${selectedCrop.nameEn}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextSecondary
                        )
                    }
                }
                Text(
                    text = "Ask AI →",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricCyan
                )
            }
        }

        // Truthful Provenance Footer
        Text(
            text = "Provenance: ${krishiDecision.sourceProvenance} • Calibrated for ${selectedCrop.nameEn}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = TextTertiary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun FarmPlanWindowCard(plan: FarmOperationWindow) {
    val statusColor = when (plan.status) {
        "FAVORABLE", "RECOMMENDED" -> AlertGreen
        "AVOID" -> AlertRed
        else -> AlertYellow
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard.copy(alpha = 0.6f))
            .border(0.5.dp, statusColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                Text(
                    text = plan.timeWindow,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                    color = TextHighlight
                )
                Text(
                    text = "• ${plan.operation}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusColor.copy(alpha = 0.18f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = plan.status,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp),
                    color = statusColor
                )
            }
        }

        Text(
            text = "Why: ${plan.whyReason}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, lineHeight = 15.sp),
            color = TextSecondary
        )
        Text(
            text = "Confidence: ${plan.confidence}",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            color = TextTertiary
        )
    }
}

@Composable
private fun AgrometRiskRow(item: AgrometRiskItem) {
    val (badgeTint, bgTint) = when (item.level) {
        AgrometRiskLevel.HIGH -> AlertRed to AlertRed.copy(alpha = 0.15f)
        AgrometRiskLevel.MODERATE -> AlertYellow to AlertYellow.copy(alpha = 0.15f)
        AgrometRiskLevel.LOW -> AlertGreen to AlertGreen.copy(alpha = 0.15f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = item.contributingFactor,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bgTint)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = item.level.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp),
                color = badgeTint
            )
        }
    }
}

@Composable
private fun PlanningItemCard(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard.copy(alpha = 0.5f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = TextHighlight
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, lineHeight = 15.sp),
            color = TextSecondary
        )
    }
}

@Composable
private fun AdvisoryPanel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    status: String,
    reason: String,
    tint: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard.copy(alpha = 0.5f))
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
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
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = tint
                )
            }
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp),
                color = TextHighlight
            )
        }
        Text(
            text = reason,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, lineHeight = 15.sp),
            color = TextSecondary
        )
    }
}

@Composable
private fun WeatherMetricPill(title: String, value: String, sub: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighlight)
        Text(text = sub, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp), color = TextSecondary)
    }
}
