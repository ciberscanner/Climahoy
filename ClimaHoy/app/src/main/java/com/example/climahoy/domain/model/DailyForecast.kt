package com.example.climahoy.domain.model

data class DailyForecast(
    val date: String,
    val dayOfWeek: String,
    val maxTemp: Double,
    val minTemp: Double,
    val condition: String,
    val iconUrl: String,
    val chanceOfRain: Int,
    val sunrise: String,
    val sunset: String,
    val moonPhase: String
)