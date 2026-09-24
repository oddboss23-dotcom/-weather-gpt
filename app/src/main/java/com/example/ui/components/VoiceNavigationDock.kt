package com.example.ui.components

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IndianLanguage
import com.example.service.HapticFeedbackManager
import com.example.service.VoiceErrorType
import com.example.service.VoiceSessionState
import com.example.service.VoiceUiState
import com.example.ui.navigation.Screen
import com.example.ui.navigation.leftNavScreens
import com.example.ui.navigation.rightNavScreens
import com.example.ui.theme.AlertGreen
import com.example.ui.theme.AlertOrange
import com.example.ui.theme.AlertRed
import com.example.ui.theme.AlertYellow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.CyanLight
import com.example.ui.theme.DeepNavyBg
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceNavy
import com.example.ui.theme.TextHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.ui.viewmodel.WeatherGPTViewModel
import kotlin.math.sin

/**
 * Backward-compatible VoiceState enum for UI references
 */
enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING
}

/**
 * 4-Tab Navigation Dock with Centered Floating Voice FAB
 * Supporting Hold-to-Talk, Tap-to-Talk, and Instant AI Assistant Launch
 */
@Composable
fun WeatherVoiceBottomDock(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    viewModel: WeatherGPTViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val sessionState by viewModel.voiceSessionState.collectAsState()
    val isListening by viewModel.voiceService.isListening.collectAsState()
    val speechRms by viewModel.voiceService.speechRms.collectAsState()
    val liveTranscript by viewModel.liveInterimTranscript.collectAsState()
    val finalTranscript by viewModel.finalTranscript.collectAsState()
    val errorType by viewModel.voiceErrorType.collectAsState()
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val isSpeaking by viewModel.voiceService.isSpeaking.collectAsState()
    val lastSpokenAnswer by viewModel.voiceService.lastSpokenAnswer.collectAsState()

    var showVoiceOverlay by remember { mutableStateOf(false) }

    fun launchVoiceListening() {
        showVoiceOverlay = true
        viewModel.voiceService.startListening(
            language = selectedLanguage,
            onResult = { text ->
                if (text.contains("radar", ignoreCase = true) || text.contains("map", ignoreCase = true)) {
                    showVoiceOverlay = false
                    onNavigate(Screen.Radar.route)
                } else {
                    viewModel.sendChatMessage(text, isVoiceInitiated = true)
                    onNavigate(Screen.Chat.route)
                    showVoiceOverlay = false
                }
            },
            onError = { _ ->
                // Error state is held in voiceService and observed by modal
            }
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchVoiceListening()
        } else {
            Toast.makeText(context, "Microphone permission is required for voice assistant.", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
    ) {
        // Bottom Navigation Bar Background Surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(68.dp)
                .background(DeepNavyBg)
                .border(
                    width = 0.5.dp,
                    color = SurfaceBorder,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
                .padding(horizontal = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 2 items (Weather, Radar)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    leftNavScreens.forEach { screen ->
                        DockNavItem(
                            screen = screen,
                            isSelected = currentRoute == screen.route,
                            onClick = {
                                HapticFeedbackManager.triggerMapInteraction(context)
                                onNavigate(screen.route)
                            }
                        )
                    }
                }

                // Center Spacer for FAB
                Spacer(modifier = Modifier.width(68.dp))

                // Right 2 items (Safety, Profile)
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    rightNavScreens.forEach { screen ->
                        DockNavItem(
                            screen = screen,
                            isSelected = currentRoute == screen.route,
                            onClick = {
                                HapticFeedbackManager.triggerMapInteraction(context)
                                onNavigate(screen.route)
                            }
                        )
                    }
                }
            }
        }

        // CENTER FLOATING VOICE FAB (Supports Hold-to-Talk and Tap-to-Talk)
        VoiceMicFAB(
            isListening = isListening || sessionState == VoiceSessionState.LISTENING,
            onPressStart = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onPressEnd = {
                if (viewModel.voiceService.isListening.value) {
                    viewModel.voiceService.stopListening()
                }
            },
            onTap = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                if (isListening) {
                    viewModel.voiceService.stopListening()
                } else {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-14).dp)
        )

        // Hold-to-Talk / Listening Full Voice HUD Overlay Modal
        AnimatedVisibility(
            visible = showVoiceOverlay || sessionState != VoiceSessionState.IDLE,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            VoiceAssistantModal(
                sessionState = sessionState,
                language = selectedLanguage,
                liveTranscript = liveTranscript,
                finalTranscript = finalTranscript,
                speechRms = speechRms,
                errorType = errorType,
                onDismiss = {
                    viewModel.voiceService.cancelListening()
                    showVoiceOverlay = false
                },
                onRetry = {
                    launchVoiceListening()
                },
                onTypeQuestion = {
                    viewModel.voiceService.cancelListening()
                    showVoiceOverlay = false
                    onNavigate(Screen.Chat.route)
                },
                onSelectPrompt = { prompt ->
                    viewModel.voiceService.cancelListening()
                    if (prompt.contains("radar", ignoreCase = true) || prompt.contains("map", ignoreCase = true)) {
                        showVoiceOverlay = false
                        onNavigate(Screen.Radar.route)
                    } else {
                        viewModel.sendChatMessage(prompt, isVoiceInitiated = true)
                        onNavigate(Screen.Chat.route)
                        showVoiceOverlay = false
                    }
                },
                lastSpokenAnswer = lastSpokenAnswer,
                onStopSpeaking = { viewModel.voiceService.stopSpeaking() }
            )
        }
    }
}

@Composable
private fun DockNavItem(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = screen.icon,
            contentDescription = screen.label,
            tint = if (isSelected) ElectricCyan else TextTertiary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = screen.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 0.2.sp
            ),
            color = if (isSelected) ElectricCyan else TextTertiary,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun VoiceMicFAB(
    isListening: Boolean,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressedLocal by remember { mutableStateOf(false) }
    val effectiveListening = isListening || isPressedLocal
    val haptic = LocalHapticFeedback.current

    val infiniteTransition = rememberInfiniteTransition(label = "pulseFab")
    val pulseScale by if (effectiveListening) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScaleActive"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val glowAlpha by if (effectiveListening) {
        infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.65f,
            animationSpec = infiniteRepeatable(
                animation = tween(450, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlphaActive"
        )
    } else {
        remember { mutableStateOf(0.28f) }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Layer 1: Ambient outer diffuse glow
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (effectiveListening) AlertRed else Color(0xFF00D9FF)).copy(alpha = glowAlpha),
                                (if (effectiveListening) AlertRed else Color(0xFF1677FF)).copy(alpha = glowAlpha * 0.4f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Layer 2: Secondary concentrated aura ring
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (effectiveListening) AlertRed else Color(0xFF00D9FF)).copy(alpha = if (effectiveListening) 0.45f else 0.30f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Layer 3: Main Neumorphic/Glass Center Floating Action Button with pointerInput
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = if (effectiveListening) 16.dp else 8.dp,
                        shape = CircleShape,
                        spotColor = if (effectiveListening) AlertRed else Color(0xFF00D9FF)
                    )
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = if (effectiveListening) {
                                listOf(Color(0xFFFF3B30), Color(0xFFD32F2F))
                            } else {
                                listOf(
                                    Color(0xFF00D9FF), // Cyan
                                    Color(0xFF1677FF)  // Electric Blue
                                )
                            }
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.85f),
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressedLocal = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onPressStart()
                                val released = tryAwaitRelease()
                                isPressedLocal = false
                                if (released) {
                                    onPressEnd()
                                } else {
                                    onPressEnd()
                                }
                            },
                            onTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTap()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                // Top subtle specular glass gloss highlight
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 18.dp)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // White Microphone Icon
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Hold or Tap to Talk with WeatherGPT AI",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        // Subtitle Label with dynamic state feedback
        Text(
            text = if (effectiveListening) "Listening…" else "Hold / Tap to Talk",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.4.sp
            ),
            color = if (effectiveListening) AlertRed else CyanLight
        )
    }
}

/**
 * Backward compatibility alias for CenterVoiceFab
 */
@Composable
fun CenterVoiceFab(
    isListening: Boolean,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    VoiceMicFAB(
        isListening = isListening,
        onPressStart = onPressStart,
        onPressEnd = onPressEnd,
        onTap = onTap,
        modifier = modifier
    )
}

/**
 * Full WeatherGPT Voice Assistant Modal with real speech detection,
 * RMS audio waveform, live interim transcript, and error recovery.
 */
@Composable
fun VoiceAssistantModal(
    sessionState: VoiceSessionState,
    language: IndianLanguage,
    liveTranscript: String,
    finalTranscript: String,
    speechRms: Float,
    errorType: VoiceErrorType?,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onTypeQuestion: () -> Unit,
    onSelectPrompt: (String) -> Unit = {},
    lastSpokenAnswer: String? = null,
    onStopSpeaking: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveAnim")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveOffset"
    )

    val isListening = sessionState == VoiceSessionState.LISTENING
    val isProcessing = sessionState == VoiceSessionState.PROCESSING_SPEECH ||
            sessionState == VoiceSessionState.UNDERSTANDING ||
            sessionState == VoiceSessionState.FETCHING_WEATHER ||
            sessionState == VoiceSessionState.GENERATING_RESPONSE
    val isSpeaking = sessionState == VoiceSessionState.SPEAKING
    val isError = sessionState == VoiceSessionState.ERROR || errorType != null

    val promptText = when {
        isError -> "Unable to detect speech"
        sessionState == VoiceSessionState.PROCESSING_SPEECH || sessionState == VoiceSessionState.UNDERSTANDING -> "Understanding speech…"
        sessionState == VoiceSessionState.FETCHING_WEATHER || sessionState == VoiceSessionState.GENERATING_RESPONSE -> "Retrieving verified weather data…"
        sessionState == VoiceSessionState.SPEAKING -> "Reading WeatherGPT intelligence aloud…"
        else -> when (language) {
            IndianLanguage.HINDI -> "सुन रहा हूँ… बोलिए"
            IndianLanguage.ODIA -> "ଶୁଣୁଛି… ପାଣିପାଗ ପଚାରନ୍ତୁ"
            IndianLanguage.BENGALI -> "শুনছি… আবহাওয়া সম্পর্কে বলুন"
            IndianLanguage.ASSAMESE -> "শুনি আছোঁ… বতৰৰ বিষয়ে কওক"
            IndianLanguage.TAMIL -> "கேட்கிறேன்… வானிலை பற்றி பேசுங்கள்"
            IndianLanguage.TELUGU -> "వింటున్నాను… వాతావరణం గురించి అడగండి"
            IndianLanguage.KANNADA -> "ಕೇಳುತ್ತಿದ್ದೇನೆ… ಹವಾಮಾನದ ಬಗ್ಗೆ ಕೇಳಿ"
            IndianLanguage.MALAYALAM -> "കേൾക്കുന്നു… കാലാവസ്ഥ ചോദിക്കൂ"
            IndianLanguage.MARATHI -> "ऐकत आहे… हवामानाबद्दल विचारा"
            IndianLanguage.GUJARATI -> "સાંભળી રહ્યો છું… હવામાન પૂછો"
            IndianLanguage.PUNJABI -> "ਸੁਣ ਰਿਹਾ ਹਾਂ… ਮੌਸਮ ਬਾਰੇ ਪੁੱਛੋ"
            else -> "Listening… Speak your weather question"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavyBg.copy(alpha = 0.94f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceNavy)
                .border(
                    1.dp,
                    when {
                        isError -> AlertRed.copy(alpha = 0.6f)
                        isSpeaking -> AlertGreen.copy(alpha = 0.6f)
                        isProcessing -> AlertOrange.copy(alpha = 0.6f)
                        else -> ElectricCyan.copy(alpha = 0.5f)
                    },
                    RoundedCornerShape(28.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Title & Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Text(
                        text = "WEATHERGPT VOICE AGENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        ),
                        color = ElectricCyan
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            // Big Central Animated Voice Orb
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                when {
                                    isError -> AlertRed
                                    isSpeaking -> AlertGreen
                                    isProcessing -> AlertOrange
                                    else -> AlertRed
                                },
                                Color(0xFF002233)
                            )
                        )
                    )
                    .border(
                        2.dp,
                        when {
                            isError -> AlertRed
                            isSpeaking -> AlertGreen
                            isProcessing -> AlertOrange
                            else -> ElectricCyan
                        },
                        CircleShape
                    )
                    .clickable {
                        if (isError) onRetry()
                        else if (isListening) onDismiss()
                        else onRetry()
                    },
                contentAlignment = Alignment.Center
            ) {
                when {
                    isProcessing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(44.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    }
                    isSpeaking -> {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speaking",
                            tint = Color.White,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                    isError -> {
                        Icon(
                            imageVector = Icons.Default.MicOff,
                            contentDescription = "Mic Error",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Mic Active",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            // Status Title
            Text(
                text = promptText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = when {
                    isError -> AlertRed
                    isSpeaking -> AlertGreen
                    isProcessing -> CyanLight
                    else -> Color.White
                },
                textAlign = TextAlign.Center
            )

            // Realtime RMS Audio Decibel Reactive Waveform
            if (isListening) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val rms = speechRms.coerceIn(0f, 1f)
                    for (i in 0..12) {
                        val sinus = sin((i * 0.48f) + (waveOffset * 3.1415f)).coerceIn(0.15f, 1.0f)
                        // Height dynamically scales from baseline 6dp up to 34dp based on real audio decibel RMS
                        val dynamicHeight = (6 + (rms * 28 * sinus)).coerceIn(6f, 34f)
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .width(4.5.dp)
                                .height(dynamicHeight.dp)
                                .clip(RoundedCornerShape(50))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(ElectricCyan, CyanAccent)
                                    )
                                )
                        )
                    }
                }
            } else if (isSpeaking) {
                // Rhythmic Playback Waveform
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0..12) {
                        val factor = if (i % 2 == 0) waveOffset else (1.2f - waveOffset)
                        val barHeight = (8 + (18 * factor)).coerceIn(6f, 26f)
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .width(4.dp)
                                .height(barHeight.dp)
                                .clip(RoundedCornerShape(50))
                                .background(AlertGreen)
                        )
                    }
                }
            }

            // Live Recognized Text or Error State
            val activeTranscript = when {
                finalTranscript.isNotBlank() -> finalTranscript
                liveTranscript.isNotBlank() -> liveTranscript
                else -> ""
            }

            if (isError) {
                // Error Card with [TRY AGAIN] and [TYPE QUESTION]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AlertRed.copy(alpha = 0.12f))
                        .border(1.dp, AlertRed.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = AlertRed, modifier = Modifier.size(16.dp))
                        Text(
                            text = errorType?.messageHi ?: "कोई आवाज़ नहीं सुनाई दी।",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }

                    Text(
                        text = errorType?.messageEn ?: "No speech detected. Please speak closer to the microphone.",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AlertRed)
                                .clickable { onRetry() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "TRY AGAIN",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = Color.White
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SurfaceCard)
                                .border(0.5.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                                .clickable { onTypeQuestion() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(imageVector = Icons.Default.Keyboard, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "TYPE QUESTION",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp),
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }
            } else if (activeTranscript.isNotBlank()) {
                // Live Recognized Text Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(0.5.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (isListening) "YOU (SPEAKING):" else "YOUR QUESTION:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                fontSize = 9.sp
                            ),
                            color = ElectricCyan
                        )
                        Text(
                            text = "\"$activeTranscript\"",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = TextHighlight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // If Agent has spoken/generated an answer, show the Agent Answer Transcript
                if (!lastSpokenAnswer.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(AlertGreen.copy(alpha = 0.12f))
                            .border(0.5.dp, AlertGreen.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .padding(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "WEATHERGPT ADVISORY:",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        fontSize = 9.sp
                                    ),
                                    color = AlertGreen
                                )
                                Text(
                                    text = "Canonical Verified",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.5.sp),
                                    color = TextTertiary
                                )
                            }
                            Text(
                                text = lastSpokenAnswer,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    lineHeight = 16.sp
                                ),
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Barge-in / Interrupt Button if speaking
                if (isSpeaking) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AlertRed.copy(alpha = 0.2f))
                            .border(1.dp, AlertRed.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                            .clickable { onStopSpeaking() }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Interrupt", tint = AlertRed, modifier = Modifier.size(14.dp))
                            Text(
                                text = "STOP SPEAKING / BARGE-IN",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.5.sp),
                                color = AlertRed
                            )
                        }
                    }
                }
            } else {
                // Voice Quick Suggestion Chips (Prioritised for Hyperlocal & Agromet Guidance)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "QUICK VOICE SUGGESTIONS 🎙️",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            fontSize = 9.sp
                        ),
                        color = ElectricCyan
                    )
                    listOf(
                        "Will it rain today?" to "🌧️",
                        "Can I spray pesticide?" to "🧪",
                        "Tomorrow weather" to "📅",
                        "Flood warning" to "🌊",
                        "Show radar" to "📡",
                        "Wind speed now" to "💨"
                    ).forEach { (phrase, emoji) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCard.copy(alpha = 0.85f))
                                .border(0.5.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                .clickable { onSelectPrompt(phrase) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "“$phrase”",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = TextHighlight
                                )
                                Text(text = emoji, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Language & Release Instruction
            Text(
                text = when {
                    isError -> "Tap Try Again or select an example question"
                    isProcessing -> "Synthesizing AI meteorological advisory…"
                    isSpeaking -> "Advisory playing • Tap mic to ask another question"
                    else -> "Tap / Release mic to process • Auto-Detect: Hindi / Hinglish / ${language.displayName}"
                },
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Backward-compatible VoiceAssistantModal overload
 */
@Composable
fun VoiceAssistantModal(
    voiceState: VoiceState,
    language: IndianLanguage,
    recognizedText: String,
    speechRms: Float,
    onDismiss: () -> Unit,
    onSelectPrompt: (String) -> Unit = {}
) {
    VoiceAssistantModal(
        sessionState = if (voiceState == VoiceState.LISTENING) VoiceSessionState.LISTENING else if (voiceState == VoiceState.PROCESSING) VoiceSessionState.UNDERSTANDING else VoiceSessionState.IDLE,
        language = language,
        liveTranscript = recognizedText,
        finalTranscript = recognizedText,
        speechRms = speechRms,
        errorType = null,
        onDismiss = onDismiss,
        onRetry = onDismiss,
        onTypeQuestion = onDismiss,
        onSelectPrompt = onSelectPrompt
    )
}
