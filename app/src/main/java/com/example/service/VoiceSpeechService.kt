package com.example.service

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.data.model.IndianLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Backward compatibility alias for UI components
 */
enum class VoiceUiState(val displayLabel: String) {
    READY("READY"),
    LISTENING("LISTENING..."),
    UNDERSTANDING("UNDERSTANDING..."),
    ANALYZING_WEATHER("ANALYZING WEATHER..."),
    SPEAKING("SPEAKING...")
}

/**
 * WeatherGPT Core Voice Speech Service.
 * Coordinates VoiceRecognitionController (SpeechRecognizer audio stream pipeline)
 * and Android TextToSpeech engine for multi-lingual vocalisation.
 */
class VoiceSpeechService(private val context: Context) : TextToSpeech.OnInitListener {

    private val TAG = "VoiceSpeechService"

    val recognitionController = VoiceRecognitionController(context)

    val sessionState: StateFlow<VoiceSessionState> = recognitionController.sessionState
    val isListening: StateFlow<Boolean> = recognitionController.isListening
    val speechRms: StateFlow<Float> = recognitionController.speechRms
    val liveInterimTranscript: StateFlow<String> = recognitionController.liveInterimTranscript
    val finalTranscript: StateFlow<String> = recognitionController.finalTranscript
    val lastErrorType: StateFlow<VoiceErrorType?> = recognitionController.lastErrorType
    val errorMessage: StateFlow<String?> = recognitionController.errorMessage

    private val _voiceUiState = MutableStateFlow(VoiceUiState.READY)
    val voiceUiState: StateFlow<VoiceUiState> = _voiceUiState.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _lastSpokenAnswer = MutableStateFlow<String?>(null)
    val lastSpokenAnswer: StateFlow<String?> = _lastSpokenAnswer.asStateFlow()

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var pendingSpeechText: String? = null
    private var pendingSpeechLang: IndianLanguage? = null
    private var pendingSpeechCallback: (() -> Unit)? = null
    private var lastSpokenLanguage: IndianLanguage = IndianLanguage.HINDI

    init {
        ensureTtsInitialized()
    }

    private fun ensureTtsInitialized() {
        if (tts == null) {
            try {
                tts = TextToSpeech(context.applicationContext, this)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to initialize TTS engine", e)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            try {
                tts?.language = Locale.forLanguageTag("en-IN")
            } catch (_: Exception) {}

            val text = pendingSpeechText
            val lang = pendingSpeechLang
            val cb = pendingSpeechCallback
            if (text != null && lang != null) {
                pendingSpeechText = null
                pendingSpeechLang = null
                pendingSpeechCallback = null
                speak(text, lang, cb ?: {})
            }
        } else {
            Log.w(TAG, "TextToSpeech initialization status: $status")
        }
    }

    fun setVoiceUiState(state: VoiceUiState) {
        _voiceUiState.value = state
        when (state) {
            VoiceUiState.READY -> {
                if (sessionState.value != VoiceSessionState.SPEAKING && sessionState.value != VoiceSessionState.LISTENING) {
                    recognitionController.setSessionState(VoiceSessionState.IDLE)
                }
            }
            VoiceUiState.LISTENING -> recognitionController.setSessionState(VoiceSessionState.LISTENING)
            VoiceUiState.UNDERSTANDING -> recognitionController.setSessionState(VoiceSessionState.UNDERSTANDING)
            VoiceUiState.ANALYZING_WEATHER -> recognitionController.setSessionState(VoiceSessionState.FETCHING_WEATHER)
            VoiceUiState.SPEAKING -> recognitionController.setSessionState(VoiceSessionState.SPEAKING)
        }
    }

    fun setSessionState(state: VoiceSessionState) {
        recognitionController.setSessionState(state)
        when (state) {
            VoiceSessionState.IDLE, VoiceSessionState.READY -> _voiceUiState.value = VoiceUiState.READY
            VoiceSessionState.LISTENING -> _voiceUiState.value = VoiceUiState.LISTENING
            VoiceSessionState.PROCESSING_SPEECH, VoiceSessionState.UNDERSTANDING -> _voiceUiState.value = VoiceUiState.UNDERSTANDING
            VoiceSessionState.FETCHING_WEATHER, VoiceSessionState.GENERATING_RESPONSE -> _voiceUiState.value = VoiceUiState.ANALYZING_WEATHER
            VoiceSessionState.SPEAKING -> _voiceUiState.value = VoiceUiState.SPEAKING
            VoiceSessionState.ERROR -> _voiceUiState.value = VoiceUiState.READY
            else -> {}
        }
    }

    fun resetState() {
        recognitionController.reset()
        _voiceUiState.value = VoiceUiState.READY
    }

    fun startListening(
        language: IndianLanguage,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        stopSpeaking()
        _voiceUiState.value = VoiceUiState.LISTENING
        recognitionController.startListening(
            language = language,
            onResult = { text ->
                _voiceUiState.value = VoiceUiState.UNDERSTANDING
                onResult(text)
            },
            onError = { err ->
                _voiceUiState.value = VoiceUiState.READY
                onError(err)
            }
        )
    }

    fun stopListening() {
        _voiceUiState.value = VoiceUiState.UNDERSTANDING
        recognitionController.stopListening()
    }

    fun cancelListening() {
        recognitionController.cancelListening()
        _voiceUiState.value = VoiceUiState.READY
    }

    fun speak(text: String, language: IndianLanguage, onComplete: () -> Unit = {}) {
        _lastSpokenAnswer.value = text
        lastSpokenLanguage = language

        if (!isTtsReady) {
            pendingSpeechText = text
            pendingSpeechLang = language
            pendingSpeechCallback = onComplete
            ensureTtsInitialized()
            return
        }
        stopSpeaking()

        val primaryLocale = getLocaleForLanguage(language)
        val effectiveLocale = try {
            val availability = tts?.isLanguageAvailable(primaryLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (availability >= TextToSpeech.LANG_AVAILABLE) {
                primaryLocale
            } else {
                val hiLocale = Locale.forLanguageTag("hi-IN")
                if ((tts?.isLanguageAvailable(hiLocale) ?: -1) >= TextToSpeech.LANG_AVAILABLE) {
                    hiLocale
                } else {
                    Locale.forLanguageTag("en-IN")
                }
            }
        } catch (_: Exception) {
            Locale.forLanguageTag("en-IN")
        }

        try {
            tts?.language = effectiveLocale
            tts?.setSpeechRate(0.95f)
            tts?.setPitch(1.0f)

            val matchingVoice = tts?.voices?.firstOrNull { voice ->
                voice.locale.language == effectiveLocale.language && !voice.isNetworkConnectionRequired
            } ?: tts?.voices?.firstOrNull { voice ->
                voice.locale.language == effectiveLocale.language
            }
            if (matchingVoice != null) {
                tts?.voice = matchingVoice
            }
        } catch (_: Exception) {}

        val cleanText = text
            .substringBefore("[DATA PROVENANCE]")
            .substringBefore("Source:")
            .replace(Regex("\\[.*?\\]"), "")
            .replace(Regex("[*#_`>]"), "")
            .take(1000)

        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
                recognitionController.setSessionState(VoiceSessionState.SPEAKING)
                _voiceUiState.value = VoiceUiState.SPEAKING
                HapticManager.voiceSpeakingStarted(context)
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                recognitionController.setSessionState(VoiceSessionState.IDLE)
                _voiceUiState.value = VoiceUiState.READY
                onComplete()
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                recognitionController.setSessionState(VoiceSessionState.IDLE)
                _voiceUiState.value = VoiceUiState.READY
            }
        })

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "WeatherGPT_TTS_${System.currentTimeMillis()}")
        try {
            recognitionController.setSessionState(VoiceSessionState.SPEAKING)
            _voiceUiState.value = VoiceUiState.SPEAKING
            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, "WeatherGPT_TTS")
        } catch (e: Exception) {
            Log.e(TAG, "TTS speak failed", e)
            _isSpeaking.value = false
            recognitionController.setSessionState(VoiceSessionState.IDLE)
            _voiceUiState.value = VoiceUiState.READY
        }
    }

    fun speakAutoDetect(text: String, fallbackLanguage: IndianLanguage = IndianLanguage.HINDI, onComplete: () -> Unit = {}) {
        val detected = LanguageDetectionService.detectLanguage(text, fallbackLanguage).preferredResponseLanguage
        speak(text, detected, onComplete)
    }

    fun replayLastSpeech() {
        val text = _lastSpokenAnswer.value
        if (!text.isNullOrBlank()) {
            speak(text, lastSpokenLanguage)
        }
    }

    fun stopSpeaking() {
        if (_isSpeaking.value || _voiceUiState.value == VoiceUiState.SPEAKING) {
            try {
                tts?.stop()
            } catch (_: Exception) {}
            HapticManager.voiceBargeIn(context)
            _isSpeaking.value = false
            recognitionController.setSessionState(VoiceSessionState.IDLE)
            _voiceUiState.value = VoiceUiState.READY
        }
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

    private fun getLocaleForLanguage(language: IndianLanguage): Locale {
        return Locale.forLanguageTag(getLanguageTag(language))
    }

    fun release() {
        stopSpeaking()
        recognitionController.destroy()
        try {
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
    }
}
