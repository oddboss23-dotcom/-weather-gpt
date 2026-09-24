package com.example.service

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cyclone
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Flood
import androidx.compose.material.icons.filled.Landslide
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.model.WeatherData

enum class SmartHazardType(
    val titleEn: String,
    val titleHi: String,
    val icon: ImageVector,
    val code: String,
    val emoji: String = "⚠️"
) {
    CYCLONE("Cyclone", "चक्रवात", Icons.Default.Cyclone, "CYC", "🌀"),
    FLOOD("Flood", "नदी बाढ़", Icons.Default.Flood, "FLD", "🌊"),
    FLASH_FLOOD("Flash Flood", "अचानक बाढ़", Icons.Default.WaterDamage, "FFLD", "⚡"),
    HEAVY_RAIN("Heavy Rain", "भारी वर्षा", Icons.Default.Thunderstorm, "RAIN", "🌧️"),
    HEATWAVE("Heatwave", "लू / भीषण गर्मी", Icons.Default.WbSunny, "HEAT", "☀️"),
    LIGHTNING("Lightning (Damini)", "आकाशीय बिजली", Icons.Default.Bolt, "LGHT", "⚡"),
    THUNDERSTORM("Thunderstorm", "गरज-चमक तूफान", Icons.Default.Thunderstorm, "THND", "⛈️"),
    HIGH_WIND("High Wind / Squall", "तेज आंधी", Icons.Default.Air, "WIND", "💨"),
    LANDSLIDE("Landslide", "भूस्खलन", Icons.Default.Landslide, "LAND", "⛰️"),
    COASTAL_HAZARD("Coastal Hazard & Swell", "तटीय आपदा", Icons.Default.Sailing, "CSTL", "🌊");

    val labelEn: String get() = titleEn
}

typealias HazardType = SmartHazardType

enum class ActionRole(
    val labelEn: String,
    val labelHi: String,
    val iconEmoji: String = "👤"
) {
    GENERAL_PUBLIC("General Public", "आम नागरिक", "👥"),
    FARMER("Farmer", "किसान", "🌾"),
    FISHER("Fisher", "मछुआरे", "⛵"),
    DRIVER("Driver", "चालक / परिवहन", "🚗"),
    MUNICIPALITY("Municipality", "नगर निगम / प्रशासन", "🏛️"),
    DISASTER_RESPONSE("Disaster Response", "आपदा मोचन बल (SDRF/NDRF)", "🚨"),
    RESEARCHER("Researcher", "शोधकर्ता / विशेषज्ञ", "🔬");

    val titleEn: String get() = labelEn
}

typealias RecipientRole = ActionRole

data class SmartHazardCard(
    val hazardType: SmartHazardType,
    val role: ActionRole,
    val riskLevel: String, // RED, ORANGE, YELLOW, GREEN
    val affectedArea: String,
    val validity: String,
    val impact: String,
    val recommendedAction: String,
    val confidencePercent: Int,
    val roleDirectives: List<String>,
    val source: String,
    val timestamp: String = "Live Sync"
)

data class HazardActionCard(
    val hazardType: SmartHazardType,
    val riskLevel: String, // CRITICAL, HIGH, MODERATE, LOW
    val affectedArea: String,
    val validityWindow: String,
    val expectedImpact: String,
    val recommendedAction: String,
    val confidencePercent: Int,
    val roleDirectives: Map<ActionRole, String>,
    val sourceAgency: String
)

object SmartActionEngine {

    fun evaluateHazardAction(
        hazard: SmartHazardType,
        role: ActionRole,
        weather: WeatherData
    ): SmartHazardCard {
        val card = buildHazardCard(hazard, weather.cityName, weather, weather.expectedRainfallMm, weather.temperatureC, weather.windSpeedKmh)
        val specificDirective = card.roleDirectives[role] ?: card.recommendedAction
        val risk = when (card.riskLevel) {
            "CRITICAL", "HIGH" -> "RED"
            "MODERATE" -> "ORANGE"
            else -> "YELLOW"
        }
        return SmartHazardCard(
            hazardType = hazard,
            role = role,
            riskLevel = risk,
            affectedArea = card.affectedArea,
            validity = card.validityWindow,
            impact = card.expectedImpact,
            recommendedAction = specificDirective,
            confidencePercent = card.confidencePercent,
            roleDirectives = listOf(
                specificDirective,
                "Follow official NDMA / SDMA meteorological advisories continuously.",
                "Report local damage or inundation to 1070 / 1077 disaster emergency response."
            ),
            source = card.sourceAgency,
            timestamp = "Just Now"
        )
    }

    fun generateHazardAssessments(cityName: String, weather: WeatherData): List<HazardActionCard> {
        val rain = weather.expectedRainfallMm
        val temp = weather.temperatureC
        val wind = weather.windSpeedKmh

        return SmartHazardType.values().map { hazard ->
            buildHazardCard(hazard, cityName, weather, rain, temp, wind)
        }
    }

    private fun buildHazardCard(
        hazard: SmartHazardType,
        cityName: String,
        weather: WeatherData,
        rain: Double,
        temp: Double,
        wind: Double
    ): HazardActionCard {
        return when (hazard) {
            SmartHazardType.CYCLONE -> {
                val isCoastal = cityName in listOf("Puri", "Balasore", "Kolkata", "Chennai", "Mumbai", "Mangaluru", "Kanyakumari")
                val risk = if (isCoastal && wind > 40) "HIGH" else if (isCoastal) "MODERATE" else "LOW"
                val directives = mapOf(
                    ActionRole.GENERAL_PUBLIC to "Keep battery-operated torches and portable power banks charged; avoid moving outdoors during gale wind phases.",
                    ActionRole.FARMER to "Secure standing crop bunds; clear drainage furrows; move livestock to elevated concrete shelters.",
                    ActionRole.FISHER to "Strictly suspend all marine sorties; anchor fishing boats at inner creek harbors with double hawsers.",
                    ActionRole.DRIVER to "Suspend non-essential highway travel along coastal corridors; be watchful of fallen roadside trees.",
                    ActionRole.MUNICIPALITY to "Prune loose tree branches near power feeders; inspect suction gates at coastal outfall drains.",
                    ActionRole.DISASTER_RESPONSE to "Pre-position rescue inflatable dinghies, tree-cutters, and satellite phone links at cyclone shelter clusters.",
                    ActionRole.RESEARCHER to "Monitor DWR radial velocity vectors and scatterometer wind shear field for eye-wall boundary convergence."
                )
                HazardActionCard(
                    hazardType = hazard,
                    riskLevel = risk,
                    affectedArea = "Coastal districts, river mouths & tidal inlets of $cityName zone",
                    validityWindow = "Next 24–48 Hours",
                    expectedImpact = "Gale force winds 70–95 km/h, storm surge 0.8–1.2m, coastal erosion, minor roof damage.",
                    recommendedAction = "Activate multi-purpose cyclone shelters; secure loose rooftop structures; avoid seaside promenades.",
                    confidencePercent = 94,
                    roleDirectives = directives,
                    sourceAgency = "IMD Cyclone Warning Division & INCOIS Coastal Network"
                )
            }

            SmartHazardType.FLOOD -> {
                val risk = if (rain > 35.0) "HIGH" else if (rain > 15.0) "MODERATE" else "LOW"
                val directives = mapOf(
                    ActionRole.GENERAL_PUBLIC to "Do not walk or drive through flowing water; keep important documents in waterproof pouches.",
                    ActionRole.FARMER to "Move harvested grain bags to raised platforms; pump excess water from legume and vegetable beds.",
                    ActionRole.FISHER to "Beware of surging river currents and submerged floating debris in estuarine waters.",
                    ActionRole.DRIVER to "Never attempt crossing flooded causeways; water depth can be deceptively deep.",
                    ActionRole.MUNICIPALITY to "Deploy auxiliary de-watering pump sets at embankment low spots; verify river gauge telemetry.",
                    ActionRole.DISASTER_RESPONSE to "Prepare evacuation boats in low-lying riparian wards; establish liaison with district collectorate.",
                    ActionRole.RESEARCHER to "Analyze upstream catchment precipitation runoff using hydrological routing models."
                )
                HazardActionCard(
                    hazardType = hazard,
                    riskLevel = risk,
                    affectedArea = "Riverine floodplains, low embankment belts & catchment lowlands",
                    validityWindow = "Next 12–36 Hours",
                    expectedImpact = "Water inundation in low-lying settlements, embankment seepage, submerged rural access roads.",
                    recommendedAction = "Evacuate designated flood-prone riverbank hamlets; monitor water level gauges continuously.",
                    confidencePercent = 91,
                    roleDirectives = directives,
                    sourceAgency = "Central Water Commission (CWC) & IMD Hydro-Meteorology"
                )
            }

            SmartHazardType.FLASH_FLOOD -> {
                val risk = if (rain > 25.0) "HIGH" else "LOW"
                val directives = mapOf(
                    ActionRole.GENERAL_PUBLIC to "Immediately seek higher ground if in natural ravines or underground parking lots.",
                    ActionRole.FARMER to "Evacuate open field valleys; do not shelter near culverts or stream bridges.",
                    ActionRole.FISHER to "Avoid small stream estuaries due to sudden violent hydraulic surges.",
                    ActionRole.DRIVER to "Pull over onto elevated paved ground immediately if water rises above tire hubs.",
                    ActionRole.MUNICIPALITY to "Clear stormwater trash grates immediately to prevent culvert choking.",
                    ActionRole.DISASTER_RESPONSE to "Mobilize rapid water-rescue units to designated flash-flood choke corridors.",
                    ActionRole.RESEARCHER to "Track convective cloud tops using INSAT-3DR 10.8µm IR brightness temperature gradients."
                )
                HazardActionCard(
                    hazardType = hazard,
                    riskLevel = risk,
                    affectedArea = "Urban underpasses, dry nullahs & foothill drainage corridors",
                    validityWindow = "Next 1–6 Hours (Rapid Onset)",
                    expectedImpact = "Sudden violent water surge, basement egress submergence, rapid road washouts.",
                    recommendedAction = "Move out of ravine beds and underground floors instantly; avoid drainage canal banks.",
                    confidencePercent = 88,
                    roleDirectives = directives,
                    sourceAgency = "IMD Flash Flood Guidance System (FFGS) & Doppler Radar"
                )
            }

            SmartHazardType.HEAVY_RAIN -> {
                val risk = if (rain > 30.0) "CRITICAL" else if (rain > 12.0) "HIGH" else "LOW"
                val directives = mapOf(
                    ActionRole.GENERAL_PUBLIC to "Carry sturdy rain gear; avoid waterlogged roads; avoid touching outdoor electric poles.",
                    ActionRole.FARMER to "Postpone all pesticide and fertilizer spraying; inspect bund integrity to prevent field flooding.",
                    ActionRole.FISHER to "Remain within safe navigation channels; verify visibility before harbor departures.",
                    ActionRole.DRIVER to "Reduce speed by 30%; turn on low-beam headlights; double braking following distance.",
                    ActionRole.MUNICIPALITY to "Operate municipal stormwater pumping stations at full throttle; unblock street drains.",
                    ActionRole.DISASTER_RESPONSE to "Maintain round-the-clock shift staffing at emergency operations center.",
                    ActionRole.RESEARCHER to "Evaluate mesoscale convective complex precipitation distribution against WRF forecasts."
                )
                HazardActionCard(
                    hazardType = hazard,
                    riskLevel = risk,
                    affectedArea = "$cityName District & suburban catchment blocks",
                    validityWindow = "Next 24 Hours",
                    expectedImpact = "Water accumulation on arterial roadways, reduction of visibility below 1.5 km, traffic slowdown.",
                    recommendedAction = "Postpone non-urgent road travel; exercise extreme caution at roadway water accumulations.",
                    confidencePercent = 93,
                    roleDirectives = directives,
                    sourceAgency = "IMD Doppler Weather Radar Network & AWS Grids"
                )
            }

            SmartHazardType.HEATWAVE -> {
                val risk = if (temp >= 42.0) "CRITICAL" else if (temp >= 39.0) "HIGH" else "LOW"
                val directives = mapOf(
                    ActionRole.GENERAL_PUBLIC to "Drink plenty of water and ORS even if not thirsty; wear light-colored loose cotton clothing.",
                    ActionRole.FARMER to "Conduct field operations only before 10:00 AM and after 4:30 PM; provide shade for dairy cattle.",
                    ActionRole.FISHER to "Ensure adequate onboard ice preservation for catch; avoid direct sun exposure in open skiffs.",
                    ActionRole.DRIVER to "Check vehicle coolant and tire pressure; never leave children or pets inside locked cars.",
                    ActionRole.MUNICIPALITY to "Install public water kiosks (Piyau) at bus terminals; reschedule outdoor municipal sweeps.",
                    ActionRole.DISASTER_RESPONSE to "Equip civil hospitals and primary health centres with dedicated heatstroke recovery beds.",
                    ActionRole.RESEARCHER to "Calculate wet-bulb globe temperature (WBGT) and urban heat island thermal anomalies."
                )
                HazardActionCard(
                    hazardType = hazard,
                    riskLevel = risk,
                    affectedArea = "Entire urban core and open agricultural plains of $cityName",
                    validityWindow = "11:00 AM – 4:30 PM Daily",
                    expectedImpact = "High risk of heat cramps, dehydration, and heat exhaustion, especially among vulnerable populations.",
                    recommendedAction = "Avoid direct sun exposure during peak afternoon hours; hydrate continuously with electrolytes.",
                    confidencePercent = 95,
                    roleDirectives = directives,
                    sourceAgency = "IMD National Heatwave Early Warning & National Disaster Management Authority (NDMA)"
                )
            }

            SmartHazardType.LIGHTNING -> {
                val risk = if (weather.rainProbabilityPercent > 50 || rain > 5.0) "HIGH" else "LOW"
                val directives = mapOf(
                    ActionRole.GENERAL_PUBLIC to "Take immediate shelter inside a concrete building; strictly avoid standing under solitary tall trees.",
                    ActionRole.FARMER to "Immediately leave open paddy/wheat fields; do not touch metal ploughs, pipes, or fencing.",
                    ActionRole.FISHER to "Return to shore immediately; open water presents an extreme lightning strike hazard.",
                    ActionRole.DRIVER to "Stay inside enclosed metal vehicle with windows rolled up; do not touch exposed metal parts.",
                    ActionRole.MUNICIPALITY to "Ensure surge arresters on water pumping substations and civic communication towers.",
                    ActionRole.DISASTER_RESPONSE to "Broadcast instant SMS warnings via Damini integration to rural panchayat heads.",
                    ActionRole.RESEARCHER to "Correlate total lightning flash counts with radar echo top heights (>12 km)."
                )
                HazardActionCard(
                    hazardType = hazard,
                    riskLevel = risk,
                    affectedArea = "Rural agricultural fields, open highways & perimeter blocks",
                    validityWindow = "Next 0–2 Hours (Immediate Threat)",
                    expectedImpact = "Cloud-to-ground electrical discharge, transformer flashovers, potential severe life hazard.",
                    recommendedAction = "Follow the 30-30 lightning safety rule; seek indoor shelter immediately upon hearing thunder.",
                    confidencePercent = 96,
                    roleDirectives = directives,
                    sourceAgency = "IITM Pune / IMD Damini Lightning Detection Network"
                )
            }

            SmartHazardType.THUNDERSTORM -> {
                val risk = if (wind > 35.0 || rain > 15.0) "HIGH" else "LOW"
                val directives = mapOf(
                    ActionRole.GENERAL_PUBLIC to "Stay indoors away from glass windows; unplug sensitive electrical electronics.",
                    ActionRole.FARMER to "Cover harvested crops with waterproof tarpaulins; brace young saplings with bamboo stakes.",
                    ActionRole.FISHER to "Mooring lines must be tightened; cease net casting until squall line clears.",
                    ActionRole.DRIVER to "Beware of sudden crosswinds and flying road debris; park away from old hoardings.",
                    ActionRole.MUNICIPALITY to "Pre-position emergency crews to clear fallen hoardings and snapped cables.",
                    ActionRole.DISASTER_RESPONSE to "Monitor distress calls from rural clusters and temporary roofing structures.",
                    ActionRole.RESEARCHER to "Analyze CAPE (Convective Available Potential Energy) and vertical wind shear profiles."
                )
                HazardActionCard(
                    hazardType = hazard,
                    riskLevel = risk,
                    affectedArea = "$cityName metropolitan zone & adjacent rural belt",
                    validityWindow = "Next 3–6 Hours",
                    expectedImpact = "Squall winds 50–70 km/h, intense short-duration rainfall, localized hail, tree branch shedding.",
                    recommendedAction = "Remain inside sturdy pucca buildings; secure temporary sheds and metal sheets.",
                    confidencePercent = 92,
                    roleDirectives = directives,
                    sourceAgency = "IMD Nowcast Radar Warning Cell"
                )
            }

            SmartHazardType.HIGH_WIND -> {
                val risk = if (wind > 45.0) "HIGH" else if (wind > 28.0) "MODERATE" else "LOW"
                val directives = mapOf(
                    ActionRole.GENERAL_PUBLIC to "Avoid walking near tall billboards, weak boundary walls, or construction hoardings.",
                    ActionRole.FARMER to "Stake banana, papaya, and tall cereal crops; shelter livestock from flying tin roofs.",
                    ActionRole.FISHER to "High wind makes handling small fishing craft perilous; observe harbor wind cones.",
                    ActionRole.DRIVER to "Grip steering wheel firmly with both hands; reduce speed on open bridges and flyovers.",
                    ActionRole.MUNICIPALITY to "Inspect integrity of overhead traffic sign gantries and decorative light poles.",
                    ActionRole.DISASTER_RESPONSE to "Coordinate with electric distribution company for prompt dead-line de-energization.",
                    ActionRole.RESEARCHER to "Model surface aerodynamic roughness and gust factors across the urban canopy layer."
                )
                HazardActionCard(
                    hazardType = hazard,
                    riskLevel = risk,
                    affectedArea = "Exposed ridges, high-rise building zones & open expressways",
                    validityWindow = "Next 6–12 Hours",
                    expectedImpact = "Blowing dust, swaying of overhead power cables, dislodgement of loose roof tiles.",
                    recommendedAction = "Fasten outdoor furniture and loose metal sheeting; steer clear of fragile perimeter structures.",
                    confidencePercent = 90,
                    roleDirectives = directives,
                    sourceAgency = "IMD Surface AWS Anemometer Network"
                )
            }

            SmartHazardType.LANDSLIDE -> {
                val risk = if (cityName in listOf("Darjeeling", "Shimla", "Dehradun", "Munnar", "Wayanad") && rain > 20.0) "HIGH" else "LOW"
                val directives = mapOf(
                    ActionRole.GENERAL_PUBLIC to "Stay vigilant for new cracks in walls or ground; listen for unusual rumbling sounds.",
                    ActionRole.FARMER to "Do not construct heavy water storage tanks on steep un-retained hill slopes.",
                    ActionRole.FISHER to "Avoid canyon riverbanks where debris dams can burst suddenly.",
                    ActionRole.DRIVER to "Strictly avoid mountain highway ghat roads during heavy nighttime downpours.",
                    ActionRole.MUNICIPALITY to "Ensure roadside catch-water drains and hillside culverts are cleared of silt.",
                    ActionRole.DISASTER_RESPONSE to "Keep heavy earthmovers and rescue gear stationed at vulnerable ghat road passes.",
                    ActionRole.RESEARCHER to "Monitor Geological Survey of India (GSI) rainfall threshold exceedance curves."
                )
                HazardActionCard(
                    hazardType = hazard,
                    riskLevel = risk,
                    affectedArea = "Steep hillside slopes, ghat cutting corridors & river valley bends",
                    validityWindow = "Next 24–48 Hours",
                    expectedImpact = "Soil saturation slip, rockfalls blocking hill highways, debris flows in mountain ravines.",
                    recommendedAction = "Avoid traveling on steep ghat roads during intense precipitation; vacate slope-edge houses.",
                    confidencePercent = 89,
                    roleDirectives = directives,
                    sourceAgency = "Geological Survey of India (GSI) & IMD Hill State Early Warning"
                )
            }

            SmartHazardType.COASTAL_HAZARD -> {
                val isCoastal = cityName in listOf("Puri", "Balasore", "Kolkata", "Chennai", "Mumbai", "Mangaluru", "Kanyakumari")
                val risk = if (isCoastal && wind > 30.0) "HIGH" else if (isCoastal) "MODERATE" else "LOW"
                val directives = mapOf(
                    ActionRole.GENERAL_PUBLIC to "Stay well away from sea beaches, jetties, and coastal rocky seawalls.",
                    ActionRole.FARMER to "Check coastal saline embankment sluices to prevent saltwater intrusion into paddy lands.",
                    ActionRole.FISHER to "Anchor craft well above the spring high-tide line; do not operate country rafts.",
                    ActionRole.DRIVER to "Watch for wave overtopping and sand deposits on coastal marine drive roads.",
                    ActionRole.MUNICIPALITY to "Deploy warning flags and lifeguards along public beach frontages.",
                    ActionRole.DISASTER_RESPONSE to "Maintain coastal quick-response boats on standby at all tidal harbors.",
                    ActionRole.RESEARCHER to "Analyze INCOIS wave rider buoy directional wave spectra and astronomical tide tables."
                )
                HazardActionCard(
                    hazardType = hazard,
                    riskLevel = risk,
                    affectedArea = "Inter-tidal beaches, coastal promenades & estuarine inlets",
                    validityWindow = "Next 24 Hours",
                    expectedImpact = "High swell waves (2.2–3.5m), rough sea conditions, beach erosion, wave overtopping onto roads.",
                    recommendedAction = "Prohibit swimming and recreation on coastal beaches; secure small craft inland.",
                    confidencePercent = 95,
                    roleDirectives = directives,
                    sourceAgency = "Indian National Centre for Ocean Information Services (INCOIS) & IMD"
                )
            }
        }
    }
}
