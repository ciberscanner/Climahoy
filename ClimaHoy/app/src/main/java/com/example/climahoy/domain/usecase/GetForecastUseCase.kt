package com.example.climahoy.domain.usecase

import com.example.climahoy.data.model.ForecastDay
import com.example.climahoy.data.repository.WeatherRepository
import javax.inject.Inject

class GetForecastUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(latitude: Double, longitude: Double, days: Int = 3): List<ForecastDay>? {
        return repository.getForecast(latitude, longitude, days)
    }
}