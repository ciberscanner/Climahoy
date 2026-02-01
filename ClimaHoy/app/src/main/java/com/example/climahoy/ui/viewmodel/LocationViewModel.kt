package com.example.climahoy.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.climahoy.utils.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationManager: LocationManager
) : ViewModel() {

    private val _currentLocation = MutableLiveData<android.location.Location?>()
    val currentLocation: LiveData<android.location.Location?> = _currentLocation

    private val _coordinates = MutableLiveData<Pair<Double, Double>?>()
    val coordinates: LiveData<Pair<Double, Double>?> = _coordinates

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _hasPermission = MutableLiveData<Boolean>()
    val hasPermission: LiveData<Boolean> = _hasPermission

    init {
        checkPermission()
    }

    fun checkPermission() {
        _hasPermission.value = locationManager.hasLocationPermission()
    }

    fun getCurrentLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val location = locationManager.getCurrentLocation()
                _currentLocation.value = location

                location?.let {
                    _coordinates.value = Pair(it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al obtener ubicación"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getLastKnownLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = locationManager.getLastKnownLocation()
            result.fold(
                onSuccess = { location ->
                    _currentLocation.value = location
                    _coordinates.value = Pair(location.latitude, location.longitude)
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Error al obtener ubicación"
                }
            )

            _isLoading.value = false
        }
    }

    fun getFreshLocation() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = locationManager.getFreshLocation()
            result.fold(
                onSuccess = { location ->
                    _currentLocation.value = location
                    _coordinates.value = Pair(location.latitude, location.longitude)
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Error al obtener ubicación"
                }
            )

            _isLoading.value = false
        }
    }

    fun getCoordinates() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val result = locationManager.getCurrentCoordinatesResult()
            result.fold(
                onSuccess = { coords ->
                    _coordinates.value = coords
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "Error al obtener coordenadas"
                }
            )

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
}