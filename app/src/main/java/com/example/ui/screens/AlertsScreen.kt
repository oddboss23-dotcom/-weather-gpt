package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.DisasterAlert
import com.example.data.model.UserMode
import com.example.data.model.WeatherData
import com.example.service.HapticFeedbackManager
import com.example.service.HazardType
import com.example.service.RecipientRole
import com.example.service.SmartActionEngine
import com.example.service.SmartHazardCard
import com.example.ui.components.AlertRuleAutomationCard
import com.example.ui.components.DisasterAlertCard
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

@Composable
fun AlertsScreen(
    viewModel: WeatherGPTViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val alerts by viewModel.alerts.collectAsState()
    val weather by viewModel.weatherData.collectAsState()
    val n8nStatus by viewModel.n8nDispatchStatus.collectAsState()
    val isN8nDispatching by viewModel.isN8nDispatching.collectAsState()
    val n8nHealth by viewModel.n8nHealthStatus.collectAsState()
    val isDisasterResponseMode by viewModel.isDisasterResponseMode.collectAsState()
    val activeRedAlert by viewModel.activeRedAlert.collectAsState()
    val selectedUserMode by viewModel.selectedUserMode.collectAsState()

    // Trigger non-blocking health check when screen opens
    LaunchedEffect(Unit) {
        viewModel.checkN8nHealth()
    }

    // Trigger differentiated vibration patterns for severe alerts, flood warnings, and general weather alerts
    LaunchedEffect(activeRedAlert?.id) {
        val alert = activeRedAlert
        if (alert != null) {
            when {
                alert.isFloodAlert -> HapticFeedbackManager.triggerFloodWarning(context, isCritical = alert.severity == AlertSeverity.RED || alert.severity == AlertSeverity.ORANGE)
                alert.severity == AlertSeverity.RED -> HapticFeedbackManager.triggerSevereAlert(context)
                alert.severity == AlertSeverity.ORANGE || alert.severity == AlertSeverity.YELLOW -> HapticFeedbackManager.triggerWeatherAlert(context, alert.severity)
            }
        }
    }

    var selectedFilterIndex by remember { mutableStateOf(0) }
    val filters = listOf("All Alerts", "Red (Extreme)", "Orange (Severe)", "Yellow (Watch)", "Green (Safe)")

    val filteredAlerts = remember(alerts, selectedFilterIndex) {
        when (selectedFilterIndex) {
            1 -> alerts.filter { it.severity == AlertSeverity.RED }
            2 -> alerts.filter { it.severity == AlertSeverity.ORANGE }
            3 -> alerts.filter { it.severity == AlertSeverity.YELLOW }
            4 -> alerts.filter { it.severity == AlertSeverity.GREEN }
            else -> alerts
        }
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner Header & n8n Health Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AlertRed.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Emergency,
                        contentDescription = "Alerts Hub",
                        tint = AlertRed,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "DISASTER EARLY WARNING",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Real-time Multi-Hazard Bulletins & n8n Pipeline",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Lightweight n8n Health Status Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = n8nHealth,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (n8nHealth.contains("🟢")) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCard)
                        .clickable { viewModel.checkN8nHealth() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry check",
                        tint = ElectricCyan,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Check",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = ElectricCyan
                    )
                }
            }
        }

        // Persistent Disaster Response Mode Banner (Visible on RED alert)
        AnimatedVisibility(visible = isDisasterResponseMode && activeRedAlert != null) {
            val redAlert = activeRedAlert
            if (redAlert != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    AlertRed.copy(alpha = 0.35f),
                                    SurfaceNavy
                                )
                            )
                        )
                        .border(1.5.dp, AlertRed, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(AlertRed)
                                )
                                Text(
                                    text = "DISASTER RESPONSE MODE ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = AlertRed
                                )
                            }
                            IconButton(
                                onClick = { viewModel.dismissDisasterMode() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = redAlert.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        Text(
                            text = "📍 Affected Area: ${redAlert.location}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = TextHighlight
                        )

                        Text(
                            text = "⏱ Timing: ${redAlert.validTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )

                        Text(
                            text = "⚠️ Risk: ${redAlert.expectedImpact}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AlertRed.copy(alpha = 0.2f))
                                .padding(10.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "RECOMMENDED ACTION:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AlertRed
                                )
                                Text(
                                    text = redAlert.recommendedAction,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Nearest Cyclone Shelter",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "Govt High School (1.4 km)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = ElectricCyan
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Control Room",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "NDRF Desk: 1078",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = AlertRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // n8n Emergency Dispatch Pipeline Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, if (n8nStatus != null) ElectricCyan.copy(alpha = 0.5f) else SurfaceBorder, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan)
                        )
                        Text(
                            text = "n8n AUTOMATION LAYER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                fontSize = 10.sp
                            ),
                            color = ElectricCyan
                        )
                    }
                    Text(
                        text = "ODDCSK CLOUD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        ),
                        color = TextTertiary
                    )
                }

                Text(
                    text = "Production Webhook: https://oddcsk.app.n8n.cloud/webhook/weathergpt/alerts",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trigger test webhook event:",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextTertiary
                    )
                    Button(
                        onClick = {
                            viewModel.dispatchTestRedAlert()
                        },
                        enabled = !isN8nDispatching,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = DeepNavyBg
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        if (isN8nDispatching) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(12.dp),
                                    color = DeepNavyBg,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Sending…",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        } else {
                            Text(
                                text = "🚀 Send Test Alert (TEST-RED-001)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }

                if (n8nStatus != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCard)
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = n8nStatus ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = if (n8nStatus?.contains("✓") == true || n8nStatus?.contains("✅") == true) AlertGreen else if (n8nStatus?.contains("✕") == true) AlertRed else TextHighlight,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Dismiss",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = CyanLight,
                                modifier = Modifier
                                    .clickable { viewModel.clearN8nStatus() }
                                    .padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Persona Advice Selection Tabs (Citizen / Farmer / Aviation / Authority)
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "ROLE-BASED ADVISORY PROFILE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 10.sp
                ),
                color = TextTertiary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                UserMode.values().take(4).forEach { mode ->
                    val isSelected = selectedUserMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else SurfaceNavy)
                            .border(1.dp, if (isSelected) ElectricCyan else SurfaceBorder, RoundedCornerShape(10.dp))
                            .clickable { viewModel.setUserMode(mode) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.title.substringBefore(" "),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp
                            ),
                            color = if (isSelected) ElectricCyan else TextSecondary
                        )
                    }
                }
            }
        }

        // ==========================================
        // SMART ACTION ENGINE 🚨 (All 10 Hazards + 7 Roles)
        // ==========================================
        SmartActionEngineSection(weather = weather)

        // Severity Category Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedFilterIndex,
            containerColor = SurfaceNavy,
            contentColor = ElectricCyan,
            edgePadding = 0.dp,
            divider = {},
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(50))
                .padding(4.dp)
        ) {
            filters.forEachIndexed { index, label ->
                Tab(
                    selected = selectedFilterIndex == index,
                    onClick = { selectedFilterIndex = index },
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selectedFilterIndex == index) SurfaceCard else Color.Transparent),
                    text = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (selectedFilterIndex == index) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = if (selectedFilterIndex == index) ElectricCyan else TextTertiary
                        )
                    }
                )
            }
        }

        // Emergency Helplines Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, ElectricTeal.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = AlertGreen, modifier = Modifier.size(16.dp))
                    Text(
                        text = "NATIONAL EMERGENCY HELPLINES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 10.sp
                        ),
                        color = AlertGreen
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HelplineItem("Disaster (NDRF)", "1078")
                    HelplineItem("Police / Control", "112")
                    HelplineItem("Ambulance", "108")
                    HelplineItem("Fire Force", "101")
                }
            }
        }

        // Active Alert List
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "ACTIVE HAZARD BULLETINS (${filteredAlerts.size})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontSize = 10.sp
                ),
                color = TextTertiary
            )

            if (filteredAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceCard.copy(alpha = 0.6f))
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active warnings for the selected category.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                filteredAlerts.forEach { alert ->
                    if (alert.isFloodAlert) {
                        com.example.ui.components.AuthoritativeFloodCard(
                            alert = alert,
                            onListenAudio = { viewModel.speakFloodAlert(alert) },
                            onViewMap = { /* Navigates to GIS radar / map */ }
                        )
                    } else {
                        DisasterAlertCard(
                            alert = alert,
                            onDispatchN8n = { viewModel.dispatchAlertToN8n(it) },
                            isDispatching = isN8nDispatching
                        )
                    }
                }
            }
        }

        // Lightweight Event-Driven Alert Rule Automation
        AlertRuleAutomationCard()

        // Cyclone & Severe Weather Preparedness Checklist
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(18.dp))
                    Text(
                        text = "SEVERE WEATHER READINESS PROTOCOL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 10.sp
                        ),
                        color = ElectricTeal
                    )
                }
                Text(
                    text = "• Keep portable battery banks, torches, and emergency first aid charged.\n• Store 48-hour potable drinking water and emergency non-perishable rations.\n• Unplug sensitive electrical appliances during severe convective storms.\n• Disseminate verified bulletins and avoid unverified social media rumors.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 17.sp),
                    color = TextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun HelplineItem(name: String, number: String) {
    Column {
        Text(text = name, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = TextSecondary)
        Text(text = number, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black), color = ElectricCyan)
    }
}

@Composable
private fun SmartActionEngineSection(weather: WeatherData) {
    var selectedHazard by remember { mutableStateOf(HazardType.CYCLONE) }
    var selectedRole by remember { mutableStateOf(RecipientRole.GENERAL_PUBLIC) }

    val cardData = remember(selectedHazard, selectedRole, weather) {
        SmartActionEngine.evaluateHazardAction(selectedHazard, selectedRole, weather)
    }

    val hazardScrollState = rememberScrollState()
    val roleScrollState = rememberScrollState()

    val riskColor = when (cardData.riskLevel) {
        "RED" -> AlertRed
        "ORANGE" -> AlertOrange
        "YELLOW" -> AlertYellow
        else -> AlertGreen
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceNavy)
            .border(1.dp, riskColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = selectedHazard.emoji, fontSize = 16.sp)
                    Text(
                        text = "SMART ACTION ENGINE 🚨",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 11.sp
                        ),
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(riskColor.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "RISK: ${cardData.riskLevel}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 9.5.sp
                        ),
                        color = riskColor
                    )
                }
            }

            // 1. Hazard Selection Chips (All 10 hazards)
            Text(
                text = "SELECT HAZARD SIGNAL",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = TextTertiary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(hazardScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HazardType.values().forEach { hazard ->
                    val isSel = selectedHazard == hazard
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) CyanLight.copy(alpha = 0.22f) else SurfaceCard)
                            .border(1.dp, if (isSel) CyanLight else SurfaceBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedHazard = hazard }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = hazard.emoji, fontSize = 12.sp)
                            Text(
                                text = hazard.labelEn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (isSel) Color.White else TextHighlight
                            )
                        }
                    }
                }
            }

            // 2. Role Selection Chips (All 7 Roles)
            Text(
                text = "TARGET RECIPIENT ROLE",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                color = TextTertiary
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(roleScrollState),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RecipientRole.values().forEach { role ->
                    val isSel = selectedRole == role
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) ElectricCyan.copy(alpha = 0.25f) else SurfaceCard)
                            .border(1.dp, if (isSel) ElectricCyan else SurfaceBorder, RoundedCornerShape(20.dp))
                            .clickable { selectedRole = role }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = role.iconEmoji, fontSize = 11.sp)
                            Text(
                                text = role.titleEn,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = if (isSel) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }

            // 3. Response Card Content: RISK LEVEL, AFFECTED AREA, VALIDITY, IMPACT, RECOMMENDED ACTION, CONFIDENCE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    // Affected Area & Validity
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "AFFECTED AREA", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold), color = TextTertiary)
                            Text(text = cardData.affectedArea, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = Color.White)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "VALIDITY WINDOW", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold), color = TextTertiary)
                            Text(text = cardData.validity, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp), color = CyanAccent)
                        }
                    }

                    // Physical Impact
                    Column {
                        Text(text = "PROJECTED IMPACT", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold), color = AlertOrange)
                        Text(text = cardData.impact, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp), color = TextHighlight)
                    }

                    // Recommended Action
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(riskColor.copy(alpha = 0.12f))
                            .border(0.8.dp, riskColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = "RECOMMENDED ACTION (${cardData.role.titleEn.uppercase()})",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Black),
                                color = riskColor
                            )
                            Text(
                                text = cardData.recommendedAction,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, fontWeight = FontWeight.Medium),
                                color = Color.White
                            )
                        }
                    }

                    // Bulleted Action Directives
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        cardData.roleDirectives.forEach { directive ->
                            Text(
                                text = "• $directive",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                                color = TextSecondary
                            )
                        }
                    }

                    // Provenance & Confidence
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Confidence: ${cardData.confidencePercent}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                            color = AlertGreen
                        )
                        Text(
                            text = "${cardData.source} • ${cardData.timestamp}",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                            color = TextTertiary
                        )
                    }
                }
            }
        }
    }
}
