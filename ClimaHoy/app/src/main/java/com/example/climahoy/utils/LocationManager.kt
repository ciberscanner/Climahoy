package com.example.climahoy.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LocationManager @Inject constructor(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    sealed class LocationState {
        object Loading : LocationState()
        data class Success(val location: Location) : LocationState()
        data class Error(val message: String) : LocationState()
        object PermissionDenied : LocationState()
        object LocationDisabled : LocationState()
    }

    /**
     * Verifica si los permisos de ubicación están concedidos
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Obtiene la última ubicación conocida (versión con suspendCancellableCoroutine)
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastKnownLocation(): Result<Location> = suspendCancellableCoroutine { continuation ->
        if (!hasLocationPermission()) {
            continuation.resumeWithException(SecurityException("Permiso de ubicación denegado"))
            return@suspendCancellableCoroutine
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    continuation.resume(Result.success(location))
                } else {
                    continuation.resume(Result.failure(Exception("Ubicación no disponible")))
                }
            }
            .addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
            }
            .addOnCanceledListener {
                continuation.cancel(CancellationException("Operación cancelada"))
            }
    }

    /**
     * Obtiene la última ubicación conocida como Flow
     */
    fun getLastKnownLocationFlow(): Flow<LocationState> = flow {
        if (!hasLocationPermission()) {
            emit(LocationState.PermissionDenied)
            return@flow
        }

        emit(LocationState.Loading)

        try {
            val result = getLastKnownLocation()
            result.fold(
                onSuccess = { location ->
                    emit(LocationState.Success(location))
                },
                onFailure = { exception ->
                    when (exception) {
                        is SecurityException -> {
                            emit(LocationState.PermissionDenied)
                        }
                        else -> {
                            emit(LocationState.Error(exception.message ?: "Error desconocido"))
                        }
                    }
                }
            )
        } catch (e: Exception) {
            emit(LocationState.Error(e.message ?: "Error desconocido"))
        }
    }

    /**
     * Versión simplificada para obtener solo la ubicación (sin Result wrapper)
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    continuation.resume(location)
                }
                .addOnFailureListener { exception ->
                    continuation.resume(null)
                }
        }
    }

    /**
     * Obtiene actualizaciones continuas de ubicación como Flow
     */
    @SuppressLint("MissingPermission")
    fun getLocationUpdates(
        intervalMillis: Long = 10000,
        fastestIntervalMillis: Long = 5000
    ): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close(SecurityException("Permiso de ubicación denegado"))
            return@callbackFlow
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            intervalMillis
        ).apply {
            setMinUpdateIntervalMillis(fastestIntervalMillis)
        }.build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach { location ->
                    trySend(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            close(e)
        } catch (e: Exception) {
            close(e)
        }

        awaitClose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Obtiene una única ubicación actualizada con alta precisión
     */
    @SuppressLint("MissingPermission")
    suspend fun getFreshLocation(timeoutMillis: Long = 30000): Result<Location> =
        suspendCancellableCoroutine { continuation ->
            if (!hasLocationPermission()) {
                continuation.resume(Result.failure(SecurityException("Permiso de ubicación denegado")))
                return@suspendCancellableCoroutine
            }

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        continuation.resume(Result.success(location))
                    } ?: run {
                        continuation.resume(Result.failure(Exception("No se pudo obtener la ubicación")))
                    }
                    // Remover el callback después de obtener una ubicación
                    fusedLocationClient.removeLocationUpdates(this)
                }
            }

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                5000
            ).apply {
                setWaitForAccurateLocation(true)
                setMinUpdateIntervalMillis(1000)
            }.build()

            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    null
                ).addOnFailureListener { exception ->
                    continuation.resume(Result.failure(exception))
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            } catch (e: Exception) {
                continuation.resume(Result.failure(e))
            }

            // Timeout para evitar esperar indefinidamente
            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }

            // Configurar timeout manual
            val handler = android.os.Handler(context.mainLooper)
            val timeoutRunnable = Runnable {
                if (continuation.isActive) {
                    continuation.resume(Result.failure(Exception("Timeout al obtener ubicación")))
                    fusedLocationClient.removeLocationUpdates(locationCallback)
                }
            }
            handler.postDelayed(timeoutRunnable, timeoutMillis)

            // Limpiar el timeout cuando se complete
            continuation.invokeOnCancellation {
                handler.removeCallbacks(timeoutRunnable)
            }
        }

    /**
     * Calcula distancia entre dos puntos en kilómetros
     */
    fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0] / 1000 // Convertir a kilómetros
    }

    /**
     * Verifica si la ubicación está habilitada en el dispositivo
     */
    fun isLocationEnabled(): Boolean {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE)
                    as android.location.LocationManager
            locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getCurrentCoordinates(): Pair<Double, Double>? {
        val location = getCurrentLocation()
        return location?.let {
            Pair(it.latitude, it.longitude)
        }
    }

    suspend fun getCurrentCoordinatesResult(): Result<Pair<Double, Double>> {
        return try {
            val result = getLastKnownLocation()
            result.map { location ->
                Pair(location.latitude, location.longitude)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}