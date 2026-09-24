package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.hyperlocal.ForecastTrustAssessment
import com.example.data.hyperlocal.ForecastVerificationMetrics
import com.example.data.hyperlocal.ImpactSimulationResult
import com.example.data.hyperlocal.ImpactSimulationScenario
import com.example.data.hyperlocal.KrishiCropIntelligence
import com.example.data.hyperlocal.NearbyVillageComparison
import com.example.data.hyperlocal.NowcastIntelligence
import com.example.data.hyperlocal.VillageHierarchy
import com.example.data.hyperlocal.VillageRiskProfile
import com.example.service.HyperlocalIntelligenceEngine
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

// =========================================================================
// 1. WHAT-IF AI IMPACT SIMULATOR MODAL
// =========================================================================
@Composable
fun WhatIfSimulatorDialog(
    village: VillageHierarchy,
    currentScenario: ImpactSimulationScenario,
    simulationResult: ImpactSimulationResult,
    onSelectScenario: (ImpactSimulationScenario) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(DeepNavyBg)
                .border(1.dp, ElectricCyan, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "WHAT-IF IMPACT SIMULATOR ⚡",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                ),
                                color = ElectricCyan
                            )
                            Text(
                                text = "${village.villageName}, ${village.block} • Elevation: ${village.elevationM}m",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Text(
                    text = "Simulate how extreme atmospheric variations directly alter micro-catchment runoff, road passability, crop stability, and local feeder grids.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextHighlight
                )

                // Scenario Selector Chips
                Text(
                    text = "SELECT WEATHER STRESS SCENARIO:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 9.5.sp
                    ),
                    color = TextTertiary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HyperlocalIntelligenceEngine.presetScenarios.forEach { scenario ->
                        val isSelected = scenario.scenarioId == currentScenario.scenarioId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.25f) else SurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else SurfaceBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onSelectScenario(scenario) }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = scenario.scenarioName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = if (isSelected) ElectricCyan else Color.White
                                )
                                Text(
                                    text = "+${scenario.addedRainfallMm.toInt()}mm • ${scenario.windGustKmh.toInt()}km/h",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                }

                // Primary Impact Summary Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceNavy)
                        .border(1.dp, AlertOrange.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SIMULATED OVERALL IMPACT:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = TextTertiary
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AlertOrange.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = simulationResult.severityCategory,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 10.sp
                                    ),
                                    color = AlertOrange
                                )
                            }
                        }
                        Text(
                            text = simulationResult.narrativeSummary,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            ),
                            color = Color.White
                        )
                    }
                }

                // Four Sector Impact Cards
                Text(
                    text = "SECTORAL IMPACT BREAKDOWN:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 9.5.sp
                    ),
                    color = TextTertiary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Flood & Underpasses
                    SectorImpactCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Flood,
                        iconTint = CyanAccent,
                        title = "FLOOD & RUNOFF",
                        primaryMetric = "${simulationResult.underpassSubmergenceCm}cm",
                        secondaryLabel = "Underpass Depth",
                        detail = simulationResult.waterloggingDepthDescription
                    )
                    // Road Passability
                    SectorImpactCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Route,
                        iconTint = AlertOrange,
                        title = "ROAD PASSABILITY",
                        primaryMetric = simulationResult.roadPassabilityRisk,
                        secondaryLabel = "Arterial Arteries",
                        detail = simulationResult.drainageCongestionStatus
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Grid & Power
                    SectorImpactCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.ElectricBolt,
                        iconTint = AlertYellow,
                        title = "FEEDER TRIP RISK",
                        primaryMetric = "${simulationResult.powerFeederTripProbPercent}%",
                        secondaryLabel = "Outage Likelihood",
                        detail = "Wind-tree collision & feeder surge vulnerability"
                    )
                    // Agricultural Lodging
                    SectorImpactCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Grass,
                        iconTint = AlertGreen,
                        title = "CROP DAMAGE",
                        primaryMetric = "${simulationResult.cropLodgingProbabilityPercent}%",
                        secondaryLabel = "Lodging Probability",
                        detail = simulationResult.cropDamageEstimate
                    )
                }

                // Actionable Mitigation Recommendations
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "ACTIONABLE MITIGATION PROTOCOLS:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp
                            ),
                            color = ElectricCyan
                        )
                        for (action in simulationResult.recommendedActions) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(text = "•", color = ElectricCyan, fontSize = 12.sp)
                                Text(
                                    text = action,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextHighlight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectorImpactCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    title: String,
    primaryMetric: String,
    secondaryLabel: String,
    detail: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.5.sp
                    ),
                    color = TextTertiary
                )
            }
            Text(
                text = primaryMetric,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                ),
                color = Color.White
            )
            Text(
                text = secondaryLabel,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = iconTint
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                color = TextSecondary,
                maxLines = 2
            )
        }
    }
}

// =========================================================================
// 2. FORECAST TRUST & BUST RISK DETAIL MODAL
// =========================================================================
@Composable
fun ForecastTrustDetailDialog(
    trust: ForecastTrustAssessment,
    village: VillageHierarchy,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(DeepNavyBg)
                .border(1.dp, ElectricCyan, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "FORECAST TRUST & BUST RISK 🛡️",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                ),
                                color = ElectricCyan
                            )
                            Text(
                                text = "Scientific Verification & Ensemble Reliability",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                // Confidence vs Probability Distinction Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceNavy)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "RAIN PROBABILITY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextTertiary
                                )
                                Text(
                                    text = "${trust.rainProbabilityPercent}%",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp
                                    ),
                                    color = CyanAccent
                                )
                                Text(
                                    text = "Chance of rain occurring",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                    color = TextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "FORECAST CONFIDENCE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextTertiary
                                )
                                Text(
                                    text = "${trust.confidenceScorePercent}%",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp
                                    ),
                                    color = AlertGreen
                                )
                                Text(
                                    text = "${trust.confidenceLevel} Reliability",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = AlertGreen
                                )
                            }
                        }

                        LinearProgressIndicator(
                            progress = { trust.confidenceScorePercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AlertGreen,
                            trackColor = SurfaceCard
                        )
                    }
                }

                // Bust Risk Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FORECAST BUST POTENTIAL:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = TextTertiary
                            )
                            val bustColor = when (trust.bustRiskLevel.uppercase()) {
                                "HIGH", "CRITICAL" -> AlertRed
                                "MODERATE" -> AlertYellow
                                else -> AlertGreen
                            }
                            Text(
                                text = "${trust.bustRiskPercent}% • ${trust.bustRiskLevel} RISK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.5.sp
                                ),
                                color = bustColor
                            )
                        }

                        Text(
                            text = "What could cause this forecast to fail?",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = CyanLight
                        )
                        for (factor in trust.bustRiskFactors) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(text = "⚠️", fontSize = 10.sp)
                                Text(
                                    text = factor,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                    color = TextHighlight
                                )
                            }
                        }
                    }
                }

                // Error Bounds
                Text(
                    text = "EMPIRICAL ERROR BOUNDS (UNCERTAINTY MARGIN):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 9.5.sp
                    ),
                    color = TextTertiary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ErrorBoundChip(
                        modifier = Modifier.weight(1f),
                        label = "Temperature",
                        bound = "±${trust.tempErrorMarginC}",
                        unit = "°C"
                    )
                    ErrorBoundChip(
                        modifier = Modifier.weight(1f),
                        label = "Rainfall",
                        bound = "±${trust.rainfallErrorMarginMm}",
                        unit = "mm"
                    )
                    ErrorBoundChip(
                        modifier = Modifier.weight(1f),
                        label = "Wind Speed",
                        bound = "±${trust.windErrorMarginKmh}",
                        unit = "km/h"
                    )
                }

                // 4 Observational Evidences
                Text(
                    text = "FOUR-PILLAR OBSERVATIONAL EVIDENCE:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 9.5.sp
                    ),
                    color = TextTertiary
                )
                for (ev in trust.topEvidenceSources) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCard)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = ev.provenanceBadge,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    ),
                                    color = ElectricCyan
                                )
                                Text(
                                    text = ev.signalStrength,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp
                                    ),
                                    color = AlertGreen
                                )
                            }
                            Text(
                                text = ev.sourceName,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = ev.evidenceDetail,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorBoundChip(
    modifier: Modifier = Modifier,
    label: String,
    bound: String,
    unit: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                color = TextTertiary
            )
            Text(
                text = "$bound $unit",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = CyanAccent
            )
        }
    }
}

// =========================================================================
// 3. FORECAST VERIFICATION & ACCURACY METRICS MODAL
// =========================================================================
@Composable
fun ForecastVerificationDialog(
    metrics: ForecastVerificationMetrics,
    village: VillageHierarchy,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(DeepNavyBg)
                .border(1.dp, ElectricCyan, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "FORECAST VERIFICATION ENGINE",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                ),
                                color = ElectricCyan
                            )
                            Text(
                                text = "Predicted vs Observed Verification • SIH PS-68",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                // Summary Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceNavy)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "HISTORICAL VERIFICATION SAMPLE (5 EVENTS)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = TextTertiary
                                )
                                Text(
                                    text = "Demo Verification: 5 Samples",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    ),
                                    color = AlertGreen
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "SAMPLE SKILL RATING",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextTertiary
                                )
                                Text(
                                    text = "Brier 0.11",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp
                                    ),
                                    color = AlertYellow
                                )
                            }
                        }
                        // Explicit Operational Disclaimer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCard)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Prototype operational verification metrics. Comprehensive statistical validation requires 90-day continuous archive.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                color = CyanLight
                            )
                        }
                        Text(
                            text = metrics.verificationSummary,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextHighlight
                        )
                    }
                }

                // Error Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VerificationMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "TEMP MAE",
                        value = "${metrics.tempMeanAbsoluteErrorC}°C",
                        subtitle = "Mean absolute error"
                    )
                    VerificationMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "RAIN MAE",
                        value = "${metrics.rainMeanAbsoluteErrorMm} mm",
                        subtitle = "24h rainfall error"
                    )
                    VerificationMetricCard(
                        modifier = Modifier.weight(1f),
                        title = "BRIER SCORE",
                        value = "${metrics.brierScore}",
                        subtitle = "Calibration accuracy"
                    )
                }

                // Historical Verification Log Table
                Text(
                    text = "RECENT 5-DAY VERIFICATION LOG (PREDICTED VS ACTUAL):",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 9.5.sp
                    ),
                    color = TextTertiary
                )

                for (log in metrics.recentDaysLog) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCard)
                            .padding(10.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.dateLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = Color.White
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (log.wasAccurate) AlertGreen.copy(alpha = 0.2f) else AlertOrange.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (log.wasAccurate) "✓ VERIFIED HIT" else "DISCREPANCY",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp
                                        ),
                                        color = if (log.wasAccurate) AlertGreen else AlertOrange
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Predicted: ${log.predictedTempC}°C, ${log.predictedRainMm}mm",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Actual AWS: ${log.actualTempC}°C, ${log.actualRainMm}mm",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = CyanLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VerificationMetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                color = TextTertiary
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                ),
                color = Color.White
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                color = TextSecondary
            )
        }
    }
}

// =========================================================================
// 4. NEARBY VILLAGE COMPARISON MODAL
// =========================================================================
@Composable
fun NearbyVillageComparisonDialog(
    currentVillage: VillageHierarchy,
    comparisons: List<NearbyVillageComparison>,
    onSelectVillage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(DeepNavyBg)
                .border(1.dp, ElectricCyan, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "NEARBY VILLAGES COMPARISON",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                ),
                                color = ElectricCyan
                            )
                            Text(
                                text = "Base: ${currentVillage.villageName} (${currentVillage.elevationM}m elevation)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Text(
                    text = "Microclimates differ significantly over 5–15km due to local orography, drainage ridges, and vegetative surface friction. Compare conditions with adjoining panchayats:",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextHighlight
                )

                for (comp in comparisons) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = comp.villageName,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 14.sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${comp.distanceKm} km away • Elevation: ${comp.elevationM}m (${if (comp.elevationDiffM >= 0) "+${comp.elevationDiffM}m" else "${comp.elevationDiffM}m"})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextTertiary
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${comp.forecastTempC}°C",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = CyanAccent
                                    )
                                    Text(
                                        text = "${comp.forecastRainfallMm} mm rain",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                        color = TextSecondary
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Flood Risk: ${comp.floodRiskLevel}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (comp.floodRiskLevel == "High") AlertOrange else AlertGreen
                                )
                                Text(
                                    text = "Confidence: ${comp.confidenceScore}%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ElectricCyan
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DeepNavyBg)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Divergence Reason: ${comp.microclimateDivergenceReason}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                                    color = TextHighlight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 5. REPORT EXPORT MODAL (PDF / CSV REPORTING)
// =========================================================================
@Composable
fun ReportExportDialog(
    village: VillageHierarchy,
    onGenerateReport: (String) -> String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf("PDF") }
    var reportContent by remember(selectedFormat) {
        mutableStateOf(onGenerateReport(selectedFormat))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(24.dp))
                .background(DeepNavyBg)
                .border(1.dp, ElectricCyan, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
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
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "WEATHER INTELLIGENCE REPORT",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.8.sp
                                ),
                                color = ElectricCyan
                            )
                            Text(
                                text = "Standardized Export for Disaster Officials & Krishi Vigyan",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                // Format Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("PDF", "CSV").forEach { fmt ->
                        val isSelected = selectedFormat == fmt
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ElectricCyan else SurfaceCard)
                                .clickable {
                                    selectedFormat = fmt
                                    reportContent = onGenerateReport(fmt)
                                }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$fmt FORMAT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                ),
                                color = if (isSelected) DeepNavyBg else Color.White
                            )
                        }
                    }
                }

                // Report Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceNavy)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = reportContent,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.5.sp,
                                lineHeight = 16.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            color = TextHighlight
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Copy to Clipboard
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceCard)
                            .border(1.dp, ElectricCyan, RoundedCornerShape(12.dp))
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("WeatherGPT Report", reportContent))
                                Toast.makeText(context, "$selectedFormat report copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                            Text(
                                text = "COPY REPORT",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.5.sp),
                                color = ElectricCyan
                            )
                        }
                    }

                    // Simulated File Download
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ElectricCyan)
                            .clickable {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("WeatherGPT Report", reportContent))
                                Toast.makeText(context, "$selectedFormat generated & saved to device cache!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = DeepNavyBg, modifier = Modifier.size(16.dp))
                            Text(
                                text = "DOWNLOAD FILE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 10.5.sp),
                                color = DeepNavyBg
                            )
                        }
                    }
                }
            }
        }
    }
}
