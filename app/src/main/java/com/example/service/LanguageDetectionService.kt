package com.example.service

import com.example.data.model.IndianLanguage
import java.util.Locale

data class LanguageDetectionResult(
    val detectedLanguage: IndianLanguage,
    val script: String,
    val confidence: Float,
    val isMixedLanguage: Boolean,
    val preferredResponseLanguage: IndianLanguage,
    val isExplicitLanguageCommand: Boolean = false,
    val systemPromptGuidance: String = ""
)

/**
 * High-precision Indian Multilingual and Transliterated/Hinglish Language Detection Service.
 * Detects native scripts (Devanagari, Odia, Bengali, Tamil, Telugu, Kannada, Gujarati, Gurmukhi)
 * as well as Romanized Indic (Hinglish, Roman-Odia, Tanglish) and explicit conversational overrides.
 */
object LanguageDetectionService {

    private val HINGLISH_KEYWORDS = setOf(
        "kal", "aaj", "parso", "baarish", "barish", "hogi", "hoga", "kaisa", "kaise",
        "rahega", "mausam", "kya", "hai", "hein", "batao", "bataye", "tapman", "garmi",
        "thandi", "toofan", "hawa", "kisan", "fasal", "khet", "pani", "paani", "dhoop", "chhat",
        "sadak", "safari", "rasta", "gaadi", "jaana", "chahiye", "safe", "hai",
        "kheti", "gehun", "gehu", "dhan", "sinchai", "dawai", "chhidkaw", "nuksan", "surakshit", "bimari"
    )

    private val ASSAMESE_ROMAN_KEYWORDS = setOf(
        "boroxun", "kheti", "botor", "batori", "dhan", "saul", "kene", "thakibo",
        "aaji", "kaali", "pani", "oxom", "axom", "pothar", "banya", "dowa"
    )

    private val ODIA_ROMAN_KEYWORDS = setOf(
        "barsha", "varsa", "heba", "ki", "kemiti", "kete", "rahaba", "panipaga",
        "kuha", "kahantu", "odia", "re", "aaji", "kaali", "batasa", "banya"
    )

    private val BENGALI_ROMAN_KEYWORDS = setOf(
        "bristi", "hobe", "ki", "kemon", "thakbe", "abhawa", "aajke", "kaalke", "bolun"
    )

    private val MARATHI_ROMAN_KEYWORDS = setOf(
        "paus", "padel", "ka", "kasa", "ahe", "udya", "aaj", "sanga", "havaman"
    )

    private val TAMIL_ROMAN_KEYWORDS = setOf(
        "mazhai", "varuma", "eppadi", "irukkum", "nalaikku", "inru", "vanilai", "sollunga"
    )

    private val TELUGU_ROMAN_KEYWORDS = setOf(
        "varsham", "padutunda", "ela", "untundi", "repu", "ee", "roju", "vatavaranam", "cheppandi"
    )

    fun detectLanguage(
        userMessage: String,
        currentAppLanguage: IndianLanguage = IndianLanguage.ENGLISH
    ): LanguageDetectionResult {
        val trimmed = userMessage.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        if (trimmed.isEmpty()) {
            return LanguageDetectionResult(
                detectedLanguage = currentAppLanguage,
                script = "Latin",
                confidence = 1.0f,
                isMixedLanguage = false,
                preferredResponseLanguage = currentAppLanguage
            )
        }

        // 1. Check for explicit switch commands
        val explicitMatch = checkExplicitLanguageCommand(lower)
        if (explicitMatch != null) {
            return LanguageDetectionResult(
                detectedLanguage = explicitMatch,
                script = getScriptForLanguage(explicitMatch),
                confidence = 0.99f,
                isMixedLanguage = false,
                preferredResponseLanguage = explicitMatch,
                isExplicitLanguageCommand = true,
                systemPromptGuidance = "User explicitly requested response in ${explicitMatch.displayName} (${explicitMatch.nativeName}). Formulate response entirely in ${explicitMatch.displayName}."
            )
        }

        // 2. Character Script Frequency Analysis
        var devanagariCount = 0
        var odiaCount = 0
        var bengaliCount = 0
        var tamilCount = 0
        var teluguCount = 0
        var kannadaCount = 0
        var gujaratiCount = 0
        var punjabiCount = 0
        var malayalamCount = 0
        var latinCount = 0

        for (char in trimmed) {
            val code = char.code
            when (code) {
                in 0x0900..0x097F -> devanagariCount++
                in 0x0B00..0x0B7F -> odiaCount++
                in 0x0980..0x09FF -> bengaliCount++
                in 0x0B80..0x0BFF -> tamilCount++
                in 0x0C00..0x0C7F -> teluguCount++
                in 0x0C80..0x0CF2 -> kannadaCount++
                in 0x0A80..0x0AFF -> gujaratiCount++
                in 0x0A00..0x0A7F -> punjabiCount++
                in 0x0D00..0x0D7F -> malayalamCount++
                in 0x0041..0x005A, in 0x0061..0x007A -> latinCount++
            }
        }

        // Check native scripts
        if (odiaCount > 0 && odiaCount >= (devanagariCount + bengaliCount)) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.ODIA,
                script = "Odia",
                confidence = 0.98f,
                isMixedLanguage = latinCount > 0,
                preferredResponseLanguage = IndianLanguage.ODIA,
                systemPromptGuidance = "Respond in authentic Odia (ଓଡ଼ିଆ) using standard meteorological terminology (ବର୍ଷା, ତାପମାତ୍ରା, ବାତ୍ୟା, କୃଷି ପରାମର୍ଶ)."
            )
        }

        if (devanagariCount > 0) {
            // Check if Marathi context words exist in devanagari
            val isMarathi = lower.contains("पाऊस") || lower.contains("हवामान") || lower.contains("सांगा")
            val targetLang = if (isMarathi) IndianLanguage.MARATHI else IndianLanguage.HINDI
            return LanguageDetectionResult(
                detectedLanguage = targetLang,
                script = "Devanagari",
                confidence = 0.98f,
                isMixedLanguage = latinCount > 0,
                preferredResponseLanguage = targetLang,
                systemPromptGuidance = if (targetLang == IndianLanguage.MARATHI) {
                    "Respond in Marathi (मराठी) with clear meteorological terms (पाऊस, हवामान, तापमान, धोक्याची सूचना)."
                } else {
                    "Respond in Hindi (हिन्दी) using natural Indian meteorological terminology (वर्षा, तापमान, आर्द्रता, मौसम चेतावनी, कृषि सलाह)."
                }
            )
        }

        if (bengaliCount > 0) {
            val isAssamese = trimmed.contains('ৰ') || trimmed.contains('ৱ') ||
                    lower.contains("অসম") || lower.contains("বৰষুণ") || lower.contains("খেতি") ||
                    lower.contains("বতৰ") || lower.contains("পানী") || lower.contains("ধন্যবাদ")
            val targetLang = if (isAssamese) IndianLanguage.ASSAMESE else IndianLanguage.BENGALI
            return LanguageDetectionResult(
                detectedLanguage = targetLang,
                script = if (isAssamese) "Assamese" else "Bengali",
                confidence = 0.98f,
                isMixedLanguage = latinCount > 0,
                preferredResponseLanguage = targetLang,
                systemPromptGuidance = if (isAssamese) {
                    "Respond in Assamese (অসমীয়া) with standard weather terminology (বৰষুণ, বতৰ, উষ্ণতা, কৃষি পৰামৰ্শ)."
                } else {
                    "Respond in Bengali (বাংলা) using natural terminology (বৃষ্টিপাত, আবহাওয়া, তাপমাত্রা, সতর্কতা)."
                }
            )
        }

        if (tamilCount > 0) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.TAMIL,
                script = "Tamil",
                confidence = 0.98f,
                isMixedLanguage = latinCount > 0,
                preferredResponseLanguage = IndianLanguage.TAMIL,
                systemPromptGuidance = "Respond in Tamil (தமிழ்) using accurate weather terms (மழை, வெப்பநிலை, வானிலை எச்சரிக்கை)."
            )
        }

        if (teluguCount > 0) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.TELUGU,
                script = "Telugu",
                confidence = 0.98f,
                isMixedLanguage = latinCount > 0,
                preferredResponseLanguage = IndianLanguage.TELUGU,
                systemPromptGuidance = "Respond in Telugu (తెలుగు) with standard weather terminology (వర్షం, ఉష్ణోగ్రత, హెచ్చరికలు)."
            )
        }

        if (kannadaCount > 0) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.KANNADA,
                script = "Kannada",
                confidence = 0.98f,
                isMixedLanguage = latinCount > 0,
                preferredResponseLanguage = IndianLanguage.KANNADA,
                systemPromptGuidance = "Respond in Kannada (ಕನ್ನಡ) using appropriate weather terms (ಮಳೆ, ಹವಾಮಾನ, ತಾಪಮಾನ)."
            )
        }

        if (gujaratiCount > 0) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.GUJARATI,
                script = "Gujarati",
                confidence = 0.98f,
                isMixedLanguage = latinCount > 0,
                preferredResponseLanguage = IndianLanguage.GUJARATI,
                systemPromptGuidance = "Respond in Gujarati (ગુજરાતી) with clear weather terminology (વરસાદ, તાપમાન, હવામાન ચેતવણી)."
            )
        }

        if (punjabiCount > 0) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.PUNJABI,
                script = "Gurmukhi",
                confidence = 0.98f,
                isMixedLanguage = latinCount > 0,
                preferredResponseLanguage = IndianLanguage.PUNJABI,
                systemPromptGuidance = "Respond in Punjabi (ਪੰਜਾਬੀ) with accurate terms (ਮੀਂਹ, ਤਾਪਮਾਨ, ਮੌਸਮ ਚੇਤਾਵਨੀ)."
            )
        }

        if (malayalamCount > 0) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.MALAYALAM,
                script = "Malayalam",
                confidence = 0.98f,
                isMixedLanguage = latinCount > 0,
                preferredResponseLanguage = IndianLanguage.MALAYALAM,
                systemPromptGuidance = "Respond in Malayalam (മലയാളം) with accurate terms (മഴ, താപനില, കാലാവസ്ഥ മുന്നറിയിപ്പ്)."
            )
        }

        // 3. Romanized Indic / Hinglish Detection
        val tokens = lower.split(Regex("[\\s,?.!]+")).filter { it.isNotBlank() }

        var hinglishHits = 0
        var odiaHits = 0
        var bengaliHits = 0
        var assameseHits = 0
        var marathiHits = 0
        var tamilHits = 0
        var teluguHits = 0

        for (token in tokens) {
            if (HINGLISH_KEYWORDS.contains(token)) hinglishHits++
            if (ODIA_ROMAN_KEYWORDS.contains(token)) odiaHits++
            if (BENGALI_ROMAN_KEYWORDS.contains(token)) bengaliHits++
            if (ASSAMESE_ROMAN_KEYWORDS.contains(token)) assameseHits++
            if (MARATHI_ROMAN_KEYWORDS.contains(token)) marathiHits++
            if (TAMIL_ROMAN_KEYWORDS.contains(token)) tamilHits++
            if (TELUGU_ROMAN_KEYWORDS.contains(token)) teluguHits++
        }

        if (assameseHits > 0 && assameseHits >= bengaliHits) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.ASSAMESE,
                script = "Latin (Assamese)",
                confidence = 0.85f,
                isMixedLanguage = true,
                preferredResponseLanguage = IndianLanguage.ASSAMESE,
                systemPromptGuidance = "Respond in Assamese (অসমীয়া) addressing weather and agricultural advisory."
            )
        }

        if (odiaHits > 0 && odiaHits >= hinglishHits) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.ODIA,
                script = "Latin (Roman Odia)",
                confidence = 0.85f,
                isMixedLanguage = true,
                preferredResponseLanguage = IndianLanguage.ODIA,
                systemPromptGuidance = "The user asked in Romanized Odia / Odia context. Respond in clear Odia (or friendly Latin Odia if requested) addressing weather, rainfall, and precautions."
            )
        }

        if (hinglishHits >= 2 || (hinglishHits == 1 && tokens.size <= 4)) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.HINDI,
                script = "Latin (Hinglish)",
                confidence = 0.88f,
                isMixedLanguage = true,
                preferredResponseLanguage = IndianLanguage.HINDI,
                systemPromptGuidance = "The user asked in conversational Hinglish/Hindi. Respond in warm, understandable Hindi/Hinglish with clear sections [मौसम पूर्वानुमान], [जोखिम], [सलाह], [डेटा स्रोत]."
            )
        }

        if (bengaliHits > 0) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.BENGALI,
                script = "Latin (Bengali)",
                confidence = 0.80f,
                isMixedLanguage = true,
                preferredResponseLanguage = IndianLanguage.BENGALI,
                systemPromptGuidance = "Respond in Bengali (বাংলা) addressing the user's inquiry."
            )
        }

        if (marathiHits > 0) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.MARATHI,
                script = "Latin (Marathi)",
                confidence = 0.80f,
                isMixedLanguage = true,
                preferredResponseLanguage = IndianLanguage.MARATHI,
                systemPromptGuidance = "Respond in Marathi (मराठी)."
            )
        }

        if (tamilHits > 0) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.TAMIL,
                script = "Latin (Tamil)",
                confidence = 0.80f,
                isMixedLanguage = true,
                preferredResponseLanguage = IndianLanguage.TAMIL,
                systemPromptGuidance = "Respond in Tamil (தமிழ்)."
            )
        }

        if (teluguHits > 0) {
            return LanguageDetectionResult(
                detectedLanguage = IndianLanguage.TELUGU,
                script = "Latin (Telugu)",
                confidence = 0.80f,
                isMixedLanguage = true,
                preferredResponseLanguage = IndianLanguage.TELUGU,
                systemPromptGuidance = "Respond in Telugu (తెలుగు)."
            )
        }

        // Default to English or current active app language
        return LanguageDetectionResult(
            detectedLanguage = currentAppLanguage,
            script = "Latin",
            confidence = 0.75f,
            isMixedLanguage = false,
            preferredResponseLanguage = currentAppLanguage,
            systemPromptGuidance = if (currentAppLanguage == IndianLanguage.ENGLISH) {
                "Respond in clear, professional English separating [WEATHER SUMMARY], [KEY PARAMETERS], [RISK ASSESSMENT], [MITIGATION], and [DATA SOURCE]."
            } else {
                "Respond in ${currentAppLanguage.displayName} (${currentAppLanguage.nativeName})."
            }
        )
    }

    private fun checkExplicitLanguageCommand(lower: String): IndianLanguage? {
        return when {
            lower.contains("answer in hindi") || lower.contains("hindi me") || lower.contains("hindi mein") || lower.contains("reply in hindi") -> IndianLanguage.HINDI
            lower.contains("answer in odia") || lower.contains("odia re") || lower.contains("odia me") || lower.contains("reply in odia") || lower.contains("oriya") -> IndianLanguage.ODIA
            lower.contains("answer in english") || lower.contains("in english") || lower.contains("english me") || lower.contains("reply in english") || lower.contains("english please") -> IndianLanguage.ENGLISH
            lower.contains("answer in bengali") || lower.contains("bengali te") || lower.contains("bangla") || lower.contains("reply in bengali") -> IndianLanguage.BENGALI
            lower.contains("answer in marathi") || lower.contains("marathi madhe") || lower.contains("reply in marathi") -> IndianLanguage.MARATHI
            lower.contains("answer in tamil") || lower.contains("tamilil") || lower.contains("reply in tamil") -> IndianLanguage.TAMIL
            lower.contains("answer in telugu") || lower.contains("telugulo") || lower.contains("reply in telugu") -> IndianLanguage.TELUGU
            lower.contains("answer in kannada") || lower.contains("kannadadalli") || lower.contains("reply in kannada") -> IndianLanguage.KANNADA
            lower.contains("answer in gujarati") || lower.contains("gujarati ma") || lower.contains("reply in gujarati") -> IndianLanguage.GUJARATI
            lower.contains("answer in punjabi") || lower.contains("punjabi vich") || lower.contains("reply in punjabi") -> IndianLanguage.PUNJABI
            lower.contains("answer in malayalam") || lower.contains("malayalamil") || lower.contains("reply in malayalam") -> IndianLanguage.MALAYALAM
            lower.contains("answer in assamese") || lower.contains("assamese te") || lower.contains("axomiya") || lower.contains("asamiya") || lower.contains("reply in assamese") -> IndianLanguage.ASSAMESE
            else -> null
        }
    }

    private fun getScriptForLanguage(lang: IndianLanguage): String {
        return when (lang) {
            IndianLanguage.ENGLISH -> "Latin"
            IndianLanguage.HINDI, IndianLanguage.MARATHI -> "Devanagari"
            IndianLanguage.ODIA -> "Odia"
            IndianLanguage.BENGALI -> "Bengali"
            IndianLanguage.ASSAMESE -> "Assamese"
            IndianLanguage.TAMIL -> "Tamil"
            IndianLanguage.TELUGU -> "Telugu"
            IndianLanguage.KANNADA -> "Kannada"
            IndianLanguage.GUJARATI -> "Gujarati"
            IndianLanguage.PUNJABI -> "Gurmukhi"
            IndianLanguage.MALAYALAM -> "Malayalam"
        }
    }
}
