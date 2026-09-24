package com.example.service

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.os.Build
import android.os.SystemClock
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log
import com.example.data.model.AlertSeverity
import com.example.data.model.DisasterAlert
import java.util.concurrent.ConcurrentHashMap

/**
 * Robust Haptic Manager for Android.
 *
 * Handles platform-safe tactile feedback across user interactions, alerts, and voice states.
 * Features:
 * - Modern Android APIs: VibratorManager (API 31+), VibrationEffect.Composition (API 30+),
 *   VibrationAttributes (API 30+/33+), VibrationEffect predefined & waveforms (API 26+).
 *   Graceful legacy fallback for older devices.
 * - Hardware safety: Verifies vibrator existence, amplitude control capability, and catches
 *   SecurityExceptions or missing hardware states without crashing.
 * - System & User Preference respect: Honors Settings.System.HAPTIC_FEEDBACK_ENABLED and
 *   app-level SharedPreferences toggle, while preserving safety-critical emergency disaster alerts.
 * - Compose Recomposition-Proof: Micro-debouncing and memory-bounded alert deduplication.
 */
object HapticManager {

    private const val TAG = "HapticManager"
    private const val PREFS_NAME = "weathergpt_haptics"
    private const val KEY_HAPTIC_ENABLED = "haptic_feedback_enabled"

    // Minimum interval (ms) between rapid identical tap events to avoid motor thrashing
    private const val RAPID_TAP_DEBOUNCE_MS = 45L

    @Volatile
    private var lastTapTimestamp = 0L

    @Volatile
    private var appContext: Context? = null

    // Cache of processed alerts to prevent duplicate vibrations on Compose recomposition
    private val processedAlerts = ConcurrentHashMap<String, AlertSeverity>()

    // Cache of processed Krishi advisories: key -> timestamp
    private val processedKrishiAdvisories = ConcurrentHashMap<String, Long>()

    // Location state transition tracker: NO_VALID_LOCATION -> VALID_LOCATION
    @Volatile
    private var hasValidLocation = false

    @Volatile
    private var lastLocationErrorTimestamp = 0L

    /**
     * Initialize with application context for lifecycle-safe, leak-free background access.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private fun resolveContext(context: Context?): Context? {
        val resolved = context?.applicationContext ?: appContext
        if (appContext == null && resolved != null) {
            appContext = resolved
        }
        return resolved
    }

    // =========================================================================
    // USER & SYSTEM PREFERENCE MANAGEMENT
    // =========================================================================

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks whether haptic feedback is enabled by both user preference and system setting.
     */
    fun isHapticEnabled(context: Context? = null): Boolean {
        val ctx = resolveContext(context) ?: return true
        val prefs = getPrefs(ctx)
        val userEnabled = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        if (!userEnabled) return false

        // Respect system-level haptic feedback toggle where applicable
        return try {
            @Suppress("DEPRECATION")
            val systemHaptic = Settings.System.getInt(
                ctx.contentResolver,
                Settings.System.HAPTIC_FEEDBACK_ENABLED,
                1
            )
            systemHaptic != 0
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Persist the user's haptic feedback preference (ON / OFF).
     */
    fun setHapticEnabled(context: Context, enabled: Boolean) {
        val ctx = context.applicationContext
        getPrefs(ctx).edit().putBoolean(KEY_HAPTIC_ENABLED, enabled).apply()
    }

    // =========================================================================
    // HARDWARE ABSTRACTION & COMPATIBILITY
    // =========================================================================

    private fun getVibrator(context: Context?): Vibrator? {
        val ctx = resolveContext(context) ?: return null
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve vibrator service", e)
            null
        }
    }

    private fun canVibrate(context: Context?, isSafetyCritical: Boolean = false): Boolean {
        // Severe safety emergency alerts are preserved unless explicitly silenced
        if (!isSafetyCritical && !isHapticEnabled(context)) {
            return false
        }
        val vibrator = getVibrator(context) ?: return false
        return try {
            vibrator.hasVibrator()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Dispatches vibration effect with platform-appropriate attributes (VibrationAttributes or AudioAttributes).
     */
    private fun dispatchVibrate(
        context: Context?,
        effect: VibrationEffect,
        isTouchUsage: Boolean = true,
        isAlarmUsage: Boolean = false
    ) {
        val vibrator = getVibrator(context) ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val usage = when {
                    isAlarmUsage -> VibrationAttributes.USAGE_ALARM
                    isTouchUsage -> VibrationAttributes.USAGE_TOUCH
                    else -> VibrationAttributes.USAGE_COMMUNICATION_REQUEST
                }
                val attrs = VibrationAttributes.Builder().setUsage(usage).build()
                vibrator.vibrate(effect, attrs)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val usage = when {
                    isAlarmUsage -> AudioAttributes.USAGE_ALARM
                    isTouchUsage -> AudioAttributes.USAGE_ASSISTANCE_SONIFICATION
                    else -> AudioAttributes.USAGE_NOTIFICATION
                }
                val audioAttrs = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(usage)
                    .build()
                @Suppress("DEPRECATION")
                vibrator.vibrate(effect, audioAttrs)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(effect)
            }
        } catch (e: Exception) {
            Log.w(TAG, "dispatchVibrate failed with VibrationEffect", e)
        }
    }

    // =========================================================================
    // 1. USER INTERACTIONS (Clicks, Taps, Ticks, Detents, Toggles)
    // =========================================================================

    /**
     * Subtle, crisp tactile click for standard interactive controls:
     * Bottom navigation tabs, primary buttons, card clicks, search buttons.
     */
    fun lightTap(context: Context? = null) {
        val now = SystemClock.uptimeMillis()
        if (now - lastTapTimestamp < RAPID_TAP_DEBOUNCE_MS) return
        lastTapTimestamp = now

        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)
            ) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.7f)
                    .compose()
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                dispatchVibrate(context, VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK), isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createOneShot(15, 60)
                } else {
                    VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(15)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform lightTap", e)
        }
    }

    /**
     * Medium tactile pulse for firmer actions:
     * Dialog confirmations, modal dismissal, refresh triggers, settings triggers.
     */
    fun mediumTap(context: Context? = null) {
        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)
            ) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 1.0f)
                    .compose()
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                dispatchVibrate(context, VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK), isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createOneShot(30, 120)
                } else {
                    VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(30)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform mediumTap", e)
        }
    }

    /**
     * Heavy tactile pulse for high-impact actions:
     * Deletions, cache clears, emergency overrides, severe toggles.
     */
    fun heavyTap(context: Context? = null) {
        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD)
            ) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f)
                    .compose()
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                dispatchVibrate(context, VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK), isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createOneShot(45, 220)
                } else {
                    VibrationEffect.createOneShot(45, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(45)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform heavyTap", e)
        }
    }

    /**
     * Subtle detent tick for selection changes:
     * Slider increments, timeline frame steps, radio button switches, filter chips.
     */
    fun selectionChanged(context: Context? = null) {
        val now = SystemClock.uptimeMillis()
        if (now - lastTapTimestamp < RAPID_TAP_DEBOUNCE_MS) return
        lastTapTimestamp = now

        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK)
            ) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.6f)
                    .compose()
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                dispatchVibrate(context, VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK), isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createOneShot(10, 40)
                } else {
                    VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform selectionChanged", e)
        }
    }

    /**
     * Tactile feedback for toggles and switches:
     * Distinct ascending tactile cue for turning ON, descending cue for turning OFF.
     */
    fun toggleSwitched(context: Context? = null, isOn: Boolean) {
        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areAllPrimitivesSupported(
                    VibrationEffect.Composition.PRIMITIVE_QUICK_RISE,
                    VibrationEffect.Composition.PRIMITIVE_QUICK_FALL
                )
            ) {
                val primitive = if (isOn) {
                    VibrationEffect.Composition.PRIMITIVE_QUICK_RISE
                } else {
                    VibrationEffect.Composition.PRIMITIVE_QUICK_FALL
                }
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(primitive, 0.8f)
                    .compose()
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = if (isOn) longArrayOf(0, 15, 30, 25) else longArrayOf(0, 25, 30, 15)
                val amplitudes = if (isOn) intArrayOf(0, 70, 0, 150) else intArrayOf(0, 150, 0, 70)
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                } else {
                    VibrationEffect.createWaveform(timings, -1)
                }
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform toggleSwitched", e)
        }
    }

    // =========================================================================
    // 2. ALERTS, NOTIFICATIONS & METEOROLOGICAL CONDITIONS
    // =========================================================================

    /**
     * Success confirmation: Dual ascending pulse.
     * Triggered on:
     * - GPS location confirmed (NO_VALID_LOCATION -> VALID_LOCATION)
     * - Weather data refresh successfully completed
     * - KrishiGPT advisory generated
     */
    fun success(context: Context? = null) {
        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areAllPrimitivesSupported(
                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                    VibrationEffect.Composition.PRIMITIVE_QUICK_RISE
                )
            ) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.6f, 0)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_RISE, 0.9f, 60)
                    .compose()
                dispatchVibrate(context, effect, isTouchUsage = false)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 35, 55, 45)
                val amplitudes = intArrayOf(0, 110, 0, 190)
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                } else {
                    VibrationEffect.createWaveform(timings, -1)
                }
                dispatchVibrate(context, effect, isTouchUsage = false)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 35, 55, 45), -1)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform success haptic", e)
        }
    }

    /**
     * Warning feedback: Two distinct caution pulses.
     * Triggered on:
     * - Alert transition to WATCH or WARNING
     * - Heavy rainfall advisory
     * - High agromet crop risk notification
     */
    fun warning(context: Context? = null) {
        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areAllPrimitivesSupported(
                    VibrationEffect.Composition.PRIMITIVE_CLICK,
                    VibrationEffect.Composition.PRIMITIVE_THUD
                )
            ) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f, 0)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 70)
                    .compose()
                dispatchVibrate(context, effect, isTouchUsage = false)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 80, 60, 100)
                val amplitudes = intArrayOf(0, 160, 0, 200)
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                } else {
                    VibrationEffect.createWaveform(timings, -1)
                }
                dispatchVibrate(context, effect, isTouchUsage = false)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 80, 60, 100), -1)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform warning haptic", e)
        }
    }

    /**
     * Error feedback: Three firm descending reject pulses.
     * Triggered on:
     * - Location acquisition failure
     * - Voice recognition error / speech transcription failure
     * - Network timeout
     */
    fun error(context: Context? = null) {
        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD)
            ) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.8f, 0)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 0.7f, 50)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, 1.0f, 50)
                    .compose()
                dispatchVibrate(context, effect, isTouchUsage = false)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 60, 50, 60, 50, 70)
                val amplitudes = intArrayOf(0, 180, 0, 160, 0, 220)
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                } else {
                    VibrationEffect.createWaveform(timings, -1)
                }
                dispatchVibrate(context, effect, isTouchUsage = false)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 60, 50, 60, 50, 70), -1)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform error haptic", e)
        }
    }

    /**
     * Severe Emergency Disaster Alerts (RED Alert, Cyclone Landfall, Severe Flash Flood).
     * Strong, distinct warning pattern with alarm priority.
     */
    fun alertTriggered(context: Context? = null, severity: AlertSeverity = AlertSeverity.RED) {
        val isCritical = severity == AlertSeverity.RED
        if (!canVibrate(context, isSafetyCritical = isCritical)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 150, 80, 220, 80, 300)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                } else {
                    VibrationEffect.createWaveform(timings, -1)
                }
                dispatchVibrate(context, effect, isTouchUsage = false, isAlarmUsage = isCritical)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 150, 80, 220, 80, 300), -1)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform alertTriggered", e)
        }
    }

    /**
     * Flood Warnings: Escalating multi-pulse hydrological waveform pattern
     * representing CWC / FMIS telemetry danger level exceedance.
     */
    fun triggerFloodWarning(context: Context, isCritical: Boolean = false) {
        if (!canVibrate(context, isSafetyCritical = isCritical)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = if (isCritical) {
                    longArrayOf(0, 140, 70, 180, 70, 260)
                } else {
                    longArrayOf(0, 100, 60, 130, 60, 180)
                }
                val amplitudes = if (isCritical) {
                    intArrayOf(0, 220, 0, 240, 0, 255)
                } else {
                    intArrayOf(0, 170, 0, 200, 0, 230)
                }
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createWaveform(timings, amplitudes, -1)
                } else {
                    VibrationEffect.createWaveform(timings, -1)
                }
                dispatchVibrate(context, effect, isTouchUsage = false, isAlarmUsage = isCritical)
            } else {
                @Suppress("DEPRECATION")
                val pattern = if (isCritical) {
                    longArrayOf(0, 140, 70, 180, 70, 260)
                } else {
                    longArrayOf(0, 100, 60, 130, 60, 180)
                }
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform triggerFloodWarning", e)
        }
    }

    // =========================================================================
    // 3. VOICE AGENT STATES (Listening, Transcript, Speaking, Barge-in, Error)
    // =========================================================================

    /**
     * Voice Agent: IDLE -> LISTENING transition.
     * Microphone opened and actively streaming audio.
     */
    fun voiceListeningStarted(context: Context? = null) {
        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areAllPrimitivesSupported(
                    VibrationEffect.Composition.PRIMITIVE_TICK,
                    VibrationEffect.Composition.PRIMITIVE_CLICK
                )
            ) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.6f, 0)
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.8f, 40)
                    .compose()
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                dispatchVibrate(context, VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK), isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createOneShot(20, 100)
                } else {
                    VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform voiceListeningStarted", e)
        }
    }

    /**
     * Voice Agent: User cancelled listening or mic closed (LISTENING -> IDLE).
     */
    fun voiceListeningStopped(context: Context? = null) {
        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_TICK)
            ) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 0.5f)
                    .compose()
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                dispatchVibrate(context, VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK), isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createOneShot(12, 50)
                } else {
                    VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(12)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform voiceListeningStopped", e)
        }
    }

    /**
     * Voice Agent: Final transcription recognized and parsed successfully.
     */
    fun voiceResultReceived(context: Context? = null) {
        lightTap(context)
    }

    /**
     * Voice Agent: TTS synthesized response begins speaking.
     */
    fun voiceSpeakingStarted(context: Context? = null) {
        if (!canVibrate(context)) return
        val vibrator = getVibrator(context) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_LOW_TICK)
            ) {
                val effect = VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_LOW_TICK, 0.4f)
                    .compose()
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = if (vibrator.hasAmplitudeControl()) {
                    VibrationEffect.createOneShot(10, 40)
                } else {
                    VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE)
                }
                dispatchVibrate(context, effect, isTouchUsage = true)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(10)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to perform voiceSpeakingStarted", e)
        }
    }

    /**
     * Voice Agent: User interrupts / barges in on TTS speech.
     */
    fun voiceBargeIn(context: Context? = null) {
        lightTap(context)
    }

    /**
     * Voice Agent: Error or unrecognized speech input.
     */
    fun voiceError(context: Context? = null) {
        error(context)
    }

    // =========================================================================
    // 4. DEDUPLICATION & STATE TRANSITION ENGINES
    // =========================================================================

    /**
     * Location Lifecycle Deduplication:
     * Only triggers [success] on NO_VALID_LOCATION -> VALID_LOCATION transition.
     * Only triggers [error] at most once per failed location session.
     */
    fun onLocationStateChanged(context: Context?, isSuccess: Boolean, isError: Boolean) {
        if (isSuccess) {
            if (!hasValidLocation) {
                hasValidLocation = true
                success(context)
            }
        } else {
            hasValidLocation = false
            if (isError) {
                val now = SystemClock.uptimeMillis()
                if (now - lastLocationErrorTimestamp > 5000L) {
                    lastLocationErrorTimestamp = now
                    error(context)
                }
            }
        }
    }

    /**
     * Resets location session status so a fresh explicit location request can trigger success()
     */
    fun resetLocationSession() {
        hasValidLocation = false
    }

    /**
     * Robust Alert Deduplication Engine:
     * Ensures Compose recomposition NEVER triggers repeated vibrations.
     * Evaluates alert ID + severity transition.
     *
     * Only triggers if:
     * 1. A genuinely new alert ID is encountered
     * 2. An existing alert escalates to higher severity (e.g. WATCH -> WARNING or WARNING -> SEVERE)
     */
    fun processAlerts(context: Context?, alerts: List<DisasterAlert>) {
        if (alerts.isEmpty()) return

        var highestNewSeverity: AlertSeverity? = null
        var hasNewFlood = false
        var isNewFloodCritical = false

        for (alert in alerts) {
            val prevSeverity = processedAlerts[alert.id]
            val isNew = prevSeverity == null
            val isEscalated = prevSeverity != null && alert.severity.ordinal > prevSeverity.ordinal

            if (isNew || isEscalated) {
                // Prevent memory leak by bounding cache size
                if (processedAlerts.size > 200) {
                    processedAlerts.clear()
                }
                processedAlerts[alert.id] = alert.severity

                if (alert.isFloodAlert) {
                    hasNewFlood = true
                    if (alert.severity == AlertSeverity.RED || alert.severity == AlertSeverity.ORANGE) {
                        isNewFloodCritical = true
                    }
                }

                if (highestNewSeverity == null || alert.severity.ordinal > highestNewSeverity.ordinal) {
                    highestNewSeverity = alert.severity
                }
            }
        }

        if (hasNewFlood) {
            triggerFloodWarning(context ?: appContext ?: return, isCritical = isNewFloodCritical)
        } else if (highestNewSeverity != null) {
            when (highestNewSeverity) {
                AlertSeverity.RED -> alertTriggered(context, AlertSeverity.RED)
                AlertSeverity.ORANGE, AlertSeverity.YELLOW -> warning(context)
                AlertSeverity.GREEN -> { /* Normal baseline: silent */ }
            }
        }
    }

    /**
     * KrishiGPT Advisory Completion Deduplication:
     * Triggers [success] when advisory calculation completes, or [warning] if severe farm risk alert.
     * Does NOT vibrate during token streaming or repeated recompositions.
     */
    fun onKrishiAdvisoryReady(context: Context?, cropId: String, riskLevelString: String, location: String) {
        val key = "$cropId-$riskLevelString-$location"
        val now = SystemClock.uptimeMillis()
        val lastTime = processedKrishiAdvisories[key]
        if (lastTime != null && now - lastTime < 15000L) {
            return
        }
        if (processedKrishiAdvisories.size > 100) {
            processedKrishiAdvisories.clear()
        }
        processedKrishiAdvisories[key] = now

        if (riskLevelString.equals("HIGH", ignoreCase = true) ||
            riskLevelString.equals("CRITICAL", ignoreCase = true) ||
            riskLevelString.equals("SEVERE", ignoreCase = true)
        ) {
            warning(context)
        } else {
            success(context)
        }
    }
}

