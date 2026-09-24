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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.nwp.DiurnalPeriod
import com.example.data.nwp.Forecast24H
import com.example.data.nwp.Forecast48H
import com.example.data.nwp.ForecastBustReport
import com.example.data.nwp.ForecastDaySummary
import com.example.data.nwp.ForecastEventMarker
import com.example.data.nwp.ForecastPoint
import com.example.data.nwp.ForecastStabilityClass
import com.example.data.nwp.TrendDirection
import com.example.ui.components.HourlyRainfallBarChart
import com.example.ui.components.HourlyTemperatureLineChart
import com.example.ui.components.WhyThisForecastCard
import com.example.ui.components.WindVelocityVectorChart
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
fun ForecastScreen(
    viewModel: WeatherGPTViewModel,
    modifier: Modifier = Modifier
) {
    val weather by viewModel.weatherData.collectAsState()
    val hourlyForecast by viewModel.hourlyForecast.collectAsState()
    val canonicalSnapshot by viewModel.canonicalSnapshot.collectAsState()
    val forecast24H by viewModel.forecast24H.collectAsState()
    val forecast48H by viewModel.forecast48H.collectAsState()
    val bustReport by viewModel.forecastBustReport.collectAsState()

    var selectedHorizonTab by remember { mutableStateOf(0) } // 0 = 24H Operational, 1 = 48H Planning
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Horizon Switcher Tab (24H vs 48H)
        TabRow(
            selectedTabIndex = selectedHorizonTab,
            containerColor = SurfaceNavy,
            contentColor = ElectricCyan,
            divider = {},
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(50))
                .padding(3.dp)
        ) {
            Tab(
                selected = selectedHorizonTab == 0,
                onClick = { selectedHorizonTab = 0 },
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selectedHorizonTab == 0) SurfaceCard else Color.Transparent),
                text = {
                    Text(
                        text = "24H OPERATIONAL HOURLY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            fontSize = 10.5.sp
                        ),
                        color = if (selectedHorizonTab == 0) ElectricCyan else TextTertiary,
                        maxLines = 1
                    )
                }
            )
            Tab(
                selected = selectedHorizonTab == 1,
                onClick = { selectedHorizonTab = 1 },
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (selectedHorizonTab == 1) SurfaceCard else Color.Transparent),
                text = {
                    Text(
                        text = "48H PLANNING & TRENDS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                            fontSize = 10.5.sp
                        ),
                        color = if (selectedHorizonTab == 1) ElectricCyan else TextTertiary,
                        maxLines = 1
                    )
                }
            )
        }

        // Forecast Explanation Layer ("Why This Forecast?")
        WhyThisForecastCard(
            weather = weather,
            snapshot = canonicalSnapshot
        )

        // Render appropriate view based on tab selection
        if (selectedHorizonTab == 0) {
            // 24H Operational Forecast
            Forecast24HView(
                forecast24H = forecast24H,
                hourlyFallback = hourlyForecast.take(24)
            )
        } else {
            // 48H Extended Planning View
            Forecast48HView(
                forecast48H = forecast48H,
                bustReport = bustReport
            )
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

/**
 * 24H Detailed Operational Forecast with Interactive Hourly Inspector & Event Markers.
 */
@Composable
private fun Forecast24HView(
    forecast24H: Forecast24H?,
    hourlyFallback: List<com.example.data.model.HourlyForecast>
) {
    val points = forecast24H?.points ?: emptyList()
    var selectedHourIndex by remember { mutableStateOf(0) }
    val selectedPoint = points.getOrNull(selectedHourIndex)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Operational 24H Header Metrics
        if (forecast24H != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceNavy)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "24H RANGE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                        color = TextTertiary
                    )
                    Text(
                        text = "${forecast24H.maxTemp.roundToInt()}° / ${forecast24H.minTemp.roundToInt()}°C",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextHighlight
                    )
                }
                Column {
                    Text(
                        text = "TOTAL PRECIP",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                        color = TextTertiary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f mm", forecast24H.totalRainMm),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = CyanLight
                    )
                }
                Column {
                    Text(
                        text = "CONFIDENCE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp, fontWeight = FontWeight.Bold),
                        color = TextTertiary
                    )
                    Text(
                        text = "${forecast24H.confidenceScore}% (${forecast24H.confidenceCategory})",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (forecast24H.confidenceScore >= 75) AlertGreen else AlertYellow
                    )
                }
            }
        }

        // Active Event Markers Banner (if detected)
        if (forecast24H != null && forecast24H.activeEvents.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceNavy)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AlertOrange, modifier = Modifier.size(16.dp))
                    Text(
                        text = "AUTOMATIC 24H EVENT MARKERS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = AlertOrange
                    )
                }
                forecast24H.activeEvents.forEach { event ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCard.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = event.type.emoji, fontSize = 14.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = event.title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            Text(text = event.description, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextSecondary)
                        }
                        Text(text = event.supportingData, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = ElectricCyan)
                    }
                }
            }
        }

        // Interactive Timeline Selector (Tap any hour)
        if (points.isNotEmpty()) {
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
                    text = "HOURLY TIMELINE (TAP TO INSPECT)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp),
                    color = ElectricCyan
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    points.forEachIndexed { index, pt ->
                        val isSelected = index == selectedHourIndex
                        Column(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.22f) else SurfaceCard.copy(alpha = 0.5f))
                                .border(1.dp, if (isSelected) ElectricCyan else Color.Transparent, RoundedCornerShape(14.dp))
                                .clickable { selectedHourIndex = index }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = pt.timeLabel, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp), color = if (isSelected) ElectricCyan else TextTertiary)
                            Text(text = "${pt.temperature.roundToInt()}°", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            if (pt.precipProbability > 0) {
                                Text(text = "${pt.precipProbability}%", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.SemiBold), color = CyanLight)
                            }
                        }
                    }
                }

                // Selected Hour Details Card
                if (selectedPoint != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SurfaceCard)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hour ${selectedPoint.timeLabel} • ${selectedPoint.condition}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextHighlight
                            )
                            Text(
                                text = "Horizon +${selectedPoint.forecastHorizon}h",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextTertiary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            DetailMiniPill("Temp", "${selectedPoint.temperature.roundToInt()}°C", "Feels ${selectedPoint.feelsLike.roundToInt()}°")
                            DetailMiniPill("Rain Prob", "${selectedPoint.precipProbability}%", String.format(Locale.US, "%.1f mm", selectedPoint.precipAmount))
                            DetailMiniPill("Wind", "${selectedPoint.windSpeed.roundToInt()} km/h", "${selectedPoint.windDirection}°")
                            DetailMiniPill("Humidity", "${selectedPoint.humidity}%", "${selectedPoint.pressure.roundToInt()} hPa")
                        }

                        if (selectedPoint.eventMarkers.isNotEmpty()) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                selectedPoint.eventMarkers.forEach { ev ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AlertOrange.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "${ev.type.emoji} ${ev.title}", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AlertOrange)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Charts Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            HourlyTemperatureLineChart(hourlyList = hourlyFallback)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            HourlyRainfallBarChart(hourlyList = hourlyFallback)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            WindVelocityVectorChart(hourlyList = hourlyFallback)
        }
    }
}

/**
 * 48H High-Level Planning View with Trends, Diurnal Periods & Bust Detection.
 */
@Composable
private fun Forecast48HView(
    forecast48H: Forecast48H?,
    bustReport: ForecastBustReport?
) {
    if (forecast48H == null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceNavy)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Loading 48-Hour Extended Planning Guidance...", color = TextSecondary)
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // 48H Executive Trend Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "48H SYNOPTIC TRENDS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp),
                    color = ElectricCyan
                )
                Text(
                    text = forecast48H.modelAgreement,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = ElectricTeal
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TrendPill("Temperature", forecast48H.tempTrend, String.format(Locale.US, "%+.1f°C", forecast48H.tempTrendDeltaC))
                TrendPill("Rain Prob", forecast48H.rainTrend, "${forecast48H.rainTrendDeltaPercent}%")
                TrendPill("Wind", forecast48H.windTrend, String.format(Locale.US, "%+.1f km/h", forecast48H.windTrendDeltaKmh))
                TrendPill("Humidity", forecast48H.humidityTrend, "${forecast48H.humidityTrendDeltaPercent}%")
            }

            // Uncertainty & Confidence Metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard.copy(alpha = 0.6f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "ESTIMATED UNCERTAINTY", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
                    Text(text = forecast48H.uncertaintyRange, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "CONFIDENCE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
                    Text(text = "${forecast48H.confidenceScore}% (${forecast48H.confidenceCategory})", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = AlertGreen)
                }
            }
        }

        // Planning Block 1: NEXT 24 HOURS (TODAY)
        DayPlanningCard(daySummary = forecast48H.next24Hours)

        // Planning Block 2: 24–48 HOURS (TOMORROW)
        DayPlanningCard(daySummary = forecast48H.hours24To48)

        // Weather Condition Transitions & Hazard Windows
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "SYNOPTIC TRANSITIONS & HAZARDS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp),
                color = ElectricCyan
            )

            forecast48H.weatherTransitions.forEach { trans ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceCard.copy(alpha = 0.5f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(14.dp))
                    Text(text = trans, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp), color = TextSecondary)
                }
            }

            forecast48H.hazardWindows.forEach { hazard ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AlertOrange.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AlertOrange, modifier = Modifier.size(14.dp))
                    Text(text = hazard, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold), color = AlertOrange)
                }
            }
        }

        // Forecast Bust Detection Panel
        if (bustReport != null) {
            ForecastBustCard(bustReport = bustReport)
        }
    }
}

@Composable
private fun DayPlanningCard(daySummary: ForecastDaySummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceNavy)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = daySummary.dayName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextHighlight
                )
                Text(
                    text = daySummary.dateLabel,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${daySummary.maxTemp.roundToInt()}° / ${daySummary.minTemp.roundToInt()}°C",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyanLight.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Rain ${daySummary.rainProbability}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 9.sp),
                        color = CyanLight
                    )
                }
            }
        }

        // 4 Diurnal Periods (Morning, Afternoon, Evening, Night)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            daySummary.periods.forEach { period ->
                DiurnalPeriodRow(period = period)
            }
        }
    }
}

@Composable
private fun DiurnalPeriodRow(period: DiurnalPeriod) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.width(80.dp)) {
            Text(text = period.periodName, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
            Text(text = period.timeRange, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
        }

        Text(
            text = period.condition,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = TextSecondary,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (period.rainProbability > 0) {
                Text(
                    text = "${period.rainProbability}%",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                    color = CyanLight
                )
            }
            Text(
                text = "${period.tempAvg.roundToInt()}°C",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = TextHighlight
            )
        }
    }
}

@Composable
private fun ForecastBustCard(bustReport: ForecastBustReport) {
    val (borderTint, bgTint, iconTint) = when (bustReport.stabilityClass) {
        ForecastStabilityClass.FORECAST_BUST -> Triple(AlertRed, AlertRed.copy(alpha = 0.12f), AlertRed)
        ForecastStabilityClass.SIGNIFICANT_REVISION -> Triple(AlertOrange, AlertOrange.copy(alpha = 0.12f), AlertOrange)
        ForecastStabilityClass.MINOR_REVISION -> Triple(AlertYellow, AlertYellow.copy(alpha = 0.12f), AlertYellow)
        ForecastStabilityClass.STABLE -> Triple(AlertGreen, AlertGreen.copy(alpha = 0.12f), AlertGreen)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceNavy)
            .border(1.dp, borderTint.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .padding(16.dp),
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
                Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
                Text(
                    text = "FORECAST BUST & STABILITY ANALYSIS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp, fontSize = 10.sp),
                    color = TextPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(bgTint)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = bustReport.stabilityClass.label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    color = iconTint
                )
            }
        }

        Text(
            text = bustReport.explanation,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
            color = TextSecondary
        )

        // Snapshot comparison table
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCard.copy(alpha = 0.6f))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            bustReport.snapshots.forEach { snap ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = snap.horizonLabel, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = TextTertiary)
                    Text(text = "${snap.temperatureC.roundToInt()}°C", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    Text(text = "${snap.rainfallMm.roundToInt()}mm", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = CyanLight)
                }
            }
        }
    }
}

@Composable
private fun TrendPill(label: String, direction: TrendDirection, delta: String) {
    val (icon, tint) = when (direction) {
        TrendDirection.RISING -> Icons.AutoMirrored.Filled.TrendingUp to AlertOrange
        TrendDirection.FALLING -> Icons.AutoMirrored.Filled.TrendingDown to CyanLight
        TrendDirection.STEADY -> Icons.AutoMirrored.Filled.TrendingFlat to AlertGreen
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard.copy(alpha = 0.6f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(12.dp))
            Text(text = delta, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = tint)
        }
    }
}

@Composable
private fun DetailMiniPill(title: String, value: String, sub: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceNavy.copy(alpha = 0.7f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextTertiary)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = TextHighlight)
        Text(text = sub, style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp), color = TextSecondary)
    }
}
