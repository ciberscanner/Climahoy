package com.example.climahoy.domain.usecase

import com.example.climahoy.data.repository.WeatherRepository
import com.example.climahoy.domain.model.WeatherData
import javax.inject.Inject

class GetWeatherByCityUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(city: String): WeatherData? {
        return repository.getWeatherByCity(city)
    }
}