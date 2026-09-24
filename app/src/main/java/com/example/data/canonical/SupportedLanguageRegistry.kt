package com.example.data.canonical

import com.example.data.model.IndianLanguage

data class LanguageCapabilityInfo(
    val language: IndianLanguage,
    val textSupported: Boolean = true,
    val translationSupported: Boolean = true,
    val sttSupported: Boolean = true,
    val ttsSupported: Boolean = true,
    val notes: String = "Fully supported across canonical inference engine"
)

object SupportedLanguageRegistry {
    private val registry = mapOf(
        IndianLanguage.ENGLISH to LanguageCapabilityInfo(IndianLanguage.ENGLISH, true, true, true, true, "Native STT/TTS & LLM grounded inference"),
        IndianLanguage.HINDI to LanguageCapabilityInfo(IndianLanguage.HINDI, true, true, true, true, "Full Devanagari & Hinglish voice support"),
        IndianLanguage.BENGALI to LanguageCapabilityInfo(IndianLanguage.BENGALI, true, true, true, true, "Bengali script & Romanized Bengali support"),
        IndianLanguage.TELUGU to LanguageCapabilityInfo(IndianLanguage.TELUGU, true, true, true, true, "Telugu script & Romanized Telugu support"),
        IndianLanguage.TAMIL to LanguageCapabilityInfo(IndianLanguage.TAMIL, true, true, true, true, "Tamil script & Romanized Tanglish support"),
        IndianLanguage.MARATHI to LanguageCapabilityInfo(IndianLanguage.MARATHI, true, true, true, true, "Marathi script & conversational query support"),
        IndianLanguage.GUJARATI to LanguageCapabilityInfo(IndianLanguage.GUJARATI, true, true, true, true, "Gujarati script support"),
        IndianLanguage.KANNADA to LanguageCapabilityInfo(IndianLanguage.KANNADA, true, true, true, true, "Kannada script support"),
        IndianLanguage.MALAYALAM to LanguageCapabilityInfo(IndianLanguage.MALAYALAM, true, true, true, true, "Malayalam script support"),
        IndianLanguage.PUNJABI to LanguageCapabilityInfo(IndianLanguage.PUNJABI, true, true, true, true, "Gurmukhi script support"),
        IndianLanguage.ODIA to LanguageCapabilityInfo(IndianLanguage.ODIA, true, true, true, true, "Odia script & Romanized Odia support"),
        IndianLanguage.ASSAMESE to LanguageCapabilityInfo(IndianLanguage.ASSAMESE, true, true, true, true, "Assamese script & Romanized support")
    )

    fun getCapability(language: IndianLanguage): LanguageCapabilityInfo {
        return registry[language] ?: LanguageCapabilityInfo(language, true, true, false, false, "Text and translation supported; voice synthesis requires regional TTS pack.")
    }

    fun getAllCapabilities(): List<LanguageCapabilityInfo> {
        return registry.values.toList()
    }
}
