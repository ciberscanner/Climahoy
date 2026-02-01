package com.example.climahoy.ui

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.climahoy.R
import com.example.climahoy.data.model.CityResponse
import com.example.climahoy.data.model.ForecastDay
import com.example.climahoy.databinding.ActivityMainBinding
import com.example.climahoy.domain.model.WeatherData
import com.example.climahoy.ui.adapter.CitySearchAdapter
import com.example.climahoy.ui.viewmodel.LocationViewModel
import com.example.climahoy.ui.viewmodel.WeatherViewModel
import com.example.climahoy.utils.PermissionManager
import com.example.climahoy.utils.UiState
import com.example.climahoy.utils.extensions.applyWindowKeyboardInsetsTo
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    //----------------------------------------------------------------------------------------------
    // Variables
    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var permissionManager: PermissionManager
    private lateinit var permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
    private lateinit var searchAdapter: CitySearchAdapter
    //----------------------------------------------------------------------------------------------
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionManager = PermissionManager.with(this)
        setupPermissionLauncher()

        setupObservers()
        setupListeners()
        setupSearchCity()

        checkLocationPermission()
        applyWindowKeyboardInsetsTo(binding.root)
        binding.swipeRefreshLayout.isEnabled = true
    }
    //----------------------------------------------------------------------------------------------
    //
    private fun setupPermissionLauncher() {
        permissionLauncher = permissionManager.createPermissionLauncher(
            this,
            onGranted = {
                onPermissionGranted()
            },
            onDenied = { permanentlyDenied ->
                onPermissionDenied(permanentlyDenied)
            }
        )
    }
    //----------------------------------------------------------------------------------------------
    //
    private fun setupObservers() {
        locationViewModel.currentLocation.observe(this) { location ->
            location?.let {
                viewModel.getWeatherByLocation(it.latitude, it.longitude)
            }
        }

        locationViewModel.coordinates.observe(this) { coordinates ->
            coordinates?.let { (lat, lon) ->
                viewModel.getWeatherByLocation(lat, lon)
                viewModel.getForecast(lat, lon)
            }
        }

        // Observar estado de carga de ubicación
        locationViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                showLocationLoading()
            }
        }

        // Observar errores de ubicación
        locationViewModel.error.observe(this) { error ->
            error?.let {
                showError("Ubicación: $it")
                locationViewModel.clearError()
            }
        }

        locationViewModel.hasPermission.observe(this) { hasPermission ->
            updateLocationButton(hasPermission)
            if (hasPermission) {
                lifecycleScope.launch {
                    kotlinx.coroutines.delay(500)
                    locationViewModel.getCurrentLocation()
                }
            }
        }

        viewModel.weatherData.observe(this) {
            when(it){
                is UiState.Error -> showError(it.message)
                UiState.Loading -> showLoading()
                is UiState.Success -> {
                    showWeather(it.data)
                    hideLocationLoading()
                }
                is UiState.Warning -> {}
            }
        }

        viewModel.forecastState.observe(this) {
            when(it){
                is UiState.Error -> showError(it.message)
                UiState.Loading -> showLoading()
                is UiState.Success -> {
                    updateForecastUI(it.data)
                    hideLocationLoading()
                }
                is UiState.Warning -> {}
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (locationViewModel.isLoading.value != true) {
                binding.progressBar.isVisible = isLoading
            }
        }

        viewModel.searchResults.observe(this) {
            when(it){
                is UiState.Error -> showError(it.message)
                UiState.Loading -> showLoading()
                is UiState.Success -> {
                    if (it.data.isEmpty())
                        binding.listCities.visibility = View.GONE
                    else
                        searchAdapter.updateList(it.data)
                    hideLocationLoading()
                }
                is UiState.Warning -> {}
            }
        }

        lifecycleScope.launch {
            viewModel.useCelsius.collect { useCelsius ->
                updateTemperatureUnit(useCelsius)
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    //
    private fun updateTemperatureUnit(useCelsius: Boolean) {
        val currentState = viewModel.weatherData.value
        if (currentState is UiState.Success) {
            val weatherData = currentState.data
            binding.tvTemperature.text = weatherData.getTemperatureWithUnit(useCelsius)
        }
    }

    private fun setupSearchCity(){
        searchAdapter = CitySearchAdapter(emptyList()){setCity(it)}
        binding.listCities.layoutManager = LinearLayoutManager(this)
        binding.listCities.adapter = searchAdapter
        binding.listCities.visibility = View.GONE
        // SearchView
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if(it.length>2){
                        binding.listCities.visibility = View.VISIBLE
                        viewModel.searchCities(it)
                    }else{
                        searchAdapter.updateList(emptyList())
                        binding.listCities.visibility = View.GONE
                    }
                }
                return true
            }
        })
        binding.searchBar.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (!hasFocus) binding.listCities.visibility = View.GONE
        }
    }
    fun setCity(city: CityResponse) {
        viewModel.getWeatherByCity(city.name)
        viewModel.getForecast(city.lat, city.lon)
    }
    //----------------------------------------------------------------------------------------------
    //
    private fun setupListeners() {
        // Botón para usar ubicación actual
        binding.useLocationButton.setOnClickListener {
            if (locationViewModel.hasPermission.value == true) {
                // Ya tenemos permisos, obtener ubicación
                locationViewModel.getCurrentLocation()
            } else {
                // Solicitar permisos
                requestLocationPermission()
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            locationViewModel.coordinates.observe(this) { coordinates ->
                coordinates?.let { (lat, lon) ->
                    viewModel.getWeatherByLocation(lat, lon)
                    viewModel.getForecast(lat, lon)
                }
            }
        }

        // Botón para forzar ubicación precisa
        binding.root.setOnLongClickListener {
            if (locationViewModel.hasPermission.value == true) {
                Toast.makeText(this, "Obteniendo ubicación precisa...", Toast.LENGTH_SHORT).show()
                locationViewModel.getFreshLocation()
            }
            true
        }


        binding.iconContainer.setOnClickListener {
            viewModel.toggleTemperatureUnit()
        }
    }

    private fun checkLocationPermission() {
        // Verificar estado actual de permisos
        locationViewModel.checkPermission()

        // También verificar con PermissionManager para consistencia
        if (permissionManager.hasLocationPermission()) {
            // Ya tenemos permisos, obtener ubicación después de un breve delay
            lifecycleScope.launch {
                kotlinx.coroutines.delay(1000)
                locationViewModel.getCurrentLocation()
            }
        }
    }

    private fun requestLocationPermission() {
        if (permissionManager.shouldShowLocationRationale(this)) {
            // Mostrar explicación de por qué necesitamos el permiso
            permissionManager.showLocationRationaleDialog(
                this,
                onContinue = {
                    // Usuario aceptó, solicitar permiso
                    permissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onCancel = {
                    Toast.makeText(
                        this,
                        "Puedes buscar ciudades manualmente",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } else {
            // Solicitar permiso directamente
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun onPermissionGranted() {
        // Actualizar ViewModel
        locationViewModel.checkPermission()

        // Mostrar mensaje de éxito
        Toast.makeText(this, "Permiso concedido", Toast.LENGTH_SHORT).show()

        // Obtener ubicación
        locationViewModel.getCurrentLocation()
    }

    private fun onPermissionDenied(permanentlyDenied: Boolean) {
        // Actualizar ViewModel
        locationViewModel.checkPermission()

        if (permanentlyDenied) {
            // Mostrar diálogo para ir a configuración
            permissionManager.showPermissionPermanentlyDeniedDialog(this)
        } else {
            // Permiso denegado temporalmente
            Toast.makeText(
                this,
                "Permiso denegado. Puedes buscar ciudades manualmente.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun updateLocationButton(hasPermission: Boolean) {
        if (hasPermission) {
            //binding.useLocationButton.text = "Usar mi ubicación"
            binding.useLocationButton.isEnabled = true
        } else {
            //binding.useLocationButton.text = "Conceder permiso de ubicación"
            binding.useLocationButton.isEnabled = true
        }
    }

    private fun showLocationLoading() {
        binding.progressBar.isVisible = true
        binding.useLocationButton.isEnabled = false
        //binding.useLocationButton.text = "Obteniendo ubicación..."
    }

    private fun hideLocationLoading() {
        binding.progressBar.isVisible = false
        binding.useLocationButton.isEnabled = true
        updateLocationButton(locationViewModel.hasPermission.value ?: false)
    }

    private fun showError(message: String) {
        binding.progressBar.isVisible = false
        binding.weatherContainer.isVisible = false
        binding.tvError.isVisible = true
        binding.tvError.text = message
        binding.swipeRefreshLayout.isRefreshing = false
    }
    //----------------------------------------------------------------------------------------------
    //
    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.weatherContainer.isVisible = false
        binding.tvError.isVisible = false
        binding.swipeRefreshLayout.isRefreshing = false
    }
    //----------------------------------------------------------------------------------------------
    //
    private fun showWeather(weatherData: WeatherData) {
        binding.progressBar.isVisible = false
        binding.weatherContainer.isVisible = true
        binding.tvError.isVisible = false
        binding.listCities.visibility = View.GONE
        binding.searchBar.setQuery("", false)
        binding.searchBar.clearFocus()
        binding.swipeRefreshLayout.isRefreshing = false
        updateUI(weatherData)
    }
    //----------------------------------------------------------------------------------------------
    //
    private fun updateUI(weatherData: WeatherData) {
        with(binding) {
            // Información de ubicación
            tvCityName.text = weatherData.locationName
            tvCountry.text = weatherData.country

            // Temperatura
            lifecycleScope.launch {
                viewModel.useCelsius.collect { useCelsius ->
                    tvTemperature.text = weatherData.getTemperatureWithUnit(useCelsius)
                }
            }

            // Condición actual
            tvWeatherCondition.text = weatherData.conditionText
            tvFeelsLike.text = weatherData.getFeelsLikeFormatted()

            // Icon
            Glide.with(this@MainActivity).load(weatherData.getIconUrl()).into(ivWeatherIcon)

            // Detalles del clima
            tvHumidity.text = getString(R.string.humidity,weatherData.getHumidityFormatted())
            tvWind.text = getString(R.string.wind,"${weatherData.getWindSpeedFormatted()} ${weatherData.windDirection}")
            tvPressure.text = getString(R.string.pressure,weatherData.getPressureFormatted())
            tvPrecipitation.text = getString(R.string.precipitation,weatherData.getPrecipitationFormatted())
            tvVisibility.text = getString(R.string.visibility,weatherData.getVisibilityFormatted())
            tvUvIndex.text = getString(R.string.uv_index,weatherData.getUvIndexDescription())

            tvLocalTime.text = getString(R.string.local_time,weatherData.getFormattedLocalTime())
            tvLastUpdated.text = weatherData.getFormattedLastUpdated()

            if (weatherData.isDay) {
                binding.root.setBackgroundResource(R.mipmap.back_day2)
                binding.card.setBackgroundResource(R.drawable.bg_glass_day)
            } else {
                binding.root.setBackgroundResource(R.mipmap.back_night)
                binding.card.setBackgroundResource(R.drawable.bg_glass_night)
            }

            tvCoordinates.text = String.format("📍 Lat: %.4f, Lon: %.4f", weatherData.latitude, weatherData.longitude)
        }
    }

    private fun updateForecastUI(forecastDays: List<ForecastDay>) {
        binding.progressBar.isVisible = false
        binding.swipeRefreshLayout.isRefreshing = false
        binding.tvError.isVisible = false
        if (forecastDays.size >= 1)
            updateForecastDay1(forecastDays[0])
        if (forecastDays.size >= 2)
            updateForecastDay2(forecastDays[1])
        if (forecastDays.size >= 3)
            updateForecastDay3(forecastDays[2])
    }

    private fun updateForecastDay1(forecastDay: ForecastDay) {
        binding.tvDay1.text = forecastDay.date
        binding.tvForecastTemp1.text = "${forecastDay.day.maxTempC.toInt()}°/${forecastDay.day.minTempC.toInt()}°"
        binding.tvForecastCondition1.text = forecastDay.day.condition.text
        binding.tvForecastRain1.text = "🌧️ ${forecastDay.hours[0].chanceOfRain}%"

        Glide.with(this)
            .load("https:${forecastDay.day.condition.icon}")
            .into(binding.ivForecastIcon1)
    }

    private fun updateForecastDay2(forecastDay: ForecastDay) {
        binding.tvDay2.text = forecastDay.date
        binding.tvForecastTemp2.text = "${forecastDay.day.maxTempC.toInt()}°/${forecastDay.day.minTempC.toInt()}°"
        binding.tvForecastCondition2.text = forecastDay.day.condition.text
        binding.tvForecastRain2.text = "🌧️ ${forecastDay.hours[0].chanceOfRain}%"

        Glide.with(this)
            .load("https:${forecastDay.day.condition.icon}")
            .into(binding.ivForecastIcon2)
    }

    private fun updateForecastDay3(forecastDay: ForecastDay) {
        binding.tvDay3.text = forecastDay.date
        binding.tvForecastTemp3.text = "${forecastDay.day.maxTempC.toInt()}°/${forecastDay.day.minTempC.toInt()}°"
        binding.tvForecastCondition3.text = forecastDay.day.condition.text
        binding.tvForecastRain3.text = "🌧️ ${forecastDay.hours[0].chanceOfRain}%"

        Glide.with(this)
            .load("https:${forecastDay.day.condition.icon}")
            .into(binding.ivForecastIcon3)
    }
    //----------------------------------------------------------------------------------------------
    //
    override fun onResume() {
        super.onResume()
        locationViewModel.checkPermission()
    }
}