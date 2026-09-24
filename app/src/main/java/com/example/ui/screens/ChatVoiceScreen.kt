package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatLanguageMode
import com.example.data.model.ChatMessage
import com.example.data.model.IndianLanguage
import com.example.data.model.ResearchAnalysis
import com.example.service.VoiceUiState
import com.example.service.report.ReportContextType
import com.example.service.report.WeatherIntelligenceReport
import com.example.service.report.WeatherReportGenerator
import com.example.ui.components.ClimateResearchTrendChart
import com.example.ui.components.FormattedMarkdownContent
import com.example.ui.components.QuickAccessTab
import com.example.ui.components.WeatherReportViewerModal
import com.example.ui.components.WeatherTelemetryInspectorModal
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow
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
import com.example.ui.viewmodel.WeatherGPTViewModel

@Composable
fun ChatVoiceScreen(
    viewModel: WeatherGPTViewModel,
    initialQuery: String? = null,
    onNavigateToGisRadar: () -> Unit = {},
    onNavigateToHomeTab: ((QuickAccessTab) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val chatStatusText by viewModel.chatStatusText.collectAsState()
    val isListening by viewModel.voiceService.isListening.collectAsState()
    val isSpeaking by viewModel.voiceService.isSpeaking.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val chatLanguageMode by viewModel.chatLanguageMode.collectAsState()
    val voiceUiState by viewModel.voiceUiState.collectAsState()
    val lastSpokenAnswer by viewModel.lastSpokenAnswer.collectAsState()
    val selectedUserMode by viewModel.selectedUserMode.collectAsState()
    val lastFailedQuery by viewModel.lastFailedQuery.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()
    val hourlyForecast by viewModel.hourlyForecast.collectAsState()
    val activeQuickAccessTab by viewModel.activeQuickAccessTab.collectAsState()
    val n8nHealthStatus by viewModel.n8nHealthStatus.collectAsState()
    val latencyStats by viewModel.aiLatencyStats.collectAsState()

    var activeReportForModal by remember { mutableStateOf<WeatherIntelligenceReport?>(null) }
    var showTelemetryModal by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showLatencyDialog by remember { mutableStateOf(false) }

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val liveTranscript by viewModel.liveInterimTranscript.collectAsState()

    LaunchedEffect(liveTranscript) {
        if (liveTranscript.isNotBlank() && isListening) {
            inputText = liveTranscript
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.voiceService.startListening(
                language = selectedLanguage,
                onResult = { recognizedText ->
                    inputText = recognizedText
                    viewModel.sendChatMessage(recognizedText, isVoiceInitiated = true)
                },
                onError = { err ->
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Microphone permission required for voice queries.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank()) {
            viewModel.sendChatMessage(initialQuery)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepNavyBg)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Location & Live Weather Context Strip with Farmer Language Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(SurfaceNavy)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f, fill = false),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                Text(
                    text = "${weatherData.cityName} • ${weatherData.temperatureC}°C ${weatherData.conditionDescription}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ElectricCyan,
                    maxLines = 1
                )
            }

            // Interactive Multilingual Switcher Chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(0.5.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                    .clickable { showLanguageDialog = true }
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Language",
                        tint = ElectricCyan,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = chatLanguageMode.title.substringBefore(" ("),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = CyanLight
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = CyanLight,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // AI Engine & Automation Status Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(AlertGreen)
                )
                Text(
                    text = "WeatherGPT AI (Gemini 3.6) Online",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AlertGreen
                )
                if (latencyStats != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SurfaceNavy)
                            .border(0.5.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .clickable { showLatencyDialog = true }
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "⚡ Avg: ${latencyStats!!.averageSec}s (n=${latencyStats!!.sampleSize})",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp, fontWeight = FontWeight.Bold),
                            color = ElectricCyan
                        )
                    }
                }
            }
            Text(
                text = n8nHealthStatus,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.5.sp,
                    color = if (n8nHealthStatus.contains("Online", ignoreCase = true)) AlertGreen else TextSecondary
                )
            )
        }

        // Latency Measurement Dialog
        if (showLatencyDialog && latencyStats != null) {
            val stats = latencyStats!!
            androidx.compose.ui.window.Dialog(onDismissRequest = { showLatencyDialog = false }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DeepNavyBg)
                        .border(1.dp, ElectricCyan, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AI RESPONSE LATENCY METRICS",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                color = ElectricCyan
                            )
                            IconButton(
                                onClick = { showLatencyDialog = false },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceCard)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Average: ${stats.averageSec} sec", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Median: ${stats.medianSec} sec", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("P95: ${stats.p95Sec} sec", color = AlertYellow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Sample size: ${stats.sampleSize}", color = TextSecondary, fontSize = 11.5.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Last Execution Breakdown:", color = CyanLight, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("• Data Retrieval: ${stats.lastRetrievalMs} ms", color = TextPrimary, fontSize = 11.sp)
                            Text("• AI Processing: ${stats.lastAiProcessingMs} ms", color = TextPrimary, fontSize = 11.sp)
                            Text("• Total Turnaround: ${stats.lastTotalSec} sec", color = AlertGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Text(
                            text = "Empirically measured client-side timing across live Gemini API & Krishi intelligence calls.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            color = TextTertiary
                        )
                    }
                }
            }
        }

        // Upgraded Voice Pipeline State & Replay Bar
        AnimatedVisibility(
            visible = voiceUiState != VoiceUiState.READY || lastSpokenAnswer != null
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (voiceUiState) {
                            VoiceUiState.LISTENING -> AlertRed.copy(alpha = 0.15f)
                            VoiceUiState.UNDERSTANDING -> AlertOrange.copy(alpha = 0.15f)
                            VoiceUiState.ANALYZING_WEATHER -> ElectricCyan.copy(alpha = 0.15f)
                            VoiceUiState.SPEAKING -> AlertGreen.copy(alpha = 0.15f)
                            VoiceUiState.READY -> SurfaceCard
                        }
                    )
                    .border(
                        1.dp,
                        when (voiceUiState) {
                            VoiceUiState.LISTENING -> AlertRed.copy(alpha = 0.6f)
                            VoiceUiState.UNDERSTANDING -> AlertOrange.copy(alpha = 0.6f)
                            VoiceUiState.ANALYZING_WEATHER -> ElectricCyan.copy(alpha = 0.6f)
                            VoiceUiState.SPEAKING -> AlertGreen.copy(alpha = 0.6f)
                            VoiceUiState.READY -> SurfaceBorder
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        when (voiceUiState) {
                            VoiceUiState.LISTENING -> {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(AlertRed)
                                )
                                Text(
                                    text = "🎙️ सुन रहा हूँ... बोलिए (Listening...)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AlertRed
                                )
                            }
                            VoiceUiState.UNDERSTANDING -> {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = AlertOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "🧠 समझ रहा हूँ... (Understanding speech...)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AlertOrange
                                )
                            }
                            VoiceUiState.ANALYZING_WEATHER -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = ElectricCyan,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "🌾 मौसम और कृषि विश्लेषण जारी है... (Analyzing...)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = ElectricCyan
                                )
                            }
                            VoiceUiState.SPEAKING -> {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = AlertGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "🔊 उत्तर बोल रहा हूँ... (Speaking advisory aloud)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = AlertGreen
                                )
                            }
                            VoiceUiState.READY -> {
                                Icon(
                                    imageVector = Icons.Default.Hearing,
                                    contentDescription = null,
                                    tint = CyanLight,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "ध्वनि उत्तर तैयार है (Voice advisory ready)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    if (voiceUiState == VoiceUiState.SPEAKING) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AlertRed.copy(alpha = 0.2f))
                                .border(0.5.dp, AlertRed, RoundedCornerShape(8.dp))
                                .clickable { viewModel.stopSpeaking() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "रोकें / Stop",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = AlertRed
                            )
                        }
                    } else if (lastSpokenAnswer != null && voiceUiState == VoiceUiState.READY) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricCyan.copy(alpha = 0.18f))
                                .border(0.5.dp, ElectricCyan, RoundedCornerShape(8.dp))
                                .clickable { viewModel.replayLastSpokenAnswer() }
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Replay",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "पुनः सुनें / Replay",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    ),
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(
                    message = msg,
                    isSpeaking = isSpeaking,
                    onSpeak = {
                        val spokenText = msg.text
                            .substringBefore("[DATA PROVENANCE]")
                            .replace(Regex("\\[.*?\\]"), "")
                            .replace(Regex("[*#_`>]"), "")
                            .take(500)
                        viewModel.speakText(spokenText)
                    },
                    onStopSpeak = { viewModel.stopSpeaking() },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("WeatherGPT", msg.text)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    onRetry = {
                        viewModel.retryLastFailedQuery()
                    },
                    onOpenMap = onNavigateToGisRadar,
                    onOpenQuickAccess = { tab ->
                        viewModel.setActiveQuickAccessTab(tab)
                        onNavigateToHomeTab?.invoke(tab)
                    },
                    onGenerateReport = {
                        val report = WeatherReportGenerator.generateReport(
                            contextType = ReportContextType.STANDARD,
                            weather = weatherData,
                            hourly = hourlyForecast,
                            moduleContext = activeQuickAccessTab,
                            userMode = selectedUserMode,
                            language = selectedLanguage
                        )
                        activeReportForModal = report
                    },
                    onViewTelemetry = {
                        showTelemetryModal = true
                    },
                    onDownloadPdf = {
                        val report = WeatherReportGenerator.generateReport(
                            contextType = ReportContextType.STANDARD,
                            weather = weatherData,
                            hourly = hourlyForecast,
                            moduleContext = activeQuickAccessTab,
                            userMode = selectedUserMode,
                            language = selectedLanguage
                        )
                        WeatherReportGenerator.exportReportAsPdf(context, report)
                    },
                    onDownloadCsv = {
                        val report = WeatherReportGenerator.generateReport(
                            contextType = ReportContextType.STANDARD,
                            weather = weatherData,
                            hourly = hourlyForecast,
                            moduleContext = activeQuickAccessTab,
                            userMode = selectedUserMode,
                            language = selectedLanguage
                        )
                        WeatherReportGenerator.exportReportAsCsv(context, report)
                    }
                )
            }

            if (isChatLoading) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ElectricCyan, strokeWidth = 2.dp)
                        Text(
                            text = chatStatusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Voice Listening Waveform Animation
        AnimatedVisibility(visible = isListening) {
            ListeningWaveformBar()
        }

        // Speaking Indicator / Stop
        AnimatedVisibility(visible = isSpeaking) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = CyanLight, modifier = Modifier.size(16.dp))
                    Text("Reading weather intelligence aloud...", style = MaterialTheme.typography.labelSmall, color = CyanLight)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AlertRed.copy(alpha = 0.18f))
                        .clickable { viewModel.stopSpeaking() }
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("Stop", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AlertRed)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Farmer Suggestion Chips (Adaptive to Selected Language)
        val farmerSuggestions = remember(chatLanguageMode) {
            when (chatLanguageMode) {
                ChatLanguageMode.HINDI, ChatLanguageMode.HINGLISH -> listOf(
                    "कल बारिश होगी क्या? गेहूं में पानी दें?",
                    "क्या आज कीटनाशक का छिड़काव करना सुरक्षित है?",
                    "अगले 3 दिनों का कृषि मौसम कैसा रहेगा?",
                    "धान की फसल में इस मौसम में क्या सावधानी रखें?",
                    "क्या तेज हवा या ओलावृष्टि का खतरा है?",
                    "आज का तापमान और आर्द्रता कितनी है?"
                )
                ChatLanguageMode.BENGALI -> listOf(
                    "আগামীকাল কি বৃষ্টি হবে? ফসলে জল দেব কি?",
                    "আজ কি জমিতে কীটনাশক স্প্রে করা যাবে?",
                    "পরবর্তী ৩ দিনের কৃষি আবহাওয়া কেমন?",
                    "ধান চাষে এই আবহাওয়ায় কী সতর্কতা নেওয়া দরকার?"
                )
                ChatLanguageMode.MARATHI -> listOf(
                    "उद्या पाऊस पडेल का? पिकाला पाणी द्यावे का?",
                    "आज कीटकनाशक फवारणी करणे सुरक्षित आहे का?",
                    "पुढील ३ दिवसांचा हवामान अंदाज काय आहे?"
                )
                ChatLanguageMode.TELUGU -> listOf(
                    "రేపు వర్షం పడుతుందా? పంటకు నీరు పెట్టవచ్చా?",
                    "ఈ రోజు మందుల పిచಿಕారీ చేయడం సురక్షితమేనా?",
                    "రాబోయే 3 రోజుల వ్యవసాయ వాతావరణం ఎలా ఉంటుంది?"
                )
                ChatLanguageMode.TAMIL -> listOf(
                    "நாளை மழை பெய்யுமா? பயிருக்கு நீர் பாய்ச்சலாமா?",
                    "இன்று பூச்சிக்கொல்லி தெளிப்பது பாதுகாப்பானதா?",
                    "அடுத்த 3 நாட்களுக்கான வானிலை எப்படி இருக்கும்?"
                )
                ChatLanguageMode.PUNJABI -> listOf(
                    "ਕੱਲ੍ਹ ਮੀਂਹ ਪਵੇਗਾ? ਕਣਕ ਨੂੰ ਪਾਣੀ ਲਾਈਏ?",
                    "ਕੀ ਅੱਜ ਕੀਟਨਾਸ਼ਕ ਦਾ ਛਿੜਕਾਅ ਕਰਨਾ ਸੁਰੱਖਿਅਤ ਹੈ?",
                    "ਅਗਲੇ 3 ਦਿਨਾਂ ਦਾ ਮੌਸਮ ਕਿਹੋ ਜਿਹਾ ਰਹੇਗਾ?"
                )
                ChatLanguageMode.ODIA -> listOf(
                    "ଆସନ୍ତାକାଲି ବର୍ଷା ହେବ କି? ଫସଲରେ ପାଣି ଦେବା ଉଚିତ କି?",
                    "ଆଜି କୀଟନାଶକ ପ୍ରୟୋଗ କରିବା ସୁରକ୍ଷିତ କି?",
                    "ଆଗାମୀ ୩ ଦିନର ପାଣିପାଗ କିପରି ରହିବ?"
                )
                ChatLanguageMode.ASSAMESE -> listOf(
                    "কাইলৈ বৰষুণ হ'বনে? শস্যত পানী দিব লাগিব নেকি?",
                    "আজি কীটনাশক স্প্ৰে কৰাটো নিৰাপদ নে?",
                    "অহা ৩ দিনৰ বতৰৰ আগজাননী কি?"
                )
                ChatLanguageMode.GUJARATI -> listOf(
                    "કાલે વરસાદ પડશે? પાકમાં પાણી આપવું જોઈએ?",
                    "આજે જંતુનાશક દવાનો છંટકાવ કરવો સુરક્ષિત છે?",
                    "આગામી 3 દિવસનું હવામાન કેવું રહેશે?"
                )
                ChatLanguageMode.KANNADA -> listOf(
                    "ನಾಳೆ ಮಳೆಯಾಗುವುದೇ? ಬೆಳೆಗೆ ನೀರು ಹಾಯಿಸಬಹುದೇ?",
                    "ಇಂದು ಕೀಟನಾಶಕ ಸಿಂಪಡಿಸುವುದು ಸುರಕ್ಷಿತವೇ?"
                )
                ChatLanguageMode.MALAYALAM -> listOf(
                    "നാളെ മഴ പെയ്യുമോ? വിളകൾക്ക് നനയ്ക്കണമো?",
                    "ഇന്ന് കീടനാശിനി തളിക്കുന്നത് സുരക്ഷിതമാണോ?"
                )
                else -> listOf(
                    "Will it rain tomorrow? Should I irrigate wheat?",
                    "Is it safe to spray pesticides today?",
                    "What is the 3-day Krishi weather advisory?",
                    "Any disease or pest risk in current humidity?",
                    "What is the cyclone and storm risk?"
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(farmerSuggestions) { suggestion ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(SurfaceNavy)
                        .border(0.5.dp, SurfaceBorder, RoundedCornerShape(50))
                        .clickable {
                            inputText = suggestion
                            viewModel.sendChatMessage(suggestion)
                            inputText = ""
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanLight
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Input Bar with Farmer Voice Mic (Tap / Hold to Talk) & Send
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    val placeholderText = when (voiceUiState) {
                        VoiceUiState.LISTENING -> "बोलिए... सुन रहा हूँ (Listening...)"
                        VoiceUiState.UNDERSTANDING -> "समझ रहा हूँ... (Understanding...)"
                        VoiceUiState.ANALYZING_WEATHER -> "मौसम विश्लेषण हो रहा है... (Analyzing...)"
                        VoiceUiState.SPEAKING -> "उत्तर बोल रहा हूँ... (Speaking advisory...)"
                        VoiceUiState.READY -> "बोलें या लिखें: बारिश, सिंचाई, खाद, मौसम..."
                    }
                    Text(
                        text = placeholderText,
                        color = TextSecondary,
                        fontSize = 12.5.sp
                    )
                },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = SurfaceBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = SurfaceNavy,
                    unfocusedContainerColor = SurfaceNavy
                ),
                shape = RoundedCornerShape(24.dp),
                maxLines = 3
            )

            // Hold-to-Talk / Tap-to-Speak Microphone
            var isMicPressed by remember { mutableStateOf(false) }
            val effectiveListening = isListening || isMicPressed || voiceUiState == VoiceUiState.LISTENING

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            effectiveListening -> AlertRed
                            voiceUiState == VoiceUiState.UNDERSTANDING || voiceUiState == VoiceUiState.ANALYZING_WEATHER -> AlertOrange
                            voiceUiState == VoiceUiState.SPEAKING -> AlertGreen
                            else -> SurfaceCard
                        }
                    )
                    .border(
                        1.5.dp,
                        when {
                            effectiveListening -> AlertRed
                            voiceUiState == VoiceUiState.UNDERSTANDING || voiceUiState == VoiceUiState.ANALYZING_WEATHER -> AlertOrange
                            voiceUiState == VoiceUiState.SPEAKING -> AlertGreen
                            else -> SurfaceBorder
                        },
                        CircleShape
                    )
                    .pointerInput(selectedLanguage) {
                        detectTapGestures(
                            onPress = {
                                isMicPressed = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                tryAwaitRelease()
                                isMicPressed = false
                                if (viewModel.voiceService.isListening.value) {
                                    viewModel.voiceService.stopListening()
                                }
                            },
                            onTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                if (viewModel.voiceService.isListening.value) {
                                    viewModel.voiceService.stopListening()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (voiceUiState == VoiceUiState.ANALYZING_WEATHER || voiceUiState == VoiceUiState.UNDERSTANDING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = if (effectiveListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Hold to Talk or Tap to Speak",
                        tint = if (effectiveListening || voiceUiState == VoiceUiState.SPEAKING) Color.White else ElectricCyan,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Send Button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) ElectricCyan else SurfaceCard)
                    .clickable(enabled = inputText.isNotBlank()) {
                        viewModel.sendChatMessage(inputText)
                        inputText = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = if (inputText.isNotBlank()) DeepNavyBg else TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    // Farmer Language Selection Modal
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
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
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "अपनी भाषा चुनें / Select Language",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    IconButton(
                        onClick = { showLanguageDialog = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(ChatLanguageMode.values()) { mode ->
                        val isSelected = chatLanguageMode == mode
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.18f) else SurfaceNavy)
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else SurfaceBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.setChatLanguageMode(mode)
                                    showLanguageDialog = false
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) ElectricCyan else TextPrimary
                                )
                                if (mode == ChatLanguageMode.AUTO) {
                                    Text(
                                        text = "Farmer speech/text auto-detected instantly",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = TextSecondary
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("बन्द करें / Close", color = CyanLight)
                }
            },
            containerColor = DeepNavyBg,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Weather Intelligence Report Modal
    activeReportForModal?.let { report ->
        WeatherReportViewerModal(
            report = report,
            onDismiss = { activeReportForModal = null },
            onSwitchContext = { newCtx ->
                activeReportForModal = WeatherReportGenerator.generateReport(
                    contextType = newCtx,
                    weather = weatherData,
                    hourly = hourlyForecast,
                    moduleContext = activeQuickAccessTab,
                    userMode = selectedUserMode,
                    language = selectedLanguage
                )
            },
            onSpeakSummary = { summaryText ->
                viewModel.speakText(summaryText)
            }
        )
    }

    // Weather Telemetry Inspector Modal
    if (showTelemetryModal) {
        WeatherTelemetryInspectorModal(
            weather = weatherData,
            onDismiss = { showTelemetryModal = false }
        )
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onStopSpeak: () -> Unit,
    onCopy: () -> Unit,
    onRetry: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenQuickAccess: (QuickAccessTab) -> Unit = {},
    onGenerateReport: () -> Unit = {},
    onViewTelemetry: () -> Unit = {},
    onDownloadPdf: () -> Unit = {},
    onDownloadCsv: () -> Unit = {}
) {
    val isUser = message.isUser
    val isErrorMessage = !isUser && (
            message.text.contains("AI service unavailable", ignoreCase = true) ||
            message.text.contains("timed out", ignoreCase = true)
    )

    var isExpanded by remember { mutableStateOf(false) }
    val isLongResponse = message.text.length > 600

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.82f else 0.98f)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(
                    if (isUser) WeatherBlue
                    else if (isErrorMessage) AlertRed.copy(alpha = 0.12f)
                    else SurfaceNavy
                )
                .border(
                    1.dp,
                    if (isUser) ElectricCyan.copy(alpha = 0.5f)
                    else if (isErrorMessage) AlertRed.copy(alpha = 0.4f)
                    else SurfaceBorder,
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (isErrorMessage) AlertRed else ElectricCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isErrorMessage) "AI SERVICE STATUS"
                                else if (message.researchData != null) "DEEP CLIMATE & RISK ANALYSIS"
                                else "WEATHER INTELLIGENCE OUTLOOK",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    fontSize = 10.sp
                                ),
                                color = if (isErrorMessage) AlertRed else ElectricCyan
                            )
                        }

                        // Audio & Action Controls
                        if (!isErrorMessage) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onCopy, modifier = Modifier.size(24.dp)) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyanLight, modifier = Modifier.size(14.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSpeaking) AlertRed.copy(alpha = 0.2f) else ElectricCyan.copy(alpha = 0.15f))
                                        .border(0.5.dp, if (isSpeaking) AlertRed else ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .clickable { if (isSpeaking) onStopSpeak() else onSpeak() }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                            contentDescription = if (isSpeaking) "Stop Audio" else "Listen Aloud",
                                            tint = if (isSpeaking) AlertRed else ElectricCyan,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = if (isSpeaking) "रोकें (Stop)" else "सुनें (Listen)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isSpeaking) AlertRed else ElectricCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Main Text / Collapsible Text
                val displayText = if (isLongResponse && !isExpanded) {
                    message.text.take(450) + "..."
                } else {
                    message.text
                }

                FormattedMarkdownContent(
                    text = displayText,
                    isUser = isUser
                )

                if (isLongResponse) {
                    Row(
                        modifier = Modifier
                            .clickable { isExpanded = !isExpanded }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Show Less" else "Show Full Analysis",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = ElectricCyan
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Retry Button if error
                if (isErrorMessage) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan)
                            .clickable { onRetry() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Retry", tint = DeepNavyBg, modifier = Modifier.size(14.dp))
                        Text(
                            text = "Retry Query",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepNavyBg
                        )
                    }
                }

                // Embedded Deep Climate Research Chart if available
                if (message.researchData != null && message.researchData.historicalData.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceCard)
                            .border(0.5.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    ) {
                        ClimateResearchTrendChart(dataPoints = message.researchData.historicalData)
                    }
                }

                // Contextual Domain Action Badge
                val suggestedTab = when {
                    !isUser && !isErrorMessage && (message.text.contains("aviation", ignoreCase = true) || message.text.contains("flight", ignoreCase = true) || message.text.contains("metar", ignoreCase = true)) -> QuickAccessTab.AVIATION
                    !isUser && !isErrorMessage && (message.text.contains("marine", ignoreCase = true) || message.text.contains("ocean", ignoreCase = true) || message.text.contains("wave", ignoreCase = true) || message.text.contains("swell", ignoreCase = true)) -> QuickAccessTab.MARINE
                    !isUser && !isErrorMessage && (message.text.contains("gfs", ignoreCase = true) || message.text.contains("ecmwf", ignoreCase = true) || message.text.contains("nwp", ignoreCase = true) || message.text.contains("model divergence", ignoreCase = true)) -> QuickAccessTab.NWP_MODELS
                    !isUser && !isErrorMessage && (message.text.contains("climate", ignoreCase = true) || message.text.contains("decadal", ignoreCase = true) || message.text.contains("anomaly", ignoreCase = true)) -> QuickAccessTab.CLIMATE
                    !isUser && !isErrorMessage && (message.text.contains("smart city", ignoreCase = true) || message.text.contains("urban heat", ignoreCase = true) || message.text.contains("waterlogging", ignoreCase = true)) -> QuickAccessTab.SMART_CITY
                    !isUser && !isErrorMessage && (message.text.contains("disaster impact", ignoreCase = true) || message.text.contains("infrastructure", ignoreCase = true) || message.text.contains("evacuation", ignoreCase = true)) -> QuickAccessTab.DISASTER_IMPACT
                    else -> QuickAccessTab.NONE
                }

                if (suggestedTab != QuickAccessTab.NONE) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ElectricTeal.copy(alpha = 0.15f))
                            .border(1.dp, ElectricTeal.copy(alpha = 0.5f), RoundedCornerShape(50))
                            .clickable { onOpenQuickAccess(suggestedTab) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(14.dp))
                            Text(
                                text = "Open " + suggestedTab.title + " Dashboard",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricTeal
                            )
                        }
                    }
                }

                // Interactive Map Action Button
                if (!isUser && !isErrorMessage && (message.text.contains("radar", ignoreCase = true) ||
                            message.text.contains("map", ignoreCase = true) ||
                            message.text.contains("cyclone", ignoreCase = true) ||
                            message.text.contains("gis", ignoreCase = true) ||
                            message.text.contains("flood", ignoreCase = true))
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.4f), RoundedCornerShape(50))
                            .clickable { onOpenMap() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Map, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                            Text(
                                text = "View in Interactive Radar & GIS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = ElectricCyan
                            )
                        }
                    }
                }

                // Weather Intelligence Action Bar (View/Generate Report, View Telemetry, PDF/CSV Download)
                if (!isUser && !isErrorMessage) {
                    val isReportLike = message.text.contains("SUMMARY", ignoreCase = true) ||
                            message.text.contains("REPORT", ignoreCase = true) ||
                            message.text.contains("CURRENT CONDITIONS", ignoreCase = true) ||
                            message.text.contains("ANALYSIS", ignoreCase = true) ||
                            message.text.contains("FORECAST", ignoreCase = true)

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(ElectricCyan.copy(alpha = 0.15f))
                                        .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                                        .clickable { onGenerateReport() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(13.dp))
                                        Text(
                                            text = if (isReportLike) "View Full Report" else "Generate Report",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = ElectricCyan
                                        )
                                    }
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SurfaceCard)
                                        .border(0.5.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                        .clickable { onViewTelemetry() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Analytics, contentDescription = null, tint = CyanLight, modifier = Modifier.size(13.dp))
                                        Text(
                                            text = "View Telemetry",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
                                            color = CyanLight
                                        )
                                    }
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AlertOrange.copy(alpha = 0.15f))
                                        .border(1.dp, AlertOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { onDownloadPdf() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = AlertOrange, modifier = Modifier.size(13.dp))
                                        Text(
                                            text = "PDF",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = AlertOrange
                                        )
                                    }
                                }
                            }

                            item {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AlertGreen.copy(alpha = 0.15f))
                                        .border(1.dp, AlertGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .clickable { onDownloadCsv() }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = AlertGreen, modifier = Modifier.size(13.dp))
                                        Text(
                                            text = "CSV",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                            color = AlertGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                    color = if (isUser) TextHighlight.copy(alpha = 0.7f) else TextSecondary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun ListeningWaveformBar() {
    val transition = rememberInfiniteTransition(label = "audioWave")
    val waveScale by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AlertRed.copy(alpha = 0.15f))
            .border(0.5.dp, AlertRed, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(AlertRed)
            )
            Text("Listening... Speak your weather query clearly", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AlertRed)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            for (i in 0..4) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height((12 * (if (i % 2 == 0) waveScale else (1.4f - waveScale))).dp)
                        .background(AlertRed)
                )
            }
        }
    }
}
