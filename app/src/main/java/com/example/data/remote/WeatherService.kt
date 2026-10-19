package com.example.data.remote

import com.example.data.model.WeatherCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class WeatherService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchWeatherForCity(cityName: String): WeatherCache? = withContext(Dispatchers.IO) {
        try {
            // Step 1: Geocode city name to lat & lon
            val cleanCity = cityName.split(",").firstOrNull()?.trim() ?: cityName.trim()
            val encodedCity = URLEncoder.encode(cleanCity, "UTF-8")
            val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedCity&count=1&language=en&format=json"

            val geoRequest = Request.Builder().url(geoUrl).build()
            val geoResponse = client.newCall(geoRequest).execute()
            val geoBody = geoResponse.body?.string() ?: return@withContext null

            val geoJson = JSONObject(geoBody)
            if (!geoJson.has("results")) return@withContext null
            val resultsArray = geoJson.getJSONArray("results")
            if (resultsArray.length() == 0) return@withContext null

            val firstResult = resultsArray.getJSONObject(0)
            val latitude = firstResult.getDouble("latitude")
            val longitude = firstResult.getDouble("longitude")

            // Step 2: Fetch weather forecast
            fetchWeatherForCoordinates(cityName, latitude, longitude)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun fetchWeatherForCoordinates(
        locationName: String,
        latitude: Double,
        longitude: Double
    ): WeatherCache? = withContext(Dispatchers.IO) {
        try {
            val forecastUrl = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min&timezone=auto"

            val request = Request.Builder().url(forecastUrl).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null

            val json = JSONObject(body)
            val current = json.getJSONObject("current")
            val temp = current.getDouble("temperature_2m")
            val humidity = current.optInt("relative_humidity_2m", 50)
            val weatherCode = current.getInt("weather_code")
            val windSpeed = current.optDouble("wind_speed_10m", 0.0)

            var highTemp = temp
            var lowTemp = temp
            if (json.has("daily")) {
                val daily = json.getJSONObject("daily")
                val maxArray = daily.optJSONArray("temperature_2m_max")
                val minArray = daily.optJSONArray("temperature_2m_min")
                if (maxArray != null && maxArray.length() > 0) {
                    highTemp = maxArray.getDouble(0)
                }
                if (minArray != null && minArray.length() > 0) {
                    lowTemp = minArray.getDouble(0)
                }
            }

            val condition = getWeatherCondition(weatherCode)
            val suggestion = generateSmartSuggestion(weatherCode, temp)

            WeatherCache(
                locationKey = locationName,
                temperature = temp,
                weatherCode = weatherCode,
                weatherCondition = condition,
                windSpeed = windSpeed,
                humidity = humidity,
                highTemp = highTemp,
                lowTemp = lowTemp,
                smartSuggestion = suggestion,
                lastUpdated = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getWeatherCondition(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Light Drizzle"
            56, 57 -> "Freezing Drizzle"
            61 -> "Slight Rain"
            63 -> "Moderate Rain"
            65 -> "Heavy Rain"
            66, 67 -> "Freezing Rain"
            71, 73, 75, 77 -> "Snow Fall"
            80, 81, 82 -> "Rain Showers"
            85, 86 -> "Snow Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Fair"
        }
    }

    private fun generateSmartSuggestion(code: Int, temp: Double): String {
        val isRainy = code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)
        val isSnowy = code in listOf(71, 73, 75, 77, 85, 86)

        return when {
            isSnowy -> "❄️ Freezing snow expected! Pack thermal underwear, snow-rated boots, and heavy insulated coat."
            isRainy -> "🌧️ Rain or showers forecast! Remember a compact umbrella, waterproof rain shell, and dry bag."
            temp >= 28.0 -> "☀️ Hot & sunny! Don't forget sunscreen SPF 50+, sunglasses, sun hat, and lightweight linen wear."
            temp in 18.0..27.0 -> "✨ Pleasant & comfortable weather! Ideal for walking tours, outdoor dining, and sightseeing."
            temp in 10.0..17.0 -> "🍂 Mild to brisk temperature. Pack a light windbreaker or fleece layer for mornings & evenings."
            else -> "🧣 Cold conditions! Pack warm knit sweaters, thermal socks, and a winter jacket."
        }
    }
}
