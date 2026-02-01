package com.example.climahoy.domain.model

import com.example.climahoy.data.model.WeatherResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WeatherData(
    val locationName: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val temperatureC: Double,
    val temperatureF: Double,
    val feelsLikeC: Double,
    val feelsLikeF: Double,
    val conditionText: String,
    val conditionIcon: String,
    val conditionCode: Int,
    val windKph: Double,
    val windDegree: Int,
    val windDirection: String,
    val pressureMb: Double,
    val precipitationMm: Double,
    val humidity: Int,
    val cloud: Int,
    val visibilityKm: Double,
    val uvIndex: Double,
    val gustKph: Double,
    val isDay: Boolean,
    val lastUpdated: String,
    val localTime: String,
    val timezone: String
) {
    fun getTemperatureWithUnit(useCelsius: Boolean = true): String {
        return if (useCelsius) "${temperatureC.toInt()}°C"
        else "${temperatureF.toInt()}°F"
    }
    fun getFeelsLikeFormatted(): String = "Sensación: ${feelsLikeC.toInt()}°C"

    fun getWindSpeedFormatted(): String = "${String.format("%.1f", windKph)} km/h"

    fun getHumidityFormatted(): String = "$humidity%"

    fun getPressureFormatted(): String = "${pressureMb.toInt()} mb"

    fun getVisibilityFormatted(): String = "${visibilityKm.toInt()} km"

    fun getPrecipitationFormatted(): String = "${String.format("%.1f", precipitationMm)} mm"

    fun getIconUrl(): String = "https:${conditionIcon}"

    fun getFormattedLocalTime(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(localTime)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            localTime
        }
    }

    fun getFormattedLastUpdated(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(lastUpdated)
            "Actualizado: ${outputFormat.format(date ?: Date())}"
        } catch (e: Exception) {
            "Actualizado: $lastUpdated"
        }
    }

    fun getUvIndexDescription(): String {
        return when {
            uvIndex <= 2 -> "Bajo"
            uvIndex <= 5 -> "Moderado"
            uvIndex <= 7 -> "Alto"
            uvIndex <= 10 -> "Muy alto"
            else -> "Extremo"
        }
    }
}

fun WeatherResponse.toWeatherData(): WeatherData {
    return WeatherData(
        locationName = this.location.name,
        region = this.location.region,
        country = this.location.country,
        latitude = this.location.lat,
        longitude = this.location.lon,
        temperatureC = this.current.tempCelsius,
        temperatureF = this.current.tempFahrenheit,
        feelsLikeC = this.current.feelsLikeC,
        feelsLikeF = this.current.feelsLikeF,
        conditionText = this.current.condition.text,
        conditionIcon = this.current.condition.icon,
        conditionCode = this.current.condition.code,
        windKph = this.current.windKph,
        windDegree = this.current.windDegree,
        windDirection = this.current.windDirection,
        pressureMb = this.current.pressureMb,
        precipitationMm = this.current.precipitationMm,
        humidity = this.current.humidity,
        cloud = this.current.cloud,
        visibilityKm = this.current.visibilityKm,
        uvIndex = this.current.uvIndex,
        gustKph = this.current.gustKph,
        isDay = this.current.isDay == 1,
        lastUpdated = this.current.lastUpdated,
        localTime = this.location.localTime,
        timezone = this.location.timezone
    )
}
fun WeatherResponse.toForecastWeatherDataList(): List<WeatherData> {
    return this.forecast?.forecastDays?.map { forecastDay ->
        WeatherData(
            locationName = this.location.name,
            region = this.location.region,
            country = this.location.country,
            latitude = this.location.lat,
            longitude = this.location.lon,
            temperatureC = this.current.tempCelsius,
            temperatureF = this.current.tempFahrenheit,
            feelsLikeC = this.current.feelsLikeC,
            feelsLikeF = this.current.feelsLikeF,
            conditionText = this.current.condition.text,
            conditionIcon = this.current.condition.icon,
            conditionCode = this.current.condition.code,
            windKph = this.current.windKph,
            windDegree = this.current.windDegree,
            windDirection = this.current.windDirection,
            pressureMb = this.current.pressureMb,
            precipitationMm = this.current.precipitationMm,
            humidity = this.current.humidity,
            cloud = this.current.cloud,
            visibilityKm = this.current.visibilityKm,
            uvIndex = this.current.uvIndex,
            gustKph = this.current.gustKph,
            isDay = this.current.isDay == 1,
            lastUpdated = this.current.lastUpdated,
            localTime = this.location.localTime,
            timezone = this.location.timezone
        )
    } ?: emptyList()
}