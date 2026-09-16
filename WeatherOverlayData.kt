package com.example.model

enum class SprayingStatus {
    OPTIMAL,
    MARGINAL,
    DO_NOT_SPRAY
}

enum class HarvestDryingCondition {
    EXCELLENT,
    FAVORABLE,
    POOR_HIGH_HUMIDITY
}

data class WeatherOverlayData(
    val temperatureC: Double = 26.4,
    val apparentTemperatureC: Double = 27.8,
    val relativeHumidityPercent: Int = 58,
    val rainfallMm: Double = 0.0,
    val windSpeedKmh: Double = 9.2,
    val windDirectionDegrees: Int = 145,
    val surfacePressureHpa: Double = 1013.2,
    val weatherCode: Int = 0,
    val conditionDescription: String = "Clear Sky & Sunshine",
    val latitude: Double = 41.5868,
    val longitude: Double = -93.6250,
    val cityName: String = "Des Moines (Grain Belt)",
    val locationName: String = "Plot Sensor Location",
    val isLiveFromNetwork: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) {
    val temperatureF: Double
        get() = (temperatureC * 9.0 / 5.0) + 32.0

    val formattedLatitude: String
        get() {
            val dir = if (latitude >= 0) "N" else "S"
            return "${"%.4f".format(Math.abs(latitude))}° $dir"
        }

    val formattedLongitude: String
        get() {
            val dir = if (longitude >= 0) "E" else "W"
            return "${"%.4f".format(Math.abs(longitude))}° $dir"
        }

    val coldnessIndex: String
        get() = when {
            temperatureC <= 0.0 -> "Freezing Frost (≤ 0°C)"
            temperatureC in 0.1..7.0 -> "Very Cold / Frost Alert"
            temperatureC in 7.1..15.0 -> "Chilly / Cool (8 - 15°C)"
            temperatureC in 15.1..24.0 -> "Mild & Temperate"
            temperatureC in 24.1..32.0 -> "Warm Growing Climate"
            else -> "Extreme Heat (> 32°C)"
        }

    val coldnessLevel: String
        get() = when {
            temperatureC <= 0.0 -> "Severe Frost (High Risk)"
            temperatureC in 0.1..7.0 -> "High Coldness (Chilly)"
            temperatureC in 7.1..15.0 -> "Moderate Coldness"
            temperatureC in 15.1..24.0 -> "Low Coldness (Comfortable)"
            else -> "Negligible (Warm)"
        }

    val windChillC: Double
        get() = if (temperatureC <= 10.0 && windSpeedKmh > 4.8) {
            13.12 + 0.6215 * temperatureC - 11.37 * Math.pow(windSpeedKmh, 0.16) + 0.3965 * temperatureC * Math.pow(windSpeedKmh, 0.16)
        } else {
            temperatureC
        }

    val sprayingStatus: SprayingStatus
        get() = when {
            windSpeedKmh > 20.0 || relativeHumidityPercent < 35 -> SprayingStatus.DO_NOT_SPRAY
            windSpeedKmh in 6.0..18.0 && relativeHumidityPercent in 45..75 -> SprayingStatus.OPTIMAL
            else -> SprayingStatus.MARGINAL
        }

    val sprayingStatusAdvice: String
        get() = when (sprayingStatus) {
            SprayingStatus.OPTIMAL -> "Optimal Spraying Window (Low drift risk, good droplet adherence)"
            SprayingStatus.MARGINAL -> "Caution: Sub-optimal drift or evaporation rate. Use anti-drift nozzles"
            SprayingStatus.DO_NOT_SPRAY -> "Do Not Spray! High wind drift (>20 km/h) or rapid droplet evaporation"
        }

    val dryingCondition: HarvestDryingCondition
        get() = when {
            relativeHumidityPercent < 55 && temperatureC >= 24.0 -> HarvestDryingCondition.EXCELLENT
            relativeHumidityPercent in 55..72 -> HarvestDryingCondition.FAVORABLE
            else -> HarvestDryingCondition.POOR_HIGH_HUMIDITY
        }

    val dryingAdvice: String
        get() = when (dryingCondition) {
            HarvestDryingCondition.EXCELLENT -> "Rapid Field Drying: Safe grain equilibrium moisture within 4-6 hours"
            HarvestDryingCondition.FAVORABLE -> "Moderate Drying: Monitor grain bulk moisture before bin storage"
            HarvestDryingCondition.POOR_HIGH_HUMIDITY -> "High Humidity Alert: Risk of re-wetting and fungal development"
        }

    companion object {
        fun getWeatherDescription(code: Int): String = when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Mainly Clear / Partly Cloudy"
            45, 48 -> "Fog / Rime Fog"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain Showers"
            71, 73, 75 -> "Snow Fall"
            80, 81, 82 -> "Heavy Rain Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Partly Cloudy"
        }
    }
}
