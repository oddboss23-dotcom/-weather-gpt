package com.example.data.repository

import com.example.data.local.WeatherCacheEntity
import com.example.data.local.WeatherDao
import com.example.data.model.DailyForecast
import com.example.data.model.HourlyForecast
import com.example.data.model.WeatherData
import com.example.data.remote.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository(private val weatherDao: WeatherDao? = null) {

    private val api = NetworkClient.openMeteoService

    suspend fun fetchWeather(
        cityName: String,
        stateName: String,
        lat: Double,
        lon: Double
    ): Triple<WeatherData, List<HourlyForecast>, List<DailyForecast>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getForecast(latitude = lat, longitude = lon)
            val current = response.current
            val hourly = response.hourly
            val daily = response.daily

            val wCode = current?.weatherCode ?: 0
            val condition = getWeatherCondition(wCode)
            val windDirDeg = current?.windDirection10m?.toInt() ?: 0
            val windDirText = getWindDirectionText(windDirDeg)

            // Current Weather
            val weatherData = WeatherData(
                cityName = cityName,
                stateOrRegion = stateName,
                country = "India",
                latitude = lat,
                longitude = lon,
                temperatureC = current?.temperature2m ?: 32.0,
                feelsLikeC = current?.apparentTemperature ?: 35.0,
                tempMinC = daily?.temperature2mMin?.firstOrNull() ?: 24.0,
                tempMaxC = daily?.temperature2mMax?.firstOrNull() ?: 35.0,
                humidityPercent = current?.relativeHumidity2m?.toInt() ?: 65,
                windSpeedKmh = current?.windSpeed10m ?: 15.0,
                windDirectionDeg = windDirDeg,
                windDirectionText = windDirText,
                pressureHpa = current?.surfacePressure ?: 1010.0,
                uvIndex = current?.uvIndex ?: 6.5,
                rainProbabilityPercent = daily?.precipitationProbabilityMax?.firstOrNull() ?: 45,
                expectedRainfallMm = daily?.precipitationSum?.firstOrNull() ?: 5.0,
                visibilityKm = 8.0,
                sunriseTime = formatIsoTime(daily?.sunrise?.firstOrNull(), "05:48 AM"),
                sunsetTime = formatIsoTime(daily?.sunset?.firstOrNull(), "06:54 PM"),
                weatherCode = wCode,
                conditionDescription = condition,
                lastUpdated = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
                dataSource = "Open-Meteo Meteorological Numerical Grid",
                isRealTimeConnected = true
            )

            // Process 48 hourly forecast points
            val hourlyList = mutableListOf<HourlyForecast>()
            val times = hourly?.time ?: emptyList()
            val temps = hourly?.temperature2m ?: emptyList()
            val rainProbs = hourly?.precipitationProbability ?: emptyList()
            val precips = hourly?.precipitation ?: emptyList()
            val humidities = hourly?.relativeHumidity2m ?: emptyList()
            val winds = hourly?.windSpeed10m ?: emptyList()
            val windDirs = hourly?.windDirection10m ?: emptyList()
            val pressures = hourly?.surfacePressure ?: emptyList()
            val codes = hourly?.weatherCode ?: emptyList()

            val count = minOf(times.size, 48)
            for (i in 0 until count) {
                val rawTime = times.getOrNull(i) ?: ""
                val hourLabel = formatHourLabel(rawTime, i)
                val hourOfDay = (i % 24)
                hourlyList.add(
                    HourlyForecast(
                        timeLabel = hourLabel,
                        hourOfDay = hourOfDay,
                        temperatureC = temps.getOrNull(i) ?: 30.0,
                        rainProbabilityPercent = rainProbs.getOrNull(i) ?: 20,
                        precipitationMm = precips.getOrNull(i) ?: 0.0,
                        humidityPercent = humidities.getOrNull(i)?.toInt() ?: 60,
                        windSpeedKmh = winds.getOrNull(i) ?: 12.0,
                        windDirectionDeg = windDirs.getOrNull(i)?.toInt() ?: 180,
                        pressureHpa = pressures.getOrNull(i) ?: 1012.0,
                        weatherCode = codes.getOrNull(i) ?: 1
                    )
                )
            }

            // Process Daily Forecast
            val dailyList = mutableListOf<DailyForecast>()
            val dTimes = daily?.time ?: emptyList()
            val dMax = daily?.temperature2mMax ?: emptyList()
            val dMin = daily?.temperature2mMin ?: emptyList()
            val dRain = daily?.precipitationSum ?: emptyList()
            val dProb = daily?.precipitationProbabilityMax ?: emptyList()
            val dCodes = daily?.weatherCode ?: emptyList()

            for (i in dTimes.indices) {
                val dayName = if (i == 0) "Today" else if (i == 1) "Tomorrow" else formatDayName(dTimes[i])
                dailyList.add(
                    DailyForecast(
                        dateLabel = dTimes[i],
                        dayName = dayName,
                        maxTempC = dMax.getOrNull(i) ?: 34.0,
                        minTempC = dMin.getOrNull(i) ?: 25.0,
                        rainProbabilityPercent = dProb.getOrNull(i) ?: 30,
                        precipitationMm = dRain.getOrNull(i) ?: 0.0,
                        conditionDescription = getWeatherCondition(dCodes.getOrNull(i) ?: 0),
                        weatherCode = dCodes.getOrNull(i) ?: 0
                    )
                )
            }

            // Cache to database
            weatherDao?.cacheWeather(
                WeatherCacheEntity(
                    locationKey = "$cityName-$stateName",
                    cityName = cityName,
                    temperatureC = weatherData.temperatureC,
                    feelsLikeC = weatherData.feelsLikeC,
                    tempMinC = weatherData.tempMinC,
                    tempMaxC = weatherData.tempMaxC,
                    humidity = weatherData.humidityPercent,
                    windSpeed = weatherData.windSpeedKmh,
                    windDirection = weatherData.windDirectionText,
                    pressure = weatherData.pressureHpa,
                    uvIndex = weatherData.uvIndex,
                    rainProb = weatherData.rainProbabilityPercent,
                    expectedRainfall = weatherData.expectedRainfallMm,
                    condition = weatherData.conditionDescription,
                    weatherCode = weatherData.weatherCode,
                    timestamp = System.currentTimeMillis()
                )
            )

            Triple(weatherData, hourlyList, dailyList)
        } catch (e: Exception) {
            // Fallback to cached or robust meteorological default with transparency tag
            val cached = weatherDao?.getCachedWeather("$cityName-$stateName")
            val fallbackWeather = if (cached != null) {
                WeatherData(
                    cityName = cached.cityName,
                    stateOrRegion = stateName,
                    latitude = lat,
                    longitude = lon,
                    temperatureC = cached.temperatureC,
                    feelsLikeC = cached.feelsLikeC,
                    tempMinC = cached.tempMinC,
                    tempMaxC = cached.tempMaxC,
                    humidityPercent = cached.humidity,
                    windSpeedKmh = cached.windSpeed,
                    windDirectionText = cached.windDirection,
                    pressureHpa = cached.pressure,
                    uvIndex = cached.uvIndex,
                    rainProbabilityPercent = cached.rainProb,
                    expectedRainfallMm = cached.expectedRainfall,
                    conditionDescription = cached.condition,
                    weatherCode = cached.weatherCode,
                    lastUpdated = "Cached Data",
                    dataSource = "Cached Observation Data",
                    isRealTimeConnected = false
                )
            } else {
                generateDemoWeather(cityName, stateName, lat, lon)
            }

            val fallbackHourly = generateDemoHourly(fallbackWeather)
            val fallbackDaily = generateDemoDaily()
            Triple(fallbackWeather, fallbackHourly, fallbackDaily)
        }
    }

    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast Sky"
            45, 48 -> "Fog & Mist"
            51, 53, 55 -> "Drizzle & Light Rain"
            61, 63 -> "Moderate Rain Showers"
            65 -> "Heavy Rain Downpour"
            71, 73, 75 -> "Snowfall Occurring"
            80, 81, 82 -> "Torrential Rain Showers"
            95 -> "Thunderstorm & Lightning"
            96, 99 -> "Severe Thunderstorm with Hail"
            else -> "Partly Cloudy"
        }
    }

    private fun getWindDirectionText(deg: Int): String {
        return when {
            deg >= 337.5 || deg < 22.5 -> "N"
            deg < 67.5 -> "NE"
            deg < 112.5 -> "E"
            deg < 157.5 -> "SE"
            deg < 202.5 -> "S"
            deg < 247.5 -> "SW"
            deg < 292.5 -> "W"
            else -> "NW"
        }
    }

    private fun formatIsoTime(iso: String?, fallback: String): String {
        if (iso.isNullOrEmpty()) return fallback
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = parser.parse(iso)
            if (date != null) formatter.format(date) else fallback
        } catch (e: Exception) {
            fallback
        }
    }

    private fun formatHourLabel(iso: String, index: Int): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val formatter = SimpleDateFormat("h a", Locale.getDefault())
            val date = parser.parse(iso)
            if (date != null) formatter.format(date) else "${(index % 12) + 1} ${if (index < 12) "AM" else "PM"}"
        } catch (e: Exception) {
            "${(index % 12) + 1} ${if (index < 12) "AM" else "PM"}"
        }
    }

    private fun formatDayName(iso: String): String {
        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatter = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
            val date = parser.parse(iso)
            if (date != null) formatter.format(date) else iso
        } catch (e: Exception) {
            iso
        }
    }

    private fun generateDemoWeather(city: String, state: String, lat: Double, lon: Double): WeatherData {
        return WeatherData(
            cityName = city,
            stateOrRegion = state,
            latitude = lat,
            longitude = lon,
            temperatureC = 33.5,
            feelsLikeC = 36.8,
            tempMinC = 26.2,
            tempMaxC = 35.0,
            humidityPercent = 68,
            windSpeedKmh = 16.5,
            windDirectionDeg = 210,
            windDirectionText = "SSW",
            pressureHpa = 1008.5,
            uvIndex = 7.0,
            rainProbabilityPercent = 65,
            expectedRainfallMm = 12.0,
            conditionDescription = "Partly Cloudy with Humidity",
            weatherCode = 2,
            lastUpdated = "Demo / Simulated Dataset",
            dataSource = "Meteorological Demo Model (API Offline)",
            isRealTimeConnected = false
        )
    }

    private fun generateDemoHourly(base: WeatherData): List<HourlyForecast> {
        val list = mutableListOf<HourlyForecast>()
        val hours = listOf(
            "12 PM", "1 PM", "2 PM", "3 PM", "4 PM", "5 PM", "6 PM", "7 PM", "8 PM", "9 PM", "10 PM", "11 PM",
            "12 AM", "1 AM", "2 AM", "3 AM", "4 AM", "5 AM", "6 AM", "7 AM", "8 AM", "9 AM", "10 AM", "11 AM"
        )
        val tempCurve = listOf(
            34.0, 35.2, 35.8, 34.5, 33.2, 31.8, 30.2, 29.1, 28.3, 27.8, 27.2, 26.8,
            26.4, 26.0, 25.8, 25.5, 25.4, 26.2, 27.5, 29.0, 30.8, 32.2, 33.5, 34.2
        )
        val rainProbs = listOf(
            40, 55, 68, 75, 80, 65, 45, 30, 20, 15, 10, 10,
            10, 15, 20, 25, 30, 40, 50, 60, 65, 60, 50, 45
        )
        val rainMm = listOf(
            0.0, 1.2, 4.5, 6.8, 8.2, 3.1, 0.8, 0.0, 0.0, 0.0, 0.0, 0.0,
            0.0, 0.0, 0.0, 0.0, 0.0, 0.2, 0.5, 1.4, 2.0, 1.0, 0.2, 0.0
        )

        for (i in 0 until 48) {
            val mod = i % 24
            list.add(
                HourlyForecast(
                    timeLabel = hours[mod] + (if (i >= 24) " (+1d)" else ""),
                    hourOfDay = mod,
                    temperatureC = tempCurve[mod],
                    rainProbabilityPercent = rainProbs[mod],
                    precipitationMm = rainMm[mod],
                    humidityPercent = (60 + (i * 2) % 30),
                    windSpeedKmh = 14.0 + (i % 7),
                    windDirectionDeg = 210,
                    pressureHpa = 1008.0 + (i % 4),
                    weatherCode = if (rainProbs[mod] > 60) 61 else 2
                )
            )
        }
        return list
    }

    private fun generateDemoDaily(): List<DailyForecast> {
        return listOf(
            DailyForecast("2026-08-24", "Today", 35.0, 26.0, 68, 14.5, "Partly Cloudy with Showers", 61),
            DailyForecast("2026-08-25", "Tomorrow", 33.5, 25.2, 75, 22.0, "Moderate Rain Downpours", 63),
            DailyForecast("2026-08-26", "Wednesday", 31.0, 24.5, 80, 28.0, "Thunderstorm & Rain", 95),
            DailyForecast("2026-08-27", "Thursday", 32.0, 25.0, 45, 6.0, "Light Scattered Showers", 51),
            DailyForecast("2026-08-28", "Friday", 34.0, 26.2, 30, 1.2, "Mainly Sunny & Humid", 1),
            DailyForecast("2026-08-29", "Saturday", 35.2, 27.0, 25, 0.0, "Clear Sky", 0),
            DailyForecast("2026-08-30", "Sunday", 36.0, 27.5, 20, 0.0, "Hot & Sunny", 0)
        )
    }
}
