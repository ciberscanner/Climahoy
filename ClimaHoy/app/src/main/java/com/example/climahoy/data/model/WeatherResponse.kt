package com.example.climahoy.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("location")
    val location: Location,

    @SerializedName("current")
    val current: CurrentWeather,

    @SerializedName("forecast")
    val forecast: Forecast? // Opcional para pronóstico extendido
)

data class Location(
    @SerializedName("name")
    val name: String,

    @SerializedName("region")
    val region: String,

    @SerializedName("country")
    val country: String,

    @SerializedName("lat")
    val lat: Double,

    @SerializedName("lon")
    val lon: Double,

    @SerializedName("tz_id")
    val timezone: String,

    @SerializedName("localtime")
    val localTime: String,

    @SerializedName("localtime_epoch")
    val localTimeEpoch: Long
)

data class CurrentWeather(
    @SerializedName("temp_c")
    val tempCelsius: Double,

    @SerializedName("temp_f")
    val tempFahrenheit: Double,

    @SerializedName("condition")
    val condition: Condition,

    @SerializedName("wind_kph")
    val windKph: Double,

    @SerializedName("wind_degree")
    val windDegree: Int,

    @SerializedName("wind_dir")
    val windDirection: String,

    @SerializedName("pressure_mb")
    val pressureMb: Double,

    @SerializedName("pressure_in")
    val pressureIn: Double,

    @SerializedName("precip_mm")
    val precipitationMm: Double,

    @SerializedName("precip_in")
    val precipitationIn: Double,

    @SerializedName("humidity")
    val humidity: Int,

    @SerializedName("cloud")
    val cloud: Int,

    @SerializedName("feelslike_c")
    val feelsLikeC: Double,

    @SerializedName("feelslike_f")
    val feelsLikeF: Double,

    @SerializedName("vis_km")
    val visibilityKm: Double,

    @SerializedName("vis_miles")
    val visibilityMiles: Double,

    @SerializedName("uv")
    val uvIndex: Double,

    @SerializedName("gust_kph")
    val gustKph: Double,

    @SerializedName("gust_mph")
    val gustMph: Double,

    @SerializedName("last_updated")
    val lastUpdated: String,

    @SerializedName("last_updated_epoch")
    val lastUpdatedEpoch: Long,

    @SerializedName("is_day")
    val isDay: Int // 1 = día, 0 = noche
)

data class Condition(
    @SerializedName("text")
    val text: String,

    @SerializedName("icon")
    val icon: String,

    @SerializedName("code")
    val code: Int
)

data class Forecast(
    @SerializedName("forecastday")
    val forecastDays: List<ForecastDay>
)

data class ForecastDay(
    @SerializedName("date")
    val date: String,

    @SerializedName("date_epoch")
    val dateEpoch: Long,

    @SerializedName("day")
    val day: DayForecast,

    @SerializedName("astro")
    val astro: Astro,

    @SerializedName("hour")
    val hours: List<HourForecast>
)

data class DayForecast(
    @SerializedName("maxtemp_c")
    val maxTempC: Double,

    @SerializedName("maxtemp_f")
    val maxTempF: Double,

    @SerializedName("mintemp_c")
    val minTempC: Double,

    @SerializedName("mintemp_f")
    val minTempF: Double,

    @SerializedName("avgtemp_c")
    val avgTempC: Double,

    @SerializedName("avgtemp_f")
    val avgTempF: Double,

    @SerializedName("maxwind_kph")
    val maxWindKph: Double,

    @SerializedName("totalprecip_mm")
    val totalPrecipMm: Double,

    @SerializedName("totalprecip_in")
    val totalPrecipIn: Double,

    @SerializedName("avgvis_km")
    val avgVisKm: Double,

    @SerializedName("avgvis_miles")
    val avgVisMiles: Double,

    @SerializedName("avghumidity")
    val avgHumidity: Double,

    @SerializedName("condition")
    val condition: Condition,

    @SerializedName("uv")
    val uv: Double
)

data class Astro(
    @SerializedName("sunrise")
    val sunrise: String,

    @SerializedName("sunset")
    val sunset: String,

    @SerializedName("moonrise")
    val moonrise: String,

    @SerializedName("moonset")
    val moonset: String,

    @SerializedName("moon_phase")
    val moonPhase: String,

    @SerializedName("moon_illumination")
    val moonIllumination: String
)

data class HourForecast(
    @SerializedName("time")
    val time: String,

    @SerializedName("temp_c")
    val tempC: Double,

    @SerializedName("temp_f")
    val tempF: Double,

    @SerializedName("condition")
    val condition: Condition,

    @SerializedName("wind_kph")
    val windKph: Double,

    @SerializedName("wind_degree")
    val windDegree: Int,

    @SerializedName("wind_dir")
    val windDirection: String,

    @SerializedName("pressure_mb")
    val pressureMb: Double,

    @SerializedName("precip_mm")
    val precipMm: Double,

    @SerializedName("humidity")
    val humidity: Int,

    @SerializedName("cloud")
    val cloud: Int,

    @SerializedName("feelslike_c")
    val feelsLikeC: Double,

    @SerializedName("feelslike_f")
    val feelsLikeF: Double,

    @SerializedName("vis_km")
    val visKm: Double,

    @SerializedName("gust_kph")
    val gustKph: Double,

    @SerializedName("chance_of_rain")
    val chanceOfRain: Int,

    @SerializedName("chance_of_snow")
    val chanceOfSnow: Int,

    @SerializedName("is_day")
    val isDay: Int
)