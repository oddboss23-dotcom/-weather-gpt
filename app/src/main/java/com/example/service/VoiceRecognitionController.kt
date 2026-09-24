package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.data.model.IndianLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceSessionState(val displayLabel: String) {
    IDLE("Ready"),
    REQUESTING_PERMISSION("Requesting Permission..."),
    READY("Ready to listen"),
    LISTENING("Listening..."),
    PROCESSING_SPEECH("Processing speech..."),
    UNDERSTANDING("Understanding question..."),
    FETCHING_WEATHER("Retrieving verified weather data..."),
    GENERATING_RESPONSE("Generating WeatherGPT advisory..."),
    SPEAKING("Speaking response aloud..."),
    ERROR("Error")
}

enum class VoiceErrorType(val messageEn: String, val messageHi: String) {
    NO_SPEECH_DETECTED(
        "No speech detected. Please speak closer to the microphone.",
        "कोई आवाज़ नहीं सुनाई दी। कृपया माइक के पास बोलें।"
    ),
    PERMISSION_DENIED(
        "Microphone permission is required for voice input.",
        "वॉयस असिस्टेंट के लिए माइक्रोफ़ोन की अनुमति आवश्यक है।"
    ),
    RECOGNITION_UNAVAILABLE(
        "Voice recognition service is not available on this device.",
        "इस डिवाइस पर वॉइस रिकग्निशन सेवा उपलब्ध नहीं है।"
    ),
    NETWORK_ERROR(
        "Voice recognition temporarily unavailable due to network.",
        "नेटवर्क समस्या के कारण आवाज़ नहीं पहचानी जा सकी।"
    ),
    AUDIO_ERROR(
        "Audio recording error. Please check your microphone.",
        "ऑडियो रिकॉर्डिंग में समस्या आई।"
    ),
    TIMEOUT(
        "Listening timed out. Please try again.",
        "समय समाप्त हो गया। कृपया दोबारा बोलें।"
    )
}

/**
 * Controller dedicated to managing Android SpeechRecognizer lifecycle,
 * audio stream initialization, real-time decibel RMS tracking,
 * interim partial transcription streaming, and multi-lingual Indian language state.
 */
class VoiceRecognitionController(private val context: Context) {

    private val TAG = "VoiceRecognitionCtrl"
    private val mainHandler = Handler(Looper.getMainLooper())

    private var speechRecognizer: SpeechRecognizer? = null

    private val _sessionState = MutableStateFlow(VoiceSessionState.IDLE)
    val sessionState: StateFlow<VoiceSessionState> = _sessionState.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _speechRms = MutableStateFlow(0f)
    val speechRms: StateFlow<Float> = _speechRms.asStateFlow()

    private val _liveInterimTranscript = MutableStateFlow("")
    val liveInterimTranscript: StateFlow<String> = _liveInterimTranscript.asStateFlow()

    private val _finalTranscript = MutableStateFlow("")
    val finalTranscript: StateFlow<String> = _finalTranscript.asStateFlow()

    private val _currentLanguage = MutableStateFlow(IndianLanguage.HINDI)
    val currentLanguage: StateFlow<IndianLanguage> = _currentLanguage.asStateFlow()

    private val _lastErrorType = MutableStateFlow<VoiceErrorType?>(null)
    val lastErrorType: StateFlow<VoiceErrorType?> = _lastErrorType.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var activeOnResult: ((String) -> Unit)? = null
    private var activeOnError: ((String) -> Unit)? = null

    // 6.5-second silence watchdog
    private val silenceTimeoutRunnable = Runnable {
        if (_sessionState.value == VoiceSessionState.LISTENING) {
            Log.d(TAG, "Watchdog timeout: No speech received within 6.5s")
            handleVoiceError(
                VoiceErrorType.NO_SPEECH_DETECTED,
                "No speech detected. Please speak closer to the microphone."
            )
        }
    }

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    fun startListening(
        language: IndianLanguage,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stopTimeoutWatchdog()

        _currentLanguage.value = language
        activeOnResult = onResult
        activeOnError = onError

        _liveInterimTranscript.value = ""
        _finalTranscript.value = ""
        _lastErrorType.value = null
        _errorMessage.value = null
        _speechRms.value = 0f

        if (!isRecognitionAvailable()) {
            handleVoiceError(
                VoiceErrorType.RECOGNITION_UNAVAILABLE,
                "Voice recognition service is not available on this device."
            )
            return
        }

        try {
            cleanupRecognizer()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(createListener())
            }

            val langTag = getLanguageTag(language)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, langTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            }

            _sessionState.value = VoiceSessionState.LISTENING
            _isListening.value = true

            speechRecognizer?.startListening(intent)
            startTimeoutWatchdog(6500)

            HapticManager.voiceListeningStarted(context)
            Log.d(TAG, "SpeechRecognizer initialized and started for language: $langTag")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start speech recognizer audio stream", e)
            handleVoiceError(
                VoiceErrorType.AUDIO_ERROR,
                "Microphone audio stream error: ${e.localizedMessage}"
            )
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "RecognitionListener.onReadyForSpeech: Ready to capture audio")
                _isListening.value = true
                _sessionState.value = VoiceSessionState.LISTENING
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "RecognitionListener.onBeginningOfSpeech: User vocalisation detected")
                stopTimeoutWatchdog()
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize dB (-2 to 10 dB) to 0.0f - 1.0f scale
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                _speechRms.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d(TAG, "RecognitionListener.onEndOfSpeech: User stopped speaking")
                _isListening.value = false
                _speechRms.value = 0f
                _sessionState.value = VoiceSessionState.PROCESSING_SPEECH
                HapticManager.voiceListeningStopped(context)
            }

            override fun onError(error: Int) {
                stopTimeoutWatchdog()
                _isListening.value = false
                _speechRms.value = 0f

                Log.w(TAG, "RecognitionListener.onError code: $error")
                val errorPair = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH ->
                        Pair(VoiceErrorType.NO_SPEECH_DETECTED, "No speech detected. Please speak clearly into the microphone.")
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        Pair(VoiceErrorType.TIMEOUT, "Listening timed out. Please try again.")
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        Pair(VoiceErrorType.NETWORK_ERROR, "Network error during speech transcription.")
                    SpeechRecognizer.ERROR_AUDIO ->
                        Pair(VoiceErrorType.AUDIO_ERROR, "Audio stream error. Please check your microphone.")
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        Pair(VoiceErrorType.PERMISSION_DENIED, "Microphone permission is required.")
                    else ->
                        Pair(VoiceErrorType.NO_SPEECH_DETECTED, "Could not detect speech. Please try again.")
                }
                handleVoiceError(errorPair.first, errorPair.second)
            }

            override fun onResults(results: Bundle?) {
                stopTimeoutWatchdog()
                _isListening.value = false
                _speechRms.value = 0f

                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull()?.trim()

                if (!recognized.isNullOrBlank()) {
                    Log.d(TAG, "RecognitionListener.onResults transcript: $recognized")
                    _finalTranscript.value = recognized
                    _liveInterimTranscript.value = recognized
                    _sessionState.value = VoiceSessionState.UNDERSTANDING

                    HapticManager.voiceResultReceived(context)
                    activeOnResult?.invoke(recognized)
                } else {
                    handleVoiceError(
                        VoiceErrorType.NO_SPEECH_DETECTED,
                        "No speech detected. Please try again."
                    )
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partialText = partialMatches?.firstOrNull()?.trim()
                if (!partialText.isNullOrBlank()) {
                    Log.d(TAG, "RecognitionListener.onPartialResults: $partialText")
                    _liveInterimTranscript.value = partialText
                    stopTimeoutWatchdog()
                    startTimeoutWatchdog(4000)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun stopListening() {
        stopTimeoutWatchdog()
        try {
            if (_isListening.value) {
                _sessionState.value = VoiceSessionState.PROCESSING_SPEECH
                speechRecognizer?.stopListening()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping SpeechRecognizer", e)
        }
        _isListening.value = false
        _speechRms.value = 0f
    }

    fun cancelListening() {
        stopTimeoutWatchdog()
        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
        cleanupRecognizer()
        HapticManager.voiceListeningStopped(context)
        reset()
    }

    fun reset() {
        stopTimeoutWatchdog()
        _isListening.value = false
        _speechRms.value = 0f
        _liveInterimTranscript.value = ""
        _finalTranscript.value = ""
        _lastErrorType.value = null
        _errorMessage.value = null
        _sessionState.value = VoiceSessionState.IDLE
    }

    fun setSessionState(state: VoiceSessionState) {
        _sessionState.value = state
    }

    private fun handleVoiceError(errorType: VoiceErrorType, msg: String) {
        _isListening.value = false
        _speechRms.value = 0f
        _lastErrorType.value = errorType
        _errorMessage.value = msg
        _sessionState.value = VoiceSessionState.ERROR
        HapticManager.voiceError(context)
        activeOnError?.invoke(msg)
    }

    private fun startTimeoutWatchdog(timeoutMs: Long) {
        mainHandler.removeCallbacks(silenceTimeoutRunnable)
        mainHandler.postDelayed(silenceTimeoutRunnable, timeoutMs)
    }

    private fun stopTimeoutWatchdog() {
        mainHandler.removeCallbacks(silenceTimeoutRunnable)
    }

    private fun cleanupRecognizer() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    private fun getLanguageTag(language: IndianLanguage): String {
        return when (language) {
            IndianLanguage.HINDI -> "hi-IN"
            IndianLanguage.ODIA -> "or-IN"
            IndianLanguage.BENGALI -> "bn-IN"
            IndianLanguage.TAMIL -> "ta-IN"
            IndianLanguage.TELUGU -> "te-IN"
            IndianLanguage.MARATHI -> "mr-IN"
            IndianLanguage.KANNADA -> "kn-IN"
            IndianLanguage.GUJARATI -> "gu-IN"
            IndianLanguage.PUNJABI -> "pa-IN"
            IndianLanguage.MALAYALAM -> "ml-IN"
            IndianLanguage.ASSAMESE -> "as-IN"
            IndianLanguage.ENGLISH -> "en-IN"
        }
    }

    fun destroy() {
        stopTimeoutWatchdog()
        cancelListening()
        cleanupRecognizer()
    }
}
