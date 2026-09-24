package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import com.example.service.HapticManager
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IndianLanguage
import com.example.data.model.UserMode
import com.example.ui.components.ArchitectureFlowDialog
import com.example.ui.components.CanonicalIntegrityAuditDialog
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
fun SettingsScreen(
    viewModel: WeatherGPTViewModel,
    onNavigateToModule: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedMode by viewModel.selectedUserMode.collectAsState()
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val aiBackendStatus by viewModel.aiBackendStatus.collectAsState()
    val n8nChatStatus by viewModel.n8nChatStatus.collectAsState()
    val geminiStatus by viewModel.geminiStatus.collectAsState()
    val alertWebhookStatus by viewModel.alertWebhookStatus.collectAsState()
    val speechRecStatus by viewModel.speechRecognitionStatus.collectAsState()
    val ttsStatus by viewModel.ttsStatus.collectAsState()
    val isTestingDiagnostics by viewModel.isTestingDiagnostics.collectAsState()
    val isHapticEnabled by viewModel.isHapticFeedbackEnabled.collectAsState()

    var showArchitectureDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = ElectricCyan, modifier = Modifier.size(20.dp))
                }
                Column {
                    Text(
                        text = "PLATFORM SETTINGS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Persona Tuning • Multilingual Engine • Diagnostics",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        // Developer Diagnostics Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceNavy)
                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text = "DEVELOPER DIAGNOSTICS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 11.sp
                        ),
                        color = ElectricCyan
                    )
                }

                Button(
                    onClick = { viewModel.runDiagnosticsTest() },
                    enabled = !isTestingDiagnostics,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElectricCyan.copy(alpha = 0.2f),
                        contentColor = ElectricCyan
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    if (isTestingDiagnostics) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), color = ElectricCyan, strokeWidth = 1.5.dp)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Ping", modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("Test AI Ping", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp))
                }
            }

            Text(
                text = "Real-time verification of n8n automation, Gemini AI backend, and local voice telemetry.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DiagnosticItem(label = "AI Backend (n8n Cloud)", status = aiBackendStatus, isPositive = !aiBackendStatus.contains("OFFLINE") && !aiBackendStatus.contains("ERROR"))
                DiagnosticItem(label = "n8n Chat Webhook", status = n8nChatStatus, isPositive = n8nChatStatus.contains("CONNECTED"))
                DiagnosticItem(label = "Gemini LLM Pipeline", status = geminiStatus, isPositive = geminiStatus.contains("ACTIVE") || geminiStatus.contains("CONNECTED"))
                DiagnosticItem(label = "Alerts Dispatch Webhook", status = alertWebhookStatus, isPositive = alertWebhookStatus.contains("CONNECTED"))
                DiagnosticItem(label = "Android Speech Recognizer", status = speechRecStatus, isPositive = speechRecStatus == "AVAILABLE")
                DiagnosticItem(label = "Android Text-to-Speech (TTS)", status = ttsStatus, isPositive = ttsStatus == "AVAILABLE")
                DiagnosticItem(label = "WhatsApp / SMS Dispatch", status = "Not configured (Live Webhook Ready)", isPositive = false, isNeutral = true)
            }

            // Data Pipeline & Integrity Audit Launcher
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable {
                        HapticManager.lightTap(context)
                        viewModel.runSystemIntegrityAudit()
                        viewModel.setIntegritySheetOpen(true)
                    }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "Run System Data Integrity Audit",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "14-point deterministic verification across all pipelines",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextSecondary
                            )
                        }
                    }
                    Text(
                        text = "View Audit →",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = ElectricCyan
                    )
                }
            }
        }

        // Integrity Audit Dialog
        CanonicalIntegrityAuditDialog(viewModel = viewModel)

        // 1. User Persona Selection
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "USER DECISION PERSONA MODE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    fontSize = 10.sp
                ),
                color = ElectricCyan
            )
            Text(
                text = "Adapts AI summaries, risk thresholds, and mitigation guidance specifically to your operational role.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            UserMode.entries.forEach { mode ->
                val isSelected = selectedMode == mode
                val icon: ImageVector = when (mode) {
                    UserMode.GENERAL_PUBLIC -> Icons.Default.Person
                    UserMode.FARMER_KRISHI -> Icons.Default.Agriculture
                    UserMode.DISASTER_MANAGEMENT -> Icons.Default.Emergency
                    UserMode.RESEARCHER -> Icons.Default.Science
                    UserMode.URBAN_PLANNING -> Icons.Default.Hub
                    UserMode.AVIATION -> Icons.Default.Settings
                    UserMode.MARINE -> Icons.Default.Language
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) SurfaceCard else SurfaceCard.copy(alpha = 0.4f))
                        .border(
                            1.dp,
                            if (isSelected) ElectricCyan else SurfaceBorder.copy(alpha = 0.5f),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable {
                            HapticManager.lightTap(context)
                            viewModel.setUserMode(mode)
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) ElectricCyan else TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) TextHighlight else TextPrimary
                            )
                            Text(
                                text = mode.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                        }
                    }
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Selected", tint = ElectricCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // 2. Multilingual Engine
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(18.dp))
                Text(
                    text = "PRIMARY LANGUAGE ENGINE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontSize = 10.sp
                    ),
                    color = ElectricTeal
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                IndianLanguage.entries.forEach { lang ->
                    val isSelected = selectedLang == lang
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SurfaceCard else SurfaceCard.copy(alpha = 0.4f))
                            .border(
                                0.5.dp,
                                if (isSelected) ElectricTeal else SurfaceBorder.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                HapticManager.selectionChanged(context)
                                viewModel.setLanguage(lang)
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = lang.displayName, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                            Text(text = "(${lang.nativeName})", style = MaterialTheme.typography.bodySmall, color = CyanLight)
                        }
                        if (isSelected) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = "Active", tint = ElectricTeal, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // 3. Haptic Feedback Setting
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isHapticEnabled) ElectricCyan.copy(alpha = 0.15f) else SurfaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Haptic Feedback",
                            tint = if (isHapticEnabled) ElectricCyan else TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Haptic Feedback",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextHighlight
                        )
                        Text(
                            text = "Use subtle vibration feedback for interactions, voice, and important alerts.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isHapticEnabled) AlertGreen.copy(alpha = 0.2f) else SurfaceCard)
                        .border(
                            1.dp,
                            if (isHapticEnabled) AlertGreen else SurfaceBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable {
                            val newState = !isHapticEnabled
                            viewModel.setHapticFeedbackEnabled(newState)
                            HapticManager.toggleSwitched(context, newState)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isHapticEnabled) AlertGreen else TextTertiary)
                        )
                        Text(
                            text = if (isHapticEnabled) "ON" else "OFF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = if (isHapticEnabled) AlertGreen else TextTertiary
                        )
                    }
                }
            }
        }

        // 4. System Architecture & Data Provenance
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = CyanLight, modifier = Modifier.size(18.dp))
                    Text(
                        text = "METEOROLOGICAL DATA PROVENANCE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = CyanLight
                    )
                }
                Text(
                    text = "• Live Numerical Weather Prediction: Open-Meteo High-Resolution Ensemble Grid & IMD Gridded Analysis.\n• Global Reanalysis: ECMWF ERA5 & GFS Meso-Scale.\n• Doppler Weather Radar (DWR): Multi-station composite.\n• n8n Automation Engine: Oddcsk Cloud Webhook Pipeline.\n• AI Reasoning Layer: Gemini 2.5 on Backend.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 18.sp),
                    color = TextPrimary
                )
            }
        }

        // 4. Local Cache & Data Management
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceCard.copy(alpha = 0.6f))
                .border(1.dp, SurfaceBorder, RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "On-Device Room Database Cache",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextHighlight
                    )
                    Text(
                        text = "Stores offline forecasts & conversational memory securely.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AlertRed.copy(alpha = 0.15f))
                        .border(0.5.dp, AlertRed.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable {
                            HapticManager.lightTap(context)
                            Toast.makeText(context, "Local cache reset successfully", Toast.LENGTH_SHORT).show()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Clear Cache", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AlertRed)
                }
            }
        }

        // 5. SIH PS-68 Advanced Intelligence Modules
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceNavy)
                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "SIH PS-68 ADVANCED MODULES",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = ElectricCyan
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onNavigateToModule("climate") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard)
                ) {
                    Text("Climate", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = { onNavigateToModule("aviation") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard)
                ) {
                    Text("Aviation", style = MaterialTheme.typography.labelSmall)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onNavigateToModule("marine") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard)
                ) {
                    Text("Marine", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = { onNavigateToModule("smart_city") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard)
                ) {
                    Text("Smart City", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // 6. SIH 2026 Technical Architecture & System Flows
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceNavy)
                .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                .clickable {
                    HapticManager.lightTap(context)
                    showArchitectureDialog = true
                }
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountTree,
                            contentDescription = "Architecture",
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "SIH 2026 Technical Architecture",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextHighlight
                        )
                        Text(
                            text = "10 Implemented Subsystem & Process-Flow Diagrams",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricCyan.copy(alpha = 0.2f))
                        .border(0.5.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "VIEW FLOWS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = ElectricCyan
                    )
                }
            }
        }

        if (showArchitectureDialog) {
            ArchitectureFlowDialog(onDismiss = { showArchitectureDialog = false })
        }

        Spacer(modifier = Modifier.height(36.dp))
    }
}

@Composable
private fun DiagnosticItem(
    label: String,
    status: String,
    isPositive: Boolean,
    isNeutral: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard.copy(alpha = 0.5f))
            .border(0.5.dp, SurfaceBorder.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = TextPrimary
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    when {
                        isNeutral -> TextTertiary.copy(alpha = 0.2f)
                        isPositive -> AlertGreen.copy(alpha = 0.18f)
                        else -> AlertRed.copy(alpha = 0.18f)
                    }
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = when {
                    isNeutral -> TextSecondary
                    isPositive -> AlertGreen
                    else -> AlertRed
                }
            )
        }
    }
}
