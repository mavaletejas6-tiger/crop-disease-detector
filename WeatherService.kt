package com.example.service

import com.example.model.WeatherOverlayData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class CitySearchResult(
    val name: String,
    val country: String,
    val adminRegion: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String
        get() = if (adminRegion.isNotBlank() && country.isNotBlank()) {
            "$name, $adminRegion ($country)"
        } else if (country.isNotBlank()) {
            "$name, $country"
        } else {
            name
        }
}

class WeatherService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun searchCitiesOnline(query: String): List<CitySearchResult> = withContext(Dispatchers.IO) {
        if (query.trim().length < 2) return@withContext emptyList()
        try {
            val encoded = java.net.URLEncoder.encode(query.trim(), "UTF-8")
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=12&language=en&format=json"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                val results = json.optJSONArray("results") ?: return@withContext emptyList()
                val list = mutableListOf<CitySearchResult>()
                for (i in 0 until results.length()) {
                    val item = results.getJSONObject(i)
                    val name = item.optString("name", "")
                    val country = item.optString("country", "")
                    val admin = item.optString("admin1", "")
                    val lat = item.optDouble("latitude", 0.0)
                    val lon = item.optDouble("longitude", 0.0)
                    if (name.isNotBlank()) {
                        list.add(CitySearchResult(name, country, admin, lat, lon))
                    }
                }
                list
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchCurrentWeather(latitude: Double, longitude: Double): WeatherOverlayData {
        return fetchCurrentWeatherWithCity(latitude, longitude, null)
    }

    suspend fun fetchCurrentWeatherWithCity(
        latitude: Double,
        longitude: Double,
        overrideCityName: String?
    ): WeatherOverlayData = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m,wind_direction_10m,surface_pressure"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val jsonString = response.body?.string() ?: ""
                val root = JSONObject(jsonString)
                val current = root.getJSONObject("current")

                val temp = current.optDouble("temperature_2m", 25.0)
                val appTemp = current.optDouble("apparent_temperature", temp)
                val humidity = current.optInt("relative_humidity_2m", 55)
                val rain = current.optDouble("precipitation", 0.0)
                val windSpeed = current.optDouble("wind_speed_10m", 10.0)
                val windDir = current.optInt("wind_direction_10m", 180)
                val pressure = current.optDouble("surface_pressure", 1013.0)
                val code = current.optInt("weather_code", 0)

                val resolvedCity = overrideCityName ?: lookupNearestCity(latitude, longitude)

                WeatherOverlayData(
                    temperatureC = temp,
                    apparentTemperatureC = appTemp,
                    relativeHumidityPercent = humidity,
                    rainfallMm = rain,
                    windSpeedKmh = windSpeed,
                    windDirectionDegrees = windDir,
                    surfacePressureHpa = pressure,
                    weatherCode = code,
                    conditionDescription = WeatherOverlayData.getWeatherDescription(code),
                    latitude = latitude,
                    longitude = longitude,
                    cityName = resolvedCity,
                    locationName = "$resolvedCity ($latitude, $longitude)",
                    isLiveFromNetwork = true,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                fallbackData(latitude, longitude, overrideCityName)
            }
        } catch (e: Exception) {
            fallbackData(latitude, longitude, overrideCityName)
        }
    }

    private fun fallbackData(
        latitude: Double,
        longitude: Double,
        overrideCityName: String? = null
    ): WeatherOverlayData {
        val resolvedCity = overrideCityName ?: lookupNearestCity(latitude, longitude)
        return WeatherOverlayData(
            temperatureC = 22.5,
            apparentTemperatureC = 23.2,
            relativeHumidityPercent = 60,
            rainfallMm = 0.0,
            windSpeedKmh = 10.5,
            windDirectionDegrees = 160,
            surfacePressureHpa = 1014.0,
            weatherCode = 1,
            conditionDescription = "Partly Cloudy (Field Station)",
            latitude = latitude,
            longitude = longitude,
            cityName = resolvedCity,
            locationName = "$resolvedCity ($latitude, $longitude)",
            isLiveFromNetwork = false,
            timestamp = System.currentTimeMillis()
        )
    }

    fun lookupNearestCity(lat: Double, lon: Double): String {
        val knownCities = listOf(
            Triple("Des Moines, USA (Corn Belt)", 41.5868, -93.6250),
            Triple("Paris, France (Wheat Plains)", 48.8566, 2.3522),
            Triple("Ludhiana, India (Punjab Grain Hub)", 30.9010, 75.8573),
            Triple("São Paulo, Brazil (Cerrado Belt)", -23.5505, -46.6333),
            Triple("Kyiv, Ukraine (Black Sea Steppe)", 50.4501, 30.5234),
            Triple("Winnipeg, Canada (Prairie Belt)", 49.8951, -97.1384),
            Triple("Buenos Aires, Argentina (Pampas)", -34.6037, -58.3816),
            Triple("Nairobi, Kenya (Highlands)", -1.2921, 36.8219),
            Triple("Tokyo, Japan (Kanto Plain)", 35.6762, 139.6503),
            Triple("Melbourne, Australia (Wheat Belt)", -37.8136, 144.9631),
            Triple("Cairo, Egypt (Nile Delta)", 30.0444, 31.2357),
            Triple("London, UK (East Anglia Fields)", 51.5074, -0.1278),
            Triple("Berlin, Germany (Brandenburg)", 52.5200, 13.4050),
            Triple("Beijing, China (North China Plain)", 39.9042, 116.4074),
            Triple("San Francisco, USA (Central Valley)", 37.7749, -122.4194)
        )

        var bestCity = "Global Agricultural Point"
        var bestDist = Double.MAX_VALUE
        for ((name, cLat, cLon) in knownCities) {
            val dLat = lat - cLat
            val dLon = lon - cLon
            val dist = dLat * dLat + dLon * dLon
            if (dist < bestDist) {
                bestDist = dist
                bestCity = name
            }
        }
        return bestCity
    }
}
