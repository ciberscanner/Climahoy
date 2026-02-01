package com.example.climahoy.data.repository

import com.example.climahoy.data.network.ErrorAPI
import com.example.climahoy.data.model.CityResponse
import com.example.climahoy.data.model.ForecastDay
import com.example.climahoy.data.network.ApiException
import com.example.climahoy.data.network.WeatherApiService
import com.example.climahoy.domain.model.WeatherData
import com.example.climahoy.domain.model.toWeatherData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(private val apiService: WeatherApiService, private val apiKey: String) {
    suspend fun getWeatherByLocation(lat: Double, lon: Double):WeatherData?{
        return withContext(Dispatchers.IO){
            val query = "$lat,$lon"
            val response = apiService.getCurrentWeather(apiKey, query)
            when {
                response.isSuccessful ->  response.body()?.toWeatherData()
                else -> {
                    val errorBody = response.errorBody()?.string()
                    val errorApi = errorBody?.let { errorJson ->
                        try {
                            Gson().fromJson(errorJson, ErrorAPI::class.java)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    throw ApiException(code = response.code(), message = response.message(), error = errorApi)
                }
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    //
    suspend fun getWeatherByCity(city: String): WeatherData? {
        return withContext(Dispatchers.IO){
            val response = apiService.getCurrentWeather(apiKey, city)
            when {
                response.isSuccessful ->  response.body()?.toWeatherData()
                else -> {
                    val errorBody = response.errorBody()?.string()
                    val errorApi = errorBody?.let { errorJson ->
                        try {
                            Gson().fromJson(errorJson, ErrorAPI::class.java)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    throw ApiException(code = response.code(), message = response.message(), error = errorApi)
                }
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    //
    suspend fun getCities(query: String): List<CityResponse>? {
        return withContext(Dispatchers.IO){
            val response = apiService.getCities(apiKey, query)
            when {
                response.isSuccessful ->  response.body()
                else -> {
                    val errorBody = response.errorBody()?.string()
                    val errorApi = errorBody?.let { errorJson ->
                        try {
                            Gson().fromJson(errorJson, ErrorAPI::class.java)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    throw ApiException(code = response.code(), message = response.message(), error = errorApi)
                }
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    //
    suspend fun getForecast(latitude: Double, longitude: Double, days: Int = 3): List<ForecastDay>? {
        return withContext(Dispatchers.IO) {
            val query = "$latitude,$longitude"
            val response = apiService.getForecast(
                apiKey = apiKey,
                query = query,
                days = days,
                language = "es"
            )

            when {
                response.isSuccessful -> {
                    response.body()?.forecast?.forecastDays?.mapIndexed { index, forecastDay ->
                        ForecastDay(
                            date = forecastDay.date,
                            dateEpoch = forecastDay.dateEpoch,
                            day = forecastDay.day,
                            astro = forecastDay.astro,
                            hours = forecastDay.hours
                        )
                    }
                }
                else -> {
                    val errorBody = response.errorBody()?.string()
                    val errorApi = errorBody?.let { errorJson ->
                        try {
                            Gson().fromJson(errorJson, ErrorAPI::class.java)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    throw ApiException(code = response.code(), message = response.message(), error = errorApi)
                }
            }
        }
    }
}