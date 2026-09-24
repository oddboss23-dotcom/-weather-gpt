package com.example.data.remote.nwp

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit Service Interface for Open-Meteo and Numerical Weather Prediction (NWP) APIs.
 *
 * Supports querying multiple global NWP models (Best-Match, ECMWF IFS, NOAA GFS, DWD ICON)
 * with explicit unit customization (celsius/fahrenheit, kmh/ms/mph/kn, mm/inch)
 * and rich atmospheric parameters.
 */
interface OpenMeteoNwpApiService {

    /**
     * Primary High-Resolution Best-Match NWP Forecast.
     * Integrates ECMWF, ICON, and regional Indian terrain models.
     */
    @GET("v1/forecast")
    suspend fun getBestMatchForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = DEFAULT_CURRENT_VARS,
        @Query("hourly") hourly: String = DEFAULT_HOURLY_VARS,
        @Query("daily") daily: String = DEFAULT_DAILY_VARS,
        @Query("temperature_unit") temperatureUnit: String = "celsius",
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
        @Query("precipitation_unit") precipitationUnit: String = "mm",
        @Query("timeformat") timeFormat: String = "iso8601",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 3,
        @Query("past_days") pastDays: Int = 0
    ): OpenMeteoNwpResponse

    /**
     * Specialized ECMWF (European Centre for Medium-Range Weather Forecasts) IFS 9km run.
     * High accuracy for synoptic frontal systems and cyclone tracks.
     */
    @GET("v1/ecmwf")
    suspend fun getEcmwfForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m",
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_sum,weather_code",
        @Query("temperature_unit") temperatureUnit: String = "celsius",
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
        @Query("precipitation_unit") precipitationUnit: String = "mm",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 3
    ): OpenMeteoNwpResponse

    /**
     * Specialized NOAA GFS (Global Forecast System) 13km run.
     * Used in multi-model consensus and forecast bust calculation.
     */
    @GET("v1/gfs")
    suspend fun getGfsForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m,cloud_cover",
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation_probability,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_direction_10m",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max,weather_code",
        @Query("temperature_unit") temperatureUnit: String = "celsius",
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
        @Query("precipitation_unit") precipitationUnit: String = "mm",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 3
    ): OpenMeteoNwpResponse

    /**
     * Specialized DWD ICON (Deutscher Wetterdienst) Global 13km / EU 7km run.
     * Exceptional boundary-layer convection modeling.
     */
    @GET("v1/dwd-icon")
    suspend fun getDwdIconForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m,wind_direction_10m",
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m,wind_direction_10m",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 3
    ): OpenMeteoNwpResponse

    /**
     * Multi-member Ensemble Forecast.
     * Provides 30–50 perturbation members for calculating spread and confidence bounds.
     */
    @GET("v1/ensemble")
    suspend fun getEnsembleForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "temperature_2m,precipitation",
        @Query("models") models: String = "icon_seamless,gfs_seamless,ecmwf_ifs04",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 3
    ): OpenMeteoNwpResponse

    companion object {
        const val DEFAULT_CURRENT_VARS =
            "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,showers,snowfall,weather_code,cloud_cover,pressure_msl,surface_pressure,wind_speed_10m,wind_direction_10m,wind_gusts_10m,uv_index"

        const val DEFAULT_HOURLY_VARS =
            "temperature_2m,relative_humidity_2m,dew_point_2m,apparent_temperature,precipitation_probability,precipitation,rain,showers,snowfall,weather_code,pressure_msl,surface_pressure,cloud_cover,visibility,wind_speed_10m,wind_direction_10m,wind_gusts_10m,uv_index,soil_temperature_0cm,soil_moisture_0_to_1cm"

        const val DEFAULT_DAILY_VARS =
            "weather_code,temperature_2m_max,temperature_2m_min,apparent_temperature_max,apparent_temperature_min,sunrise,sunset,uv_index_max,precipitation_sum,rain_sum,precipitation_hours,precipitation_probability_max,wind_speed_10m_max,wind_gusts_10m_max,wind_direction_10m_dominant,et0_fao_evapotranspiration"
    }
}
