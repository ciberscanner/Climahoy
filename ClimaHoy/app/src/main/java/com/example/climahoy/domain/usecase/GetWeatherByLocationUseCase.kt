package com.example.climahoy.domain.usecase

import com.example.climahoy.data.repository.WeatherRepository
import com.example.climahoy.domain.model.WeatherData
import javax.inject.Inject

class GetWeatherByLocationUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(lat: Double, lon: Double): WeatherData? {
        return repository.getWeatherByLocation(lat, lon)
    }
}