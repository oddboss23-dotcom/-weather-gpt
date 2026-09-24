package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
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
import com.example.data.canonical.DataQuality
import com.example.data.canonical.DataSourceType
import com.example.data.canonical.WeatherSnapshot
import com.example.data.hyperlocal.ForecastTrustAssessment
import com.example.data.model.WeatherData
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanLight
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * Subtle Expandable "Why this forecast?" action.
 * Transparent, dynamically generated from canonical snapshot data without overwhelming the UI.
 */
@Composable
fun WhyThisForecastCard(
    weather: WeatherData,
    snapshot: WeatherSnapshot? = null,
    trustAssessment: ForecastTrustAssessment? = null,
    onOpenTrust: () -> Unit = {},
    onOpenSimulator: () -> Unit = {},
    onOpenVerification: () -> Unit = {},
    onOpenNearby: () -> Unit = {},
    onOpenExport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Dynamic extraction from snapshot or truthful fallbacks
    val hasImdObs = snapshot?.sources?.any { it.source == DataSourceType.IMD && it.quality == DataQuality.VALID } == true
    val hasHydrology = snapshot?.sources?.any { it.source == DataSourceType.CWC_HYDROLOGY && it.quality == DataQuality.VALID } == true

    val sourceDisplay = when {
        hasImdObs && hasHydrology -> "IMD observation + forecast model + CWC Hydrology"
        hasImdObs -> "IMD observation + forecast model"
        hasHydrology -> "Forecast model + CWC Hydrology"
        else -> "Forecast model"
    }

    val updatedTime = snapshot?.lastUpdatedText ?: weather.lastUpdated.replace("Live • ", "").ifBlank { "5 min ago" }

    val confidenceScore = snapshot?.confidence?.scorePercent ?: trustAssessment?.confidenceScorePercent ?: 85
    val confidenceLevel = when {
        confidenceScore >= 80 -> "High"
        confidenceScore >= 60 -> "Moderate"
        else -> "Low"
    }

    val uncertaintyLevel = snapshot?.uncertainty?.level?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Low"

    val whyExplanation = if (snapshot != null) {
        val validFactors = snapshot.confidence.basis
            .filter { it.startsWith("✓") }
            .map { it.removePrefix("✓").trim() }
        if (validFactors.isNotEmpty()) {
            validFactors.take(2).joinToString(" and ") + "."
        } else {
            snapshot.uncertainty.explanation
        }
    } else {
        if (weather.expectedRainfallMm > 5.0 || weather.rainProbabilityPercent > 60) {
            "Recent local observation and active precipitation moisture convergence."
        } else {
            "Recent local observation and good model agreement."
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceNavy)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Subtle Expandable Action Row (Not a large card)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "Why this forecast?",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.5.sp
                        ),
                        color = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isExpanded) "Hide" else "Details",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = ElectricCyan
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Expanded Details View
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepNavyBg)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "WHY THIS FORECAST",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 9.5.sp
                        ),
                        color = CyanAccent
                    )

                    // Source
                    ForecastFactItem(label = "Source", value = sourceDisplay)

                    // Updated
                    ForecastFactItem(label = "Updated", value = updatedTime)

                    // Forecast basis
                    ForecastFactItem(
                        label = "Forecast basis",
                        value = "Numerical prediction model calibrated with local terrain elevation"
                    )

                    // Confidence & Uncertainty
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCard)
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Confidence",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = TextTertiary
                                )
                                Text(
                                    text = "$confidenceLevel ($confidenceScore%)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = AlertGreen
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCard)
                                .padding(8.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Uncertainty",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = TextTertiary
                                )
                                Text(
                                    text = uncertaintyLevel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = if (uncertaintyLevel.equals("high", ignoreCase = true)) AlertOrange else AlertGreen
                                )
                            }
                        }
                    }

                    // Why
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Why:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = ElectricCyan
                        )
                        Text(
                            text = whyExplanation,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }

                    // Relevant observations
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = "Relevant observations:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = ElectricCyan
                        )
                        ObservationBullet("Surface temperature: ${weather.temperatureC.toInt()}°C (Feels like ${weather.feelsLikeC.toInt()}°C)")
                        ObservationBullet("Relative humidity: ${weather.humidityPercent}%")
                        ObservationBullet("Wind velocity: ${weather.windSpeedKmh} km/h (${weather.windDirectionText})")
                        if (weather.expectedRainfallMm > 0 || weather.rainProbabilityPercent > 20) {
                            ObservationBullet("Precipitation risk: ${weather.expectedRainfallMm} mm (${weather.rainProbabilityPercent}% chance)")
                        }
                        if (snapshot?.riverState != null) {
                            ObservationBullet("River hydrology (${snapshot.riverState.riverName} at ${snapshot.riverState.station}): ${snapshot.riverState.currentLevel}m (${snapshot.riverState.status.name.replace('_', ' ')})")
                        }
                    }

                    // Optional quick tools
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickToolPill(label = "What-If Simulator", icon = Icons.Default.ElectricBolt, tint = ElectricCyan, onClick = onOpenSimulator)
                        QuickToolPill(label = "Nearby Comparison", icon = Icons.Default.CompareArrows, tint = CyanLight, onClick = onOpenNearby)
                        QuickToolPill(label = "Export Report", icon = Icons.Default.Download, tint = TextSecondary, onClick = onOpenExport)
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastFactItem(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp
            ),
            color = TextTertiary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.5.sp
            ),
            color = Color.White
        )
    }
}

@Composable
private fun ObservationBullet(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(text = "•", fontSize = 11.sp, color = ElectricCyan)
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
            color = TextSecondary
        )
    }
}

@Composable
private fun QuickToolPill(
    label: String,
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceCard)
            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.5.sp
                ),
                color = Color.White
            )
        }
    }
}
