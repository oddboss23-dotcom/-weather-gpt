package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.DisasterAlert
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * Authoritative River Flood Early-Warning Alert Card.
 * Complies strictly with Central Water Commission (CWC) & Bihar WRD FMIS standards.
 *
 * Implements the 6 Mandatory Actions:
 * 1. VIEW DETAILS
 * 2. LISTEN AUDIO
 * 3. COPY SMS
 * 4. VIEW MAP
 * 5. VIEW RIVER LEVEL
 * 6. VIEW SAFE ACTIONS
 */
@Composable
fun AuthoritativeFloodCard(
    alert: DisasterAlert,
    onListenAudio: () -> Unit,
    onViewMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDetailsDialog by remember { mutableStateOf(false) }
    var showRiverLevelDialog by remember { mutableStateOf(false) }
    var showSafeActionsDialog by remember { mutableStateOf(false) }

    val isDanger = alert.severity == AlertSeverity.RED || (alert.floodDistanceAboveDanger ?: -1.0) >= 0.0
    val headerBg = if (isDanger) AlertRed else AlertOrange
    val cardBorderColor = if (isDanger) AlertRed.copy(alpha = 0.8f) else AlertOrange.copy(alpha = 0.8f)

    androidx.compose.runtime.LaunchedEffect(alert.id) {
        com.example.service.HapticFeedbackManager.triggerFloodWarning(context, isDanger)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, cardBorderColor, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: Official Alert Strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Flood Warning",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isDanger) "CRITICAL FLOOD ALERT (CWC / FMIS)" else "FLOOD WARNING ADVISORY",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = alert.floodTrend?.uppercase() ?: "RISING ▲",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }

            // Body Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // River & Station Identification
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${alert.floodRiver ?: "River"} River",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = "${alert.floodStation ?: "Hydrological"} Station • ${alert.location}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = CyanAccent
                        )
                    }

                    // Status Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDanger) AlertRed.copy(alpha = 0.2f) else AlertOrange.copy(alpha = 0.2f))
                            .border(1.dp, if (isDanger) AlertRed else AlertOrange, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = alert.floodStatus ?: "ABOVE DANGER LEVEL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (isDanger) AlertRed else AlertOrange
                        )
                    }
                }

                // River Level Metrics Grid
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LevelMetricItem(
                                label = "CURRENT LEVEL",
                                value = "${alert.floodCurrentLevel ?: "--"} m",
                                isHighlight = true,
                                valueColor = if (isDanger) AlertRed else AlertOrange
                            )
                            LevelMetricItem(
                                label = "DANGER LEVEL",
                                value = "${alert.floodDangerLevel ?: "--"} m",
                                isHighlight = false,
                                valueColor = TextPrimary
                            )
                            LevelMetricItem(
                                label = "WARNING LEVEL",
                                value = "${alert.floodWarningLevel ?: "--"} m",
                                isHighlight = false,
                                valueColor = TextSecondary
                            )
                            LevelMetricItem(
                                label = "HIGHEST (HFL)",
                                value = "${alert.floodHighestFloodLevel ?: "--"} m",
                                isHighlight = false,
                                valueColor = TextTertiary
                            )
                        }

                        // Water level progress bar relative to Danger Level
                        val current = alert.floodCurrentLevel ?: 0.0
                        val warning = alert.floodWarningLevel ?: (current - 1.0)
                        val danger = alert.floodDangerLevel ?: (warning + 1.0)
                        val ratio = if (danger > warning) {
                            ((current - (warning - 1.0)) / ((danger + 1.0) - (warning - 1.0))).coerceIn(0.0, 1.0).toFloat()
                        } else 0.7f

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Flow Relative to Danger Mark", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary)
                                val distAbove = alert.floodDistanceAboveDanger ?: 0.0
                                Text(
                                    text = if (distAbove >= 0) "+${String.format("%.2f", distAbove)} m above Danger Level" else "${String.format("%.2f", distAbove)} m below Danger Level",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = if (distAbove >= 0) AlertRed else AlertOrange
                                )
                            }
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (isDanger) AlertRed else AlertOrange,
                                trackColor = DeepNavyBg
                            )
                        }
                    }
                }

                // Recommended Action Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DeepNavyBg.copy(alpha = 0.6f))
                        .border(0.5.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Recommended Action:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyanAccent
                        )
                        Text(
                            text = alert.recommendedAction,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextHighlight
                        )
                    }
                }

                // Metadata Provenance Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Source: ${alert.verifiedSource}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                        color = TextTertiary
                    )
                    Text(
                        text = alert.floodObservationTime ?: "Live Telemetry",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontFamily = FontFamily.Monospace),
                        color = TextSecondary
                    )
                }

                HorizontalDivider(color = SurfaceBorder, thickness = 0.5.dp)

                // The 6 Mandatory Actions Strip (Horizontally Scrollable)
                Text(
                    text = "AUTHORITATIVE FLOOD ACTIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = CyanAccent
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Action 1: VIEW DETAILS
                    AlertActionButton(
                        icon = Icons.Default.Info,
                        label = "VIEW DETAILS",
                        onClick = {
                            com.example.service.HapticFeedbackManager.triggerMapInteraction(context)
                            showDetailsDialog = true
                        }
                    )

                    // Action 2: LISTEN AUDIO
                    AlertActionButton(
                        icon = Icons.Default.VolumeUp,
                        label = "LISTEN AUDIO",
                        onClick = {
                            com.example.service.HapticFeedbackManager.triggerVoiceInteraction(context)
                            onListenAudio()
                        }
                    )

                    // Action 3: COPY SMS
                    AlertActionButton(
                        icon = Icons.Default.ContentCopy,
                        label = "COPY SMS",
                        onClick = {
                            com.example.service.HapticFeedbackManager.triggerMapInteraction(context)
                            val smsText = buildSmsAlertText(alert)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Flood Warning SMS", smsText))
                            Toast.makeText(context, "Flood Alert SMS copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )

                    // Action 4: VIEW MAP
                    AlertActionButton(
                        icon = Icons.Default.Map,
                        label = "VIEW MAP",
                        onClick = {
                            com.example.service.HapticFeedbackManager.triggerMapInteraction(context)
                            onViewMap()
                        }
                    )

                    // Action 5: VIEW RIVER LEVEL
                    AlertActionButton(
                        icon = Icons.Default.ShowChart,
                        label = "VIEW RIVER LEVEL",
                        onClick = {
                            com.example.service.HapticFeedbackManager.triggerMapInteraction(context)
                            showRiverLevelDialog = true
                        }
                    )

                    // Action 6: VIEW SAFE ACTIONS
                    AlertActionButton(
                        icon = Icons.Default.Security,
                        label = "VIEW SAFE ACTIONS",
                        onClick = {
                            com.example.service.HapticFeedbackManager.triggerMapInteraction(context)
                            showSafeActionsDialog = true
                        }
                    )
                }
            }
        }
    }

    // Modal 1: Full Hydrological Details
    if (showDetailsDialog) {
        FloodDetailsDialog(
            alert = alert,
            onDismiss = { showDetailsDialog = false }
        )
    }

    // Modal 2: River Level Gauge View
    if (showRiverLevelDialog) {
        RiverLevelGaugeDialog(
            alert = alert,
            onDismiss = { showRiverLevelDialog = false }
        )
    }

    // Modal 3: Safe Actions & Evacuation Protocol
    if (showSafeActionsDialog) {
        SafeActionsDialog(
            alert = alert,
            onDismiss = { showSafeActionsDialog = false }
        )
    }
}

@Composable
private fun LevelMetricItem(
    label: String,
    value: String,
    isHighlight: Boolean,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
            color = TextTertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isHighlight) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = if (isHighlight) 15.sp else 13.sp
            ),
            color = valueColor
        )
    }
}

@Composable
private fun AlertActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = CyanAccent,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = Color.White
            )
        }
    }
}

private fun buildSmsAlertText(alert: DisasterAlert): String {
    val river = alert.floodRiver ?: "River"
    val station = alert.floodStation ?: "Gauge"
    val level = alert.floodCurrentLevel ?: 0.0
    val danger = alert.floodDangerLevel ?: 0.0
    val status = alert.floodStatus ?: "CRITICAL"
    return "🚨 FLOOD WARNING (CWC/FMIS)\n$river River at $station: $status.\nCurrent: ${level}m (Danger: ${danger}m, Trend: ${alert.floodTrend ?: "Rising"}).\nAction: Move to high ground immediately. Follow SDRF/NDRF orders.\nSource: WeatherGPT / CWC & Bihar FMIS"
}

/**
 * Fallback Component when CWC / Bihar Flood monitoring data cannot be retrieved.
 * Adheres strictly to failure handling rule:
 * "Display: 'Flood monitoring data temporarily unavailable.' and show the last successful observation timestamp if available."
 */
@Composable
fun FloodDataUnavailableBanner(
    lastSuccessfulTimestamp: Long?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceNavy)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDamage,
                    contentDescription = "River Flood Gauge",
                    tint = TextTertiary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = "Flood monitoring data temporarily unavailable.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    if (lastSuccessfulTimestamp != null && lastSuccessfulTimestamp > 0) {
                        Text(
                            text = "Last verified telemetry: ${java.text.SimpleDateFormat("dd MMM, hh:mm a 'IST'", java.util.Locale.getDefault()).format(java.util.Date(lastSuccessfulTimestamp))}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextTertiary
                        )
                    } else {
                        Text(
                            text = "Awaiting official CWC / Bihar WRD gauge telemetry transmission.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextTertiary
                        )
                    }
                }
            }

            TextButton(onClick = onRetry) {
                Text(
                    text = "RETRY",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricCyan
                )
            }
        }
    }
}

@Composable
private fun FloodDetailsDialog(
    alert: DisasterAlert,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Hydrological Basin Telemetry",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow("River Basin", alert.floodRiver ?: "Gandak / Kosi Basin")
                DetailRow("Gauge Station", alert.floodStation ?: "Central Water Commission")
                DetailRow("Location & District", alert.location)
                DetailRow("Current Water Level", "${alert.floodCurrentLevel ?: "--"} m above MSL")
                DetailRow("Warning Level (WL)", "${alert.floodWarningLevel ?: "--"} m")
                DetailRow("Danger Level (DL)", "${alert.floodDangerLevel ?: "--"} m")
                DetailRow("Highest Flood Level (HFL)", "${alert.floodHighestFloodLevel ?: "--"} m")
                DetailRow("Hydro Trend", alert.floodTrend ?: "Rising")
                DetailRow("Observed Rainfall (24h)", "${alert.floodRainfallMm ?: 0.0} mm")
                DetailRow("Forecast Rainfall (48h)", "${alert.floodForecastRainfallMm ?: 0.0} mm")
                DetailRow("Authoritative Source", alert.verifiedSource)
                DetailRow("Observation Timestamp", alert.floodObservationTime ?: "Real-time Telemetry")

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Expected Inundation Impact:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CyanAccent
                )
                Text(
                    text = alert.expectedImpact,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("CLOSE", color = DeepNavyBg, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceNavy,
        tonalElevation = 8.dp
    )
}

@Composable
private fun RiverLevelGaugeDialog(
    alert: DisasterAlert,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "River Water Level Gauge",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${alert.floodRiver ?: "River"} @ ${alert.floodStation ?: "Station"}",
                    style = MaterialTheme.typography.titleSmall,
                    color = CyanAccent
                )

                // Visual Gauge Cross-Section
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        GaugeLevelLine(
                            name = "HIGHEST FLOOD LEVEL (HFL)",
                            value = "${alert.floodHighestFloodLevel ?: "--"} m",
                            color = Color(0xFF7C3AED)
                        )
                        GaugeLevelLine(
                            name = "DANGER LEVEL (DL)",
                            value = "${alert.floodDangerLevel ?: "--"} m",
                            color = AlertRed
                        )
                        GaugeLevelLine(
                            name = "CURRENT WATER LEVEL",
                            value = "${alert.floodCurrentLevel ?: "--"} m (${alert.floodTrend ?: "Rising"})",
                            color = Color.White,
                            isCurrent = true
                        )
                        GaugeLevelLine(
                            name = "WARNING LEVEL (WL)",
                            value = "${alert.floodWarningLevel ?: "--"} m",
                            color = AlertOrange
                        )
                    }
                }

                Text(
                    text = "Status: ${alert.floodStatus}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = if (alert.severity == AlertSeverity.RED) AlertRed else AlertOrange
                )
                Text(
                    text = "All water level measurements are calibrated to Mean Sea Level (MSL) benchmarks verified by Central Water Commission (CWC) hydro-observers.",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextTertiary
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("DONE", color = DeepNavyBg, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceNavy
    )
}

@Composable
private fun SafeActionsDialog(
    alert: DisasterAlert,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Safe Actions & Evacuation Protocol",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Authoritative NDRF / SDRF Flood Guidelines:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = CyanAccent
                )

                SafetyStepItem(
                    number = "1",
                    title = "Move Away From Riverbanks",
                    description = alert.recommendedAction
                )
                SafetyStepItem(
                    number = "2",
                    title = "Protect Livestock & Assets",
                    description = "Untie and move cattle, goats, and agricultural pumps to elevated embankments or designated shelter grounds."
                )
                SafetyStepItem(
                    number = "3",
                    title = "Prepare Emergency Go-Bag",
                    description = "Pack 3-day drinking water, dry sattu/chura, torch/flashlight, waterproof pouch with Aadhar/land deeds, and essential medicines."
                )
                SafetyStepItem(
                    number = "4",
                    title = "Do NOT Cross Flowing Water",
                    description = "Never attempt to walk, wade, or drive through submerged roads or culverts. Just 15 cm of moving water can knock down an adult."
                )
                SafetyStepItem(
                    number = "5",
                    title = "Emergency Contact Numbers",
                    description = "National Disaster Helpline: 1070 | District Emergency Operations Center (DEOC): 1077 | NDRF Control Room: 011-24363260"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
            ) {
                Text("UNDERSTOOD", color = DeepNavyBg, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = SurfaceNavy
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary
        )
    }
}

@Composable
private fun GaugeLevelLine(
    name: String,
    value: String,
    color: Color,
    isCurrent: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isCurrent) color.copy(alpha = 0.15f) else Color.Transparent)
            .padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium,
                    fontSize = if (isCurrent) 11.sp else 10.sp
                ),
                color = if (isCurrent) Color.White else color
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = if (isCurrent) 12.sp else 10.5.sp
            ),
            color = if (isCurrent) ElectricCyan else color
        )
    }
}

@Composable
private fun SafetyStepItem(number: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(ElectricCyan)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = DeepNavyBg
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = TextSecondary
            )
        }
    }
}
