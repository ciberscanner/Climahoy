package com.example.climahoy.domain.usecase

import com.example.climahoy.data.model.CityResponse
import com.example.climahoy.data.repository.WeatherRepository
import javax.inject.Inject

class GetCityUseCase @Inject constructor(private val repository: WeatherRepository) {
    suspend operator fun invoke(city: String): List<CityResponse>? {
        return repository.getCities(city)
    }
}