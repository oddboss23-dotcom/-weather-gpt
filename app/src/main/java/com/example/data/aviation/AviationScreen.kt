package com.example.data.aviation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WindPower
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
fun AviationScreen(
    currentLocation: CityLocation,
    canonicalRepo: CanonicalWeatherRepository,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var briefing by remember { mutableStateOf<AviationBriefing?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentLocation) {
        isLoading = true
        briefing = AviationRepository.getAviationBriefing(currentLocation, canonicalRepo)
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
                        .background(ElectricCyan.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.AirplanemodeActive, contentDescription = "Aviation", tint = ElectricCyan, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "AVIATION WEATHER INTELLIGENCE",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Color.White
                    )
                    Text(
                        text = "Aerodrome Briefing & Model Guidance",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ElectricCyan)
            }
        } else {
            briefing?.let { b ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceNavy)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = b.airportCode, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        Surface(
                            color = if (b.flightCategory.startsWith("VFR")) AlertGreen.copy(alpha = 0.15f) else AlertOrange.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = b.flightCategory,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (b.flightCategory.startsWith("VFR")) AlertGreen else AlertOrange
                            )
                        }
                    }

                    Text(text = b.airportName, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                    Divider(color = SurfaceBorder)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AviationMetric("Visibility", "${b.visibilityKm} km")
                        AviationMetric("Wind Speed", "${b.windSpeedKmh} km/h")
                        AviationMetric("Wind Direction", "${b.windDirectionDeg}°")
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AviationMetric("Wind Gusts", "${b.windGustKmh} km/h")
                        AviationMetric("Temperature", "${b.temperatureC}°C")
                        AviationMetric("Pressure", "${b.pressureHpa} hPa")
                    }

                    Divider(color = SurfaceBorder)

                    Text(text = "Ceiling & Clouds: ${b.ceilingCloudCoverage}", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    Text(text = "Convective Hazard: ${b.thunderstormRisk}", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)

                    Text(
                        text = "Source: ${b.modelGuidanceSource}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun AviationMetric(label: String, value: String) {
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
