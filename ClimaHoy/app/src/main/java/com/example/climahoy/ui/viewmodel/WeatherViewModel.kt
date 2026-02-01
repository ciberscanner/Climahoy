package com.example.climahoy.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.climahoy.data.model.CityResponse
import com.example.climahoy.data.model.ForecastDay
import com.example.climahoy.data.network.ApiException
import com.example.climahoy.domain.model.WeatherData
import com.example.climahoy.domain.usecase.GetCityUseCase
import com.example.climahoy.domain.usecase.GetForecastUseCase
import com.example.climahoy.domain.usecase.GetWeatherByCityUseCase
import com.example.climahoy.domain.usecase.GetWeatherByLocationUseCase
import com.example.climahoy.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okio.IOException
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(private val getWeatherByLocationUseCase: GetWeatherByLocationUseCase,
                                           private val getWeatherByCityUseCase: GetWeatherByCityUseCase,
                                           private val getCityUseCase: GetCityUseCase,
                                           private val getForecastUseCase: GetForecastUseCase) : ViewModel() {
    //----------------------------------------------------------------------------------------------
    // Variables
    private val _weatherData = MutableLiveData<UiState<WeatherData>>()
    val weatherData: LiveData<UiState<WeatherData>> = _weatherData

    private val _searchResults = MutableLiveData<UiState<List<CityResponse>>>()
    val searchResults: LiveData<UiState<List<CityResponse>>> = _searchResults

    private val _forecastState = MutableLiveData<UiState<List<ForecastDay>>>()
    val forecastState: LiveData<UiState<List<ForecastDay>>> = _forecastState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _useCelsius = MutableStateFlow(true)
    val useCelsius = _useCelsius.asStateFlow()
    //----------------------------------------------------------------------------------------------
    //
    fun getWeatherByLocation(lat: Double, lon: Double){
        viewModelScope.launch {
            try {
                val response = getWeatherByLocationUseCase(lat, lon)
                if (response != null) {
                    _weatherData.postValue(UiState.Success(response, ""))
                } else {
                    _weatherData.postValue(UiState.Error("No response from server"))
                }
            }catch (e: ApiException) {
                Timber.e("API Error: ${e.message}")
            } catch (e: IOException) {
                Timber.e("Network Error: ${e.message}")
            } catch (e: Exception) {
                Timber.e("Unexpected Error: ${e.message}")
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    //
    fun getWeatherByCity(city: String) {
        viewModelScope.launch {
            try {
                val response = getWeatherByCityUseCase(city)
                if (response != null) {
                    _weatherData.postValue(UiState.Success(response, ""))
                } else {
                    _weatherData.postValue(UiState.Error("No response from server"))
                }
            }catch (e: ApiException) {
                Timber.e("API Error: ${e.message}")
            } catch (e: IOException) {
                Timber.e("Network Error: ${e.message}")
            } catch (e: Exception) {
                Timber.e("Unexpected Error: ${e.message}")
            }
        }
    }

    fun toggleTemperatureUnit() {
        _useCelsius.value = !_useCelsius.value
    }

    fun searchCities(query: String) {
        viewModelScope.launch {
            if (query.length >= 2) {
                try {
                    val response = getCityUseCase(query)
                    if (response != null) {
                        _searchResults.postValue(UiState.Success(response, ""))
                    } else {
                        _searchResults.postValue(UiState.Error("No response from server"))
                    }
                }catch (e: ApiException) {
                    Timber.e("API Error: ${e.message}")
                } catch (e: IOException) {
                    Timber.e("Network Error: ${e.message}")
                } catch (e: Exception) {
                    Timber.e("Unexpected Error: ${e.message}")
                }
            }else{
                _searchResults.postValue(UiState.Success(emptyList(), ""))
            }
        }
    }

    fun getForecast(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val response = getForecastUseCase(latitude, longitude)
                if (response != null) {
                    _forecastState.postValue(UiState.Success(response, ""))
                } else {
                    _forecastState.postValue(UiState.Error("No response from server"))
                }
            }catch (e: ApiException) {
                Timber.e("API Error: ${e.message}")
            } catch (e: IOException) {
                Timber.e("Network Error: ${e.message}")
            } catch (e: Exception) {
                Timber.e("Unexpected Error: ${e.message}")
            }
        }
    }
}