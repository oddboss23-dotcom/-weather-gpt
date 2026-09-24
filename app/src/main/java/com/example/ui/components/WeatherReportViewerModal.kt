package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.service.report.ReportContextType
import com.example.service.report.WeatherIntelligenceReport
import com.example.service.report.WeatherReportGenerator
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
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

@Composable
fun WeatherReportViewerModal(
    report: WeatherIntelligenceReport,
    onDismiss: () -> Unit,
    onSwitchContext: (ReportContextType) -> Unit,
    onSpeakSummary: (String) -> Unit
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(DeepNavyBg)
                .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
            color = DeepNavyBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Top Action & Title Bar
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
                                .background(ElectricCyan.copy(alpha = 0.15f))
                                .border(1.dp, ElectricCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "WEATHERGPT INTELLIGENCE REPORT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    fontSize = 10.sp
                                ),
                                color = ElectricCyan
                            )
                            Text(
                                text = report.locationName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Context Switcher Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val contexts = listOf(
                        ReportContextType.STANDARD,
                        ReportContextType.TOMORROW_FORECAST,
                        ReportContextType.CYCLONE_HAZARD,
                        ReportContextType.AGRICULTURE_KRISHI,
                        ReportContextType.AVIATION,
                        ReportContextType.MARINE
                    )
                    items(contexts) { ctx ->
                        val isSelected = ctx == report.contextType
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSelected) ElectricCyan else SurfaceNavy)
                                .border(0.5.dp, if (isSelected) ElectricCyan else SurfaceBorder, RoundedCornerShape(50))
                                .clickable { onSwitchContext(ctx) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = ctx.badge,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 10.sp
                                ),
                                color = if (isSelected) DeepNavyBg else CyanLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Download & Share Quick Action Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceNavy)
                        .border(0.5.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Download PDF Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE53935).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFE53935).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable {
                                try {
                                    val file = WeatherReportGenerator.exportToPdf(report, context)
                                    WeatherReportGenerator.shareReportFile(file, "application/pdf", context)
                                    Toast.makeText(context, "Exported PDF Report: ${file.name}", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(16.dp))
                            Text("Download PDF", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Download CSV Button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricTeal.copy(alpha = 0.2f))
                            .border(1.dp, ElectricTeal.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .clickable {
                                try {
                                    val file = WeatherReportGenerator.exportToCsv(report, context)
                                    WeatherReportGenerator.shareReportFile(file, "text/csv", context)
                                    Toast.makeText(context, "Exported CSV Dataset: ${file.name}", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(16.dp))
                            Text("Download CSV", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Listen Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SurfaceCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                onSpeakSummary("${report.title}. ${report.aiAnalysis}. Recommendations: ${report.recommendations.joinToString("; ")}")
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "Listen", tint = CyanLight, modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Main Report Content Scroll
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Card with Hazard Badge
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceNavy)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = report.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                        color = Color.White
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                when (report.hazardLevel) {
                                                    "HIGH WARNING" -> AlertRed
                                                    "MODERATE ADVISORY" -> AlertOrange
                                                    "HEATWAVE ADVISORY" -> AlertOrange
                                                    else -> AlertGreen
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = report.hazardLevel,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                            color = Color.White
                                        )
                                    }
                                }

                                Text(
                                    text = "Generated at ${report.generatedTimestamp} • Coordinates: ${report.coordinates}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )

                                Text(
                                    text = report.hazardDescription,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = TextHighlight
                                )
                            }
                        }
                    }

                    // Section 1: Surface Observation Grid
                    item {
                        ReportSectionCard(title = "SECTION 1: LIVE SURFACE OBSERVATIONS") {
                            val w = report.currentConditions
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    ReportParameterItem("Temperature", "${w.temperatureC}°C (Feels ${w.feelsLikeC}°C)")
                                    ReportParameterItem("Observed Condition", w.conditionDescription)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    ReportParameterItem("Rain Probability", "${w.rainProbabilityPercent}% (${w.expectedRainfallMm} mm)")
                                    ReportParameterItem("Relative Humidity", "${w.humidityPercent}%")
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    ReportParameterItem("Wind Velocity", "${w.windSpeedKmh} km/h ${w.windDirectionText}")
                                    ReportParameterItem("Surface Pressure", "${w.pressureHpa} hPa")
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    ReportParameterItem("Visibility", "${w.visibilityKm} km")
                                    ReportParameterItem("UV Index", "${w.uvIndex}")
                                }
                            }
                        }
                    }

                    // Section 2: 48-Hour Forecast Horizon Table
                    item {
                        ReportSectionCard(title = "SECTION 2: 48-HOUR NUMERICAL FORECAST OUTLOOK") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("TIME", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = TextTertiary, modifier = Modifier.weight(1f))
                                    Text("TEMP", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = TextTertiary, modifier = Modifier.weight(1f))
                                    Text("RAIN %", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = TextTertiary, modifier = Modifier.weight(1f))
                                    Text("WIND", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = TextTertiary, modifier = Modifier.weight(1f))
                                    Text("PRESSURE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp), color = TextTertiary, modifier = Modifier.weight(1.2f))
                                }

                                val previewList = report.forecast48h.filterIndexed { idx, _ -> idx in listOf(0, 3, 6, 12, 24, 36) }
                                previewList.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item.timeLabel, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = Color.White, modifier = Modifier.weight(1f))
                                        Text("${item.temperatureC}°C", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = ElectricCyan, modifier = Modifier.weight(1f))
                                        Text("${item.rainProbabilityPercent}%", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = CyanLight, modifier = Modifier.weight(1f))
                                        Text("${item.windSpeedKmh}k", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary, modifier = Modifier.weight(1f))
                                        Text("${item.pressureHpa.toInt()} hPa", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary, modifier = Modifier.weight(1.2f))
                                    }
                                }
                            }
                        }
                    }

                    // Section 3: NWP Model Consensus
                    item {
                        ReportSectionCard(title = "SECTION 3: NWP ENSEMBLE CONSENSUS & PHYSICS") {
                            Text(
                                text = report.nwpModelInfo,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp, fontSize = 12.sp),
                                color = TextPrimary
                            )
                        }
                    }

                    // Section 4: AI Analysis & Sector Impact
                    item {
                        ReportSectionCard(title = "SECTION 4: SYNOPTIC AI ANALYSIS & SECTOR IMPACT") {
                            Text(
                                text = report.aiAnalysis,
                                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp, fontSize = 12.sp),
                                color = TextPrimary
                            )
                        }
                    }

                    // Section 5: Actionable Recommendations
                    item {
                        ReportSectionCard(title = "SECTION 5: OPERATIONAL RECOMMENDATIONS") {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                report.recommendations.forEachIndexed { idx, rec ->
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = "${idx + 1}.",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ElectricCyan
                                        )
                                        Text(
                                            text = rec,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section 6: Provenance & Uncertainty
                    item {
                        ReportSectionCard(title = "SECTION 6: OFFICIAL PROVENANCE & UNCERTAINTY") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Confidence Level: ${report.confidencePercent}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AlertGreen
                                )
                                Text(
                                    text = report.uncertaintyNotes,
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Data Sources: ${report.dataSources}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = TextTertiary
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
private fun ReportSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceNavy)
            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontSize = 10.sp
                ),
                color = ElectricCyan
            )
            content()
        }
    }
}

@Composable
private fun ReportParameterItem(
    label: String,
    value: String
) {
    Column(modifier = Modifier.width(140.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 12.sp), color = Color.White)
    }
}
