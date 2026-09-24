package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.DisasterAlert
import com.example.data.model.IndianLanguage
import com.example.data.model.UserMode
import com.example.data.model.WeatherData
import com.example.data.model.WeatherInsight
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
import com.example.ui.theme.WeatherBlue

@Composable
fun TopWeatherBar(
    currentLocation: String,
    selectedLanguage: IndianLanguage,
    selectedMode: UserMode,
    isRealTime: Boolean,
    onLocationClick: () -> Unit,
    onGpsClick: () -> Unit,
    onLanguageSelected: (IndianLanguage) -> Unit,
    onModeSelected: (UserMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var langMenuExpanded by remember { mutableStateOf(false) }
    var modeMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DeepNavyBg)
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        // Master Header Row: Title & Tagline + Actions (Section 6)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding & Tagline
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "WEATHERGPT",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        ),
                        color = ElectricCyan
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "AI v3.6",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = CyanLight
                        )
                    }
                }
                Text(
                    text = "Smart Weather. Safer Tomorrow.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = TextSecondary
                )
            }

            // Top-Right Control Pills: Language & Persona Mode
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Language Pill
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SurfaceCard)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                            .clickable { langMenuExpanded = true }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Language",
                            tint = ElectricCyan,
                            modifier = Modifier.size(13.dp)
                        )
                        val langHeaderBadge = when (selectedLanguage) {
                            IndianLanguage.ENGLISH -> "ENG"
                            IndianLanguage.HINDI -> "हिंदी"
                            IndianLanguage.MARATHI -> "मराठी"
                            IndianLanguage.TAMIL -> "தமிழ்"
                            IndianLanguage.BENGALI -> "বাংলা"
                            IndianLanguage.TELUGU -> "తెలుగు"
                            IndianLanguage.GUJARATI -> "ગુજરાતી"
                            IndianLanguage.KANNADA -> "ಕನ್ನಡ"
                            IndianLanguage.MALAYALAM -> "മലയാളം"
                            IndianLanguage.PUNJABI -> "ਪੰਜਾਬੀ"
                            IndianLanguage.ODIA -> "ଓଡ଼ିଆ"
                            IndianLanguage.ASSAMESE -> "অসমীয়া"
                        }
                        Text(
                            text = langHeaderBadge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = langMenuExpanded,
                        onDismissRequest = { langMenuExpanded = false },
                        modifier = Modifier.background(SurfaceNavy)
                    ) {
                        IndianLanguage.values().forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(lang.displayName, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                        Text(lang.nativeName, color = CyanAccent, style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                onClick = {
                                    onLanguageSelected(lang)
                                    langMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Persona / Mode Pill
                Box {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard)
                            .border(1.dp, SurfaceBorder, CircleShape)
                            .clickable { modeMenuExpanded = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (selectedMode) {
                                UserMode.FARMER_KRISHI -> Icons.Default.Agriculture
                                UserMode.DISASTER_MANAGEMENT -> Icons.Default.Shield
                                else -> Icons.Default.Person
                            },
                            contentDescription = "User Mode",
                            tint = if (selectedMode == UserMode.FARMER_KRISHI) AlertGreen else if (selectedMode == UserMode.DISASTER_MANAGEMENT) AlertRed else ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = modeMenuExpanded,
                        onDismissRequest = { modeMenuExpanded = false },
                        modifier = Modifier.background(SurfaceNavy)
                    ) {
                        UserMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(mode.title, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                        Text(mode.subtitle, color = TextSecondary, style = MaterialTheme.typography.labelSmall)
                                    }
                                },
                                onClick = {
                                    onModeSelected(mode)
                                    modeMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Prominent Location Bar (Clickable with dropdown indicator)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard.copy(alpha = 0.5f))
                .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                .clickable { onLocationClick() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$currentLocation ▼",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // GPS Subline / Detection trigger
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(DeepNavyBg)
                    .clickable { onGpsClick() }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "GPS",
                    tint = CyanAccent,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "Detect GPS",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = CyanAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Provenance & Live Data Status Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LiveStatusBadge(isRealTime = isRealTime)
            Text(
                text = "IMD AWS & Open-Meteo Grid",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun LiveStatusBadge(isRealTime: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isRealTime) AlertGreen else AlertOrange)
        )
        Text(
            text = if (isRealTime) "● LIVE DATA • Updated 5 min ago" else "● OFFLINE • Cached",
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = if (isRealTime) AlertGreen else AlertOrange
        )
    }
}

@Composable
fun CurrentWeatherHeroCard(
    weather: WeatherData,
    confidencePercent: Int? = null,
    confidenceBasis: List<String> = emptyList(),
    modifier: Modifier = Modifier
) {
    var showWhyConfidence by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Weather Display with condition-matching visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            SurfaceNavy,
                            DeepNavyBg
                        )
                    )
                )
                .border(1.dp, ElectricCyan.copy(alpha = 0.25f), RoundedCornerShape(24.dp))
                .padding(vertical = 18.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Weather condition icon & visual badge
                val conditionLower = weather.conditionDescription.lowercase()
                val (condIcon, condColor) = when {
                    conditionLower.contains("rain") || conditionLower.contains("drizzle") || conditionLower.contains("shower") ->
                        Pair(Icons.Default.WaterDrop, ElectricCyan)
                    conditionLower.contains("thunder") ->
                        Pair(Icons.Default.Thunderstorm, AlertOrange)
                    conditionLower.contains("cloud") || conditionLower.contains("overcast") ->
                        Pair(Icons.Default.Grain, CyanLight)
                    conditionLower.contains("fog") || conditionLower.contains("mist") ->
                        Pair(Icons.Default.Air, TextSecondary)
                    else ->
                        Pair(Icons.Default.WbSunny, AlertYellow)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = condIcon,
                        contentDescription = weather.conditionDescription,
                        tint = condColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = weather.conditionDescription,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                }

                // Bold Temperature
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${weather.temperatureC.toInt()}",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 76.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-2).sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "°C",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Normal,
                            fontSize = 32.sp
                        ),
                        color = ElectricCyan,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }

                // Feels like + Min-Max
                Text(
                    text = "Feels like ${weather.feelsLikeC.toInt()}°C • H: ${weather.tempMaxC.toInt()}° L: ${weather.tempMinC.toInt()}°",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.5.sp
                    ),
                    color = TextSecondary
                )

                // Freshness and confidence tag
                if (confidencePercent != null && confidencePercent > 0) {
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SurfaceCard.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(AlertGreen)
                        )
                        Text(
                            text = "Confidence: $confidencePercent%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = CyanLight
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextTertiary
                        )
                        Text(
                            text = "Why?",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                            ),
                            color = ElectricCyan,
                            modifier = Modifier.clickable { showWhyConfidence = true }
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SurfaceCard.copy(alpha = 0.6f))
                            .padding(horizontal = 10.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(TextTertiary)
                        )
                        Text(
                            text = "Confidence unavailable",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Confidence explanation dialog
        if (showWhyConfidence) {
            AlertDialog(
                onDismissRequest = { showWhyConfidence = false },
                title = {
                    Text(
                        text = "Forecast Confidence: ${confidencePercent ?: 0}%",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Confidence is based on:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = ElectricCyan
                        )
                        val factors = if (confidenceBasis.isNotEmpty()) {
                            confidenceBasis
                        } else {
                            listOf(
                                "• Data freshness: Recent numerical telemetry",
                                "• Forecast agreement: Multi-model atmospheric convergence",
                                "• Data completeness: Verified atmospheric profile"
                            )
                        }
                        factors.forEach { factor ->
                            val cleanFactor = if (factor.startsWith("•") || factor.startsWith("✓") || factor.startsWith("⚠") || factor.startsWith("ℹ")) factor else "• $factor"
                            Text(
                                text = cleanFactor,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = TextSecondary
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showWhyConfidence = false }) {
                        Text("Got It", color = ElectricCyan, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SurfaceNavy,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Clean Responsive Metrics Grid (7 Metrics, NO DUPLICATE UV CARD)
        // Row 1: Precipitation and UV Index
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Precipitation Card
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceCard.copy(alpha = 0.6f))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "PRECIPITATION",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = TextTertiary
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${weather.rainProbabilityPercent}%",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                        val rainLabel = if (weather.rainProbabilityPercent < 25) "Low" else if (weather.rainProbabilityPercent < 60) "Moderate" else "High"
                        val rainColor = if (weather.rainProbabilityPercent < 25) AlertGreen else if (weather.rainProbabilityPercent < 60) AlertYellow else AlertOrange
                        Text(
                            text = rainLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = rainColor
                        )
                    }
                    Text(
                        text = "${weather.expectedRainfallMm} mm expected",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = ElectricCyan
                    )
                }
            }

            // UV Index Card (Sole UV representation with semantic category)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceCard.copy(alpha = 0.6f))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "UV INDEX",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = TextTertiary
                    )
                    val uvCategory = when {
                        weather.uvIndex <= 2.9 -> "Low"
                        weather.uvIndex <= 5.9 -> "Moderate"
                        weather.uvIndex <= 7.9 -> "High"
                        weather.uvIndex <= 10.9 -> "Very High"
                        else -> "Extreme"
                    }
                    val uvColor = when {
                        weather.uvIndex <= 2.9 -> AlertGreen
                        weather.uvIndex <= 5.9 -> AlertYellow
                        weather.uvIndex <= 7.9 -> AlertOrange
                        else -> AlertRed
                    }
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${weather.uvIndex}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = Color.White
                        )
                        Text(
                            text = uvCategory,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = uvColor
                        )
                    }
                    Text(
                        text = if (weather.uvIndex > 6.0) "Protection required" else "Safe exposure",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = uvColor
                    )
                }
            }
        }

        // Row 2: Humidity, Wind, Pressure
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SleekMetricMiniCard(
                icon = Icons.Default.WaterDrop,
                label = "HUMIDITY",
                value = "${weather.humidityPercent}%",
                subValue = if (weather.humidityPercent > 70) "Humid" else "Optimal",
                accentColor = ElectricCyan,
                modifier = Modifier.weight(1f)
            )
            SleekMetricMiniCard(
                icon = Icons.Default.Air,
                label = "WIND",
                value = "${weather.windSpeedKmh.toInt()} km/h",
                subValue = weather.windDirectionText,
                accentColor = CyanAccent,
                modifier = Modifier.weight(1f)
            )
            SleekMetricMiniCard(
                icon = Icons.Default.Compress,
                label = "PRESSURE",
                value = "${weather.pressureHpa.toInt()}",
                subValue = "hPa",
                accentColor = ElectricTeal,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Visibility, Sunset, Sunrise (NO DUPLICATE UV)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SleekMetricMiniCard(
                icon = Icons.Default.Visibility,
                label = "VISIBILITY",
                value = "${weather.visibilityKm.toInt()} km",
                subValue = if (weather.visibilityKm >= 10.0) "Excellent" else "Moderate",
                accentColor = CyanLight,
                modifier = Modifier.weight(1f)
            )
            SleekMetricMiniCard(
                icon = Icons.Default.WbTwilight,
                label = "SUNSET",
                value = weather.sunsetTime,
                subValue = "Dusk",
                accentColor = AlertOrange,
                modifier = Modifier.weight(1f)
            )
            SleekMetricMiniCard(
                icon = Icons.Default.WbSunny,
                label = "SUNRISE",
                value = weather.sunriseTime,
                subValue = "Dawn",
                accentColor = AlertYellow,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SleekMetricMiniCard(
    icon: ImageVector,
    label: String,
    value: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard.copy(alpha = 0.5f))
            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextTertiary
                )
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = Color.White
            )
            Text(
                text = subValue,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = accentColor
            )
        }
    }
}

/**
 * Polished Horizontal Hourly Forecast Strip (Section 9)
 * Highlights the current hour, next 6-12 hours immediately understandable.
 */
@Composable
fun HorizontalHourlyForecastStrip(
    hourlyList: List<com.example.data.model.HourlyForecast>,
    modifier: Modifier = Modifier
) {
    if (hourlyList.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceNavy)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(14.dp),
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
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "HOURLY FORECAST",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.5.sp
                    ),
                    color = CyanLight
                )
            }
            Text(
                text = "Next 12 Hours",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextTertiary
            )
        }

        // Horizontal scrolling items
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
        ) {
            items(hourlyList.size.coerceAtMost(12)) { index ->
                val item = hourlyList[index]
                val isNow = index == 0
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isNow) ElectricCyan.copy(alpha = 0.16f) else SurfaceCard.copy(alpha = 0.5f))
                        .border(
                            width = if (isNow) 1.dp else 0.5.dp,
                            color = if (isNow) ElectricCyan else SurfaceBorder,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isNow) "Now" else item.timeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isNow) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp
                            ),
                            color = if (isNow) ElectricCyan else TextSecondary
                        )

                        // Weather Icon
                        val isRain = item.rainProbabilityPercent > 35
                        Icon(
                            imageVector = if (isRain) Icons.Default.WaterDrop else Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = if (isRain) ElectricCyan else AlertYellow,
                            modifier = Modifier.size(18.dp)
                        )

                        Text(
                            text = "${item.temperatureC.toInt()}°",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = Color.White
                        )

                        // Rain probability
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "💧 ${item.rainProbabilityPercent}%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (item.rainProbabilityPercent > 50) AlertOrange else TextTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AiInsightCard(
    insight: WeatherInsight,
    onSpeakClick: () -> Unit = {},
    onActionClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedMitigationTab by remember { mutableStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(SurfaceNavy)
            .border(1.dp, ElectricCyan.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header: ✨ AI INTELLIGENCE & Status Badge (Section 10)
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
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElectricCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = DeepNavyBg,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "AI INTELLIGENCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            fontSize = 11.5.sp
                        ),
                        color = CyanLight
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AlertGreen.copy(alpha = 0.15f))
                            .border(0.5.dp, AlertGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = AlertGreen
                        )
                    }
                    IconButton(onClick = onSpeakClick, modifier = Modifier.size(26.dp)) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak",
                            tint = CyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Concise explanation
            Text(
                text = insight.summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp,
                    fontSize = 13.5.sp
                ),
                color = TextHighlight
            )

            // Contextual Action Buttons based on conditions (Section 10)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(ElectricCyan.copy(alpha = 0.15f))
                        .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .clickable { onActionClick("Detailed Forecast") }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Detailed Forecast",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = CyanLight
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AlertGreen.copy(alpha = 0.15f))
                        .border(1.dp, AlertGreen.copy(alpha = 0.4f), RoundedCornerShape(50))
                        .clickable { onActionClick("Crop Advisory") }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Crop Advisory",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = AlertGreen
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SurfaceCard)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(50))
                        .clickable { expanded = !expanded }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (expanded) "Hide Plan" else "Action Plan",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = TextSecondary
                    )
                }
            }

            // Expandable Multi-Stakeholder Guidance
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabs = listOf("Citizens", "Farmers", "Authorities", "Disaster Mgmt")
                    ScrollableTabRow(
                        selectedTabIndex = selectedMitigationTab,
                        containerColor = SurfaceNavy,
                        contentColor = ElectricCyan,
                        edgePadding = 0.dp,
                        divider = {},
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    ) {
                        tabs.forEachIndexed { idx, label ->
                            Tab(
                                selected = selectedMitigationTab == idx,
                                onClick = { selectedMitigationTab = idx },
                                text = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (selectedMitigationTab == idx) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 11.sp
                                        ),
                                        color = if (selectedMitigationTab == idx) ElectricCyan else TextSecondary
                                    )
                                }
                            )
                        }
                    }

                    val mitigationText = when (selectedMitigationTab) {
                        0 -> insight.mitigationCitizen
                        1 -> insight.mitigationFarmer
                        2 -> insight.mitigationAuthority
                        else -> insight.mitigationDisaster
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceNavy.copy(alpha = 0.8f))
                            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = mitigationText,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FarmerAdvisoryCard(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF061E1A)) // Sleek subtle emerald slate
            .border(1.dp, AlertGreen.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.Agriculture,
                        contentDescription = null,
                        tint = AlertGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "KRISHI AGRO-METEOROLOGY",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AlertGreen
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AlertGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("Kharif / Rabi", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = AlertGreen)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FarmerAdvisoryRow(
                    title = "Irrigation Timing",
                    detail = if (weather.rainProbabilityPercent > 50)
                        "Withhold scheduled irrigation: ${weather.expectedRainfallMm} mm precipitation anticipated."
                    else
                        "Optimal window for light evening drip/sprinkler irrigation.",
                    isCaution = weather.rainProbabilityPercent > 50
                )
                FarmerAdvisoryRow(
                    title = "Chemical Spraying",
                    detail = if (weather.windSpeedKmh > 20 || weather.rainProbabilityPercent > 40)
                        "Avoid foliar pesticide spraying due to drift and rain wash-off risk."
                    else
                        "Favorable conditions for scheduled crop nutrient/pest sprays.",
                    isCaution = weather.rainProbabilityPercent > 40
                )
            }
        }
    }
}

@Composable
private fun FarmerAdvisoryRow(
    title: String,
    detail: String,
    isCaution: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceNavy.copy(alpha = 0.5f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isCaution) AlertOrange else AlertGreen)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isCaution) AlertOrange else CyanLight
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextPrimary
            )
        }
    }
}

@Composable
fun DisasterAlertCard(
    alert: DisasterAlert,
    modifier: Modifier = Modifier,
    onDispatchN8n: ((DisasterAlert) -> Unit)? = null,
    isDispatching: Boolean = false
) {
    val (badgeBg, badgeText, badgeColor) = when (alert.severity) {
        AlertSeverity.RED -> Triple(AlertRed.copy(alpha = 0.15f), "RED ALERT • TAKE ACTION", AlertRed)
        AlertSeverity.ORANGE -> Triple(AlertOrange.copy(alpha = 0.15f), "ORANGE ALERT • BE PREPARED", AlertOrange)
        AlertSeverity.YELLOW -> Triple(AlertYellow.copy(alpha = 0.15f), "YELLOW WATCH • BE AWARE", AlertYellow)
        AlertSeverity.GREEN -> Triple(AlertGreen.copy(alpha = 0.15f), "GREEN • NORMAL / SAFE", AlertGreen)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceNavy)
            .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .border(0.5.dp, badgeColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = badgeColor
                    )
                }

                Text(
                    text = alert.location,
                    style = MaterialTheme.typography.labelSmall,
                    color = CyanLight
                )
            }

            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )

            Text(
                text = alert.validTime,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .padding(10.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Expected Impact:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = TextSecondary)
                    Text(alert.expectedImpact, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Recommended Action:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = CyanLight)
                    Text(alert.recommendedAction, style = MaterialTheme.typography.bodySmall, color = TextHighlight)
                }
            }

            if (onDispatchN8n != null && (alert.severity == AlertSeverity.RED || alert.severity == AlertSeverity.ORANGE || alert.severity == AlertSeverity.YELLOW)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "n8n Automation Node Connected",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextTertiary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDispatching) SurfaceCard else ElectricCyan.copy(alpha = 0.15f))
                            .border(0.5.dp, if (isDispatching) SurfaceBorder else ElectricCyan, RoundedCornerShape(8.dp))
                            .clickable(enabled = !isDispatching) { onDispatchN8n(alert) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = if (isDispatching) "Broadcasting..." else "⚡ Push to n8n Webhook",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = if (isDispatching) TextSecondary else ElectricCyan
                        )
                    }
                }
            }
        }
    }
}
