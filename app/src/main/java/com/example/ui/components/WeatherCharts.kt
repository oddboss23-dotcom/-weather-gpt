package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HistoricalDataPoint
import com.example.data.model.HourlyForecast
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.ElectricTeal
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.theme.WeatherBlue
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive Real-Time Hourly Temperature Chart with dynamic crosshairs,
 * tactile drag-to-inspect tooltips, smooth cubic curves, and gradient fill.
 */
@Composable
fun HourlyTemperatureLineChart(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    if (hourlyList.isEmpty()) return

    val scrollState = rememberScrollState()
    val animatedProgress = remember { Animatable(0f) }
    var selectedIndex by remember(hourlyList) { mutableStateOf<Int?>(null) }

    LaunchedEffect(hourlyList.size) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(500))
    }

    val pointWidth = 64.dp
    val totalWidth = (pointWidth.value * hourlyList.size).dp

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(ElectricCyan)
                )
                Text(
                    text = "REAL-TIME TEMPERATURE TREND",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = ElectricCyan
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = TextTertiary,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = if (selectedIndex != null) "Inspecting" else "Tap/drag point",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (selectedIndex != null) ElectricCyan else TextSecondary
                )
            }
        }

        // Active Inspector Banner / Tooltip (Recharts-equivalent tooltip header)
        AnimatedVisibility(
            visible = selectedIndex != null && selectedIndex!! in hourlyList.indices,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            val idx = selectedIndex ?: 0
            if (idx in hourlyList.indices) {
                val item = hourlyList[idx]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCard)
                        .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.timeLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            val conditionLabel = when (item.weatherCode) {
                                0 -> "Clear Sky"
                                1, 2, 3 -> "Partly Cloudy"
                                45, 48 -> "Fog"
                                51, 53, 55, 61, 63, 65 -> "Rain Showers"
                                80, 81, 82 -> "Heavy Rain"
                                95, 96, 99 -> "Thunderstorm"
                                else -> "Cloudy"
                            }
                            Text(
                                text = "•  $conditionLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "Temp: ${item.temperatureC}°C",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricCyan
                                )
                            )
                            Text(
                                text = "Rain: ${item.rainProbabilityPercent}%",
                                style = MaterialTheme.typography.labelSmall.copy(color = CyanAccent)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width(totalWidth)
                    .height(145.dp)
                    .pointerInput(hourlyList) {
                        detectTapGestures { offset ->
                            val stepX = pointWidth.toPx()
                            val clickedIndex = (offset.x / stepX).toInt().coerceIn(0, hourlyList.size - 1)
                            selectedIndex = if (selectedIndex == clickedIndex) null else clickedIndex
                        }
                    }
                    .pointerInput(hourlyList) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                change.consume()
                                val stepX = pointWidth.toPx()
                                val dragIndex = (change.position.x / stepX).toInt().coerceIn(0, hourlyList.size - 1)
                                selectedIndex = dragIndex
                            }
                        )
                    }
            ) {
                val minTemp = (hourlyList.minOfOrNull { it.temperatureC } ?: 20.0) - 2.0
                val maxTemp = (hourlyList.maxOfOrNull { it.temperatureC } ?: 40.0) + 2.0
                val range = (maxTemp - minTemp).coerceAtLeast(1.0)

                val height = size.height - 40.dp.toPx()
                val stepX = pointWidth.toPx()

                val points = hourlyList.mapIndexed { index, item ->
                    val x = index * stepX + (stepX / 2)
                    val normalizedY = ((item.temperatureC - minTemp) / range).toFloat()
                    val y = height - (normalizedY * height * animatedProgress.value) + 12.dp.toPx()
                    Offset(x, y)
                }

                // Draw horizontal grid guide lines
                for (g in 0..3) {
                    val gridY = (height / 3) * g + 12.dp.toPx()
                    drawLine(
                        color = SurfaceBorder.copy(alpha = 0.35f),
                        start = Offset(0f, gridY),
                        end = Offset(size.width, gridY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }

                // Fill gradient under curve
                if (points.size > 1) {
                    val fillPath = Path().apply {
                        moveTo(points.first().x, size.height - 20.dp.toPx())
                        lineTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val cx = (p1.x + p2.x) / 2
                            cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                        }
                        lineTo(points.last().x, size.height - 20.dp.toPx())
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                ElectricCyan.copy(alpha = 0.35f),
                                WeatherBlue.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = size.height
                        )
                    )

                    // Line Path
                    val linePath = Path().apply {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val cx = (p1.x + p2.x) / 2
                            cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                        }
                    }

                    drawPath(
                        path = linePath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(ElectricCyan, CyanAccent, ElectricTeal)
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Interactive Crosshair for selected point
                val currentSel = selectedIndex
                if (currentSel != null && currentSel in points.indices) {
                    val selPoint = points[currentSel]
                    // Vertical guideline
                    drawLine(
                        color = ElectricCyan.copy(alpha = 0.7f),
                        start = Offset(selPoint.x, 10.dp.toPx()),
                        end = Offset(selPoint.x, size.height - 22.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                    )
                    // Highlighting halo
                    drawCircle(
                        color = ElectricCyan.copy(alpha = 0.25f),
                        radius = 12.dp.toPx(),
                        center = selPoint
                    )
                }

                // Draw circles and temperature labels
                points.forEachIndexed { i, p ->
                    val item = hourlyList[i]
                    val isSelected = i == currentSel

                    // Outer glow
                    drawCircle(
                        color = if (isSelected) ElectricCyan else ElectricCyan.copy(alpha = 0.4f),
                        radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                        center = p
                    )
                    // Inner dot
                    drawCircle(
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
                        radius = if (isSelected) 4.dp.toPx() else 2.5.dp.toPx(),
                        center = p
                    )

                    // Native Canvas Text for values
                    val tempText = "${item.temperatureC.toInt()}°"
                    drawContext.canvas.nativeCanvas.drawText(
                        tempText,
                        p.x,
                        p.y - (if (isSelected) 12.dp.toPx() else 8.dp.toPx()),
                        android.graphics.Paint().apply {
                            color = if (isSelected) android.graphics.Color.CYAN else android.graphics.Color.WHITE
                            textSize = if (isSelected) 13.sp.toPx() else 11.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = isSelected
                        }
                    )

                    // Time label
                    drawContext.canvas.nativeCanvas.drawText(
                        item.timeLabel,
                        p.x,
                        size.height - 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#94A3B8")
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = isSelected
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HourlyRainfallBarChart(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    if (hourlyList.isEmpty()) return
    val scrollState = rememberScrollState()
    val pointWidth = 56.dp
    val totalWidth = (pointWidth.value * hourlyList.size).dp

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RAINFALL ACCUMULATION & PROBABILITY",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = CyanAccent
            )
            Text(
                text = "Precipitation (mm)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width(totalWidth)
                    .height(130.dp)
            ) {
                val maxPrecip = (hourlyList.maxOfOrNull { it.precipitationMm } ?: 10.0).coerceAtLeast(5.0)
                val chartHeight = size.height - 35.dp.toPx()
                val stepX = pointWidth.toPx()
                val barWidth = 20.dp.toPx()

                hourlyList.forEachIndexed { i, item ->
                    val x = i * stepX + (stepX / 2) - (barWidth / 2)
                    val barHeight = ((item.precipitationMm / maxPrecip) * chartHeight).toFloat().coerceAtLeast(3.dp.toPx())
                    val topY = chartHeight - barHeight + 5.dp.toPx()

                    val barColor = when {
                        item.precipitationMm > 15.0 -> AlertRed
                        item.precipitationMm > 7.5 -> AlertOrange
                        item.precipitationMm > 2.5 -> AlertYellow
                        item.precipitationMm > 0.1 -> CyanAccent
                        else -> WeatherBlue.copy(alpha = 0.35f)
                    }

                    // Draw bar
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(barColor, barColor.copy(alpha = 0.5f)),
                            startY = topY,
                            endY = chartHeight + 5.dp.toPx()
                        ),
                        topLeft = Offset(x, topY),
                        size = Size(barWidth, barHeight)
                    )

                    // Probability badge on top of bar
                    if (item.rainProbabilityPercent > 0) {
                        drawContext.canvas.nativeCanvas.drawText(
                            "${item.rainProbabilityPercent}%",
                            x + (barWidth / 2),
                            topY - 4.dp.toPx(),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.parseColor("#38BDF8")
                                textSize = 9.sp.toPx()
                                textAlign = android.graphics.Paint.Align.CENTER
                                isFakeBoldText = true
                            }
                        )
                    }

                    // Mm value or Time label
                    drawContext.canvas.nativeCanvas.drawText(
                        item.timeLabel,
                        x + (barWidth / 2),
                        size.height - 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#94A3B8")
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun WindVelocityVectorChart(
    hourlyList: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    if (hourlyList.isEmpty()) return
    val scrollState = rememberScrollState()
    val pointWidth = 60.dp
    val totalWidth = (pointWidth.value * hourlyList.size).dp

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WIND SPEED & DIRECTION VECTORS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = ElectricTeal
            )
            Text(
                text = "Velocity (km/h)",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width(totalWidth)
                    .height(110.dp)
            ) {
                val stepX = pointWidth.toPx()

                hourlyList.forEachIndexed { i, item ->
                    val centerX = i * stepX + (stepX / 2)
                    val centerY = 35.dp.toPx()

                    // Draw Compass circle background
                    drawCircle(
                        color = SurfaceBorder.copy(alpha = 0.5f),
                        radius = 16.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )

                    // Draw Directional Wind Arrow based on windDirectionDeg
                    val rad = Math.toRadians((item.windDirectionDeg - 90).toDouble())
                    val arrowLen = 12.dp.toPx()
                    val endX = centerX + (arrowLen * cos(rad)).toFloat()
                    val endY = centerY + (arrowLen * sin(rad)).toFloat()

                    drawLine(
                        color = ElectricCyan,
                        start = Offset(centerX, centerY),
                        end = Offset(endX, endY),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = ElectricTeal,
                        radius = 3.dp.toPx(),
                        center = Offset(endX, endY)
                    )

                    // Speed text
                    drawContext.canvas.nativeCanvas.drawText(
                        "${item.windSpeedKmh.toInt()}k",
                        centerX,
                        centerY + 28.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                            isFakeBoldText = true
                        }
                    )

                    // Time label
                    drawContext.canvas.nativeCanvas.drawText(
                        item.timeLabel,
                        centerX,
                        size.height - 4.dp.toPx(),
                        android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#94A3B8")
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ClimateResearchTrendChart(
    dataPoints: List<HistoricalDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "10-YEAR HISTORICAL MONSOON ANOMALY (mm)",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = ElectricCyan
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("■ Surplus", style = MaterialTheme.typography.labelSmall, color = AlertGreen)
                Text("■ Deficit", style = MaterialTheme.typography.labelSmall, color = AlertOrange)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val count = dataPoints.size
            val stepX = size.width / count
            val zeroY = size.height / 2
            val maxAnomaly = 400.0

            // Baseline 0-line
            drawLine(
                color = SurfaceBorder,
                start = Offset(0f, zeroY),
                end = Offset(size.width, zeroY),
                strokeWidth = 1.5.dp.toPx()
            )

            dataPoints.forEachIndexed { i, dp ->
                val x = i * stepX + (stepX / 2) - 8.dp.toPx()
                val barW = 16.dp.toPx()
                val anomalyRatio = (dp.rainfallAnomalyMm / maxAnomaly).coerceIn(-1.0, 1.0).toFloat()
                val barH = (zeroY - 20.dp.toPx()) * anomalyRatio

                val color = if (dp.rainfallAnomalyMm >= 0) AlertGreen else AlertOrange

                if (dp.rainfallAnomalyMm >= 0) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(color, color.copy(alpha = 0.5f)),
                            startY = zeroY - barH,
                            endY = zeroY
                        ),
                        topLeft = Offset(x, zeroY - barH),
                        size = Size(barW, barH)
                    )
                } else {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(color.copy(alpha = 0.5f), color),
                            startY = zeroY,
                            endY = zeroY - barH
                        ),
                        topLeft = Offset(x, zeroY),
                        size = Size(barW, -barH)
                    )
                }

                // Year Label
                drawContext.canvas.nativeCanvas.drawText(
                    dp.label,
                    x + (barW / 2),
                    size.height - 2.dp.toPx(),
                    android.graphics.Paint().apply {
                        this.color = android.graphics.Color.parseColor("#94A3B8")
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )

                // Total Rain value
                val rainText = "${dp.rainfallMm.toInt()}mm"
                drawContext.canvas.nativeCanvas.drawText(
                    rainText,
                    x + (barW / 2),
                    if (dp.rainfallAnomalyMm >= 0) zeroY - barH - 4.dp.toPx() else zeroY - barH + 12.dp.toPx(),
                    android.graphics.Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        textSize = 9.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                )
            }
        }
    }
}

/**
 * Interactive Historical Temperature Pattern Chart (Decadal / Multi-Year Climatology).
 * Recharts-style Compose interactive multi-point graph featuring:
 * - Touch inspection / drag crosshair cursor
 * - Active data point tooltip showing year, recorded mean, and baseline deviation
 * - Baseline reference line indicating climatological normal
 * - Glowing anomaly peaks with adaptive warming gradients
 */
@Composable
fun HistoricalTemperaturePatternChart(
    trendPoints: List<Pair<String, Double>>,
    baselineTemp: Double = 28.2,
    parameterName: String = "Temperature",
    unit: String = "°C",
    modifier: Modifier = Modifier
) {
    if (trendPoints.isEmpty()) return

    var selectedIndex by remember(trendPoints) { mutableStateOf<Int?>(null) }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(trendPoints) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(1f, animationSpec = tween(600))
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AlertOrange)
                )
                Text(
                    text = "HISTORICAL $parameterName PATTERNS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = AlertOrange
                )
            }
            Text(
                text = "Baseline: $baselineTemp$unit",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondary
            )
        }

        // Active Inspector Tooltip Box
        AnimatedVisibility(
            visible = selectedIndex != null && selectedIndex!! in trendPoints.indices,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            val idx = selectedIndex ?: 0
            if (idx in trendPoints.indices) {
                val (year, value) = trendPoints[idx]
                val diff = value - baselineTemp
                val diffStr = if (diff >= 0) "+${String.format("%.2f", diff)}" else String.format("%.2f", diff)
                val diffColor = if (diff >= 0) AlertOrange else ElectricCyan

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCard)
                        .border(1.dp, AlertOrange.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Record Year: $year",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                text = "$value$unit",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AlertOrange
                                )
                            )
                            Text(
                                text = "Anomaly: $diffStr$unit",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = diffColor
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .pointerInput(trendPoints) {
                    detectTapGestures { offset ->
                        val count = trendPoints.size
                        val stepX = size.width / count
                        val clickedIndex = (offset.x / stepX).toInt().coerceIn(0, count - 1)
                        selectedIndex = if (selectedIndex == clickedIndex) null else clickedIndex
                    }
                }
                .pointerInput(trendPoints) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            change.consume()
                            val count = trendPoints.size
                            val stepX = size.width / count
                            val dragIndex = (change.position.x / stepX).toInt().coerceIn(0, count - 1)
                            selectedIndex = dragIndex
                        }
                    )
                }
        ) {
            val count = trendPoints.size
            val minVal = (trendPoints.minOfOrNull { it.second } ?: (baselineTemp - 2.0)).coerceAtMost(baselineTemp - 1.0)
            val maxVal = (trendPoints.maxOfOrNull { it.second } ?: (baselineTemp + 2.0)).coerceAtLeast(baselineTemp + 1.0)
            val range = (maxVal - minVal).coerceAtLeast(0.5)

            val chartHeight = size.height - 35.dp.toPx()
            val stepX = size.width / count

            // Baseline Y coordinate
            val baselineNormalized = ((baselineTemp - minVal) / range).toFloat()
            val baselineY = chartHeight - (baselineNormalized * chartHeight) + 10.dp.toPx()

            // Draw baseline guideline
            drawLine(
                color = TextTertiary.copy(alpha = 0.5f),
                start = Offset(0f, baselineY),
                end = Offset(size.width, baselineY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
            )

            // Baseline label
            drawContext.canvas.nativeCanvas.drawText(
                "Norm",
                size.width - 24.dp.toPx(),
                baselineY - 4.dp.toPx(),
                android.graphics.Paint().apply {
                    color = android.graphics.Color.parseColor("#64748B")
                    textSize = 8.5.sp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )

            val points = trendPoints.mapIndexed { index, (_, value) ->
                val x = index * stepX + (stepX / 2)
                val normalizedY = ((value - minVal) / range).toFloat()
                val y = chartHeight - (normalizedY * chartHeight * animatedProgress.value) + 10.dp.toPx()
                Offset(x, y)
            }

            // Fill area below trend curve
            if (points.size > 1) {
                val fillPath = Path().apply {
                    moveTo(points.first().x, chartHeight + 10.dp.toPx())
                    lineTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val cx = (p1.x + p2.x) / 2
                        cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                    }
                    lineTo(points.last().x, chartHeight + 10.dp.toPx())
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AlertOrange.copy(alpha = 0.35f),
                            AlertYellow.copy(alpha = 0.1f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height
                    )
                )

                // Draw line path
                val linePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val cx = (p1.x + p2.x) / 2
                        cubicTo(cx, p1.y, cx, p2.y, p2.x, p2.y)
                    }
                }

                drawPath(
                    path = linePath,
                    brush = Brush.horizontalGradient(
                        colors = listOf(AlertYellow, AlertOrange, AlertRed)
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Draw active cursor crosshair
            val currentSel = selectedIndex
            if (currentSel != null && currentSel in points.indices) {
                val selPoint = points[currentSel]
                drawLine(
                    color = AlertOrange.copy(alpha = 0.8f),
                    start = Offset(selPoint.x, 8.dp.toPx()),
                    end = Offset(selPoint.x, size.height - 20.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
                drawCircle(
                    color = AlertOrange.copy(alpha = 0.3f),
                    radius = 12.dp.toPx(),
                    center = selPoint
                )
            }

            // Draw data points & value labels
            points.forEachIndexed { i, p ->
                val (year, value) = trendPoints[i]
                val isSelected = i == currentSel

                // Node marker
                drawCircle(
                    color = if (isSelected) AlertOrange else AlertYellow.copy(alpha = 0.6f),
                    radius = if (isSelected) 7.dp.toPx() else 4.dp.toPx(),
                    center = p
                )
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 3.5.dp.toPx() else 2.dp.toPx(),
                    center = p
                )

                // Temperature value
                val valText = if (unit == "°C") "${String.format("%.1f", value)}°" else "${value.toInt()}"
                drawContext.canvas.nativeCanvas.drawText(
                    valText,
                    p.x,
                    p.y - (if (isSelected) 10.dp.toPx() else 6.dp.toPx()),
                    android.graphics.Paint().apply {
                        color = if (isSelected) android.graphics.Color.parseColor("#FB923C") else android.graphics.Color.WHITE
                        textSize = if (isSelected) 11.sp.toPx() else 9.5.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = isSelected
                    }
                )

                // Year text
                drawContext.canvas.nativeCanvas.drawText(
                    year,
                    p.x,
                    size.height - 4.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = if (isSelected) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#94A3B8")
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = isSelected
                    }
                )
            }
        }
    }
}
