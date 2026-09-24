package com.example.ui.components

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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.example.data.gis.GisTimelineFrame
import com.example.data.gis.WeatherGisLayer
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
 * Weather Layers Selector & Transparency Opacity Control
 */
@Composable
fun GisLayerSelectorBar(
    selectedLayer: WeatherGisLayer,
    layerOpacity: Float,
    comparisonLayer: WeatherGisLayer?,
    onLayerSelected: (WeatherGisLayer) -> Unit,
    onOpacityChanged: (Float) -> Unit,
    onComparisonLayerSelected: (WeatherGisLayer?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showOpacitySlider by remember { mutableStateOf(false) }
    var showCompareDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Horizontally scrollable list of GIS weather layers
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            WeatherGisLayer.values().forEach { layer ->
                val isSelected = selectedLayer == layer
                val layerIcon = getIconForGisLayer(layer)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) SurfaceCard else SurfaceNavy)
                        .border(
                            1.dp,
                            if (isSelected) ElectricCyan else SurfaceBorder.copy(alpha = 0.6f),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onLayerSelected(layer) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = layerIcon,
                            contentDescription = layer.displayName,
                            tint = if (isSelected) ElectricCyan else TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = layer.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }

        // Layer Controls: Active Layer Provenance Badge, Opacity Slider Toggle, and Compare Mode Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceNavy)
                .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Provenance Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(ElectricTeal.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = selectedLayer.productType,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = ElectricCyan
                    )
                }
                Text(
                    text = selectedLayer.primarySource,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary
                )
            }

            // Controls on Right: Opacity button & Compare button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Opacity Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (showOpacitySlider) SurfaceCard else Color.Transparent)
                        .border(0.5.dp, if (showOpacitySlider) ElectricCyan else SurfaceBorder, RoundedCornerShape(6.dp))
                        .clickable { showOpacitySlider = !showOpacitySlider }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Opacity ${(layerOpacity * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                        color = ElectricCyan
                    )
                }

                // Compare Layer Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (comparisonLayer != null) AlertOrange.copy(alpha = 0.2f) else SurfaceCard)
                        .border(0.5.dp, if (comparisonLayer != null) AlertOrange else SurfaceBorder, RoundedCornerShape(6.dp))
                        .clickable {
                            if (comparisonLayer != null) {
                                onComparisonLayerSelected(null) // Exit compare
                            } else {
                                // Default compare with Rainfall or Satellite
                                val target = if (selectedLayer == WeatherGisLayer.SATELLITE) WeatherGisLayer.RAINFALL else WeatherGisLayer.SATELLITE
                                onComparisonLayerSelected(target)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (comparisonLayer != null) "Split: vs ${comparisonLayer.displayName} [✕]" else "Compare ◫",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = if (comparisonLayer != null) AlertOrange else CyanAccent
                    )
                }
            }
        }

        // Expanded Opacity Slider
        if (showOpacitySlider) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "${selectedLayer.displayName} Opacity",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary
                )
                Slider(
                    value = layerOpacity,
                    onValueChange = onOpacityChanged,
                    valueRange = 0.1f..1.0f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = ElectricCyan,
                        activeTrackColor = ElectricCyan,
                        inactiveTrackColor = SurfaceBorder
                    )
                )
                Text(
                    text = "${(layerOpacity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = ElectricCyan
                )
            }
        }
    }
}

/**
 * Animated Satellite & Meteorological Timeline Slider Component
 * Supports: Play / Pause, Previous, Next, Manual slider drag, and Frame Timestamps
 */
@Composable
fun GisTimelineAnimationBar(
    frames: List<GisTimelineFrame>,
    currentFrameIndex: Int,
    isPlaying: Boolean,
    onFrameSelected: (Int) -> Unit,
    onTogglePlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (frames.isEmpty()) return
    val currentFrame = frames.getOrElse(currentFrameIndex) { frames.firstOrNull() ?: return }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceNavy)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Controls Row: Play/Pause, Prev, Next, Current Timestamp Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Play / Pause Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(0.5.dp, SurfaceBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause Timeline" else "Play Timeline",
                        tint = ElectricCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Previous Frame
                IconButton(
                    onClick = {
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

                // Relative Label badge (e.g. "T-2h", "NOW", "+3h")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (currentFrame.isForecast) AlertOrange.copy(alpha = 0.2f) else ElectricTeal.copy(alpha = 0.25f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = currentFrame.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = if (currentFrame.isForecast) AlertOrange else ElectricCyan
                    )
                }
            }

            // Real Time Label (e.g. "08 Sep 14:20 IST")
            Text(
                text = currentFrame.formattedTime,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                color = TextPrimary
            )
        }

        // Timeline Slider with frame ticks
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
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Dynamic Color Legend based on active GIS layer
 */
@Composable
fun GisDynamicColorLegend(
    activeLayer: WeatherGisLayer,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard.copy(alpha = 0.7f))
            .border(0.5.dp, SurfaceBorder.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${activeLayer.displayName} (${activeLayer.unit}):",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
            color = TextSecondary
        )

        when (activeLayer) {
            WeatherGisLayer.RAINFALL -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    LegendItem("0 mm", Color(0xFF1E293B))
                    LegendItem("5 mm", CyanAccent)
                    LegendItem("15 mm", AlertYellow)
                    LegendItem("30 mm", AlertOrange)
                    LegendItem("50+ mm", AlertRed)
                }
            }
            WeatherGisLayer.TEMPERATURE -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    LegendItem("<15°", Color(0xFF38BDF8))
                    LegendItem("20°", Color(0xFF34D399))
                    LegendItem("30°", Color(0xFFFBBF24))
                    LegendItem("35°", Color(0xFFF97316))
                    LegendItem("40°+", Color(0xFFEF4444))
                }
            }
            WeatherGisLayer.HUMIDITY -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    LegendItem("<30%", Color(0xFFE2E8F0))
                    LegendItem("50%", Color(0xFF7DD3FC))
                    LegendItem("70%", Color(0xFF0284C7))
                    LegendItem("90%+", Color(0xFF0C4A6E))
                }
            }
            WeatherGisLayer.WIND -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    LegendItem("<15 km/h", CyanLight)
                    LegendItem("30 km/h", AlertYellow)
                    LegendItem("60 km/h", AlertOrange)
                    LegendItem("100+ Gale", AlertRed)
                }
            }
            else -> {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    LegendItem("Light", CyanAccent)
                    LegendItem("Moderate", AlertYellow)
                    LegendItem("Heavy", AlertOrange)
                    LegendItem("Extreme", AlertRed)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextPrimary)
    }
}

private fun getIconForGisLayer(layer: WeatherGisLayer): ImageVector {
    return when (layer) {
        WeatherGisLayer.SATELLITE -> Icons.Default.SatelliteAlt
        WeatherGisLayer.RADAR -> Icons.Default.Radar
        WeatherGisLayer.RAINFALL -> Icons.Default.Grain
        WeatherGisLayer.WIND -> Icons.Default.Air
        WeatherGisLayer.TEMPERATURE -> Icons.Default.Thermostat
        WeatherGisLayer.HUMIDITY -> Icons.Default.Opacity
        WeatherGisLayer.CLOUD_COVER -> Icons.Default.Cloud
        WeatherGisLayer.LIGHTNING -> Icons.Default.Bolt
        WeatherGisLayer.CYCLONE -> Icons.Default.Warning
        WeatherGisLayer.HEAT_INDEX -> Icons.Default.Thermostat
        WeatherGisLayer.FOG -> Icons.Default.Visibility
        WeatherGisLayer.PRESSURE -> Icons.Default.Compress
        WeatherGisLayer.VISIBILITY -> Icons.Default.Visibility
        WeatherGisLayer.FLOOD_RISK -> Icons.Default.WaterDamage
        WeatherGisLayer.WATERLOGGING -> Icons.Default.WaterDamage
        WeatherGisLayer.FORECAST_CONFIDENCE -> Icons.Default.Shield
        WeatherGisLayer.FORECAST_BUST_RISK -> Icons.Default.ElectricBolt
    }
}
