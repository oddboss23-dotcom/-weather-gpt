package com.example.data.remote.nwp

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.roundToInt

/**
 * Enterprise Meteorological Unit Normalizer for WeatherGPT.
 *
 * Implements rigorous, scientific unit normalization for:
 * - Temperature (°C, °F, K) -> Standardized to Celsius (°C)
 * - Precipitation (mm, inches) -> Standardized to Millimeters (mm)
 * - Wind Speed (km/h, m/s, mph, knots) -> Standardized to Kilometers per hour (km/h)
 * - Pressure (hPa, mbar, inHg, mmHg) -> Standardized to Hectopascals (hPa)
 * - Visibility (meters, miles, feet) -> Standardized to Kilometers (km)
 *
 * Enforces meteorological boundary clamps based on the climatological envelope
 * of the Indian subcontinent (Himalayan sub-zero extremes to Thar Desert heatwaves).
 */

enum class TemperatureUnit(val symbol: String) {
    CELSIUS("°C"),
    FAHRENHEIT("°F"),
    KELVIN("K")
}

enum class PrecipitationUnit(val symbol: String) {
    MILLIMETERS("mm"),
    INCHES("inch")
}

enum class WindSpeedUnit(val symbol: String) {
    KILOMETERS_PER_HOUR("km/h"),
    METERS_PER_SECOND("m/s"),
    MILES_PER_HOUR("mph"),
    KNOTS("kn")
}

enum class PressureUnit(val symbol: String) {
    HECTOPASCALS("hPa"),
    MILLIBARS("mbar"),
    INCHES_OF_MERCURY("inHg"),
    MILLIMETERS_OF_MERCURY("mmHg")
}

enum class VisibilityUnit(val symbol: String) {
    KILOMETERS("km"),
    METERS("m"),
    MILES("mi"),
    FEET("ft")
}

data class NormalizedCurrentWeather(
    val timeIso: String,
    val epochMillis: Long,
    val temperatureC: Double,
    val apparentTemperatureC: Double,
    val relativeHumidityPercent: Int,
    val precipitationMm: Double,
    val rainMm: Double,
    val surfacePressureHpa: Double,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val windGustsKmh: Double,
    val cloudCoverPercent: Int,
    val uvIndex: Double,
    val weatherCode: Int,
    val conditionDescription: String
)

data class NormalizedHourlyPoint(
    val timeIso: String,
    val epochMillis: Long,
    val temperatureC: Double,
    val apparentTemperatureC: Double,
    val dewPointC: Double,
    val relativeHumidityPercent: Int,
    val precipitationProbabilityPercent: Int,
    val precipitationMm: Double,
    val rainMm: Double,
    val weatherCode: Int,
    val conditionDescription: String,
    val surfacePressureHpa: Double,
    val cloudCoverPercent: Int,
    val visibilityKm: Double,
    val windSpeedKmh: Double,
    val windDirectionDeg: Int,
    val windGustsKmh: Double,
    val uvIndex: Double
)

data class NormalizedDailyPoint(
    val dateIso: String,
    val epochMillis: Long,
    val maxTempC: Double,
    val minTempC: Double,
    val apparentMaxTempC: Double,
    val apparentMinTempC: Double,
    val precipitationSumMm: Double,
    val precipitationProbabilityMaxPercent: Int,
    val maxWindSpeedKmh: Double,
    val dominantWindDirectionDeg: Int,
    val weatherCode: Int,
    val conditionDescription: String,
    val sunriseIso: String?,
    val sunsetIso: String?,
    val uvIndexMax: Double
)

object WeatherUnitNormalizer {

    // -------------------------------------------------------------------------
    // TEMPERATURE NORMALIZATION
    // Standard Target: Celsius (°C)
    // Physical Subcontinent Range: -50.0°C (Siachen) to +58.0°C (Phalodi)
    // -------------------------------------------------------------------------

    fun normalizeTemperature(raw: Double?, unitStr: String? = null): Double {
        if (raw == null || raw.isNaN() || raw.isInfinite()) return 28.0

        val normalized = when (detectTemperatureUnit(unitStr)) {
            TemperatureUnit.FAHRENHEIT -> fahrenheitToCelsius(raw)
            TemperatureUnit.KELVIN -> kelvinToCelsius(raw)
            TemperatureUnit.CELSIUS -> raw
        }
        return (normalized.coerceIn(-50.0, 60.0) * 10.0).roundToInt() / 10.0
    }

    fun fahrenheitToCelsius(f: Double): Double = (f - 32.0) * 5.0 / 9.0

    fun celsiusToFahrenheit(c: Double): Double = (c * 9.0 / 5.0) + 32.0

    fun kelvinToCelsius(k: Double): Double = k - 273.15

    fun detectTemperatureUnit(unitStr: String?): TemperatureUnit {
        if (unitStr.isNullOrBlank()) return TemperatureUnit.CELSIUS
        val cleaned = unitStr.trim().lowercase()
        return when {
            cleaned.contains("f") -> TemperatureUnit.FAHRENHEIT
            cleaned.contains("k") -> TemperatureUnit.KELVIN
            else -> TemperatureUnit.CELSIUS
        }
    }

    // -------------------------------------------------------------------------
    // PRECIPITATION NORMALIZATION
    // Standard Target: Millimeters (mm)
    // Physical Subcontinent Range: 0.0 mm to 350.0 mm (Cherrapunji cloudburst envelope)
    // -------------------------------------------------------------------------

    fun normalizePrecipitation(raw: Double?, unitStr: String? = null): Double {
        if (raw == null || raw.isNaN() || raw.isInfinite() || raw < 0.0) return 0.0

        val normalized = when (detectPrecipitationUnit(unitStr)) {
            PrecipitationUnit.INCHES -> inchesToMillimeters(raw)
            PrecipitationUnit.MILLIMETERS -> raw
        }
        return (normalized.coerceIn(0.0, 350.0) * 10.0).roundToInt() / 10.0
    }

    fun inchesToMillimeters(inches: Double): Double = inches * 25.4

    fun millimetersToInches(mm: Double): Double = mm / 25.4

    fun detectPrecipitationUnit(unitStr: String?): PrecipitationUnit {
        if (unitStr.isNullOrBlank()) return PrecipitationUnit.MILLIMETERS
        val cleaned = unitStr.trim().lowercase()
        return when {
            cleaned.contains("in") || cleaned.contains("inch") -> PrecipitationUnit.INCHES
            else -> PrecipitationUnit.MILLIMETERS
        }
    }

    // -------------------------------------------------------------------------
    // WIND SPEED NORMALIZATION
    // Standard Target: Kilometers per hour (km/h)
    // Physical Subcontinent Range: 0.0 km/h to 350.0 km/h (Super Cyclone threshold)
    // -------------------------------------------------------------------------

    fun normalizeWindSpeed(raw: Double?, unitStr: String? = null): Double {
        if (raw == null || raw.isNaN() || raw.isInfinite() || raw < 0.0) return 10.0

        val normalized = when (detectWindSpeedUnit(unitStr)) {
            WindSpeedUnit.METERS_PER_SECOND -> metersPerSecondToKmh(raw)
            WindSpeedUnit.MILES_PER_HOUR -> mphToKmh(raw)
            WindSpeedUnit.KNOTS -> knotsToKmh(raw)
            WindSpeedUnit.KILOMETERS_PER_HOUR -> raw
        }
        return (normalized.coerceIn(0.0, 350.0) * 10.0).roundToInt() / 10.0
    }

    fun metersPerSecondToKmh(ms: Double): Double = ms * 3.6

    fun mphToKmh(mph: Double): Double = mph * 1.609344

    fun knotsToKmh(knots: Double): Double = knots * 1.852

    fun kmhToMetersPerSecond(kmh: Double): Double = kmh / 3.6

    fun kmhToKnots(kmh: Double): Double = kmh / 1.852

    fun detectWindSpeedUnit(unitStr: String?): WindSpeedUnit {
        if (unitStr.isNullOrBlank()) return WindSpeedUnit.KILOMETERS_PER_HOUR
        val cleaned = unitStr.trim().lowercase()
        return when {
            cleaned == "m/s" || cleaned == "ms" || cleaned.contains("meter") -> WindSpeedUnit.METERS_PER_SECOND
            cleaned == "mph" || cleaned.contains("mile") -> WindSpeedUnit.MILES_PER_HOUR
            cleaned == "kn" || cleaned == "knot" || cleaned == "kts" -> WindSpeedUnit.KNOTS
            else -> WindSpeedUnit.KILOMETERS_PER_HOUR
        }
    }

    // -------------------------------------------------------------------------
    // ATMOSPHERIC PRESSURE NORMALIZATION
    // Standard Target: Hectopascals (hPa)
    // Physical Subcontinent Range: 870.0 hPa (Extremely severe cyclone eye) to 1085.0 hPa
    // -------------------------------------------------------------------------

    fun normalizePressure(raw: Double?, unitStr: String? = null): Double {
        if (raw == null || raw.isNaN() || raw.isInfinite()) return 1010.0

        val normalized = when (detectPressureUnit(unitStr)) {
            PressureUnit.INCHES_OF_MERCURY -> inchesOfMercuryToHpa(raw)
            PressureUnit.MILLIMETERS_OF_MERCURY -> mmOfMercuryToHpa(raw)
            PressureUnit.MILLIBARS -> raw // 1 mbar = 1 hPa
            PressureUnit.HECTOPASCALS -> raw
        }
        return (normalized.coerceIn(870.0, 1085.0) * 10.0).roundToInt() / 10.0
    }

    fun inchesOfMercuryToHpa(inHg: Double): Double = inHg * 33.863886

    fun mmOfMercuryToHpa(mmHg: Double): Double = mmHg * 1.333224

    fun detectPressureUnit(unitStr: String?): PressureUnit {
        if (unitStr.isNullOrBlank()) return PressureUnit.HECTOPASCALS
        val cleaned = unitStr.trim().lowercase()
        return when {
            cleaned.contains("inhg") -> PressureUnit.INCHES_OF_MERCURY
            cleaned.contains("mmhg") || cleaned.contains("torr") -> PressureUnit.MILLIMETERS_OF_MERCURY
            cleaned.contains("mbar") -> PressureUnit.MILLIBARS
            else -> PressureUnit.HECTOPASCALS
        }
    }

    // -------------------------------------------------------------------------
    // VISIBILITY NORMALIZATION
    // Standard Target: Kilometers (km)
    // Physical Range: 0.0 km (Zero-visibility fog) to 50.0 km
    // -------------------------------------------------------------------------

    fun normalizeVisibility(raw: Double?, unitStr: String? = null): Double {
        if (raw == null || raw.isNaN() || raw.isInfinite() || raw < 0.0) return 10.0

        val normalized = when (detectVisibilityUnit(unitStr, raw)) {
            VisibilityUnit.METERS -> raw / 1000.0
            VisibilityUnit.MILES -> raw * 1.609344
            VisibilityUnit.FEET -> raw * 0.0003048
            VisibilityUnit.KILOMETERS -> raw
        }
        return (normalized.coerceIn(0.0, 50.0) * 10.0).roundToInt() / 10.0
    }

    private fun detectVisibilityUnit(unitStr: String?, rawVal: Double): VisibilityUnit {
        if (!unitStr.isNullOrBlank()) {
            val cleaned = unitStr.trim().lowercase()
            return when {
                cleaned == "m" || cleaned == "meter" || cleaned == "meters" -> VisibilityUnit.METERS
                cleaned == "mi" || cleaned == "mile" || cleaned == "miles" -> VisibilityUnit.MILES
                cleaned == "ft" || cleaned == "feet" -> VisibilityUnit.FEET
                else -> VisibilityUnit.KILOMETERS
            }
        }
        // Heuristic: If value > 100, Open-Meteo sent meters (e.g. 10000.0 = 10km)
        return if (rawVal > 100.0) VisibilityUnit.METERS else VisibilityUnit.KILOMETERS
    }

    // -------------------------------------------------------------------------
    // ANCILLARY NORMALIZERS
    // -------------------------------------------------------------------------

    fun normalizeRelativeHumidity(raw: Double?): Int {
        if (raw == null || raw.isNaN() || raw.isInfinite()) return 60
        return raw.roundToInt().coerceIn(0, 100)
    }

    fun normalizeProbability(raw: Int?): Int {
        if (raw == null) return 0
        return raw.coerceIn(0, 100)
    }

    fun normalizeWindDirection(rawDeg: Double?): Int {
        if (rawDeg == null || rawDeg.isNaN()) return 0
        val deg = rawDeg.roundToInt() % 360
        return if (deg < 0) deg + 360 else deg
    }

    fun normalizeCloudCover(raw: Int?): Int {
        if (raw == null) return 20
        return raw.coerceIn(0, 100)
    }

    fun normalizeUvIndex(raw: Double?): Double {
        if (raw == null || raw.isNaN() || raw.isInfinite() || raw < 0.0) return 0.0
        return (raw.coerceIn(0.0, 16.0) * 10.0).roundToInt() / 10.0
    }

    fun parseWeatherCodeDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Fog / Depositing Rime Fog"
            51 -> "Light Drizzle"
            53 -> "Moderate Drizzle"
            55 -> "Dense Drizzle"
            56, 57 -> "Freezing Drizzle"
            61 -> "Slight Rain"
            63 -> "Moderate Rain"
            65 -> "Heavy Downpour"
            66, 67 -> "Freezing Rain"
            71 -> "Slight Snowfall"
            73 -> "Moderate Snowfall"
            75 -> "Heavy Snowfall"
            77 -> "Snow Grains"
            80 -> "Slight Rain Showers"
            81 -> "Moderate Rain Showers"
            82 -> "Violent Rain Showers"
            85, 86 -> "Snow Showers"
            95 -> "Thunderstorm"
            96, 99 -> "Severe Thunderstorm with Hail"
            else -> "Fair Meteorological Conditions"
        }
    }

    fun parseIsoToEpochMillis(isoString: String?, fallbackZoneId: ZoneId = ZoneId.systemDefault()): Long {
        if (isoString.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            Instant.parse(isoString).toEpochMilli()
        } catch (_: DateTimeParseException) {
            try {
                LocalDateTime.parse(isoString, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .atZone(fallbackZoneId)
                    .toInstant()
                    .toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    // -------------------------------------------------------------------------
    // WHOLE-RESPONSE NORMALIZATION MAPPERS
    // -------------------------------------------------------------------------

    fun normalizeCurrentWeather(response: OpenMeteoNwpResponse): NormalizedCurrentWeather {
        val curr = response.current
        val units = response.currentUnits
        val timeIso = curr?.time ?: Instant.now().toString()
        val epochMillis = parseIsoToEpochMillis(curr?.time)

        val tempC = normalizeTemperature(curr?.temperature2m, units?.temperatureUnit)
        val apparentC = normalizeTemperature(curr?.apparentTemperature ?: curr?.temperature2m, units?.apparentTemperatureUnit ?: units?.temperatureUnit)
        val precipMm = normalizePrecipitation(curr?.precipitation, units?.precipitationUnit)
        val rainMm = normalizePrecipitation(curr?.rain ?: curr?.precipitation, units?.rainUnit ?: units?.precipitationUnit)
        val windKmh = normalizeWindSpeed(curr?.windSpeed10m, units?.windSpeedUnit)
        val gustsKmh = normalizeWindSpeed(curr?.windGusts10m ?: curr?.windSpeed10m, units?.windGustsUnit ?: units?.windSpeedUnit)
        val pressureHpa = normalizePressure(curr?.surfacePressure ?: curr?.pressureMsl, units?.surfacePressureUnit ?: units?.pressureMslUnit)
        val weatherCode = curr?.weatherCode ?: 0

        return NormalizedCurrentWeather(
            timeIso = timeIso,
            epochMillis = epochMillis,
            temperatureC = tempC,
            apparentTemperatureC = apparentC,
            relativeHumidityPercent = normalizeRelativeHumidity(curr?.relativeHumidity2m),
            precipitationMm = precipMm,
            rainMm = rainMm,
            surfacePressureHpa = pressureHpa,
            windSpeedKmh = windKmh,
            windDirectionDeg = normalizeWindDirection(curr?.windDirection10m),
            windGustsKmh = gustsKmh,
            cloudCoverPercent = normalizeCloudCover(curr?.cloudCover),
            uvIndex = normalizeUvIndex(curr?.uvIndex),
            weatherCode = weatherCode,
            conditionDescription = parseWeatherCodeDescription(weatherCode)
        )
    }

    fun normalizeHourlyForecast(response: OpenMeteoNwpResponse): List<NormalizedHourlyPoint> {
        val hourly = response.hourly ?: return emptyList()
        val units = response.hourlyUnits
        val times = hourly.time ?: return emptyList()
        val count = times.size

        val result = ArrayList<NormalizedHourlyPoint>(count)
        for (i in 0 until count) {
            val timeIso = times.getOrNull(i) ?: continue
            val tempRaw = hourly.temperature2m?.getOrNull(i)
            val appRaw = hourly.apparentTemperature?.getOrNull(i) ?: tempRaw
            val dewRaw = hourly.dewPoint2m?.getOrNull(i)
            val humidityRaw = hourly.relativeHumidity2m?.getOrNull(i)
            val probRaw = hourly.precipitationProbability?.getOrNull(i)
            val precipRaw = hourly.precipitation?.getOrNull(i)
            val rainRaw = hourly.rain?.getOrNull(i) ?: precipRaw
            val codeRaw = hourly.weatherCode?.getOrNull(i) ?: 0
            val pressureRaw = hourly.surfacePressure?.getOrNull(i)
            val cloudRaw = hourly.cloudCover?.getOrNull(i)
            val visRaw = hourly.visibility?.getOrNull(i)
            val windRaw = hourly.windSpeed10m?.getOrNull(i)
            val dirRaw = hourly.windDirection10m?.getOrNull(i)
            val gustsRaw = hourly.windGusts10m?.getOrNull(i)
            val uvRaw = hourly.uvIndex?.getOrNull(i)

            result.add(
                NormalizedHourlyPoint(
                    timeIso = timeIso,
                    epochMillis = parseIsoToEpochMillis(timeIso),
                    temperatureC = normalizeTemperature(tempRaw, units?.temperatureUnit),
                    apparentTemperatureC = normalizeTemperature(appRaw, units?.apparentTemperatureUnit ?: units?.temperatureUnit),
                    dewPointC = normalizeTemperature(dewRaw, units?.dewPointUnit ?: units?.temperatureUnit),
                    relativeHumidityPercent = normalizeRelativeHumidity(humidityRaw),
                    precipitationProbabilityPercent = normalizeProbability(probRaw),
                    precipitationMm = normalizePrecipitation(precipRaw, units?.precipitationUnit),
                    rainMm = normalizePrecipitation(rainRaw, units?.rainUnit ?: units?.precipitationUnit),
                    weatherCode = codeRaw,
                    conditionDescription = parseWeatherCodeDescription(codeRaw),
                    surfacePressureHpa = normalizePressure(pressureRaw, units?.surfacePressureUnit),
                    cloudCoverPercent = normalizeCloudCover(cloudRaw),
                    visibilityKm = normalizeVisibility(visRaw, units?.visibilityUnit),
                    windSpeedKmh = normalizeWindSpeed(windRaw, units?.windSpeedUnit),
                    windDirectionDeg = normalizeWindDirection(dirRaw),
                    windGustsKmh = normalizeWindSpeed(gustsRaw ?: windRaw, units?.windGustsUnit ?: units?.windSpeedUnit),
                    uvIndex = normalizeUvIndex(uvRaw)
                )
            )
        }
        return result
    }

    fun normalizeDailyForecast(response: OpenMeteoNwpResponse): List<NormalizedDailyPoint> {
        val daily = response.daily ?: return emptyList()
        val units = response.dailyUnits
        val times = daily.time ?: return emptyList()
        val count = times.size

        val result = ArrayList<NormalizedDailyPoint>(count)
        for (i in 0 until count) {
            val dateIso = times.getOrNull(i) ?: continue
            val maxRaw = daily.temperature2mMax?.getOrNull(i)
            val minRaw = daily.temperature2mMin?.getOrNull(i)
            val appMaxRaw = daily.apparentTemperatureMax?.getOrNull(i) ?: maxRaw
            val appMinRaw = daily.apparentTemperatureMin?.getOrNull(i) ?: minRaw
            val precipSumRaw = daily.precipitationSum?.getOrNull(i)
            val probMaxRaw = daily.precipitationProbabilityMax?.getOrNull(i)
            val windMaxRaw = daily.windSpeed10mMax?.getOrNull(i)
            val dirDomRaw = daily.windDirection10mDominant?.getOrNull(i)
            val codeRaw = daily.weatherCode?.getOrNull(i) ?: 0
            val sunriseIso = daily.sunrise?.getOrNull(i)
            val sunsetIso = daily.sunset?.getOrNull(i)
            val uvMaxRaw = daily.uvIndexMax?.getOrNull(i)

            result.add(
                NormalizedDailyPoint(
                    dateIso = dateIso,
                    epochMillis = parseIsoToEpochMillis(dateIso),
                    maxTempC = normalizeTemperature(maxRaw, units?.temperatureMaxUnit),
                    minTempC = normalizeTemperature(minRaw, units?.temperatureMinUnit),
                    apparentMaxTempC = normalizeTemperature(appMaxRaw, units?.apparentTempMaxUnit ?: units?.temperatureMaxUnit),
                    apparentMinTempC = normalizeTemperature(appMinRaw, units?.apparentTempMinUnit ?: units?.temperatureMinUnit),
                    precipitationSumMm = normalizePrecipitation(precipSumRaw, units?.precipitationSumUnit),
                    precipitationProbabilityMaxPercent = normalizeProbability(probMaxRaw),
                    maxWindSpeedKmh = normalizeWindSpeed(windMaxRaw, units?.windSpeedMaxUnit),
                    dominantWindDirectionDeg = normalizeWindDirection(dirDomRaw?.toDouble()),
                    weatherCode = codeRaw,
                    conditionDescription = parseWeatherCodeDescription(codeRaw),
                    sunriseIso = sunriseIso,
                    sunsetIso = sunsetIso,
                    uvIndexMax = normalizeUvIndex(uvMaxRaw)
                )
            )
        }
        return result
    }
}
