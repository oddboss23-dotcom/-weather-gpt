package com.example.data.climate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.canonical.CanonicalWeatherRepository
import com.example.data.model.CityLocation
import com.example.service.HapticManager
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClimateScreen(
    currentLocation: CityLocation,
    canonicalRepo: CanonicalWeatherRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedVariable by remember { mutableStateOf(ClimateVariable.TEMPERATURE) }
    var dataset by remember { mutableStateOf<HistoricalWeatherDataset?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    val context = androidx.compose.ui.platform.LocalContext.current

    val cal = Calendar.getInstance()
    val endDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    cal.add(Calendar.DAY_OF_YEAR, -30)
    val startDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)

    LaunchedEffect(currentLocation, selectedVariable) {
        isLoading = true
        dataset = ClimateRepository.fetchHistoricalData(
            lat = currentLocation.latitude,
            lon = currentLocation.longitude,
            locationName = currentLocation.name,
            variable = selectedVariable,
            startDate = startDateStr,
            endDate = endDateStr
        )
        isLoading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ElectricCyan.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Timeline, contentDescription = "Climate", tint = ElectricCyan, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "CLIMATE & HISTORICAL ANALYSIS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Color.White
                    )
                    Text(
                        text = "Location: ${currentLocation.name} • 30-Day Reanalysis",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Variable Selector Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClimateVariable.values().forEach { variable ->
                val isSelected = selectedVariable == variable
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        HapticManager.selectionChanged(context)
                        selectedVariable = variable
                    },
                    label = { Text(variable.title) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricCyan,
                        selectedLabelColor = DeepNavyBg,
                        containerColor = SurfaceNavy,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricCyan)
            }
        } else {
            dataset?.let { data ->
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Summary Card
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceNavy)
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = data.variable, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Surface(
                                    color = AlertGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Coverage: ${data.dataCoveragePercent}%",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AlertGreen
                                    )
                                }
                            }

                            Divider(color = SurfaceBorder)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                MetricBox("Average", "${data.averageValue} ${data.dataPoints.firstOrNull()?.unit ?: ""}")
                                MetricBox("Maximum", "${data.maxValue} ${data.dataPoints.firstOrNull()?.unit ?: ""}")
                                MetricBox("Minimum", "${data.minValue} ${data.dataPoints.firstOrNull()?.unit ?: ""}")
                            }

                            Text(
                                text = "Trend Assessment: ${data.trendDescription}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Text(
                                text = "Source: ${data.source}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Historical Timeline Records",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    items(data.dataPoints) { pt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Date", tint = CyanLight, modifier = Modifier.size(18.dp))
                                Text(text = pt.date, style = MaterialTheme.typography.bodyMedium, color = Color.White)
                            }
                            Text(
                                text = "${pt.value} ${pt.unit}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricBox(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}
