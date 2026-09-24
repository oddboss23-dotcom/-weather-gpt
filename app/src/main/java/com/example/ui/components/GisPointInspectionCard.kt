package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.gis.GisPointInspection
import com.example.data.gis.UnifiedWeatherData
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

/**
 * Click-Anywhere Precision Atmospheric Analysis Card
 * Displays:
 * - Exact Latitude / Longitude
 * - Locality, District & State
 * - Comprehensive multi-variable observations (Temp, Feels Like, Humidity, Rain Rate, Prob, Wind, Pressure, Cloud Cover, Visibility, UV, Lightning, Heat Index)
 * - Provenance Badges, Timestamps, Freshness status
 * - Contextual WeatherGPT Meteorological Reasoning
 * - Action buttons: "Ask WeatherGPT", "View Satellite", "View Radar", "Compare Layers"
 */
@Composable
fun GisPointInspectionCard(
    inspection: UnifiedWeatherData,
    onClose: () -> Unit,
    onAskWeatherGpt: (query: String) -> Unit,
    onSwitchLayer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceNavy)
            .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Card Header: Coordinate Pin, Locality & Close Button
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
                        .background(ElectricTeal.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📍", fontSize = 16.sp)
                }
                Column {
                    Text(
                        text = "${inspection.localityName}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${inspection.districtName}, ${inspection.stateName} • ${inspection.latitude}°N, ${inspection.longitude}°E",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ElectricCyan
                    )
                }
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Inspection",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Source & Freshness Metadata Badge Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Freshness
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(inspection.freshness.badgeColorHex).copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = inspection.freshness.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = Color(inspection.freshness.badgeColorHex)
                    )
                }
                // Provenance
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceCard)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = inspection.provenance.tag,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold),
                        color = CyanAccent
                    )
                }
            }

            Text(
                text = inspection.formattedTime,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextTertiary
            )
        }

        // Atmospheric Metrics Grid (3x3 layout)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceCard)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem("Temperature", "${inspection.temperatureC}°C", "Feels ${inspection.feelsLikeC}°C", ElectricCyan, Modifier.weight(1f))
                MetricItem("Rainfall Rate", "${inspection.rainfallRateMmH} mm/h", "Prob: ${inspection.rainProbabilityPercent}%", AlertOrange, Modifier.weight(1f))
                MetricItem("Humidity", "${inspection.humidityPercent}%", "Dew pt 22°C", CyanLight, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem("Wind", "${inspection.windSpeedKmh} km/h", "${inspection.windDirectionText} (${inspection.windDirectionDeg}°)", TextPrimary, Modifier.weight(1f))
                MetricItem("MSLP Pressure", "${inspection.pressureHpa} hPa", "Gradient Normal", TextSecondary, Modifier.weight(1f))
                MetricItem("Cloud Cover", "${inspection.cloudCoverPercent}%", "Optical Frac.", CyanAccent, Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MetricItem("Visibility", "${inspection.visibilityKm} km", "Surface Range", AlertGreen, Modifier.weight(1f))
                MetricItem("Lightning Risk", inspection.lightningRisk, "Damini Grid", if (inspection.lightningRisk == "High") AlertRed else AlertYellow, Modifier.weight(1f))
                MetricItem("Heat Index", inspection.heatIndexCategory, "Rothfusz Eq.", if (inspection.heatIndexCategory == "Danger") AlertRed else AlertGreen, Modifier.weight(1f))
            }
        }

        // WeatherGPT AI Analysis Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DeepNavyBg)
                .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "WeatherGPT",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "WEATHERGPT ATMOSPHERIC INTERPRETATION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp, fontSize = 10.sp),
                        color = ElectricCyan
                    )
                }

                Text(
                    text = if (inspection.rainfallRateMmH >= 20.0) {
                        "High-reflectivity convective cell detected over ${inspection.localityName}. Rainfall rate is ${inspection.rainfallRateMmH} mm/h and accumulating rapidly. High urban runoff and flash-flood susceptibility for low-lying arterial underpasses. Cloud Motion Vectors track ${inspection.windDirectionText} at ${inspection.windSpeedKmh} km/h."
                    } else if (inspection.heatIndexCategory == "Extreme Danger" || inspection.heatIndexCategory == "Danger") {
                        "Elevated biometeorological heat stress alert over ${inspection.localityName}. Apparent temperature is ${inspection.feelsLikeC}°C (${inspection.heatIndexCategory}). Severe risk of heat exhaustion and cramps during outdoor exposure. Urban heat island buffer active."
                    } else if (inspection.rainfallRateMmH > 0.0) {
                        "Light stratiform showers active across ${inspection.districtName}. Relative humidity is high (${inspection.humidityPercent}%) with moderate cloud coverage (${inspection.cloudCoverPercent}%). No flash flood threat currently."
                    } else {
                        "Stable atmospheric conditions over ${inspection.localityName}. MSLP is ${inspection.pressureHpa} hPa with dry surface winds at ${inspection.windSpeedKmh} km/h. Heat index is categorized as '${inspection.heatIndexCategory}'."
                    },
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = TextPrimary
                )

                Text(
                    text = "Source: ${inspection.primarySourceBadge} • Confidence ${inspection.confidenceScore}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = TextTertiary
                )
            }
        }

        // Quick Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    onAskWeatherGpt("Analyze meteorological conditions and next 6-hour forecast at ${inspection.localityName} (${inspection.latitude}N, ${inspection.longitude}E).")
                },
                modifier = Modifier.weight(1.3f),
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = DeepNavyBg, modifier = Modifier.size(14.dp))
                    Text("Ask WeatherGPT", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = DeepNavyBg)
                }
            }

            OutlinedButton(
                onClick = { onSwitchLayer("satellite") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Text("Satellite", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
            }

            OutlinedButton(
                onClick = { onSwitchLayer("radar") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Text("Radar", style = MaterialTheme.typography.labelSmall, color = ElectricCyan)
            }
        }
    }
}

@Composable
private fun MetricItem(
    title: String,
    primaryValue: String,
    secondaryValue: String,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
        Text(text = primaryValue, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = primaryColor)
        Text(text = secondaryValue, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp), color = TextSecondary)
    }
}
