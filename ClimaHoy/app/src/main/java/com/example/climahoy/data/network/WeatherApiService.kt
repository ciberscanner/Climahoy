package com.example.climahoy.data.network

import com.example.climahoy.data.model.CityResponse
import com.example.climahoy.data.model.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("aqi") aqi: String = "no",
        @Query("days") days: Int = 3,
        @Query("lang") language: String = "es"
    ): Response<WeatherResponse>

    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("days") days: Int = 3,
        @Query("aqi") airQuality: String = "no",
        @Query("alerts") alerts: String = "no",
        @Query("lang") language: String = "es"
    ): Response<WeatherResponse>

    @GET("search.json")
    suspend fun getCities(
        @Query("key") apiKey: String,
        @Query("q") query: String
    ): Response<List<CityResponse>>
}