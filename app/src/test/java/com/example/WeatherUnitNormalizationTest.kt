package com.example

import com.example.data.remote.nwp.NwpNetworkClient
import com.example.data.remote.nwp.OpenMeteoNwpResponse
import com.example.data.remote.nwp.WeatherUnitNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Tests for NWP Moshi Deserialization and Meteorological Unit Normalization.
 */
class WeatherUnitNormalizationTest {

    @Test
    fun testTemperatureNormalization() {
        // Celsius
        val c = WeatherUnitNormalizer.normalizeTemperature(32.5, "°C")
        assertEquals(32.5, c, 0.01)

        // Fahrenheit to Celsius: 86°F = 30°C
        val fromF = WeatherUnitNormalizer.normalizeTemperature(86.0, "°F")
        assertEquals(30.0, fromF, 0.1)

        // Fahrenheit freezing: 32°F = 0°C
        val freezingF = WeatherUnitNormalizer.normalizeTemperature(32.0, "fahrenheit")
        assertEquals(0.0, freezingF, 0.1)

        // Kelvin to Celsius: 300 K = 26.85°C
        val fromK = WeatherUnitNormalizer.normalizeTemperature(300.0, "K")
        assertEquals(26.9, fromK, 0.2)

        // Boundary Clamping: Extreme high capped at 60°C
        val extremeHeat = WeatherUnitNormalizer.normalizeTemperature(85.0, "°C")
        assertEquals(60.0, extremeHeat, 0.01)

        // Boundary Clamping: Extreme low capped at -50°C
        val extremeCold = WeatherUnitNormalizer.normalizeTemperature(-90.0, "°C")
        assertEquals(-50.0, extremeCold, 0.01)
    }

    @Test
    fun testPrecipitationNormalization() {
        // Millimeters
        val mm = WeatherUnitNormalizer.normalizePrecipitation(15.2, "mm")
        assertEquals(15.2, mm, 0.01)

        // Inches to mm: 2.0 inches = 50.8 mm
        val fromInch = WeatherUnitNormalizer.normalizePrecipitation(2.0, "inch")
        assertEquals(50.8, fromInch, 0.1)

        // Boundary clamping: Negative to 0.0
        val negative = WeatherUnitNormalizer.normalizePrecipitation(-5.0, "mm")
        assertEquals(0.0, negative, 0.01)

        // Boundary clamping: Extreme cloudburst capped at 350.0 mm
        val cloudburst = WeatherUnitNormalizer.normalizePrecipitation(600.0, "mm")
        assertEquals(350.0, cloudburst, 0.01)
    }

    @Test
    fun testWindSpeedNormalization() {
        // Kilometers per hour
        val kmh = WeatherUnitNormalizer.normalizeWindSpeed(25.0, "km/h")
        assertEquals(25.0, kmh, 0.01)

        // Meters per second to km/h: 10 m/s = 36.0 km/h
        val fromMs = WeatherUnitNormalizer.normalizeWindSpeed(10.0, "m/s")
        assertEquals(36.0, fromMs, 0.1)

        // Knots to km/h: 20 knots = 37.04 km/h
        val fromKnots = WeatherUnitNormalizer.normalizeWindSpeed(20.0, "kn")
        assertEquals(37.0, fromKnots, 0.2)

        // Miles per hour to km/h: 20 mph = 32.19 km/h
        val fromMph = WeatherUnitNormalizer.normalizeWindSpeed(20.0, "mph")
        assertEquals(32.2, fromMph, 0.2)

        // Super cyclone upper bound clamp: 350 km/h
        val cyclone = WeatherUnitNormalizer.normalizeWindSpeed(500.0, "km/h")
        assertEquals(350.0, cyclone, 0.01)
    }

    @Test
    fun testPressureAndVisibilityNormalization() {
        // Pressure: inHg to hPa: 29.92 inHg = 1013.2 hPa
        val fromInHg = WeatherUnitNormalizer.normalizePressure(29.92, "inHg")
        assertEquals(1013.2, fromInHg, 0.5)

        // Visibility: meters to km: 8000 m = 8.0 km
        val fromMeters = WeatherUnitNormalizer.normalizeVisibility(8000.0, "m")
        assertEquals(8.0, fromMeters, 0.1)

        // Visibility heuristic (raw > 100): 12000.0 = 12.0 km
        val heuristicMeters = WeatherUnitNormalizer.normalizeVisibility(12000.0, null)
        assertEquals(12.0, heuristicMeters, 0.1)
    }

    @Test
    fun testMoshiJsonDeserializationAndMapping() {
        val sampleJson = """
            {
                "latitude": 28.61,
                "longitude": 77.20,
                "generationtime_ms": 0.12,
                "utc_offset_seconds": 19800,
                "timezone": "Asia/Kolkata",
                "elevation": 216.0,
                "current_units": {
                    "temperature_2m": "°C",
                    "relative_humidity_2m": "%",
                    "wind_speed_10m": "km/h",
                    "precipitation": "mm"
                },
                "current": {
                    "time": "2026-09-15T12:00",
                    "temperature_2m": 31.4,
                    "relative_humidity_2m": 68.0,
                    "apparent_temperature": 35.8,
                    "precipitation": 0.4,
                    "weather_code": 2,
                    "surface_pressure": 1008.2,
                    "wind_speed_10m": 14.5,
                    "wind_direction_10m": 120.0
                },
                "hourly_units": {
                    "temperature_2m": "°C",
                    "precipitation": "mm"
                },
                "hourly": {
                    "time": ["2026-09-15T12:00", "2026-09-15T13:00"],
                    "temperature_2m": [31.4, 32.1],
                    "relative_humidity_2m": [68.0, 64.0],
                    "apparent_temperature": [35.8, 36.2],
                    "precipitation_probability": [20, 35],
                    "precipitation": [0.4, 1.2],
                    "weather_code": [2, 61],
                    "surface_pressure": [1008.2, 1007.8],
                    "wind_speed_10m": [14.5, 16.2],
                    "wind_direction_10m": [120.0, 130.0]
                },
                "daily_units": {
                    "temperature_2m_max": "°C",
                    "temperature_2m_min": "°C"
                },
                "daily": {
                    "time": ["2026-09-15"],
                    "weather_code": [61],
                    "temperature_2m_max": [34.5],
                    "temperature_2m_min": [25.2],
                    "precipitation_sum": [4.8],
                    "precipitation_probability_max": [45],
                    "wind_speed_10m_max": [22.0]
                }
            }
        """.trimIndent()

        val adapter = NwpNetworkClient.moshi.adapter(OpenMeteoNwpResponse::class.java)
        val response = adapter.fromJson(sampleJson)

        assertTrue(response != null)
        assertEquals(28.61, response!!.latitude!!, 0.01)

        val normalizedCurrent = WeatherUnitNormalizer.normalizeCurrentWeather(response)
        assertEquals(31.4, normalizedCurrent.temperatureC, 0.01)
        assertEquals(68, normalizedCurrent.relativeHumidityPercent)
        assertEquals(14.5, normalizedCurrent.windSpeedKmh, 0.01)
        assertEquals("Partly Cloudy", normalizedCurrent.conditionDescription)

        val normalizedHourly = WeatherUnitNormalizer.normalizeHourlyForecast(response)
        assertEquals(2, normalizedHourly.size)
        assertEquals(31.4, normalizedHourly[0].temperatureC, 0.01)
        assertEquals(32.1, normalizedHourly[1].temperatureC, 0.01)

        val normalizedDaily = WeatherUnitNormalizer.normalizeDailyForecast(response)
        assertEquals(1, normalizedDaily.size)
        assertEquals(34.5, normalizedDaily[0].maxTempC, 0.01)
        assertEquals(25.2, normalizedDaily[0].minTempC, 0.01)
        assertEquals(4.8, normalizedDaily[0].precipitationSumMm, 0.01)
    }
}
