package com.example.data.smartcity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Warning
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
fun SmartCityScreen(
    currentLocation: CityLocation,
    canonicalRepo: CanonicalWeatherRepository,
    modifier: Modifier = Modifier
) {
    var cityData by remember { mutableStateOf<SmartCityDashboardData?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentLocation) {
        isLoading = true
        cityData = SmartCityRepository.getSmartCityData(currentLocation, canonicalRepo)
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
                        .background(CyanLight.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.LocationCity, contentDescription = "Smart City", tint = CyanLight, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "SMART CITY & URBAN WEATHER",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Color.White
                    )
                    Text(
                        text = "Ward Resilience & Heat Island Intelligence",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanLight)
            }
        } else {
            cityData?.let { data ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceNavy)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(text = data.cityName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        CityMetric("Air Quality (AQI)", "${data.overallAirQuality}")
                        CityMetric("Heat Island Index", "${data.heatIslandIndex}°C")
                    }

                    Divider(color = SurfaceBorder)

                    Text(text = "Traffic Advisory: ${data.trafficAdvisory}", style = MaterialTheme.typography.bodyMedium, color = CyanLight)

                    Text(
                        text = "Ward Resilience Breakdown",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    data.wardRisks.forEach { ward ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = ward.wardName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                Text(text = "AQI: ${ward.airQualityIndex}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Text(text = "Heat Risk: ${ward.heatRiskLevel} | Waterlogging: ${ward.waterloggingRisk}", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(text = "Traffic: ${ward.trafficImpact}", style = MaterialTheme.typography.labelSmall, color = CyanLight)
                        }
                    }

                    Text(
                        text = "Source: ${data.dataSource}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun CityMetric(label: String, value: String) {
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
