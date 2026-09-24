package com.example.service

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.WeatherData

enum class ImpactSector(
    val titleEn: String,
    val titleHi: String,
    val icon: ImageVector,
    val domainCode: String
) {
    AGRICULTURE("Agriculture", "फसल व कृषि", Icons.Default.Agriculture, "AGRI"),
    ROADS("Roads & Highways", "सड़क व राजमार्ग", Icons.Default.DirectionsCar, "ROAD"),
    TRANSPORT("Public Transport & Rail", "रेल व सार्वजनिक परिवहन", Icons.Default.DirectionsBus, "TRANS"),
    AVIATION("Aviation Ops", "उड्डयन व विमानन", Icons.Default.Flight, "AVIA"),
    MARINE("Marine & Fisheries", "समुद्री व मत्स्य", Icons.Default.Sailing, "MARI"),
    SMART_CITY("Smart City & Urban", "स्मार्ट सिटी व जल निकासी", Icons.Default.Apartment, "URBN"),
    ENERGY("Power & Energy Grid", "विद्युत व ऊर्जा ग्रिड", Icons.Default.Bolt, "EGY"),
    SCHOOLS("Schools & Education", "विद्यालय व शिक्षा", Icons.Default.School, "SCH"),
    OUTDOOR("Outdoor & Construction", "निर्माण व बाहरी गतिविधियां", Icons.Default.Work, "OUT")
}

data class SectorImpactAssessment(
    val sector: ImpactSector,
    val weatherSignal: String,
    val riskSeverity: String, // CRITICAL, HIGH, MODERATE, LOW
    val riskDescription: String,
    val affectedAreaOrEntity: String,
    val actionableDirective: String,
    val confidenceScore: Int,
    val formattedChain: String
)

object ImpactGptEngine {

    fun evaluateSectorImpacts(weather: WeatherData): List<SectorImpactAssessment> {
        return ImpactSector.values().map { sector ->
            evaluateSingleSector(sector, weather)
        }
    }

    fun evaluateSingleSector(sector: ImpactSector, weather: WeatherData): SectorImpactAssessment {
        val rain = weather.expectedRainfallMm
        val prob = weather.rainProbabilityPercent
        val temp = weather.temperatureC
        val wind = weather.windSpeedKmh
        val visKm = weather.visibilityKm

        val isTorrential = rain > 40.0
        val isHeavyRain = rain > 20.0 || prob >= 70
        val isModerateRain = rain > 5.0 || prob >= 50
        val isExtremeHeat = temp >= 40.0
        val isHighWind = wind >= 40.0
        val isFogOrLowVis = visKm < 2.5

        return when (sector) {
            ImpactSector.AGRICULTURE -> {
                val signal = if (isHeavyRain) "Heavy rainfall ($rain mm expected, $prob% prob)" else if (isExtremeHeat) "High thermal load ($temp°C ambient)" else "Normal seasonal condition ($temp°C, $rain mm rain)"
                val risk = if (isTorrential) "CRITICAL" else if (isHeavyRain) "HIGH" else if (isExtremeHeat) "MODERATE" else "LOW"
                val riskDesc = if (isHeavyRain) "Furrow waterlogging, root asphyxiation, agrochemical runoff" else if (isExtremeHeat) "Canopy transpiration stress & soil moisture deficit" else "Nominal agricultural weather exposure"
                val area = "Standing field crops, vegetable nurseries & open grain threshed stocks"
                val action = if (isHeavyRain) "Withhold irrigation; excavate peripheral drainage furrows; postpone chemical spraying" else if (isExtremeHeat) "Schedule evening drip irrigation and apply soil mulching" else "Continue regular crop management and pest monitoring"
                val conf = if (isHeavyRain) 93 else 90
                buildAssessment(sector, signal, risk, riskDesc, area, action, conf)
            }

            ImpactSector.ROADS -> {
                val signal = if (isHeavyRain) "Persistent rain & localized road surface wetness" else if (isFogOrLowVis) "Impaired horizontal visibility (${visKm} km)" else if (isHighWind) "Crosswind gusts ($wind km/h)" else "Dry road surface ($temp°C)"
                val risk = if (isTorrential) "CRITICAL" else if (isHeavyRain || isFogOrLowVis) "HIGH" else "LOW"
                val riskDesc = if (isTorrential) "Severe underpass flooding, vehicle hydroplaning, traffic gridlock" else if (isHeavyRain) "Waterlogging at grade junctions, reduced braking traction" else if (isFogOrLowVis) "Fog collision hazard on expressways" else "Safe vehicular transit conditions"
                val area = "Arterial ring roads, railway underpasses & highway corridors"
                val action = if (isTorrential || isHeavyRain) "Avoid low-lying underpasses; reduce speed to 40 km/h; use hazard indicators in dense rain" else if (isFogOrLowVis) "Switch on fog headlamps; maintain 3x stopping distance" else "Maintain standard cruising speeds"
                val conf = if (isHeavyRain) 94 else 91
                buildAssessment(sector, signal, risk, riskDesc, area, action, conf)
            }

            ImpactSector.TRANSPORT -> {
                val signal = if (isHeavyRain) "Monsoon downpour & stormwater accumulation" else if (isHighWind) "High atmospheric gusts ($wind km/h)" else "Favorable transit conditions"
                val risk = if (isTorrential) "CRITICAL" else if (isHeavyRain) "HIGH" else "LOW"
                val riskDesc = if (isTorrential) "Suburban train track submergence & inter-city bus schedule dislocation" else if (isHeavyRain) "Subway entry seepage and bus transit delays of 20-35 mins" else "Transit schedules operational without disruption"
                val area = "Suburban rail tracks, low-lying bus depots & metro egress"
                val action = if (isHeavyRain) "Commuters plan 30 min departure buffer; transit authorities deploy track de-watering pumps" else "Transit running on published schedules"
                val conf = 92
                buildAssessment(sector, signal, risk, riskDesc, area, action, conf)
            }

            ImpactSector.AVIATION -> {
                val signal = if (isHighWind || isHeavyRain) "Convective cloud cells, wind shear ($wind km/h) & rain squalls" else if (isFogOrLowVis) "Reduced RVR (Runway Visual Range: ${visKm} km)" else "VFR visual meteorological conditions"
                val risk = if (isTorrential || (isHighWind && isHeavyRain)) "HIGH" else if (isModerateRain || isFogOrLowVis) "MODERATE" else "LOW"
                val riskDesc = if (isTorrential || isHighWind) "Severe approach turbulence, crosswind limits exceeded, holding patterns" else if (isFogOrLowVis) "CAT-II/III instrument landing procedures required" else "Clean visual approaches and stable glide paths"
                val area = "Regional airport runway approaches and terminal holding airspaces"
                val action = if (isHighWind || isHeavyRain) "Anticipate holding delays of 15–40 mins; verify METAR/TAF before descent" else "Normal flight operations"
                val conf = 95
                buildAssessment(sector, signal, risk, riskDesc, area, action, conf)
            }

            ImpactSector.MARINE -> {
                val signal = if (isHighWind) "Coastal gale wind ($wind km/h) & elevated swell height" else if (isHeavyRain) "Squally marine weather with lightning over open waters" else "Calm coastal waters with swell < 1.2m"
                val risk = if (isHighWind || isTorrential) "CRITICAL" else if (isHeavyRain) "HIGH" else "LOW"
                val riskDesc = if (isHighWind || isHeavyRain) "Dangerous sea condition; risk of craft capsizing and estuarine surge" else "Safe sea condition for mechanized and country boats"
                val area = "Coastal inshore waters, artisanal fishing zones & tidal jetties"
                val action = if (isHighWind || isHeavyRain) "Fishermen strictly advised not to venture into deep sea or coastal waters; anchor crafts securely" else "Safe for artisanal and mechanized fishing operations"
                val conf = 96
                buildAssessment(sector, signal, risk, riskDesc, area, action, conf)
            }

            ImpactSector.SMART_CITY -> {
                val signal = if (isHeavyRain) "Stormwater runoff surge with $rain mm precipitation" else if (isExtremeHeat) "Elevated urban heat island index ($temp°C)" else "Normal urban drainage baseline"
                val risk = if (isTorrential) "CRITICAL" else if (isHeavyRain) "HIGH" else if (isExtremeHeat) "MODERATE" else "LOW"
                val riskDesc = if (isTorrential) "Stormwater sewer overtopping, basement flooding, electrical substation inundation" else if (isHeavyRain) "Localized water accumulation at road intersections" else "Drainage channels operating within safe hydraulic load"
                val area = "Stormwater trunk drains, underpasses, commercial basements"
                val action = if (isHeavyRain) "Activate automated sluice gates and secondary diesel dewatering pumps; clear grate trash screens" else "Routine municipal maintenance"
                val conf = 91
                buildAssessment(sector, signal, risk, riskDesc, area, action, conf)
            }

            ImpactSector.ENERGY -> {
                val signal = if (isHighWind) "High wind gusts ($wind km/h) with thunderstorm potential" else if (isExtremeHeat) "Peak air-conditioning load under $temp°C ambient heat" else "Stable transmission line ambient conditions"
                val risk = if (isHighWind && isHeavyRain) "HIGH" else if (isExtremeHeat) "MODERATE" else "LOW"
                val riskDesc = if (isHighWind) "Tree branches falling across 11kV/33kV distribution lines; feeder tripping" else if (isExtremeHeat) "Transformer thermal overload and sub-station transformer heating" else "Distribution network operating at nominal baseline"
                val area = "Overhead distribution feeders, pole-mounted transformers, residential substations"
                val action = if (isHighWind) "Deploy emergency tree-trimming gangs and standby restoration teams; monitor feeder auto-reclosers" else "Maintain normal grid power dispatch"
                val conf = 89
                buildAssessment(sector, signal, risk, riskDesc, area, action, conf)
            }

            ImpactSector.SCHOOLS -> {
                val signal = if (isTorrential) "Extreme downpour with flooded access roads" else if (isExtremeHeat) "Severe afternoon heatwave ($temp°C)" else "Normal weather for school schedule"
                val risk = if (isTorrential) "CRITICAL" else if (isHeavyRain || isExtremeHeat) "MODERATE" else "LOW"
                val riskDesc = if (isTorrential) "Severe student transit hazard, waterlogged bus routes and campus puddling" else if (isExtremeHeat) "Dehydration, heat stroke risk during afternoon assembly/sports" else "Safe outdoor conditions for students"
                val area = "Primary and secondary school campuses, school bus transit routes"
                val action = if (isTorrential) "District administration may consider shifting to online classes or half-day closure" else if (isExtremeHeat) "Curtail outdoor sports; ensure continuous clean drinking water and ORS availability" else "Regular school operations with standard hydration"
                val conf = 92
                buildAssessment(sector, signal, risk, riskDesc, area, action, conf)
            }

            ImpactSector.OUTDOOR -> {
                val signal = if (isTorrential || isHighWind) "Severe wind squalls ($wind km/h) & torrential rain" else if (isExtremeHeat) "Scorching solar radiation ($temp°C, feels like ${weather.feelsLikeC}°C)" else "Pleasant open-air conditions"
                val risk = if (isTorrential || isHighWind) "HIGH" else if (isExtremeHeat) "HIGH" else "LOW"
                val riskDesc = if (isTorrential || isHighWind) "Scaffolding collapse, crane instability, lightning hazard in open terrain" else if (isExtremeHeat) "Occupational heat exhaustion, wet-bulb thermal cramps" else "Favorable conditions for construction and outdoor tasks"
                val area = "High-rise construction sites, open-air brick kilns, outdoor manual laborers"
                val action = if (isTorrential || isHighWind) "Halt tower crane operations and exterior scaffolding works immediately; seek indoor shelter" else if (isExtremeHeat) "Mandate mandatory rest breaks in shaded areas from 12:00 PM to 3:30 PM; ensure electrolyte hydration" else "Normal outdoor and construction workflow"
                val conf = 94
                buildAssessment(sector, signal, risk, riskDesc, area, action, conf)
            }
        }
    }

    private fun buildAssessment(
        sector: ImpactSector,
        signal: String,
        risk: String,
        riskDesc: String,
        area: String,
        action: String,
        confidence: Int
    ): SectorImpactAssessment {
        val chain = "WEATHER SIGNAL: $signal\n" +
                "→ RISK: $risk ($riskDesc)\n" +
                "→ AFFECTED SECTOR: ${sector.titleEn} (${sector.titleHi})\n" +
                "→ ACTION: $action"

        return SectorImpactAssessment(
            sector = sector,
            weatherSignal = signal,
            riskSeverity = risk,
            riskDescription = riskDesc,
            affectedAreaOrEntity = area,
            actionableDirective = action,
            confidenceScore = confidence,
            formattedChain = chain
        )
    }
}
