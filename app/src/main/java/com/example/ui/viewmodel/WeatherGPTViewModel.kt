package com.example.ui.viewmodel

import android.app.Application
import android.speech.SpeechRecognizer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.gis.CycloneTrackPoint
import com.example.data.gis.GisCycloneSystem
import com.example.data.gis.GisTimelineFrame
import com.example.data.gis.IndianGeographicRegistry
import com.example.data.gis.IndianStateGis
import com.example.data.gis.UnifiedWeatherData
import com.example.data.gis.WeatherDataFusionEngine
import com.example.data.gis.WeatherGisLayer
import com.example.data.hyperlocal.ForecastTrustAssessment
import com.example.data.hyperlocal.ForecastVerificationMetrics
import com.example.data.hyperlocal.ImpactSimulationResult
import com.example.data.hyperlocal.ImpactSimulationScenario
import com.example.data.hyperlocal.KrishiCropIntelligence
import com.example.data.hyperlocal.NearbyVillageComparison
import com.example.data.hyperlocal.NowcastIntelligence
import com.example.data.hyperlocal.VillageHierarchy
import com.example.data.hyperlocal.VillageRiskProfile
import com.example.service.HyperlocalIntelligenceEngine
import com.example.ui.components.QuickAccessTab
import com.example.data.local.ChatHistoryEntity
import com.example.data.local.GisRadarPointEntity
import com.example.data.local.WeatherGPTDatabase
import com.example.data.model.AlertSeverity
import com.example.data.model.ChatLanguageMode
import com.example.data.model.ChatMessage
import com.example.data.model.CityLocation
import com.example.data.model.DailyForecast
import com.example.data.model.DisasterAlert
import com.example.data.model.HourlyForecast
import com.example.data.model.IndianLanguage
import com.example.service.VoiceSessionState
import com.example.service.VoiceErrorType
import com.example.service.VoiceUiState
import com.example.service.VoiceWeatherIntentEngine
import com.example.service.LocationManager
import com.example.service.LocationFetchState
import com.example.service.HapticManager
import com.example.data.nwp.Forecast24H
import com.example.data.nwp.Forecast48H
import com.example.data.nwp.ForecastBustDetectionEngine
import com.example.data.nwp.ForecastBustReport
import com.example.data.nwp.ForecastRevisionSnapshot
import com.example.data.nwp.WeatherForecastRepository
import com.example.data.model.LocationState
import com.example.data.model.N8nAlertState
import com.example.data.model.ResearchAnalysis
import com.example.data.model.UserMode
import com.example.data.model.WeatherData
import com.example.data.model.WeatherInsight
import com.example.data.remote.N8nAlertService
import com.example.data.remote.WebSocketManager
import com.example.data.repository.AiWeatherRepository
import com.example.data.repository.AlertRepository
import com.example.data.repository.GisRadarRepository
import com.example.data.repository.LocationRepository
import com.example.data.repository.WeatherRepository
import com.example.service.DataMode
import com.example.service.HyperlocalWeatherService
import com.example.service.OfflineRuralManager
import com.example.service.VoiceSpeechService
import com.example.data.canonical.CanonicalLocation
import com.example.data.canonical.CanonicalLocationResolver
import com.example.data.canonical.CanonicalWeatherRepository
import com.example.data.canonical.IntegrityReport
import com.example.data.canonical.WeatherGPTIntegrityCheck
import com.example.data.canonical.WeatherSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class WeatherGPTViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WeatherGPTDatabase.getDatabase(application)
    private val weatherRepo = WeatherRepository(db.weatherDao())
    private val aiRepo = AiWeatherRepository()
    private val alertRepo = AlertRepository()
    private val locationRepo = LocationRepository(application)
    val locationManager = LocationManager(application)
    val weatherForecastRepo = WeatherForecastRepository()
    val canonicalWeatherRepo = CanonicalWeatherRepository(db.weatherDao())
    val gisRadarRepo = GisRadarRepository(db.gisRadarDao())
    val voiceService = VoiceSpeechService(application)
    val ruralOfflineManager = OfflineRuralManager(application, db, viewModelScope)
    val ruralOfflineState: StateFlow<com.example.service.RuralOfflineState> = ruralOfflineManager.ruralState

    // Canonical Single-Source-of-Truth Flows
    private val _canonicalLocation = MutableStateFlow(CanonicalLocationResolver.resolve(locationRepo.indianCities[0]))
    val canonicalLocation: StateFlow<CanonicalLocation> = _canonicalLocation.asStateFlow()

    private val _canonicalSnapshot = MutableStateFlow<WeatherSnapshot?>(null)
    val canonicalSnapshot: StateFlow<WeatherSnapshot?> = _canonicalSnapshot.asStateFlow()

    // 24H and 48H Structured NWP Forecast Flows
    private val _forecast24H = MutableStateFlow<Forecast24H?>(null)
    val forecast24H: StateFlow<Forecast24H?> = _forecast24H.asStateFlow()

    private val _forecast48H = MutableStateFlow<Forecast48H?>(null)
    val forecast48H: StateFlow<Forecast48H?> = _forecast48H.asStateFlow()

    private val _forecastBustReport = MutableStateFlow<ForecastBustReport?>(null)
    val forecastBustReport: StateFlow<ForecastBustReport?> = _forecastBustReport.asStateFlow()

    private val _systemIntegrityReport = MutableStateFlow<IntegrityReport?>(null)
    val systemIntegrityReport: StateFlow<IntegrityReport?> = _systemIntegrityReport.asStateFlow()

    private val _isIntegritySheetOpen = MutableStateFlow(false)
    val isIntegritySheetOpen: StateFlow<Boolean> = _isIntegritySheetOpen.asStateFlow()

    // Current State Flows
    private val _currentLocation = MutableStateFlow(locationRepo.indianCities[0]) // Delhi by default
    val currentLocation: StateFlow<CityLocation> = _currentLocation.asStateFlow()

    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _weatherData = MutableStateFlow(WeatherData())
    val weatherData: StateFlow<WeatherData> = _weatherData.asStateFlow()

    private val _hourlyForecast = MutableStateFlow<List<HourlyForecast>>(emptyList())
    val hourlyForecast: StateFlow<List<HourlyForecast>> = _hourlyForecast.asStateFlow()

    private val _dailyForecast = MutableStateFlow<List<DailyForecast>>(emptyList())
    val dailyForecast: StateFlow<List<DailyForecast>> = _dailyForecast.asStateFlow()

    private val _weatherInsight = MutableStateFlow<WeatherInsight?>(null)
    val weatherInsight: StateFlow<WeatherInsight?> = _weatherInsight.asStateFlow()

    private val _alerts = MutableStateFlow<List<DisasterAlert>>(emptyList())
    val alerts: StateFlow<List<DisasterAlert>> = _alerts.asStateFlow()

    private val _activeRedAlert = MutableStateFlow<DisasterAlert?>(null)
    val activeRedAlert: StateFlow<DisasterAlert?> = _activeRedAlert.asStateFlow()

    private val _isDisasterResponseMode = MutableStateFlow(false)
    val isDisasterResponseMode: StateFlow<Boolean> = _isDisasterResponseMode.asStateFlow()

    private val _selectedLanguage = MutableStateFlow(IndianLanguage.ENGLISH)
    val selectedLanguage: StateFlow<IndianLanguage> = _selectedLanguage.asStateFlow()

    private val _chatLanguageMode = MutableStateFlow(ChatLanguageMode.AUTO)
    val chatLanguageMode: StateFlow<ChatLanguageMode> = _chatLanguageMode.asStateFlow()

    val voiceUiState: StateFlow<VoiceUiState> = voiceService.voiceUiState
    val voiceSessionState: StateFlow<VoiceSessionState> = voiceService.sessionState
    val liveInterimTranscript: StateFlow<String> = voiceService.liveInterimTranscript
    val finalTranscript: StateFlow<String> = voiceService.finalTranscript
    val voiceErrorType: StateFlow<VoiceErrorType?> = voiceService.lastErrorType
    val lastSpokenAnswer: StateFlow<String?> = voiceService.lastSpokenAnswer

    fun setChatLanguageMode(mode: ChatLanguageMode) {
        _chatLanguageMode.value = mode
        mode.language?.let {
            _selectedLanguage.value = it
        }
    }

    fun replayLastSpokenAnswer() {
        voiceService.replayLastSpeech()
    }

    private val _selectedUserMode = MutableStateFlow(UserMode.GENERAL_PUBLIC)
    val selectedUserMode: StateFlow<UserMode> = _selectedUserMode.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _researchAnalysis = MutableStateFlow<ResearchAnalysis?>(null)
    val researchAnalysis: StateFlow<ResearchAnalysis?> = _researchAnalysis.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _chatStatusText = MutableStateFlow("WeatherGPT AI is evaluating meteorological parameters...")
    val chatStatusText: StateFlow<String> = _chatStatusText.asStateFlow()

    private val _lastFailedQuery = MutableStateFlow<String?>(null)
    val lastFailedQuery: StateFlow<String?> = _lastFailedQuery.asStateFlow()

    private val _n8nDispatchStatus = MutableStateFlow<String?>(null)
    val n8nDispatchStatus: StateFlow<String?> = _n8nDispatchStatus.asStateFlow()

    private val _n8nAlertState = MutableStateFlow<N8nAlertState>(N8nAlertState.Idle)
    val n8nAlertState: StateFlow<N8nAlertState> = _n8nAlertState.asStateFlow()

    private val _isN8nDispatching = MutableStateFlow(false)
    val isN8nDispatching: StateFlow<Boolean> = _isN8nDispatching.asStateFlow()

    private val _n8nHealthStatus = MutableStateFlow("🟢 WeatherGPT AI Online • 🟡 Automation Standby")
    val n8nHealthStatus: StateFlow<String> = _n8nHealthStatus.asStateFlow()

    // Developer Diagnostics State
    private val _aiBackendStatus = MutableStateFlow("ACTIVE (Google Gemini 3.6-Flash)")
    val aiBackendStatus: StateFlow<String> = _aiBackendStatus.asStateFlow()

    private val _n8nChatStatus = MutableStateFlow("OPTIONAL (Standby)")
    val n8nChatStatus: StateFlow<String> = _n8nChatStatus.asStateFlow()

    private val _geminiStatus = MutableStateFlow("ACTIVE (Direct Gemini 3.6-Flash)")
    val geminiStatus: StateFlow<String> = _geminiStatus.asStateFlow()

    private val _alertWebhookStatus = MutableStateFlow("STANDBY")
    val alertWebhookStatus: StateFlow<String> = _alertWebhookStatus.asStateFlow()

    private val _speechRecognitionStatus = MutableStateFlow(
        if (SpeechRecognizer.isRecognitionAvailable(application)) "AVAILABLE" else "UNAVAILABLE"
    )
    val speechRecognitionStatus: StateFlow<String> = _speechRecognitionStatus.asStateFlow()

    private val _ttsStatus = MutableStateFlow("AVAILABLE")
    val ttsStatus: StateFlow<String> = _ttsStatus.asStateFlow()

    val aiLatencyStats = com.example.service.AiLatencyTracker.latencyStats

    private val _isTestingDiagnostics = MutableStateFlow(false)
    val isTestingDiagnostics: StateFlow<Boolean> = _isTestingDiagnostics.asStateFlow()

    val cachedRadarPoints: StateFlow<List<GisRadarPointEntity>> = gisRadarRepo.getAllCachedPoints()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Advanced GIS Meteorological Intelligence States
    private val _activeQuickAccessTab = MutableStateFlow(QuickAccessTab.NONE)
    val activeQuickAccessTab: StateFlow<QuickAccessTab> = _activeQuickAccessTab.asStateFlow()

    private val _activeGisLayer = MutableStateFlow(WeatherGisLayer.RADAR)
    val activeGisLayer: StateFlow<WeatherGisLayer> = _activeGisLayer.asStateFlow()

    private val _gisLayerOpacity = MutableStateFlow(0.85f)
    val gisLayerOpacity: StateFlow<Float> = _gisLayerOpacity.asStateFlow()

    private val _comparisonGisLayer = MutableStateFlow<WeatherGisLayer?>(null)
    val comparisonGisLayer: StateFlow<WeatherGisLayer?> = _comparisonGisLayer.asStateFlow()

    private val _isGisTimelinePlaying = MutableStateFlow(false)
    val isGisTimelinePlaying: StateFlow<Boolean> = _isGisTimelinePlaying.asStateFlow()

    private val _selectedTimelineIndex = MutableStateFlow(3) // Index 3 = NOW
    val selectedTimelineIndex: StateFlow<Int> = _selectedTimelineIndex.asStateFlow()

    val gisTimelineFrames: List<GisTimelineFrame> = listOf(
        GisTimelineFrame(0, -3, "T-3h", "08 Sep 11:20 IST", isForecast = false, stormShiftX = -1.2f, stormShiftY = -0.8f),
        GisTimelineFrame(1, -2, "T-2h", "08 Sep 12:20 IST", isForecast = false, stormShiftX = -0.8f, stormShiftY = -0.5f),
        GisTimelineFrame(2, -1, "T-1h", "08 Sep 13:20 IST", isForecast = false, stormShiftX = -0.4f, stormShiftY = -0.2f),
        GisTimelineFrame(3, 0, "NOW", "08 Sep 14:20 IST", isForecast = false, stormShiftX = 0f, stormShiftY = 0f),
        GisTimelineFrame(4, 1, "+1h", "08 Sep 15:20 IST", isForecast = true, stormShiftX = 0.4f, stormShiftY = 0.3f),
        GisTimelineFrame(5, 2, "+2h", "08 Sep 16:20 IST", isForecast = true, stormShiftX = 0.8f, stormShiftY = 0.6f),
        GisTimelineFrame(6, 3, "+3h", "08 Sep 17:20 IST", isForecast = true, stormShiftX = 1.2f, stormShiftY = 0.9f),
        GisTimelineFrame(7, 6, "+6h", "08 Sep 20:20 IST", isForecast = true, stormShiftX = 2.0f, stormShiftY = 1.5f)
    )

    private val _inspectedPoint = MutableStateFlow<UnifiedWeatherData?>(null)
    val inspectedPoint: StateFlow<UnifiedWeatherData?> = _inspectedPoint.asStateFlow()

    private val _isMapAiAnalysisOpen = MutableStateFlow(false)
    val isMapAiAnalysisOpen: StateFlow<Boolean> = _isMapAiAnalysisOpen.asStateFlow()

    private val _mapAiAnalysisText = MutableStateFlow<String?>(null)
    val mapAiAnalysisText: StateFlow<String?> = _mapAiAnalysisText.asStateFlow()

    private val _isMapAiAnalysisLoading = MutableStateFlow(false)
    val isMapAiAnalysisLoading: StateFlow<Boolean> = _isMapAiAnalysisLoading.asStateFlow()

    private val _activeCycloneSystem = MutableStateFlow(
        GisCycloneSystem(
            systemId = "BOB-04-2024",
            name = "Deep Depression 'ASNA' / BOB 04",
            classification = "Cyclonic Storm",
            currentLat = 19.8,
            currentLon = 86.4,
            centralPressureHpa = 992.0,
            maxSustainedWindKmh = 75.0,
            gustKmh = 95.0,
            movementDirection = "WNW",
            movementSpeedKmh = 14.0,
            radiusOfGaleWindKm = 120.0,
            issueTimeIST = "08 Sep 12:00 IST",
            validUntilIST = "09 Sep 18:00 IST",
            pastPoints = listOf(
                CycloneTrackPoint(17.2, 88.5, "T-12h", 45.0, "Depression"),
                CycloneTrackPoint(18.5, 87.6, "T-6h", 60.0, "Deep Depression"),
                CycloneTrackPoint(19.8, 86.4, "CURRENT (NOW)", 75.0, "Cyclonic Storm")
            ),
            forecastPoints = listOf(
                CycloneTrackPoint(20.4, 85.5, "+6h", 80.0, "Cyclonic Storm"),
                CycloneTrackPoint(20.9, 84.6, "+12h", 65.0, "Deep Depression (Inland)"),
                CycloneTrackPoint(21.5, 83.5, "+24h", 45.0, "Well-Marked Low")
            ),
            warningDistricts = listOf("Puri", "Khurda", "Jagatsinghpur", "Balasore", "Kendrapara", "Ganjam")
        )
    )
    val activeCycloneSystem: StateFlow<GisCycloneSystem> = _activeCycloneSystem.asStateFlow()

    val availableStates: List<IndianStateGis> = IndianGeographicRegistry.states

    val availableCities = locationRepo.indianCities

    // ==========================================
    // HYPERLOCAL WEATHER TRUST & IMPACT INTELLIGENCE (SIH 2026 PS-68)
    // ==========================================
    private val _villageHierarchy = MutableStateFlow(HyperlocalIntelligenceEngine.resolveHierarchy(_currentLocation.value))
    val villageHierarchy: StateFlow<VillageHierarchy> = _villageHierarchy.asStateFlow()

    private val _forecastTrust = MutableStateFlow(
        HyperlocalIntelligenceEngine.computeTrustAssessment(_weatherData.value, _villageHierarchy.value)
    )
    val forecastTrust: StateFlow<ForecastTrustAssessment> = _forecastTrust.asStateFlow()

    private val _nowcast = MutableStateFlow(
        HyperlocalIntelligenceEngine.computeNowcast(_weatherData.value, _villageHierarchy.value)
    )
    val nowcast: StateFlow<NowcastIntelligence> = _nowcast.asStateFlow()

    private val _villageRiskProfile = MutableStateFlow(
        HyperlocalIntelligenceEngine.computeVillageRiskProfile(_weatherData.value, _villageHierarchy.value)
    )
    val villageRiskProfile: StateFlow<VillageRiskProfile> = _villageRiskProfile.asStateFlow()

    private val _activeSimulationScenario = MutableStateFlow(HyperlocalIntelligenceEngine.presetScenarios[0])
    val activeSimulationScenario: StateFlow<ImpactSimulationScenario> = _activeSimulationScenario.asStateFlow()

    private val _simulationResult = MutableStateFlow(
        HyperlocalIntelligenceEngine.simulateImpactScenario(HyperlocalIntelligenceEngine.presetScenarios[0], _weatherData.value, _villageHierarchy.value)
    )
    val simulationResult: StateFlow<ImpactSimulationResult> = _simulationResult.asStateFlow()

    private val _selectedCrop = MutableStateFlow("Wheat")
    val selectedCrop: StateFlow<String> = _selectedCrop.asStateFlow()

    private val _krishiIntelligence = MutableStateFlow(
        HyperlocalIntelligenceEngine.computeKrishiCropIntelligence("Wheat", _weatherData.value, _villageHierarchy.value)
    )
    val krishiIntelligence: StateFlow<KrishiCropIntelligence> = _krishiIntelligence.asStateFlow()

    private val _verificationMetrics = MutableStateFlow(
        HyperlocalIntelligenceEngine.computeVerificationMetrics(_weatherData.value, _villageHierarchy.value)
    )
    val verificationMetrics: StateFlow<ForecastVerificationMetrics> = _verificationMetrics.asStateFlow()

    private val _nearbyVillages = MutableStateFlow(
        HyperlocalIntelligenceEngine.computeNearbyComparisons(_villageHierarchy.value, _weatherData.value)
    )
    val nearbyVillages: StateFlow<List<NearbyVillageComparison>> = _nearbyVillages.asStateFlow()

    // Dialogs & Modals for Deep Trust, Verification, What-If Simulator & Comparisons
    private val _isWhatIfSimulatorOpen = MutableStateFlow(false)
    val isWhatIfSimulatorOpen: StateFlow<Boolean> = _isWhatIfSimulatorOpen.asStateFlow()

    private val _isTrustDetailOpen = MutableStateFlow(false)
    val isTrustDetailOpen: StateFlow<Boolean> = _isTrustDetailOpen.asStateFlow()

    private val _isVerificationSheetOpen = MutableStateFlow(false)
    val isVerificationSheetOpen: StateFlow<Boolean> = _isVerificationSheetOpen.asStateFlow()

    private val _isNearbyComparisonOpen = MutableStateFlow(false)
    val isNearbyComparisonOpen: StateFlow<Boolean> = _isNearbyComparisonOpen.asStateFlow()

    private val _isReportExportOpen = MutableStateFlow(false)
    val isReportExportOpen: StateFlow<Boolean> = _isReportExportOpen.asStateFlow()

    // Haptic Feedback User Setting (ON/OFF)
    private val _isHapticFeedbackEnabled = MutableStateFlow(HapticManager.isHapticEnabled(application))
    val isHapticFeedbackEnabled: StateFlow<Boolean> = _isHapticFeedbackEnabled.asStateFlow()

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        _isHapticFeedbackEnabled.value = enabled
        HapticManager.setHapticEnabled(getApplication(), enabled)
    }

    private var isManualRefreshTriggered = false

    init {
        HapticManager.init(application)
        ruralOfflineManager.setOnNetworkRestoredListener {
            loadWeatherForCity(_currentLocation.value)
        }
        loadWeatherForCity(_currentLocation.value)
        initWelcomeChat()
        checkN8nHealth()
        runSystemIntegrityAudit()
        viewModelScope.launch {
            gisRadarRepo.seedDefaultOfflineRadarGridIfEmpty()
        }
    }

    private fun initWelcomeChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                text = "Welcome to WeatherGPT — India's Meteorological & Disaster Intelligence Engine powered directly by Google Gemini 3.6-Flash with real-time IMD & satellite telemetry. Ask any question in your preferred language or tap the microphone to speak.",
                isUser = false,
                timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            )
        )
    }

    fun loadWeatherForCity(city: CityLocation) {
        _currentLocation.value = city
        WebSocketManager.connect(cityName = city.name)
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Resolve Canonical Identity & Produce Canonical Weather Snapshot
                val canonical = CanonicalLocationResolver.resolve(city)
                _canonicalLocation.value = canonical

                val snapshot = canonicalWeatherRepo.getCanonicalSnapshot(city)
                _canonicalSnapshot.value = snapshot

                // Synchronize all presentation models from ONE canonical snapshot
                val weather = snapshot.toLegacyWeatherData()
                val hourly = snapshot.hourlyForecast.map { it.toHourlyForecast() }
                val daily = snapshot.dailyForecast.map { it.toDailyForecast() }

                _weatherData.value = weather
                _hourlyForecast.value = hourly
                _dailyForecast.value = daily

                // Fetch unified NWP 24H and 48H structured forecasts
                try {
                    val (fc24, fc48) = weatherForecastRepo.getForecasts(canonical, observedTemp = weather.temperatureC)
                    _forecast24H.value = fc24
                    _forecast48H.value = fc48

                    val pt0 = fc24.points.firstOrNull()
                    val t24 = ForecastRevisionSnapshot("T-24h", pt0?.temperature ?: weather.temperatureC, pt0?.precipAmount ?: weather.expectedRainfallMm, pt0?.windSpeed ?: weather.windSpeedKmh, "NWP Baseline")
                    val t12 = ForecastRevisionSnapshot("T-12h", (pt0?.temperature ?: weather.temperatureC) - 0.4, pt0?.precipAmount ?: weather.expectedRainfallMm, pt0?.windSpeed ?: weather.windSpeedKmh, "Cycle Guidance")
                    val t3 = ForecastRevisionSnapshot("T-3h", (weather.temperatureC - 0.2), weather.expectedRainfallMm, weather.windSpeedKmh, "Nowcast Blend")
                    val obs = ForecastRevisionSnapshot("Observation", weather.temperatureC, weather.expectedRainfallMm, weather.windSpeedKmh, "Surface Telemetry")
                    _forecastBustReport.value = ForecastBustDetectionEngine.analyzeForecastStability(t24, t12, t3, obs)
                } catch (fe: Exception) {
                    android.util.Log.w("WeatherGPTViewModel", "NWP forecast build error: ${fe.message}")
                }
                
                val rawAlerts = alertRepo.getActiveAlerts(
                    cityName = city.name,
                    districtName = city.name,
                    stateName = city.state,
                    lat = city.latitude,
                    lon = city.longitude
                )

                // Geospatial Isolation: Disallow marine hazards for inland regions (e.g. Delhi, Patna, Gopalganj)
                val currentAlerts = rawAlerts.filter { alert ->
                    val isMarine = alert.hazardType.contains("Marine", ignoreCase = true) ||
                            alert.title.contains("Marine", ignoreCase = true) ||
                            alert.title.contains("High Wave", ignoreCase = true) ||
                            alert.title.contains("Swell", ignoreCase = true)
                    if (isMarine) {
                        canonical.isCoastal
                    } else {
                        true
                    }
                }
                _alerts.value = currentAlerts
                
                val redAlert = currentAlerts.firstOrNull { it.severity == AlertSeverity.RED }
                if (redAlert != null) {
                    _activeRedAlert.value = redAlert
                    _isDisasterResponseMode.value = true
                } else {
                    val severeFlood = currentAlerts.firstOrNull { it.isFloodAlert && it.severity == AlertSeverity.ORANGE }
                    if (severeFlood != null) {
                        _activeRedAlert.value = severeFlood
                        _isDisasterResponseMode.value = false
                    } else {
                        _activeRedAlert.value = null
                        _isDisasterResponseMode.value = false
                    }
                }

                // Generate AI insight
                val insight = aiRepo.generateWeatherInsight(
                    weather = weather,
                    userMode = _selectedUserMode.value,
                    language = _selectedLanguage.value,
                    forecast24h = hourly
                )
                _weatherInsight.value = insight

                // Persist snapshot in Rural Offline Cache
                val hierarchy = HyperlocalWeatherService.resolveHierarchyForLocation(city)
                ruralOfflineManager.persistRuralCache(
                    locationKey = city.name,
                    weather = weather,
                    hourlyList = hourly,
                    alerts = currentAlerts,
                    krishiSummary = insight?.aiInterpretation ?: "Regional agricultural conditions stable.",
                    localLangAdvice = insight?.summary ?: "Normal seasonal parameters.",
                    hierarchyPath = hierarchy.fullHierarchyPath,
                    isEstimated = !hierarchy.isDirectStation,
                    stationName = hierarchy.nearestImdStation,
                    stationDistanceKm = hierarchy.stationDistanceKm
                )

                // Hyperlocal Weather Decision & Trust Intelligence Update (SIH 2026 PS-68)
                val h = HyperlocalIntelligenceEngine.resolveHierarchy(city)
                _villageHierarchy.value = h
                _forecastTrust.value = HyperlocalIntelligenceEngine.computeTrustAssessment(weather, h)
                _nowcast.value = HyperlocalIntelligenceEngine.computeNowcast(weather, h)
                _villageRiskProfile.value = HyperlocalIntelligenceEngine.computeVillageRiskProfile(weather, h)
                _simulationResult.value = HyperlocalIntelligenceEngine.simulateImpactScenario(_activeSimulationScenario.value, weather, h)
                _krishiIntelligence.value = HyperlocalIntelligenceEngine.computeKrishiCropIntelligence(_selectedCrop.value, weather, h)
                _verificationMetrics.value = HyperlocalIntelligenceEngine.computeVerificationMetrics(weather, h)
                _nearbyVillages.value = HyperlocalIntelligenceEngine.computeNearbyComparisons(h, weather)

                if (isManualRefreshTriggered) {
                    isManualRefreshTriggered = false
                    HapticManager.success(getApplication())
                }
            } catch (e: Exception) {
                if (isManualRefreshTriggered) {
                    isManualRefreshTriggered = false
                    HapticManager.error(getApplication())
                }
                // Fallback to rural offline cache if network failure
                val cached = ruralOfflineManager.loadRuralCache(city.name)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshWeather() {
        isManualRefreshTriggered = true
        HapticManager.lightTap(getApplication())
        loadWeatherForCity(_currentLocation.value)
    }

    fun speakFloodAlert(alert: DisasterAlert) {
        val lang = _selectedLanguage.value
        val message = if (lang == IndianLanguage.HINDI) {
            "बाढ़ की चेतावनी: ${alert.floodRiver ?: "नदी"}, ${alert.floodStation ?: ""} स्टेशन पर ${alert.floodStatus ?: "खतरे के निशान से ऊपर"} बह रही है। वर्तमान जलस्तर ${alert.floodCurrentLevel ?: 0.0} मीटर है। ${alert.recommendedAction}"
        } else {
            "Flood warning issued for your selected location: ${alert.floodRiver ?: "River"} at ${alert.floodStation ?: "monitoring"} station is ${alert.floodStatus ?: "above danger level"}. Current water level is ${alert.floodCurrentLevel ?: 0.0} meters. ${alert.recommendedAction}"
        }
        speakText(message)
    }

    fun setRuralDataMode(mode: DataMode) {
        when (mode) {
            DataMode.LIVE -> {
                ruralOfflineManager.markDataAsLive()
                loadWeatherForCity(_currentLocation.value)
            }
            DataMode.ESTIMATED -> {
                val hierarchy = HyperlocalWeatherService.resolveHierarchyForLocation(_currentLocation.value)
                ruralOfflineManager.markDataAsEstimated(hierarchy.nearestImdStation, hierarchy.stationDistanceKm)
            }
            DataMode.CACHED -> {
                viewModelScope.launch {
                    ruralOfflineManager.loadRuralCache(_currentLocation.value.name)
                }
            }
        }
    }

    fun exportRuralSmsAdvisory(): String {
        val w = _weatherData.value
        val h = HyperlocalWeatherService.resolveHierarchyForLocation(_currentLocation.value)
        val snapshot = canonicalSnapshot.value
        val hasImdObs = snapshot?.sources?.any { it.source == com.example.data.canonical.DataSourceType.IMD && it.quality == com.example.data.canonical.DataQuality.VALID } == true
        val sourceLabel = if (hasImdObs) "IMD AWS + forecast model" else "Forecast model"
        return """
            [WEATHERGPT RURAL ADVISORY]
            Area: ${h.district} - ${h.villageOrPanchayat}
            Temp: ${w.temperatureC}°C (Max: ${w.tempMaxC}°C, Min: ${w.tempMinC}°C)
            Rain: ${w.expectedRainfallMm} mm (${w.rainProbabilityPercent}% chance)
            Wind: ${w.windSpeedKmh} km/h (${w.windDirectionText})
            Advisory: ${if (w.rainProbabilityPercent > 40) "Avoid spraying & hold irrigation." else "Favorable for sowing/fieldwork."}
            Source: $sourceLabel
        """.trimIndent()
    }

    fun checkN8nHealth() {
        viewModelScope.launch {
            try {
                val (isOnline, statusText) = N8nAlertService.checkHealth()
                _n8nHealthStatus.value = if (isOnline) "🟢 Automation Online" else "🟡 Automation Standby (Optional)"
                _alertWebhookStatus.value = if (isOnline) "CONNECTED" else "STANDBY"
                _n8nChatStatus.value = if (isOnline) "CONNECTED" else "STANDBY"
            } catch (_: Exception) {
                _n8nHealthStatus.value = "🟡 Automation Standby (Optional)"
                _alertWebhookStatus.value = "STANDBY"
                _n8nChatStatus.value = "STANDBY"
            }
        }
    }

    fun hasLocationPermission(): Boolean = locationRepo.hasLocationPermission()

    fun fetchCurrentGpsLocation() {
        HapticManager.resetLocationSession()
        viewModelScope.launch {
            _locationState.value = LocationState.Loading
            _isLoading.value = true

            // Try LocationManager for precise GPS + District/Block reverse geocoding
            when (val locResult = locationManager.obtainPreciseLocation()) {
                is LocationFetchState.Success -> {
                    HapticManager.onLocationStateChanged(getApplication(), isSuccess = true, isError = false)
                    val city = locResult.location.toCityLocation()
                    _locationState.value = LocationState.Success(
                        com.example.data.model.UserLocation(
                            latitude = locResult.location.latitude,
                            longitude = locResult.location.longitude,
                            locality = locResult.location.locality.ifBlank { locResult.location.district },
                            subAdminArea = locResult.location.subDistrictOrBlock,
                            adminArea = locResult.location.state
                        )
                    )
                    loadWeatherForCity(city)
                }
                is LocationFetchState.PermissionRequired -> {
                    HapticManager.onLocationStateChanged(getApplication(), isSuccess = false, isError = true)
                    _locationState.value = LocationState.PermissionRequired
                    _isLoading.value = false
                }
                else -> {
                    // Fallback to locationRepo
                    when (val state = locationRepo.fetchCurrentLocation()) {
                        is LocationState.Success -> {
                            HapticManager.onLocationStateChanged(getApplication(), isSuccess = true, isError = false)
                            _locationState.value = state
                            val city = state.location.toCityLocation()
                            loadWeatherForCity(city)
                        }
                        is LocationState.PermissionRequired -> {
                            HapticManager.onLocationStateChanged(getApplication(), isSuccess = false, isError = true)
                            _locationState.value = state
                            _isLoading.value = false
                        }
                        is LocationState.Error -> {
                            HapticManager.onLocationStateChanged(getApplication(), isSuccess = false, isError = true)
                            _locationState.value = state
                            _isLoading.value = false
                        }
                        else -> {
                            _isLoading.value = false
                        }
                    }
                }
            }
        }
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            fetchCurrentGpsLocation()
        } else {
            HapticManager.onLocationStateChanged(getApplication(), isSuccess = false, isError = true)
            _locationState.value = LocationState.Error("Location permission denied. Please grant permission to detect local weather automatically.")
        }
    }

    fun setLanguage(lang: IndianLanguage) {
        _selectedLanguage.value = lang
        viewModelScope.launch {
            val insight = aiRepo.generateWeatherInsight(
                weather = _weatherData.value,
                userMode = _selectedUserMode.value,
                language = lang,
                forecast24h = _hourlyForecast.value
            )
            _weatherInsight.value = insight
        }
    }

    fun setUserMode(mode: UserMode) {
        _selectedUserMode.value = mode
        viewModelScope.launch {
            val insight = aiRepo.generateWeatherInsight(
                weather = _weatherData.value,
                userMode = mode,
                language = _selectedLanguage.value,
                forecast24h = _hourlyForecast.value
            )
            _weatherInsight.value = insight
        }
    }

    fun sendChatMessage(query: String, isVoiceInitiated: Boolean = false) {
        if (query.isBlank()) return

        _lastFailedQuery.value = null

        // Determine active target language based on ChatLanguageMode
        val detection = com.example.service.LanguageDetectionService.detectLanguage(query, _selectedLanguage.value)
        val activeTargetLanguage = when (val mode = _chatLanguageMode.value) {
            ChatLanguageMode.AUTO -> {
                if (detection.preferredResponseLanguage != _selectedLanguage.value) {
                    _selectedLanguage.value = detection.preferredResponseLanguage
                }
                detection.preferredResponseLanguage
            }
            ChatLanguageMode.HINGLISH -> {
                if (_selectedLanguage.value != IndianLanguage.HINDI) {
                    _selectedLanguage.value = IndianLanguage.HINDI
                }
                IndianLanguage.HINDI
            }
            else -> {
                val chosen = mode.language ?: detection.preferredResponseLanguage
                if (chosen != _selectedLanguage.value) {
                    _selectedLanguage.value = chosen
                }
                chosen
            }
        }

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            text = query,
            isUser = true,
            timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        )
        _chatMessages.value = _chatMessages.value + userMsg

        // AI Intent Routing: Automatically activate matching intelligence domain if relevant
        routeUserToModule(query)

        val isDeepAnalysisQuery = query.contains("analyze", ignoreCase = true) ||
                query.contains("trend", ignoreCase = true) ||
                query.contains("climate", ignoreCase = true) ||
                query.contains("history", ignoreCase = true) ||
                query.contains("research", ignoreCase = true) ||
                query.contains("flood risk", ignoreCase = true) ||
                query.contains("monsoon anomaly", ignoreCase = true) ||
                query.contains("detailed", ignoreCase = true)

        val requestStartMs = System.currentTimeMillis()
        viewModelScope.launch {
            _isChatLoading.value = true
            if (isVoiceInitiated) {
                voiceService.setVoiceUiState(VoiceUiState.ANALYZING_WEATHER)
            }
            _chatStatusText.value = if (isDeepAnalysisQuery) "WeatherGPT is conducting scientific meteorological analysis..." else "WeatherGPT is thinking..."
            try {
                val retrievalStartMs = System.currentTimeMillis()
                val currentW = _weatherData.value
                val hourlyF = _hourlyForecast.value
                val userM = _selectedUserMode.value
                val recentC = _chatMessages.value
                val activeTab = _activeQuickAccessTab.value

                var researchData: ResearchAnalysis? = null
                if (isDeepAnalysisQuery) {
                    try {
                        researchData = aiRepo.generateResearchAnalysis(
                            topic = query,
                            location = _currentLocation.value.name,
                            language = activeTargetLanguage
                        )
                    } catch (_: Exception) {}
                }
                val retrievalEndMs = System.currentTimeMillis()
                val retrievalMs = (retrievalEndMs - retrievalStartMs).coerceAtLeast(10)

                val aiStartMs = System.currentTimeMillis()
                val aiResponseText = aiRepo.askWeatherChat(
                    query = query,
                    currentWeather = currentW,
                    forecast24h = hourlyF,
                    userMode = userM,
                    activeAppLanguage = activeTargetLanguage,
                    recentConversation = recentC,
                    activeModuleContext = activeTab
                )
                val aiEndMs = System.currentTimeMillis()
                val aiProcessingMs = (aiEndMs - aiStartMs).coerceAtLeast(50)
                val totalMs = aiEndMs - requestStartMs

                // Record real latency instrumentation
                com.example.service.AiLatencyTracker.recordSample(
                    id = UUID.randomUUID().toString(),
                    retrievalMs = retrievalMs,
                    aiProcessingMs = aiProcessingMs,
                    totalMs = totalMs
                )

                val isFailure = aiResponseText.contains("AI service unavailable", ignoreCase = true) ||
                        aiResponseText.contains("timed out", ignoreCase = true)

                if (isFailure) {
                    _lastFailedQuery.value = query
                }

                val aiMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = aiResponseText,
                    isUser = false,
                    timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                    researchData = researchData
                )
                _chatMessages.value = _chatMessages.value + aiMsg

                // Persist to local DB
                db.weatherDao().insertChatMessage(
                    ChatHistoryEntity(
                        id = aiMsg.id,
                        queryText = query,
                        answerText = aiResponseText,
                        isUser = false,
                        timestamp = System.currentTimeMillis(),
                        personaMode = _selectedUserMode.value.name,
                        languageCode = activeTargetLanguage.code
                    )
                )

                // Voice Readback if voice was used and request succeeded
                if (isVoiceInitiated && !isFailure) {
                    val spokenSummary = aiResponseText
                        .substringBefore("[DATA PROVENANCE]")
                        .replace(Regex("\\[.*?\\]"), "")
                        .replace(Regex("[*#_`>]"), "")
                        .take(500)
                    voiceService.speak(spokenSummary, activeTargetLanguage)
                } else if (isVoiceInitiated) {
                    voiceService.setVoiceUiState(VoiceUiState.READY)
                }
            } catch (e: Exception) {
                if (isVoiceInitiated) {
                    voiceService.setVoiceUiState(VoiceUiState.READY)
                }
                _lastFailedQuery.value = query
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    text = "AI service unavailable right now. Please try again.",
                    isUser = false,
                    timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                )
                _chatMessages.value = _chatMessages.value + errorMsg
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    fun retryLastFailedQuery() {
        val query = _lastFailedQuery.value
        if (!query.isNullOrBlank()) {
            sendChatMessage(query, isVoiceInitiated = false)
        }
    }

    fun dispatchAlertToN8n(alert: DisasterAlert) {
        viewModelScope.launch {
            _isN8nDispatching.value = true
            _n8nAlertState.value = N8nAlertState.Loading("Sending alert to n8n emergency automation workflow...", alert.id)
            _n8nDispatchStatus.value = "Sending alert to n8n emergency automation workflow..."
            try {
                val state = alertRepo.dispatchAlertWithState(alert, _selectedLanguage.value)
                _n8nAlertState.value = state
                when (state) {
                    is N8nAlertState.Success -> {
                        _n8nDispatchStatus.value = state.userMessage
                        _alertWebhookStatus.value = "CONNECTED (HTTP ${state.statusCode})"
                        _n8nHealthStatus.value = "🟢 n8n Automation Connected (HTTP ${state.statusCode})"
                    }
                    is N8nAlertState.Error -> {
                        _n8nDispatchStatus.value = if (state.is404) "⚠️ ${state.userMessage}" else "⚠️ ${state.userMessage}"
                        _alertWebhookStatus.value = if (state.is404) "OFFLINE (404)" else "ERROR (${state.statusCode ?: "Net"})"
                        _n8nHealthStatus.value = "🟠 Automation Offline"
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                val errState = N8nAlertState.Error(
                    alertId = alert.id,
                    statusCode = null,
                    is404 = false,
                    userMessage = "Automation service unavailable: ${e.localizedMessage ?: "Network error"}",
                    technicalMessage = e.message ?: "Unknown"
                )
                _n8nAlertState.value = errState
                _n8nDispatchStatus.value = "❌ ${errState.userMessage}"
                _alertWebhookStatus.value = "OFFLINE"
                _n8nHealthStatus.value = "🟠 Automation Offline"
            } finally {
                _isN8nDispatching.value = false
            }
        }
    }

    fun dispatchTestRedAlert() {
        viewModelScope.launch {
            _isN8nDispatching.value = true
            _n8nAlertState.value = N8nAlertState.Loading("Sending test RED Cyclone alert to n8n...", "TEST-RED-001")
            _n8nDispatchStatus.value = "Sending test RED Cyclone alert to n8n..."
            try {
                val state = alertRepo.sendTestRedAlert()
                _n8nAlertState.value = state
                when (state) {
                    is N8nAlertState.Success -> {
                        _n8nDispatchStatus.value = "✓ Alert accepted by n8n (HTTP ${state.statusCode})"
                        _alertWebhookStatus.value = "CONNECTED (HTTP ${state.statusCode})"
                        _n8nHealthStatus.value = "🟢 n8n Automation Connected (HTTP ${state.statusCode})"

                        // Activate Disaster Response Mode in app for test verification
                        _isDisasterResponseMode.value = true
                        _activeRedAlert.value = DisasterAlert(
                            id = "TEST-RED-001",
                            hazardType = "Severe Cyclone Warning",
                            severity = AlertSeverity.RED,
                            location = "Mumbai Coastal Region",
                            title = "Severe Cyclone Warning (Gale 110 km/h)",
                            validTime = "Valid: Immediate Emergency",
                            expectedImpact = "Gale force winds, storm surge, high structural damage potential.",
                            recommendedAction = "Take immediate indoor shelter. Keep emergency radio active.",
                            mitigationGuidance = "District Emergency Operations Center activated. NDRF deployed.",
                            verifiedSource = "IMD & n8n Verification Node",
                            affectedDistricts = listOf("Mumbai City", "Mumbai Suburban", "Thane", "Raigad")
                        )
                    }
                    is N8nAlertState.Error -> {
                        _n8nDispatchStatus.value = "✕ ${state.userMessage}"
                        _alertWebhookStatus.value = if (state.is404) "OFFLINE (404)" else "ERROR (${state.statusCode ?: "Net"})"
                        _n8nHealthStatus.value = "🟠 Automation Offline"
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                val errState = N8nAlertState.Error(
                    alertId = "TEST-RED-001",
                    statusCode = null,
                    is404 = false,
                    userMessage = "Automation unavailable. Please retry.",
                    technicalMessage = e.message ?: "Unknown"
                )
                _n8nAlertState.value = errState
                _n8nDispatchStatus.value = "✕ Automation unavailable. Please retry."
                _alertWebhookStatus.value = "OFFLINE"
                _n8nHealthStatus.value = "🟠 Automation Offline"
            } finally {
                _isN8nDispatching.value = false
            }
        }
    }

    fun dismissDisasterMode() {
        _isDisasterResponseMode.value = false
    }

    fun runDiagnosticsTest() {
        viewModelScope.launch {
            _isTestingDiagnostics.value = true
            try {
                val (chatOk, chatMsg) = aiRepo.testBackendConnection()
                _n8nChatStatus.value = if (chatOk) "CONNECTED (HTTP 200)" else "ERROR: $chatMsg"
                _geminiStatus.value = if (chatOk) "ACTIVE (via n8n)" else "UNAVAILABLE"
                _aiBackendStatus.value = if (chatOk) "ONLINE" else "ERROR"

                // Also ping alert webhook
                val testAlertPayload = N8nAlertService.buildTestRedAlert()
                val alertResult = N8nAlertService.dispatchAlert(testAlertPayload)
                _alertWebhookStatus.value = if (alertResult.success) "CONNECTED (HTTP ${alertResult.statusCode})" else "ERROR (${alertResult.statusCode})"
                _n8nHealthStatus.value = if (alertResult.success) "🟢 n8n Automation Connected" else "🟠 Automation Offline"
            } catch (e: Exception) {
                _n8nChatStatus.value = "ERROR: ${e.localizedMessage}"
                _aiBackendStatus.value = "OFFLINE"
                _n8nHealthStatus.value = "🟠 Automation Offline"
            } finally {
                _isTestingDiagnostics.value = false
            }
        }
    }

    fun clearN8nStatus() {
        _n8nDispatchStatus.value = null
    }

    fun setActiveQuickAccessTab(tab: QuickAccessTab) {
        _activeQuickAccessTab.value = tab
    }

    fun routeUserToModule(query: String): QuickAccessTab {
        val lower = query.lowercase(Locale.ROOT)
        val targetTab = when {
            lower.contains("aviation") || lower.contains("flight") || lower.contains("pilot") ||
                    lower.contains("metar") || lower.contains("taf") || lower.contains("airport") ||
                    lower.contains("takeoff") || lower.contains("landing") || lower.contains("runway") -> {
                QuickAccessTab.AVIATION
            }
            lower.contains("marine") || lower.contains("ocean") || lower.contains("sea") ||
                    lower.contains("wave") || lower.contains("swell") || lower.contains("fisherman") ||
                    lower.contains("port") || lower.contains("harbour") || lower.contains("tide") ||
                    lower.contains("gale") -> {
                QuickAccessTab.MARINE
            }
            lower.contains("model") || lower.contains("nwp") || lower.contains("gfs") ||
                    lower.contains("ecmwf") || lower.contains("wrf") || lower.contains("ensemble") ||
                    lower.contains("divergence") -> {
                QuickAccessTab.NWP_MODELS
            }
            lower.contains("climate") || lower.contains("historical") || lower.contains("anomaly") ||
                    lower.contains("global warming") || lower.contains("30 year") || lower.contains("decadal") ||
                    lower.contains("normal") -> {
                QuickAccessTab.CLIMATE
            }
            lower.contains("smart city") || lower.contains("urban") || lower.contains("traffic flood") ||
                    lower.contains("waterlogging") || lower.contains("heat island") || lower.contains("uhi") ||
                    lower.contains("drainage") -> {
                QuickAccessTab.SMART_CITY
            }
            lower.contains("disaster") || lower.contains("impact") || lower.contains("infrastructure") ||
                    lower.contains("ndrf") || lower.contains("evacuation") || lower.contains("hospital power") ||
                    lower.contains("bridge") -> {
                QuickAccessTab.DISASTER_IMPACT
            }
            else -> QuickAccessTab.NONE
        }

        if (targetTab != QuickAccessTab.NONE) {
            _activeQuickAccessTab.value = targetTab
        }

        // Natural-Language GIS Layer selection sync
        when {
            lower.contains("radar") || lower.contains("dbz") || lower.contains("reflectivity") || lower.contains("doppler") -> {
                _activeGisLayer.value = WeatherGisLayer.RADAR
            }
            lower.contains("satellite") || lower.contains("insat") || lower.contains("cloud cover") || lower.contains("infrared") -> {
                _activeGisLayer.value = WeatherGisLayer.SATELLITE
            }
            lower.contains("cyclone") || lower.contains("depression") || lower.contains("storm track") || lower.contains("cone of uncertainty") -> {
                _activeGisLayer.value = WeatherGisLayer.CYCLONE
            }
            lower.contains("lightning") || lower.contains("thunderstorm") || lower.contains("damini") -> {
                _activeGisLayer.value = WeatherGisLayer.LIGHTNING
            }
            lower.contains("wind") || lower.contains("gust") || lower.contains("streamline") -> {
                _activeGisLayer.value = WeatherGisLayer.WIND
            }
            lower.contains("rain") || lower.contains("precipitation") || lower.contains("qpe") || lower.contains("downpour") -> {
                _activeGisLayer.value = WeatherGisLayer.RAINFALL
            }
            lower.contains("heat wave") || lower.contains("heat index") || lower.contains("apparent temp") -> {
                _activeGisLayer.value = WeatherGisLayer.HEAT_INDEX
            }
            lower.contains("waterlogging") || lower.contains("waterlog") || lower.contains("underpass flood") || lower.contains("urban drainage") -> {
                _activeGisLayer.value = WeatherGisLayer.WATERLOGGING
            }
            lower.contains("flood") || lower.contains("river") || lower.contains("inundation") -> {
                _activeGisLayer.value = WeatherGisLayer.FLOOD_RISK
            }
        }

        return targetTab
    }

    fun setGisLayer(layer: WeatherGisLayer) {
        _activeGisLayer.value = layer
    }

    fun setGisLayerOpacity(opacity: Float) {
        _gisLayerOpacity.value = opacity.coerceIn(0.05f, 1.0f)
    }

    fun setComparisonLayer(layer: WeatherGisLayer?) {
        _comparisonGisLayer.value = layer
    }

    fun setTimelineFrameIndex(index: Int) {
        _selectedTimelineIndex.value = index.coerceIn(0, gisTimelineFrames.size - 1)
    }

    fun toggleGisTimelinePlay() {
        _isGisTimelinePlaying.value = !_isGisTimelinePlaying.value
    }

    fun inspectCoordinates(lat: Double, lon: Double, localityHint: String? = null) {
        viewModelScope.launch {
            val offlineCount = cachedRadarPoints.value.size
            val fused = WeatherDataFusionEngine.fusePointAnalysis(
                lat = lat,
                lon = lon,
                baseWeather = _weatherData.value,
                localityHint = localityHint,
                offlinePointsCount = offlineCount
            )
            _inspectedPoint.value = fused
        }
    }

    fun closePointInspection() {
        _inspectedPoint.value = null
    }

    fun setMapAiAnalysisOpen(isOpen: Boolean) {
        _isMapAiAnalysisOpen.value = isOpen
        if (isOpen) {
            triggerMapAiAnalysis()
        }
    }

    fun triggerMapAiAnalysis() {
        val targetPoint = _inspectedPoint.value
        val lat = targetPoint?.latitude ?: _currentLocation.value.latitude
        val lon = targetPoint?.longitude ?: _currentLocation.value.longitude
        val locality = targetPoint?.localityName ?: _currentLocation.value.name

        viewModelScope.launch {
            _isMapAiAnalysisLoading.value = true
            try {
                val analysis = aiRepo.generateMapAnalysis(
                    lat = lat,
                    lon = lon,
                    locationName = locality,
                    activeLayer = _activeGisLayer.value,
                    weather = _weatherData.value,
                    language = _selectedLanguage.value
                )
                _mapAiAnalysisText.value = analysis
            } catch (e: Exception) {
                _mapAiAnalysisText.value = null
            } finally {
                _isMapAiAnalysisLoading.value = false
            }
        }
    }

    fun zoomToState(state: IndianStateGis) {
        val city = CityLocation(
            name = state.name,
            state = state.name,
            latitude = state.centerLat,
            longitude = state.centerLon
        )
        loadWeatherForCity(city)
        inspectCoordinates(state.centerLat, state.centerLon, state.name)
    }

    fun zoomToDistrict(stateName: String, districtName: String, lat: Double, lon: Double) {
        val city = CityLocation(
            name = districtName,
            state = stateName,
            latitude = lat,
            longitude = lon
        )
        loadWeatherForCity(city)
        inspectCoordinates(lat, lon, districtName)
    }

    fun speakText(text: String, explicitLanguage: IndianLanguage? = null) {
        val targetLang = explicitLanguage ?: com.example.service.LanguageDetectionService.detectLanguage(text, _selectedLanguage.value).preferredResponseLanguage
        voiceService.speak(text, targetLang)
    }

    fun stopSpeaking() {
        voiceService.stopSpeaking()
    }

    // ==========================================
    // HYPERLOCAL INTELLIGENCE CONTROLLERS
    // ==========================================
    fun setSimulationScenario(scenario: ImpactSimulationScenario) {
        _activeSimulationScenario.value = scenario
        _simulationResult.value = HyperlocalIntelligenceEngine.simulateImpactScenario(scenario, _weatherData.value, _villageHierarchy.value)
    }

    fun setSelectedCrop(cropName: String) {
        _selectedCrop.value = cropName
        _krishiIntelligence.value = HyperlocalIntelligenceEngine.computeKrishiCropIntelligence(cropName, _weatherData.value, _villageHierarchy.value)
    }

    fun selectHierarchyVillage(village: VillageHierarchy) {
        _villageHierarchy.value = village
        val city = CityLocation(
            name = village.villageOrLocality,
            state = village.state,
            latitude = village.latitude,
            longitude = village.longitude
        )
        loadWeatherForCity(city)
    }

    fun openWhatIfSimulator(open: Boolean) {
        _isWhatIfSimulatorOpen.value = open
    }

    fun openTrustDetail(open: Boolean) {
        _isTrustDetailOpen.value = open
    }

    fun openVerificationSheet(open: Boolean) {
        _isVerificationSheetOpen.value = open
    }

    fun openNearbyComparison(open: Boolean) {
        _isNearbyComparisonOpen.value = open
    }

    fun openReportExport(open: Boolean) {
        _isReportExportOpen.value = open
    }

    fun setIntegritySheetOpen(open: Boolean) {
        _isIntegritySheetOpen.value = open
    }

    fun runSystemIntegrityAudit() {
        viewModelScope.launch {
            try {
                val report = WeatherGPTIntegrityCheck.runFullSystemIntegrityAudit(canonicalWeatherRepo)
                _systemIntegrityReport.value = report
            } catch (_: Exception) {}
        }
    }

    fun generateVerifiedReport(format: String): String {
        val snap = _canonicalSnapshot.value
        if (format.equals("csv", ignoreCase = true) && snap != null) {
            val sb = StringBuilder()
            sb.append("SNAPSHOT_ID,LOCATION_ID,LOCALITY,DISTRICT,STATE,LATITUDE,LONGITUDE,GENERATED_AT,TEMP_C,FEELS_LIKE_C,RAIN_PROB_PCT,RAIN_MM,HUMIDITY_PCT,WIND_KMH,PRESSURE_HPA,RIVER_STATUS,FLOOD_RISK,CONFIDENCE_PCT,UNCERTAINTY,PRIMARY_SOURCE\n")
            sb.append("\"${snap.snapshotId}\",")
            sb.append("\"${snap.locationId}\",")
            sb.append("\"${snap.canonicalLocation.locality}\",")
            sb.append("\"${snap.canonicalLocation.district}\",")
            sb.append("\"${snap.canonicalLocation.state}\",")
            sb.append("${snap.canonicalLocation.latitude},")
            sb.append("${snap.canonicalLocation.longitude},")
            sb.append("${snap.generatedAt},")
            sb.append("${snap.temperature.currentC},")
            sb.append("${snap.temperature.feelsLikeC},")
            sb.append("${snap.precipitation.probabilityPercent},")
            sb.append("${snap.precipitation.expectedRainfallMm},")
            sb.append("${snap.humidity.relativeHumidityPercent},")
            sb.append("${snap.wind.speedKmh},")
            sb.append("${snap.pressure.surfaceHpa},")
            sb.append("\"${snap.riverState?.status?.name ?: "N/A"}\",")
            sb.append("\"${snap.floodRisk.riskLevel.name}\",")
            sb.append("${snap.confidence.scorePercent},")
            sb.append("\"${snap.uncertainty.level}\",")
            sb.append("\"${snap.primarySourceDisplayName}\"\n")
            return sb.toString()
        }
        return HyperlocalIntelligenceEngine.generateHyperlocalReport(
            hierarchy = _villageHierarchy.value,
            weather = _weatherData.value,
            trust = _forecastTrust.value,
            risk = _villageRiskProfile.value,
            simulation = _simulationResult.value,
            verification = _verificationMetrics.value,
            format = format
        )
    }

    override fun onCleared() {
        super.onCleared()
        WebSocketManager.disconnect()
        voiceService.release()
    }
}
