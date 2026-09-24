package com.example.service

import com.example.data.canonical.WeatherSnapshot
import com.example.data.model.WeatherData
import com.example.data.nwp.Forecast24H
import com.example.data.nwp.Forecast48H
import java.util.Locale
import kotlin.math.roundToInt

enum class IrrigationMethod(val labelEn: String, val labelHi: String) {
    DRIP("Drip Irrigation (Micro-irrigation)", "ड्रिप सिंचाई (सूक्ष्म सिंचाई)"),
    FLOOD_FURROW("Flood / Furrow Irrigation", "पारंपरिक / क्यारी सिंचाई"),
    SPRINKLER("Sprinkler Irrigation", "स्प्रिंकलर / फव्वारा सिंचाई"),
    RAINFED("Rainfed (No Supplemental Irrigation)", "बारानी / वर्षा आधारित")
}

enum class SoilType(val labelEn: String, val labelHi: String, val drainageRate: String) {
    ALLUVIAL_LOAM("Alluvial / Loamy Soil", "जलोढ़ / दोमट मिट्टी", "Medium"),
    CLAY_BLACK("Black / Clayey Soil (High Retention)", "काली / चिकनी मिट्टी", "Slow"),
    SANDY_LOAM("Sandy / Light Soil (Rapid Drainage)", "बलुई / रेतीली मिट्टी", "Fast"),
    RED_LATERITE("Red / Laterite Soil", "लाल / लेटराइट मिट्टी", "Medium-Fast")
}

data class CropProfile(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val category: String,
    val iconEmoji: String,
    val baseWaterNeedMm: Double,
    val optimalTempMin: Double,
    val optimalTempMax: Double,
    val criticalPests: List<String>,
    val varieties: List<String> = listOf("Standard / Regional High-Yield", "Climate-Resilient Hybrid")
)

enum class CropGrowthStage(val labelEn: String, val labelHi: String) {
    SOWING_GERMINATION("Sowing / Germination", "बुवाई / अंकुरण"),
    VEGETATIVE("Vegetative / Tillering", "वानस्पतिक / कल्ले फूटना"),
    FLOWERING("Flowering / Pollination", "फूल / परागण अवस्था"),
    GRAIN_FILLING("Grain Filling / Fruit Set", "दाना भराव / फल विकास"),
    HARVESTING("Harvesting / Maturity", "कटाई / परिपक्वता")
}

data class FarmProfile(
    val crop: CropProfile,
    val variety: String,
    val stage: CropGrowthStage,
    val locationName: String,
    val irrigationMethod: IrrigationMethod = IrrigationMethod.FLOOD_FURROW,
    val soilType: SoilType = SoilType.ALLUVIAL_LOAM
)

enum class AgrometRiskLevel(val label: String, val colorTag: String) {
    LOW("LOW", "GREEN"),
    MODERATE("MODERATE", "YELLOW"),
    HIGH("HIGH", "ORANGE_RED")
}

data class AgrometRiskItem(
    val name: String,
    val level: AgrometRiskLevel,
    val contributingFactor: String
)

data class AgrometRiskScorecard(
    val sprayRisk: AgrometRiskItem,
    val irrigationRisk: AgrometRiskItem,
    val rainRisk: AgrometRiskItem,
    val heatRisk: AgrometRiskItem,
    val diseaseWeatherRisk: AgrometRiskItem
)

data class FarmOperationWindow(
    val operation: String, // "SPRAYING", "IRRIGATION", "FERTILIZER_APPLICATION", "FIELDWORK"
    val timeWindow: String, // "08:00–11:00", etc.
    val status: String, // "FAVORABLE", "AVOID", "PROCEED WITH CAUTION"
    val whyReason: String,
    val confidence: String // "High", "Moderate", "Low"
)

data class Krishi48HPlan(
    val rainOpportunity: String,
    val sprayingOpportunity: String,
    val irrigationOpportunity: String,
    val harvestRisk: String,
    val diseaseWeatherTrend: String,
    val summary: String
)

data class KrishiDecisionResult(
    val profile: FarmProfile,
    val weatherSummary: String,
    val cropImpact: String,
    val recommendedAction: String,
    val confidencePercent: Int,
    val todaysFarmPlan: List<FarmOperationWindow>,
    val next48HPlan: Krishi48HPlan,
    val riskScorecard: AgrometRiskScorecard,
    val irrigationAdvisory: String,
    val irrigationReason: String,
    val sprayAdvisory: String,
    val sprayReason: String,
    val sowingHarvestingWindow: String,
    val sowingHarvestingReason: String,
    val heatRisk: String,
    val frostRisk: String,
    val rainRisk: String,
    val pestDiseaseRisk: String,
    val pestManagementAction: String,
    val sourceProvenance: String = "WeatherSnapshot + agrometeorological decision rules"
)

object KrishiGptEngine {

    val availableCrops: List<CropProfile> = listOf(
        CropProfile(
            "paddy", "Paddy (Rice)", "धान", "Cereal", "🌾", 18.0, 22.0, 32.0,
            listOf("Rice Blast (झुलसा)", "Stem Borer (तना छेदक)", "Brown Plant Hopper (भूरा फुदका)"),
            listOf("Pusa Basmati 1509", "Pusa 44", "Swarna Sub-1", "MTU 7029 (Swarna)", "PR 126")
        ),
        CropProfile(
            "wheat", "Wheat", "गेहूं", "Rabi Cereal", "🌾", 8.0, 15.0, 26.0,
            listOf("Yellow Rust (पीला रतुआ)", "Loose Smut (कंडुआ)", "Aphids (माहू)"),
            listOf("HD 2967", "HD 3086", "DBW 187 (Karan Vandana)", "PBW 343", "WH 1105")
        ),
        CropProfile(
            "cotton", "Cotton", "कपास", "Cash Crop", "🌱", 12.0, 21.0, 35.0,
            listOf("Pink Bollworm (गुलाबी सुंडी)", "Whitefly (सफेद मक्खी)", "Bacterial Leaf Blight"),
            listOf("Bt Cotton Hybrid (Bollgard II)", "RCH 659", "Suraj", "DCH 32")
        ),
        CropProfile(
            "mustard", "Mustard", "सरसों", "Oilseed", "🌼", 6.0, 10.0, 25.0,
            listOf("Mustard Aphid (माहू)", "White Rust (सफेद रोली)", "Downy Mildew"),
            listOf("Pusa Bold", "RH 749", "Giriraj", "Varuna (T-59)", "NRCHB 101")
        ),
        CropProfile(
            "sugarcane", "Sugarcane", "गन्ना", "Perennial", "🎋", 20.0, 20.0, 38.0,
            listOf("Early Shoot Borer", "Red Rot (लाल सड़न)", "Top Borer"),
            listOf("Co 0238", "CoLk 94184", "Co 86032", "Co 15023")
        ),
        CropProfile(
            "soybean", "Soybean", "सोयाबीन", "Legume", "🫘", 10.0, 18.0, 30.0,
            listOf("Pod Borer", "Yellow Mosaic Virus", "Rust"),
            listOf("JS 9560", "JS 2034", "NRC 37", "RVS 2001-4")
        ),
        CropProfile(
            "maize", "Maize", "मक्का", "Coarse Cereal", "🌽", 11.0, 18.0, 32.0,
            listOf("Fall Armyworm (सैनिक कीट)", "Leaf Blight", "Stem Borer"),
            listOf("Pioneer P3396", "Dekalb 9108", "HQPM 1", "DHM 117")
        ),
        CropProfile(
            "pulses", "Gram / Pulses", "चना / दालें", "Pulse", "🫘", 5.0, 12.0, 28.0,
            listOf("Pod Borer (फली छेदक)", "Wilt (उकठा रोग)", "Ascochyta Blight"),
            listOf("JG 11", "JAKI 9218", "Pusa 362", "GNG 1581")
        ),
        CropProfile(
            "tomato", "Tomato / Vegetables", "टमाटर / सब्जियां", "Horticulture", "🍅", 9.0, 16.0, 29.0,
            listOf("Early & Late Blight", "Fruit Borer", "Leaf Curl Virus"),
            listOf("Abhinav F1", "Sahu F1", "Arka Rakshak", "Pusa Ruby")
        ),
        CropProfile(
            "potato", "Potato", "आलू", "Tuber", "🥔", 7.0, 14.0, 24.0,
            listOf("Late Blight (पिछेती झुलसा)", "Early Blight", "Aphids"),
            listOf("Kufri Pukhraj", "Kufri Jyoti", "Kufri Chipsona", "Kufri Bahar")
        )
    )

    fun evaluateAgrometDecision(
        profile: FarmProfile,
        weather: WeatherData,
        forecast24H: Forecast24H? = null,
        forecast48H: Forecast48H? = null
    ): KrishiDecisionResult {
        val crop = profile.crop
        val stage = profile.stage
        val soil = profile.soilType
        val irrigation = profile.irrigationMethod

        val rainMm = weather.expectedRainfallMm
        val rainProb = weather.rainProbabilityPercent
        val temp = weather.temperatureC
        val humidity = weather.humidityPercent
        val wind = weather.windSpeedKmh

        val isRainLikely = rainMm > 8.0 || rainProb >= 50
        val isHeavyRain = rainMm > 25.0
        val isSprayFavorable = wind < 14.0 && rainProb < 30 && rainMm < 1.0
        val isExtremeHeat = temp > crop.optimalTempMax + 3.0
        val isFrostRisk = temp < 5.0 && crop.optimalTempMin > 8.0

        // ==============================================================
        // TRANSPARENT AGRICULTURAL RISK SCORING (LOW, MODERATE, HIGH)
        // ==============================================================
        val sprayRisk = when {
            wind >= 16.0 -> AgrometRiskItem("Spray Risk", AgrometRiskLevel.HIGH, "Elevated wind (${wind.roundToInt()} km/h) causes severe chemical droplet drift.")
            rainProb >= 45 || rainMm >= 3.0 -> AgrometRiskItem("Spray Risk", AgrometRiskLevel.HIGH, "Rainfall risk ($rainProb%) causes pesticide washout before plant absorption.")
            wind >= 12.0 || humidity > 85 -> AgrometRiskItem("Spray Risk", AgrometRiskLevel.MODERATE, "Moderate wind or high canopy dew requires fine spray calibration.")
            else -> AgrometRiskItem("Spray Risk", AgrometRiskLevel.LOW, "Calm wind (${wind.roundToInt()} km/h) and clear foliage allow safe spray absorption.")
        }

        val irrigationRisk = when {
            isHeavyRain -> AgrometRiskItem("Irrigation Risk", AgrometRiskLevel.HIGH, "Rainfall (${rainMm.roundToInt()} mm) will cause root asphyxiation & nutrient leaching.")
            isRainLikely -> AgrometRiskItem("Irrigation Risk", AgrometRiskLevel.MODERATE, "Projected rain ($rainProb%) reduces immediate supplemental water need.")
            soil == SoilType.CLAY_BLACK && rainProb >= 35 -> AgrometRiskItem("Irrigation Risk", AgrometRiskLevel.MODERATE, "Black clay soil retains moisture; avoid over-saturation.")
            temp >= 36.0 && rainMm < 1.0 -> AgrometRiskItem("Irrigation Risk", AgrometRiskLevel.LOW, "High evapotranspiration under heat. Moisture needed.")
            else -> AgrometRiskItem("Irrigation Risk", AgrometRiskLevel.LOW, "Normal soil moisture depletion schedule.")
        }

        val rainRiskScore = when {
            isHeavyRain -> AgrometRiskItem("Rain Risk", AgrometRiskLevel.HIGH, "Heavy precipitation ($rainMm mm) will waterlog active root zones.")
            isRainLikely -> AgrometRiskItem("Rain Risk", AgrometRiskLevel.MODERATE, "Convective moisture present ($rainProb% probability).")
            else -> AgrometRiskItem("Rain Risk", AgrometRiskLevel.LOW, "Dry to fair conditions across farm perimeter.")
        }

        val heatRiskScore = when {
            temp >= 40.0 -> AgrometRiskItem("Heat Risk", AgrometRiskLevel.HIGH, "Extreme ambient heat ($temp°C) triggers pollen sterility & leaf scorching.")
            temp >= crop.optimalTempMax -> AgrometRiskItem("Heat Risk", AgrometRiskLevel.MODERATE, "Above optimal thermal range (${crop.optimalTempMin.roundToInt()}–${crop.optimalTempMax.roundToInt()}°C).")
            else -> AgrometRiskItem("Heat Risk", AgrometRiskLevel.LOW, "Within healthy physiological range for ${crop.nameEn}.")
        }

        val diseaseRiskScore = when {
            humidity >= 85 && temp in 20.0..30.0 -> AgrometRiskItem(
                "Disease-Weather Risk",
                AgrometRiskLevel.HIGH,
                "Prolonged humidity ($humidity%) and warm temps trigger rapid fungal spore germination."
            )
            humidity >= 70 || isRainLikely -> AgrometRiskItem(
                "Disease-Weather Risk",
                AgrometRiskLevel.MODERATE,
                "Canopy dampness conducive to foliar blight and mildews."
            )
            else -> AgrometRiskItem("Disease-Weather Risk", AgrometRiskLevel.LOW, "Low relative humidity ($humidity%) keeps microbial pressure low.")
        }

        val riskScorecard = AgrometRiskScorecard(
            sprayRisk = sprayRisk,
            irrigationRisk = irrigationRisk,
            rainRisk = rainRiskScore,
            heatRisk = heatRiskScore,
            diseaseWeatherRisk = diseaseRiskScore
        )

        // ==============================================================
        // TODAY'S FARM PLAN: HOURLY OPERATIONAL WINDOWS
        // ==============================================================
        val farmPlan = mutableListOf<FarmOperationWindow>()

        // 08:00–11:00 Spray window
        if (isSprayFavorable) {
            farmPlan.add(
                FarmOperationWindow(
                    operation = "SPRAYING",
                    timeWindow = "08:00–11:00",
                    status = "FAVORABLE",
                    whyReason = "Low wind speed (${wind.roundToInt()} km/h) & dry canopy allow optimal droplet deposition.",
                    confidence = "High"
                )
            )
        } else {
            farmPlan.add(
                FarmOperationWindow(
                    operation = "SPRAYING",
                    timeWindow = "08:00–11:00",
                    status = "AVOID",
                    whyReason = if (wind >= 14.0) "Wind speed ${wind.roundToInt()} km/h exceeds 14 km/h safety threshold for drift." else "High rain probability (${rainProb}%) causes active ingredient wash-off.",
                    confidence = "High"
                )
            )
        }

        // 11:00–15:00 Afternoon window
        if (temp >= 33.0) {
            farmPlan.add(
                FarmOperationWindow(
                    operation = "SPRAYING & FIELD LABOR",
                    timeWindow = "11:00–15:00",
                    status = "AVOID",
                    whyReason = "Peak midday thermal radiation ($temp°C) causes droplet flash-evaporation & labor heat stress.",
                    confidence = "High"
                )
            )
        } else {
            farmPlan.add(
                FarmOperationWindow(
                    operation = "WEEDING & FIELDWORK",
                    timeWindow = "11:00–15:00",
                    status = "FAVORABLE",
                    whyReason = "Moderate temperature and dry soil surface support intercultural operations.",
                    confidence = "Moderate"
                )
            )
        }

        // 17:00–20:00 Evening Irrigation window
        if (isRainLikely || isHeavyRain) {
            farmPlan.add(
                FarmOperationWindow(
                    operation = "IRRIGATION",
                    timeWindow = "17:00–20:00",
                    status = "AVOID",
                    whyReason = "Incoming convective moisture ($rainProb%, $rainMm mm) will supply field capacity naturally.",
                    confidence = "High"
                )
            )
        } else if (irrigation == IrrigationMethod.RAINFED) {
            farmPlan.add(
                FarmOperationWindow(
                    operation = "SOIL MOISTURE CONSERVATION",
                    timeWindow = "17:00–20:00",
                    status = "APPLY MULCH",
                    whyReason = "Rainfed crop relies on subsoil moisture; evening mulching reduces overnight evaporation.",
                    confidence = "Moderate"
                )
            )
        } else {
            val recText = if (soil == SoilType.SANDY_LOAM) "LIGHT IRRIGATION (High Drainage)" else "NORMAL IRRIGATION CYCLE"
            farmPlan.add(
                FarmOperationWindow(
                    operation = "IRRIGATION",
                    timeWindow = "17:00–20:00",
                    status = "RECOMMENDED",
                    whyReason = "Low evening evapotranspiration maximizes water absorption for ${crop.nameEn} in ${soil.labelEn}.",
                    confidence = "High"
                )
            )
        }

        // ==============================================================
        // NEXT 48H FARM PLANNING OUTLOOK
        // ==============================================================
        val day2RainProb = forecast48H?.hours24To48?.rainProbability ?: (rainProb + 10).coerceAtMost(90)
        val day2RainMm = forecast48H?.hours24To48?.expectedRainMm ?: rainMm
        val day2MaxTemp = forecast48H?.hours24To48?.maxTemp ?: (temp + 1.0)

        val next48HPlan = Krishi48HPlan(
            rainOpportunity = if (day2RainProb >= 50) "High chance of precipitation ($day2RainProb% probability, ${String.format(Locale.US, "%.1f", day2RainMm)} mm). Natural soil recharge expected." else "Low rain probability ($day2RainProb%). Dry spell continuing across local fields.",
            sprayingOpportunity = if (day2RainProb >= 40) "Postpone foliar sprays across 48h horizon due to rain wash-off risk." else "Clear 48h spray window available. Proceed with scheduled nutrient/pesticide sprays.",
            irrigationOpportunity = if (day2RainProb >= 45 || day2RainMm >= 5.0) "Hold irrigation. Incoming rain window will meet crop moisture demand." else "Schedule supplemental irrigation if soil moisture probe drops below field capacity.",
            harvestRisk = if (stage == CropGrowthStage.HARVESTING && day2RainProb >= 40) "HIGH HARVEST RISK: Rain will damage harvested open produce. Expedite harvesting or protect grains." else "Favorable harvest window. Low moisture risk over 48 hours.",
            diseaseWeatherTrend = if (day2RainProb >= 45 && humidity > 70) "RISING: Wet canopy + high humidity increases risk of fungal blight for ${crop.nameEn}." else "STABLE: Meteorological conditions not favoring pest/disease outbreaks.",
            summary = "48-Hour Agromet Outlook for ${profile.locationName}: Plan fieldwork around the 24–48h rain probability (${day2RainProb}%)."
        )

        // General Advisories
        val irrigationAdvisory = when {
            isHeavyRain -> "HOLD OFF (No Irrigation)"
            isRainLikely -> "HOLD OFF (Postpone 48h)"
            temp > 35.0 && rainMm < 1.0 -> "EVENING WATERING RECOMMENDED"
            else -> "NORMAL IRRIGATION CYCLE"
        }

        val irrigationReason = when {
            isHeavyRain -> "$rainMm mm precipitation expected; irrigation will cause root waterlogging and nutrient leaching."
            isRainLikely -> "$rainMm mm rainfall expected ($rainProb% probability). Soil moisture sufficient."
            temp > 35.0 && rainMm < 1.0 -> "High evapotranspiration under $temp°C heat. Apply light evening irrigation."
            else -> "Soil moisture balance optimal under current ${weather.humidityPercent}% relative humidity in ${soil.labelEn}."
        }

        val sprayAdvisory = when {
            wind >= 16.0 -> "UNFAVORABLE (High Wind Drift)"
            rainProb >= 45 || rainMm >= 3.0 -> "UNFAVORABLE (Washout Risk)"
            isSprayFavorable -> "FAVORABLE (Safe Window)"
            else -> "MODERATE (Check Local Cloudiness)"
        }

        val sprayReason = when {
            wind >= 16.0 -> "Wind speed at ${wind.roundToInt()} km/h causes severe spray droplet drift outside target foliage."
            rainProb >= 45 || rainMm >= 3.0 -> "Rainfall within 24h will wash off chemical ingredients before cuticle absorption."
            else -> "Wind (${wind.roundToInt()} km/h) and canopy conditions provide safe foliar coverage."
        }

        val sowingHarvestingWindow = when (stage) {
            CropGrowthStage.SOWING_GERMINATION -> if (isHeavyRain) "DELAY SOWING" else "FAVORABLE SOWING WINDOW"
            CropGrowthStage.HARVESTING -> if (isRainLikely) "HOLD HARVEST (Rain Risk)" else "CRITICAL HARVEST WINDOW OPEN"
            else -> "FIELD MAINTENANCE ACTIVE"
        }

        val sowingHarvestingReason = when (stage) {
            CropGrowthStage.SOWING_GERMINATION -> if (isHeavyRain) "Excessive rain causes seed furrow rotting and soil crusting." else "Moisture profile supports high germination rate."
            CropGrowthStage.HARVESTING -> if (isRainLikely) "Rain on mature crop causes grain discoloration and lodging." else "Low moisture and clear sunshine favor harvesting and threshing."
            else -> "Vegetative development requires balanced nutrition and weed control."
        }

        val (pestRisk, pestAction) = evaluatePestRisk(crop, stage, temp, humidity, isRainLikely)

        val weatherSummary = if (isRainLikely) {
            "${String.format(Locale.US, "%.1f", rainMm)} mm rain expected (Prob: $rainProb%, Temp: ${temp.roundToInt()}°C, Wind: ${wind.roundToInt()} km/h)"
        } else {
            "Clear to dry, ${temp.roundToInt()}°C ambient temp, RH $humidity%, wind ${wind.roundToInt()} km/h"
        }

        val cropImpact = when (stage) {
            CropGrowthStage.SOWING_GERMINATION -> if (isHeavyRain) "Soil saturation may suffocate germinating seeds" else "Soil moisture condition ideal for emergence"
            CropGrowthStage.VEGETATIVE -> if (isRainLikely) "Sufficient moisture for tillering; dampness elevates fungal spore activity" else "Normal canopy transpiration and nutrient uptake"
            CropGrowthStage.FLOWERING -> if (isHeavyRain) "Heavy raindrops can dislodge pollen grains" else "Normal pollination conditions"
            CropGrowthStage.GRAIN_FILLING -> if (isRainLikely) "Risk of lodging in heavy rain pockets" else "Steady starch translocation into grains"
            CropGrowthStage.HARVESTING -> if (isRainLikely) "Risk of grain spoilage and fungal mold" else "Optimal conditions for safe harvesting and drying"
        }

        val recommendedAction = when {
            isHeavyRain -> "Postpone all irrigation. Ensure drainage channels are clear. Do not spray chemicals."
            isRainLikely -> "Hold irrigation. Postpone foliar pesticide sprays until weather clears."
            isExtremeHeat -> "Apply light evening irrigation to reduce canopy temperature; apply mulch to conserve moisture."
            isSprayFavorable -> "Optimal spray window open today between 08:00–11:00 AM before wind picks up."
            else -> "Maintain routine field operations and monitor pest traps."
        }

        return KrishiDecisionResult(
            profile = profile,
            weatherSummary = weatherSummary,
            cropImpact = cropImpact,
            recommendedAction = recommendedAction,
            confidencePercent = if (isRainLikely) 82 else 88,
            todaysFarmPlan = farmPlan,
            next48HPlan = next48HPlan,
            riskScorecard = riskScorecard,
            irrigationAdvisory = irrigationAdvisory,
            irrigationReason = irrigationReason,
            sprayAdvisory = sprayAdvisory,
            sprayReason = sprayReason,
            sowingHarvestingWindow = sowingHarvestingWindow,
            sowingHarvestingReason = sowingHarvestingReason,
            heatRisk = heatRiskScore.level.label,
            frostRisk = if (isFrostRisk) "HIGH" else "NONE",
            rainRisk = rainRiskScore.level.label,
            pestDiseaseRisk = pestRisk,
            pestManagementAction = pestAction
        )
    }

    private fun evaluatePestRisk(
        crop: CropProfile,
        stage: CropGrowthStage,
        temp: Double,
        humidity: Int,
        isRainLikely: Boolean
    ): Pair<String, String> {
        val targetPest = crop.criticalPests.firstOrNull() ?: "Foliar Pests"
        val isFungalFavorable = humidity >= 75 && temp in 18.0..30.0

        return if (isFungalFavorable) {
            Pair(
                "ELEVATED ($targetPest)",
                "Humid conditions favor fungal spore proliferation. Scout field edges. If threshold exceeded, apply recommended biopesticide during morning calm window."
            )
        } else if (temp > 34.0 && humidity < 50) {
            Pair(
                "MODERATE (Sucking Pests / Mites)",
                "Dry heat favors spider mites and aphids. Inspect undersides of leaves in afternoon."
            )
        } else {
            Pair(
                "LOW (Routine Monitoring)",
                "Pest pressure within economic threshold limit. Maintain predatory insect biodiversity."
            )
        }
    }
}
