package com.example.service

import android.content.Context
import com.example.data.model.AlertSeverity
import com.example.data.model.DisasterAlert

/**
 * Backward-compatible facade for [HapticManager].
 * All calls route to the centralized haptic controller with preference,
 * hardware safety, and event deduplication guarantees.
 */
object HapticFeedbackManager {

    fun isHapticEnabled(context: Context? = null): Boolean =
        HapticManager.isHapticEnabled(context)

    fun setHapticEnabled(context: Context, enabled: Boolean) =
        HapticManager.setHapticEnabled(context, enabled)

    fun lightTap(context: Context? = null) =
        HapticManager.lightTap(context)

    fun mediumTap(context: Context? = null) =
        HapticManager.mediumTap(context)

    fun heavyTap(context: Context? = null) =
        HapticManager.heavyTap(context)

    fun selectionChanged(context: Context? = null) =
        HapticManager.selectionChanged(context)

    fun toggleSwitched(context: Context? = null, isOn: Boolean) =
        HapticManager.toggleSwitched(context, isOn)

    fun success(context: Context? = null) =
        HapticManager.success(context)

    fun warning(context: Context? = null) =
        HapticManager.warning(context)

    fun error(context: Context? = null) =
        HapticManager.error(context)

    fun voiceListeningStarted(context: Context? = null) =
        HapticManager.voiceListeningStarted(context)

    fun voiceListeningStopped(context: Context? = null) =
        HapticManager.voiceListeningStopped(context)

    fun voiceResultReceived(context: Context? = null) =
        HapticManager.voiceResultReceived(context)

    fun voiceSpeakingStarted(context: Context? = null) =
        HapticManager.voiceSpeakingStarted(context)

    fun voiceBargeIn(context: Context? = null) =
        HapticManager.voiceBargeIn(context)

    fun voiceError(context: Context? = null) =
        HapticManager.voiceError(context)

    fun alertTriggered(context: Context? = null, severity: AlertSeverity = AlertSeverity.RED) =
        HapticManager.alertTriggered(context, severity)

    fun triggerMapInteraction(context: Context) =
        HapticManager.lightTap(context)

    fun triggerMapViewSwitch(context: Context) =
        HapticManager.selectionChanged(context)

    fun triggerWeatherAlert(context: Context, severity: AlertSeverity = AlertSeverity.YELLOW) =
        HapticManager.warning(context)

    fun triggerFloodWarning(context: Context, isCritical: Boolean = false) =
        HapticManager.triggerFloodWarning(context, isCritical)

    fun triggerSevereAlert(context: Context) =
        HapticManager.alertTriggered(context, AlertSeverity.RED)

    fun triggerVoiceInteraction(context: Context) =
        HapticManager.voiceListeningStarted(context)

    fun processAlerts(context: Context?, alerts: List<DisasterAlert>) =
        HapticManager.processAlerts(context, alerts)

    fun onLocationStateChanged(context: Context?, isSuccess: Boolean, isError: Boolean) =
        HapticManager.onLocationStateChanged(context, isSuccess, isError)

    fun onKrishiAdvisoryReady(context: Context?, cropId: String, riskLevelString: String, location: String) =
        HapticManager.onKrishiAdvisoryReady(context, cropId, riskLevelString, location)
}
