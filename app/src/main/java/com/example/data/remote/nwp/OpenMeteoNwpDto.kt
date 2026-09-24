package com.example.data.remote.nwp

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Moshi Data Models for Open-Meteo & Numerical Weather Prediction (NWP) APIs.
 *
 * Implements full schema deserialization including raw units metadata
 * to enable dynamic unit detection and normalization across different NWP sources.
 */

@JsonClass(generateAdapter = true)
data class OpenMeteoNwpResponse(
    @Json(name = "latitude") val latitude: Double?,
    @Json(name = "longitude") val longitude: Double?,
    @Json(name = "generationtime_ms") val generationTimeMs: Double?,
    @Json(name = "utc_offset_seconds") val utcOffsetSeconds: Int?,
    @Json(name = "timezone") val timezone: String?,
    @Json(name = "timezone_abbreviation") val timezoneAbbreviation: String?,
    @Json(name = "elevation") val elevation: Double?,
    @Json(name = "current_units") val currentUnits: NwpCurrentUnitsDto? = null,
    @Json(name = "current") val current: NwpCurrentValuesDto? = null,
    @Json(name = "hourly_units") val hourlyUnits: NwpHourlyUnitsDto? = null,
    @Json(name = "hourly") val hourly: NwpHourlyValuesDto? = null,
    @Json(name = "daily_units") val dailyUnits: NwpDailyUnitsDto? = null,
    @Json(name = "daily") val daily: NwpDailyValuesDto? = null
)

@JsonClass(generateAdapter = true)
data class NwpCurrentUnitsDto(
    @Json(name = "time") val time: String? = "iso8601",
    @Json(name = "temperature_2m") val temperatureUnit: String? = "°C",
    @Json(name = "relative_humidity_2m") val humidityUnit: String? = "%",
    @Json(name = "apparent_temperature") val apparentTemperatureUnit: String? = "°C",
    @Json(name = "precipitation") val precipitationUnit: String? = "mm",
    @Json(name = "rain") val rainUnit: String? = "mm",
    @Json(name = "surface_pressure") val surfacePressureUnit: String? = "hPa",
    @Json(name = "pressure_msl") val pressureMslUnit: String? = "hPa",
    @Json(name = "wind_speed_10m") val windSpeedUnit: String? = "km/h",
    @Json(name = "wind_direction_10m") val windDirectionUnit: String? = "°",
    @Json(name = "wind_gusts_10m") val windGustsUnit: String? = "km/h",
    @Json(name = "cloud_cover") val cloudCoverUnit: String? = "%",
    @Json(name = "uv_index") val uvIndexUnit: String? = null
)

@JsonClass(generateAdapter = true)
data class NwpCurrentValuesDto(
    @Json(name = "time") val time: String?,
    @Json(name = "temperature_2m") val temperature2m: Double?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: Double?,
    @Json(name = "apparent_temperature") val apparentTemperature: Double?,
    @Json(name = "precipitation") val precipitation: Double?,
    @Json(name = "rain") val rain: Double? = null,
    @Json(name = "showers") val showers: Double? = null,
    @Json(name = "snowfall") val snowfall: Double? = null,
    @Json(name = "weather_code") val weatherCode: Int?,
    @Json(name = "cloud_cover") val cloudCover: Int? = null,
    @Json(name = "pressure_msl") val pressureMsl: Double? = null,
    @Json(name = "surface_pressure") val surfacePressure: Double? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: Double?,
    @Json(name = "wind_direction_10m") val windDirection10m: Double?,
    @Json(name = "wind_gusts_10m") val windGusts10m: Double? = null,
    @Json(name = "uv_index") val uvIndex: Double? = null
)

@JsonClass(generateAdapter = true)
data class NwpHourlyUnitsDto(
    @Json(name = "time") val time: String? = "iso8601",
    @Json(name = "temperature_2m") val temperatureUnit: String? = "°C",
    @Json(name = "relative_humidity_2m") val humidityUnit: String? = "%",
    @Json(name = "dew_point_2m") val dewPointUnit: String? = "°C",
    @Json(name = "apparent_temperature") val apparentTemperatureUnit: String? = "°C",
    @Json(name = "precipitation_probability") val precipProbabilityUnit: String? = "%",
    @Json(name = "precipitation") val precipitationUnit: String? = "mm",
    @Json(name = "rain") val rainUnit: String? = "mm",
    @Json(name = "surface_pressure") val surfacePressureUnit: String? = "hPa",
    @Json(name = "visibility") val visibilityUnit: String? = "m",
    @Json(name = "wind_speed_10m") val windSpeedUnit: String? = "km/h",
    @Json(name = "wind_direction_10m") val windDirectionUnit: String? = "°",
    @Json(name = "wind_gusts_10m") val windGustsUnit: String? = "km/h",
    @Json(name = "uv_index") val uvIndexUnit: String? = null,
    @Json(name = "soil_moisture_0_to_1cm") val soilMoistureUnit: String? = "m³/m³"
)

@JsonClass(generateAdapter = true)
data class NwpHourlyValuesDto(
    @Json(name = "time") val time: List<String>?,
    @Json(name = "temperature_2m") val temperature2m: List<Double>?,
    @Json(name = "relative_humidity_2m") val relativeHumidity2m: List<Double>?,
    @Json(name = "dew_point_2m") val dewPoint2m: List<Double>? = null,
    @Json(name = "apparent_temperature") val apparentTemperature: List<Double>?,
    @Json(name = "precipitation_probability") val precipitationProbability: List<Int>?,
    @Json(name = "precipitation") val precipitation: List<Double>?,
    @Json(name = "rain") val rain: List<Double>? = null,
    @Json(name = "showers") val showers: List<Double>? = null,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
    @Json(name = "surface_pressure") val surfacePressure: List<Double>?,
    @Json(name = "cloud_cover") val cloudCover: List<Int>? = null,
    @Json(name = "visibility") val visibility: List<Double>? = null,
    @Json(name = "wind_speed_10m") val windSpeed10m: List<Double>?,
    @Json(name = "wind_direction_10m") val windDirection10m: List<Double>?,
    @Json(name = "wind_gusts_10m") val windGusts10m: List<Double>? = null,
    @Json(name = "uv_index") val uvIndex: List<Double>? = null,
    @Json(name = "soil_temperature_0cm") val soilTemperature0cm: List<Double>? = null,
    @Json(name = "soil_moisture_0_to_1cm") val soilMoisture0to1cm: List<Double>? = null
)

@JsonClass(generateAdapter = true)
data class NwpDailyUnitsDto(
    @Json(name = "time") val time: String? = "iso8601",
    @Json(name = "temperature_2m_max") val temperatureMaxUnit: String? = "°C",
    @Json(name = "temperature_2m_min") val temperatureMinUnit: String? = "°C",
    @Json(name = "apparent_temperature_max") val apparentTempMaxUnit: String? = "°C",
    @Json(name = "apparent_temperature_min") val apparentTempMinUnit: String? = "°C",
    @Json(name = "precipitation_sum") val precipitationSumUnit: String? = "mm",
    @Json(name = "precipitation_probability_max") val precipProbabilityMaxUnit: String? = "%",
    @Json(name = "wind_speed_10m_max") val windSpeedMaxUnit: String? = "km/h",
    @Json(name = "wind_gusts_10m_max") val windGustsMaxUnit: String? = "km/h",
    @Json(name = "et0_fao_evapotranspiration") val et0Unit: String? = "mm"
)

@JsonClass(generateAdapter = true)
data class NwpDailyValuesDto(
    @Json(name = "time") val time: List<String>?,
    @Json(name = "weather_code") val weatherCode: List<Int>?,
    @Json(name = "temperature_2m_max") val temperature2mMax: List<Double>?,
    @Json(name = "temperature_2m_min") val temperature2mMin: List<Double>?,
    @Json(name = "apparent_temperature_max") val apparentTemperatureMax: List<Double>? = null,
    @Json(name = "apparent_temperature_min") val apparentTemperatureMin: List<Double>? = null,
    @Json(name = "sunrise") val sunrise: List<String>?,
    @Json(name = "sunset") val sunset: List<String>?,
    @Json(name = "uv_index_max") val uvIndexMax: List<Double>? = null,
    @Json(name = "precipitation_sum") val precipitationSum: List<Double>?,
    @Json(name = "rain_sum") val rainSum: List<Double>? = null,
    @Json(name = "precipitation_hours") val precipitationHours: List<Double>? = null,
    @Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>?,
    @Json(name = "wind_speed_10m_max") val windSpeed10mMax: List<Double>?,
    @Json(name = "wind_gusts_10m_max") val windGusts10mMax: List<Double>? = null,
    @Json(name = "wind_direction_10m_dominant") val windDirection10mDominant: List<Int>? = null,
    @Json(name = "et0_fao_evapotranspiration") val et0FaoEvapotranspiration: List<Double>? = null
)
