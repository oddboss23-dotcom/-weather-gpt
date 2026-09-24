package com.example.data.marine

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Water
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
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun MarineScreen(
    currentLocation: CityLocation,
    canonicalRepo: CanonicalWeatherRepository,
    modifier: Modifier = Modifier
) {
    var briefing by remember { mutableStateOf<MarineBriefing?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentLocation) {
        isLoading = true
        briefing = MarineRepository.getMarineBriefing(currentLocation, canonicalRepo)
        isLoading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                        .background(CyanAccent.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Water, contentDescription = "Marine", tint = CyanAccent, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "MARINE & COASTAL INTELLIGENCE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Color.White
                    )
                    Text(
                        text = "Offshore State & Wave Forecasting",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanAccent)
            }
        } else {
            briefing?.let { m ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceNavy)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = m.portName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    Text(text = m.seaState, style = MaterialTheme.typography.bodyMedium, color = CyanLight)

                    Divider(color = SurfaceBorder)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MarineMetric("Wind Speed", "${m.windSpeedKmh} km/h")
                        MarineMetric("Direction", "${m.windDirectionDeg}°")
                        MarineMetric("Wave Height", if (m.waveHeightMeters != null) "${m.waveHeightMeters} m" else "N/A")
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MarineMetric("Pressure", "${m.pressureHpa} hPa")
                        MarineMetric("Temperature", "${m.temperatureC}°C")
                        MarineMetric("Status", "Operational")
                    }

                    Divider(color = SurfaceBorder)

                    Text(text = "Marine Advisory: ${m.marineHazardWarning}", style = MaterialTheme.typography.bodyMedium, color = AlertOrange)

                    Text(
                        text = "Source: ${m.dataSource}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun MarineMetric(label: String, value: String) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}
