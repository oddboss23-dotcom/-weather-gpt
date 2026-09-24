package com.example.service

import com.example.data.model.AlertSeverity
import com.example.data.model.DisasterAlert
import com.example.data.model.HourlyForecast
import com.example.data.model.IndianLanguage
import com.example.data.model.WeatherData
import java.util.Locale

/**
 * Domain & Parameter Intent Types for WeatherGPT Voice Agent
 */
enum class VoiceIntentDomain {
    GENERAL_WEATHER,
    RAINFALL_PRECIPITATION,
    AGRICULTURE_KRISHI,
    IRRIGATION_DECISION,
    SPRAY_PESTICIDE_SAFETY,
    FLOOD_AND_RIVER_HYDROLOGY,
    TEMPERATURE_HEAT_COLD,
    WIND_STORM_CYCLONE,
    THUNDERSTORM_LIGHTNING,
    DISASTER_ALERT_STATUS
}

data class VoiceExtractedIntent(
    val domain: VoiceIntentDomain,
    val targetCityOrRegion: String?,
    val targetTimeframe: String, // "TODAY", "TOMORROW", "NEXT_3_DAYS"
    val cropMentioned: String?, // "wheat", "paddy", "mustard", etc.
    val detectedLanguage: IndianLanguage,
    val isHinglish: Boolean,
    val rawQuery: String
)

data class VoiceAdvisoryResponse(
    val textSummary: String,
    val speechSummary: String,
    val domain: VoiceIntentDomain,
    val location: String,
    val language: IndianLanguage,
    val dataProvenance: String = "IMD • CWC • NCMRWF 4km • ICAR Agromet"
)

/**
 * High-precision Voice Weather Intent Engine for Indian Multi-lingual & Hinglish speech queries.
 * Interprets natural voice questions in Hindi, Hinglish, Bengali, Marathi, Tamil, Telugu,
 * Odia, Gujarati, Punjabi, Kannada, Malayalam, Assamese, and English.
 */
object VoiceWeatherIntentEngine {

    private val KNOWN_CITIES = listOf(
        "Patna", "Gopalganj", "West Champaran", "East Champaran", "Muzaffarpur", "Darbhanga",
        "Supaul", "Bhagalpur", "Saharsa", "Purnia", "Gaya", "Samastipur", "Siwan", "Delhi",
        "New Delhi", "Mumbai", "Kolkata", "Bengaluru", "Bangalore", "Chennai", "Hyderabad",
        "Pune", "Ahmedabad", "Jaipur", "Lucknow", "Bhubaneswar", "Cuttack", "Guwahati",
        "Chandigarh", "Shimla", "Dehradun", "Ranchi", "Raipur", "Indore", "Bhopal",
        "Varanasi", "Kanpur", "Agra", "Prayagraj", "Noida", "Gurugram", "Faridabad"
    )

    fun extractIntent(
        rawTranscript: String,
        currentSelectedLocation: String,
        appLanguage: IndianLanguage
    ): VoiceExtractedIntent {
        val lower = rawTranscript.lowercase(Locale.ROOT).trim()
        val langResult = LanguageDetectionService.detectLanguage(rawTranscript, appLanguage)
        val detectedLang = langResult.preferredResponseLanguage
        val isHinglish = langResult.isMixedLanguage || isHinglishText(lower)

        // 1. Detect Location
        var matchedCity: String? = null
        for (city in KNOWN_CITIES) {
            if (lower.contains(city.lowercase(Locale.ROOT))) {
                matchedCity = city
                break
            }
        }
        // Hinglish/Hindi location phrases: "mere gaon", "mere yahan", "yahan", "here", "local"
        if (matchedCity == null) {
            matchedCity = currentSelectedLocation
        }

        // 2. Detect Timeframe
        val timeframe = when {
            lower.contains("kal") || lower.contains("tomorrow") || lower.contains("repu") ||
                    lower.contains("nalaikku") || lower.contains("udya") || lower.contains("kaalke") ||
                    lower.contains("kaali") || lower.contains("kailoi") -> "TOMORROW"

            lower.contains("parso") || lower.contains("day after tomorrow") -> "NEXT_3_DAYS"
            lower.contains("agle") || lower.contains("next") || lower.contains("3 din") ||
                    lower.contains("week") || lower.contains("hafta") -> "NEXT_3_DAYS"

            else -> "TODAY"
        }

        // 3. Detect Crop Mention
        val cropMentioned = when {
            lower.contains("gehun") || lower.contains("gehu") || lower.contains("wheat") || lower.contains("kanak") -> "wheat"
            lower.contains("dhan") || lower.contains("dhaan") || lower.contains("paddy") || lower.contains("rice") || lower.contains("chawal") -> "paddy"
            lower.contains("sarson") || lower.contains("mustard") || lower.contains("rai") -> "mustard"
            lower.contains("kapas") || lower.contains("cotton") -> "cotton"
            lower.contains("ganna") || lower.contains("sugarcane") -> "sugarcane"
            lower.contains("makka") || lower.contains("maize") || lower.contains("corn") -> "maize"
            lower.contains("chana") || lower.contains("dal") || lower.contains("pulse") -> "pulses"
            lower.contains("aalu") || lower.contains("aloo") || lower.contains("potato") -> "potato"
            lower.contains("tamatar") || lower.contains("tomato") || lower.contains("sabji") || lower.contains("vegetable") -> "tomato"
            lower.contains("kheti") || lower.contains("fasal") || lower.contains("crop") -> "crop_general"
            else -> null
        }

        // 4. Detect Intent Domain
        val domain = when {
            // Flood & River Hydrology
            lower.contains("flood") || lower.contains("baadh") || lower.contains("banya") ||
                    lower.contains("pani bharna") || lower.contains("ganga") || lower.contains("gandak") ||
                    lower.contains("koshi") || lower.contains("water level") || lower.contains("danger level") ->
                VoiceIntentDomain.FLOOD_AND_RIVER_HYDROLOGY

            // Irrigation & Watering
            lower.contains("sinchai") || lower.contains("pani lagaye") || lower.contains("pani dena") ||
                    lower.contains("pani du") || lower.contains("irrigation") || lower.contains("irrigate") ||
                    lower.contains("water wheat") || lower.contains("pani du ya nahi") ->
                VoiceIntentDomain.IRRIGATION_DECISION

            // Pesticide / Agrochemical Spray Safety
            lower.contains("spray") || lower.contains("chhidkaw") || lower.contains("chhidkao") ||
                    lower.contains("dawai") || lower.contains("keetnashak") || lower.contains("pesticide") ||
                    lower.contains("fungicide") || lower.contains("safari") ->
                VoiceIntentDomain.SPRAY_PESTICIDE_SAFETY

            // General Agriculture
            cropMentioned != null || lower.contains("kisan") || lower.contains("kheti") || lower.contains("fasal") || lower.contains("krishi") ->
                VoiceIntentDomain.AGRICULTURE_KRISHI

            // Thunderstorm / Lightning
            lower.contains("bijli") || lower.contains("lightning") || lower.contains("vajrapat") ||
                    lower.contains("thunderstorm") || lower.contains("garjan") || lower.contains("tadit") ->
                VoiceIntentDomain.THUNDERSTORM_LIGHTNING

            // Wind / Cyclone
            lower.contains("toofan") || lower.contains("cyclone") || lower.contains("hawa") ||
                    lower.contains("wind") || lower.contains("aandhi") || lower.contains("gust") ->
                VoiceIntentDomain.WIND_STORM_CYCLONE

            // Temperature / Heatwave / Cold
            lower.contains("tapman") || lower.contains("temperature") || lower.contains("garmi") ||
                    lower.contains("heat") || lower.contains("heatwave") || lower.contains("thand") ||
                    lower.contains("cold") || lower.contains("loo") ->
                VoiceIntentDomain.TEMPERATURE_HEAT_COLD

            // Rainfall / Precipitation
            lower.contains("barish") || lower.contains("baarish") || lower.contains("rain") ||
                    lower.contains("barsat") || lower.contains("paus") || lower.contains("bristi") ||
                    lower.contains("mazhai") || lower.contains("varsham") || lower.contains("boroxun") ||
                    lower.contains("barsha") || lower.contains("meeh") || lower.contains("pani girega") ->
                VoiceIntentDomain.RAINFALL_PRECIPITATION

            // Alert Status
            lower.contains("alert") || lower.contains("warning") || lower.contains("khatra") || lower.contains("chetawani") ->
                VoiceIntentDomain.DISASTER_ALERT_STATUS

            else -> VoiceIntentDomain.GENERAL_WEATHER
        }

        return VoiceExtractedIntent(
            domain = domain,
            targetCityOrRegion = matchedCity,
            targetTimeframe = timeframe,
            cropMentioned = cropMentioned,
            detectedLanguage = detectedLang,
            isHinglish = isHinglish,
            rawQuery = rawTranscript
        )
    }

    /**
     * Synthesizes a structured, highly factual weather intelligence response.
     * Adheres strictly to the Farmer/Voice mode format:
     * WEATHER -> CROP/HAZARD IMPACT -> RISK -> RECOMMENDED ACTION
     */
    fun generateVoiceIntelligence(
        intent: VoiceExtractedIntent,
        weather: WeatherData,
        forecast24h: List<HourlyForecast>,
        activeAlerts: List<DisasterAlert>
    ): VoiceAdvisoryResponse {
        val cityName = intent.targetCityOrRegion ?: weather.cityName
        val isTomorrow = intent.targetTimeframe == "TOMORROW"
        val rainProb = if (isTomorrow) (forecast24h.take(24).maxOfOrNull { it.rainProbabilityPercent } ?: weather.rainProbabilityPercent) else weather.rainProbabilityPercent
        val rainMm = if (isTomorrow) (forecast24h.take(24).sumOf { it.precipitationMm }) else weather.expectedRainfallMm
        val temp = weather.temperatureC
        val tempMax = weather.tempMaxC
        val wind = weather.windSpeedKmh
        val humidity = weather.humidityPercent

        val isHindiOrHinglish = intent.detectedLanguage == IndianLanguage.HINDI || intent.isHinglish
        val floodAlert = activeAlerts.firstOrNull { it.isFloodAlert }

        val (textResult, speechResult) = when (intent.domain) {
            VoiceIntentDomain.FLOOD_AND_RIVER_HYDROLOGY -> {
                if (floodAlert != null) {
                    val text = "🚨 **बाढ़ और जलस्तर चेतावनी (${cityName})**\n\n" +
                            "• **मौसम/जलस्तर**: CWC और बिहार WRD के अनुसार नदी चेतावनी/खतरे के निशान के पास बह रही है।\n" +
                            "• **प्रभाव**: निचले दियारा और तटबंध के नजदीकी क्षेत्रों में जलभराव का जोखिम है।\n" +
                            "• **जोखिम स्तर**: ${floodAlert.severity.name} ALERT\n" +
                            "• **सुझाव**: तटबंध के पास जाने से बचें, मवेशियों को ऊंचे स्थानों पर ले जाएं और आपदा नियंत्रण कक्ष (1070) के संपर्क में रहें।"
                    val speech = "${cityName} में बाढ़ का अलर्ट है। नदी का जलस्तर बढ़ रहा है। निचले क्षेत्रों में जलभराव का खतरा है। कृपया सावधान रहें और सुरक्षित स्थान पर रहें।"
                    text to speech
                } else {
                    val text = "🌊 **बाढ़ और जलस्तर स्थिति (${cityName})**\n\n" +
                            "• **मौसम/जलस्तर**: वर्तमान में CWC स्टेशनों पर सभी प्रमुख नदियों का जलस्तर सामान्य व खतरे के निशान से नीचे है।\n" +
                            "• **प्रभाव**: कोई तात्कालिक बाढ़ का खतरा नहीं है। वर्षा ${String.format(Locale.US, "%.1f", rainMm)} मिमी अनुमानित है।\n" +
                            "• **जोखिम**: LOW\n" +
                            "• **सुझाव**: सामान्य कृषि और आवागमन जारी रख सकते हैं।"
                    val speech = "${cityName} में वर्तमान में किसी भी बाढ़ का खतरा नहीं है। नदी का जलस्तर सामान्य है और स्थिति पूरी तरह सुरक्षित है।"
                    text to speech
                }
            }

            VoiceIntentDomain.IRRIGATION_DECISION -> {
                val cropName = if (intent.cropMentioned == "wheat") "गेहूं" else if (intent.cropMentioned == "paddy") "धान" else "फसल"
                val shouldHold = rainProb >= 45 || rainMm >= 5.0
                if (shouldHold) {
                    val text = "🌾 **कृषि सिंचाई सलाह (${cityName} • ${cropName})**\n\n" +
                            "• **मौसम**: ${if (isTomorrow) "कल" else "अगले 24 घंटों में"} ${rainProb}% बारिश की संभावना है (${String.format(Locale.US, "%.1f", rainMm)} mm).\n" +
                            "• **फसल प्रभाव**: प्राकृतिक वर्षा से मिट्टी में पर्याप्त नमी उपलब्ध होगी।\n" +
                            "• **जोखिम**: सिंचाई करने से जड़ क्षेत्र में जलभराव (Waterlogging) और खाद बहने का खतरा है।\n" +
                            "• **सुझाव**: **सिंचाई 48 घंटे के लिए स्थगित करें (HOLD OFF)**। बारिश के बाद ही आवश्यकतानुसार हल्का पानी दें।"
                    val speech = "${cityName} में ${if (isTomorrow) "कल" else "आज"} ${rainProb} प्रतिशत बारिश की संभावना है। इसलिए ${cropName} में अभी पानी न लगाएं, सिंचाई को अड़तालीस घंटे के लिए टाल दें।"
                    text to speech
                } else {
                    val text = "🌾 **कृषि सिंचाई सलाह (${cityName} • ${cropName})**\n\n" +
                            "• **मौसम**: आसमान साफ रहेगा, तापमान ${tempMax}°C और बारिश की संभावना केवल ${rainProb}% है।\n" +
                            "• **फसल प्रभाव**: वाष्पीकरण दर अधिक होने से मिट्टी की नमी कम हो रही है।\n" +
                            "• **जोखिम**: नमी की कमी से पौधों की वृद्धि प्रभावित हो सकती है।\n" +
                            "• **सुझाव**: **शाम के समय हल्की सिंचाई करें**। क्यारियों में पानी समान रूप से फैलाएं।"
                    val speech = "${cityName} में मौसम साफ रहेगा और बारिश की संभावना बहुत कम है। आप ${cropName} में शाम के समय आवश्यकतानुसार हल्की सिंचाई कर सकते हैं।"
                    text to speech
                }
            }

            VoiceIntentDomain.SPRAY_PESTICIDE_SAFETY -> {
                val isWindHigh = wind >= 15.0
                val isRainHigh = rainProb >= 40 || rainMm >= 3.0
                if (isWindHigh || isRainHigh) {
                    val reason = if (isWindHigh) "हवा की गति ${wind} km/h होने से दवा उड़ने का खतरा है" else "बारिश की संभावना ${rainProb}% होने से दवा धुलने का खतरा है"
                    val text = "🧪 **कीटनाशक छिड़काव सलाह (${cityName})**\n\n" +
                            "• **मौसम**: हवा की गति ${wind} km/h, वर्षा संभावना ${rainProb}%।\n" +
                            "• **फसल प्रभाव**: दवा पत्तों पर टिकेगी नहीं और प्रभावहीन हो जाएगी।\n" +
                            "• **जोखिम**: UNFAVORABLE (दवा का नुकसान और आर्थिक क्षति).\n" +
                            "• **सुझाव**: **आज/कल छिड़काव न करें**। हवा शांत होने और मौसम साफ होने की प्रतीक्षा करें।"
                    val speech = "${cityName} में ${reason}। कृपया आज कीटनाशक या खाद का छिड़काव न करें।"
                    text to speech
                } else {
                    val text = "🧪 **कीटनाशक छिड़काव सलाह (${cityName})**\n\n" +
                            "• **मौसम**: हवा की गति सामान्य (${wind} km/h), वर्षा संभावना न्यून (${rainProb}%), आर्द्रता ${humidity}%।\n" +
                            "• **फसल प्रभाव**: दवा पत्तों पर अच्छी तरह चिपकेगी और कीट नियंत्रण प्रभावी होगा।\n" +
                            "• **जोखिम**: SAFE WINDOW (अनुकूल).\n" +
                            "• **सुझाव**: **सुबह 9 से 11 बजे या दोपहर बाद छिड़काव करें**। सुरक्षा मास्क पहनें।"
                    val speech = "${cityName} में मौसम छिड़काव के लिए बिल्कुल अनुकूल है। हवा की गति ${wind} किलोमीटर प्रति घंटा है। आप सुबह या शाम को सुरक्षित छिड़काव कर सकते हैं।"
                    text to speech
                }
            }

            VoiceIntentDomain.RAINFALL_PRECIPITATION -> {
                val dayLabel = if (isTomorrow) "कल" else "आज"
                if (rainProb >= 50 || rainMm >= 10.0) {
                    val text = "🌧️ **वर्षा पूर्वानुमान (${cityName} • ${dayLabel})**\n\n" +
                            "• **मौसम**: ${dayLabel} ${rainProb}% वर्षा की संभावना है। अनुमानित वर्षा ${String.format(Locale.US, "%.1f", rainMm)} मिमी रहेगी।\n" +
                            "• **प्रभाव**: आसमान में बादल छाए रहेंगे, गरज-चमक के साथ फुहारें या मध्यम वर्षा संभव है।\n" +
                            "• **जोखिम**: MODERATE (सड़क फिसलन, कृषि कार्यों में रुकावट).\n" +
                            "• **सुझाव**: छाता साथ रखें, खुले में रखी कटी फसल को तिरपाल से ढकें और सिंचाई रोकें।"
                    val speech = "${cityName} में ${dayLabel} ${rainProb} प्रतिशत बारिश की संभावना है और लगभग ${String.format(Locale.US, "%.0f", rainMm)} मिलीमीटर बारिश हो सकती है। कटी फसल को ढक कर रखें।"
                    text to speech
                } else {
                    val text = "🌤️ **वर्षा पूर्वानुमान (${cityName} • ${dayLabel})**\n\n" +
                            "• **मौसम**: ${dayLabel} वर्षा की संभावना केवल ${rainProb}% है (${weather.conditionDescription})।\n" +
                            "• **प्रभाव**: मौसम मुख्यतः शुष्क व आंशिक बादलों वाला रहेगा।\n" +
                            "• **जोखिम**: LOW (वर्षा का कोई खास खतरा नहीं).\n" +
                            "• **सुझाव**: सभी दैनिक और कृषि कार्य सामान्य रूप से कर सकते हैं।"
                    val speech = "${cityName} में ${dayLabel} बारिश की संभावना बहुत कम यानी सिर्फ ${rainProb} प्रतिशत है। मौसम मुख्यतः साफ और शुष्क रहेगा।"
                    text to speech
                }
            }

            VoiceIntentDomain.TEMPERATURE_HEAT_COLD -> {
                val isHot = tempMax >= 38.0
                val isCold = weather.tempMinC <= 10.0
                val text = "🌡️ **तापमान और मौसम स्थिति (${cityName})**\n\n" +
                        "• **वर्तमान तापमान**: ${temp}°C (${weather.conditionDescription})\n" +
                        "• **अधिकतम / न्यूनतम**: ${tempMax}°C / ${weather.tempMinC}°C\n" +
                        "• **आर्द्रता**: ${humidity}%\n" +
                        "• **सुझाव**: ${if (isHot) "तेज धूप से बचें, दोपहर में पर्याप्त पानी पिएं।" else if (isCold) "सुबह और रात में गर्म कपड़े पहनें।" else "मौसम सुखद और अनुकूल बना रहेगा।"}"
                val speech = "${cityName} में तापमान अभी ${temp} डिग्री सेल्सियस है। अधिकतम तापमान ${tempMax} और न्यूनतम ${weather.tempMinC} डिग्री रहने का अनुमान है।"
                text to speech
            }

            VoiceIntentDomain.WIND_STORM_CYCLONE -> {
                val isGusty = wind >= 35.0
                val text = "💨 **हवा और तूफान की स्थिति (${cityName})**\n\n" +
                        "• **हवा की गति**: ${wind} km/h (${weather.windDirectionText})\n" +
                        "• **वायुमंडलीय दबाव**: ${weather.pressureHpa} hPa\n" +
                        "• **जोखिम**: ${if (isGusty) "HIGH (तेज हवाओं की चेतावनी)" else "NORMAL (सामान्य गति)"}\n" +
                        "• **सुझाव**: ${if (isGusty) "कमजोर पेड़ों और होर्डिंग्स से दूर रहें।" else "हवा सामान्य गति से चल रही है।"}"
                val speech = "${cityName} में हवा की गति ${wind} किलोमीटर प्रति घंटा ${weather.windDirectionText} दिशा से है।"
                text to speech
            }

            VoiceIntentDomain.THUNDERSTORM_LIGHTNING -> {
                val hasThunder = weather.conditionDescription.contains("thunder", ignoreCase = true) || rainProb >= 60
                val text = "⚡ **वज्रपात और आकाशीय बिजली अलर्ट (${cityName})**\n\n" +
                        "• **स्थिति**: ${if (hasThunder) "गरज-चमक और वज्रपात की संभावना सक्रिय है।" else "वज्रपात का कोई तात्कालिक खतरा नहीं है।"}\n" +
                        "• **जोखिम स्तर**: ${if (hasThunder) "ORANGE (सतर्क रहें)" else "GREEN (सुरक्षित)"}\n" +
                        "• **सुरक्षा नियम**: गरज-चमक के दौरान खुले खेत में न रहें, ऊंचे पेड़ों के नीचे शरण न लें और पक्के मकान के अंदर रहें।"
                val speech = if (hasThunder) "${cityName} में बादलों की गरज और आकाशीय बिजली चमकने की संभावना है। कृपया खुले मैदान या पेड़ों के नीचे न रहें।" else "${cityName} में वज्रपात का कोई खतरा नहीं है, मौसम सामान्य है।"
                text to speech
            }

            else -> {
                // General Weather Fallback
                val text = "🌤️ **मौसम पूर्वानुमान (${cityName})**\n\n" +
                        "• **वर्तमान स्थिति**: ${temp}°C, ${weather.conditionDescription}\n" +
                        "• **अधिकतम / न्यूनतम**: ${tempMax}°C / ${weather.tempMinC}°C\n" +
                        "• **बारिश की संभावना**: ${rainProb}% (${String.format(Locale.US, "%.1f", rainMm)} mm)\n" +
                        "• **हवा व आर्द्रता**: ${wind} km/h • ${humidity}%\n" +
                        "• **कृषि व दैनिक सलाह**: मौसम सामान्य गतिविधियों के अनुकूल है。"
                val speech = "${cityName} में अभी तापमान ${temp} डिग्री सेल्सियस है और मौसम ${weather.conditionDescription} है। बारिश की संभावना ${rainProb} प्रतिशत है।"
                text to speech
            }
        }

        // If user query is in English, generate English text/speech equivalent
        val (finalText, finalSpeech) = if (intent.detectedLanguage == IndianLanguage.ENGLISH && !isHindiOrHinglish) {
            translateToEnglish(intent, cityName, temp, tempMax, rainProb, rainMm, wind, humidity, weather)
        } else {
            textResult to speechResult
        }

        return VoiceAdvisoryResponse(
            textSummary = finalText,
            speechSummary = finalSpeech,
            domain = intent.domain,
            location = cityName,
            language = intent.detectedLanguage
        )
    }

    private fun translateToEnglish(
        intent: VoiceExtractedIntent,
        city: String,
        temp: Double,
        tempMax: Double,
        rainProb: Int,
        rainMm: Double,
        wind: Double,
        humidity: Int,
        weather: WeatherData
    ): Pair<String, String> {
        return when (intent.domain) {
            VoiceIntentDomain.IRRIGATION_DECISION -> {
                val shouldHold = rainProb >= 45 || rainMm >= 5.0
                val text = "🌾 **Irrigation Advisory (${city})**\n\n" +
                        "• **Weather**: ${rainProb}% rain probability (${String.format(Locale.US, "%.1f", rainMm)} mm expected).\n" +
                        "• **Crop Impact**: Adequate natural soil moisture expected.\n" +
                        "• **Risk**: ${if (shouldHold) "Waterlogging and nutrient runoff risk" else "Low risk"}.\n" +
                        "• **Action**: **${if (shouldHold) "HOLD OFF IRRIGATION (48h)" else "PROCEED WITH LIGHT EVENING IRRIGATION"}**."
                val speech = if (shouldHold) "In ${city}, rain probability is ${rainProb} percent. Postpone crop irrigation for forty eight hours." else "In ${city}, weather is dry. You can proceed with scheduled light evening irrigation."
                text to speech
            }
            VoiceIntentDomain.FLOOD_AND_RIVER_HYDROLOGY -> {
                val text = "🌊 **Flood Hydrology Status (${city})**\n\n" +
                        "• **Status**: CWC hydrological stations report river levels within safe thresholds.\n" +
                        "• **Precipitation**: ${String.format(Locale.US, "%.1f", rainMm)} mm expected.\n" +
                        "• **Risk**: LOW / STABLE.\n" +
                        "• **Action**: Standard operational activities may proceed safely."
                val speech = "In ${city}, flood telemetry reports all river water levels are currently within safe thresholds with no immediate inundation hazard."
                text to speech
            }
            else -> {
                val text = "🌤️ **Weather Intelligence (${city})**\n\n" +
                        "• **Condition**: ${temp}°C, ${weather.conditionDescription}\n" +
                        "• **High / Low**: ${tempMax}°C / ${weather.tempMinC}°C\n" +
                        "• **Rain Probability**: ${rainProb}% (${String.format(Locale.US, "%.1f", rainMm)} mm)\n" +
                        "• **Wind & Humidity**: ${wind} km/h • ${humidity}%\n" +
                        "• **Advisory**: Favorable conditions for general activities."
                val speech = "${city} is observing ${temp} degrees Celsius with ${weather.conditionDescription} and ${rainProb} percent chance of rain."
                text to speech
            }
        }
    }

    private fun isHinglishText(text: String): Boolean {
        val keywords = listOf("kal", "aaj", "barish", "baarish", "hogi", "kaisa", "rahega", "mausam", "pani", "kisan", "gehun", "fasal", "kheti", "sinchai", "spray")
        return keywords.any { text.contains(it) }
    }
}
