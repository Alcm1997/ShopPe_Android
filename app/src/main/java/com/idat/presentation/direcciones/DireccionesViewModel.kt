package com.idat.presentation.direcciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.idat.domain.model.Direccion
import com.idat.domain.model.Tarjeta
import com.idat.domain.repository.UsuarioRepository
import com.idat.domain.repository.TarjetaRepository
import android.content.Context
import android.location.Geocoder
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import android.os.Looper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DireccionesViewModel @Inject constructor(
    private val repository: UsuarioRepository,
    private val tarjetaRepository: TarjetaRepository,
    private val auth: FirebaseAuth,
    private val fusedLocationClient: FusedLocationProviderClient,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val userId: String
        get() = auth.currentUser?.uid ?: ""

    val direcciones: StateFlow<List<Direccion>> = repository.getDirecciones(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tarjetas: StateFlow<List<Tarjeta>> = tarjetaRepository.getTarjetas(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _ubicacionActual = MutableStateFlow("Detectando ubicación...")
    val ubicacionActual: StateFlow<String> = _ubicacionActual.asStateFlow()

    private val _estaCargandoUbicacion = MutableStateFlow(false)
    val estaCargandoUbicacion: StateFlow<Boolean> = _estaCargandoUbicacion.asStateFlow()

    // Callback y request para actualizaciones en tiempo real
    private var locationCallback: LocationCallback? = null
    private val locationRequest: LocationRequest by lazy {
        // Usar la nueva API Builder para evitar métodos deprecados
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(2000L)
            .build()
    }

    fun saveDireccion(direccion: Direccion) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.guardarDireccion(userId, direccion)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    fun deleteDireccion(direccionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.eliminarDireccion(userId, direccionId)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    fun deleteTarjeta(tarjetaId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = tarjetaRepository.deleteTarjeta(userId, tarjetaId)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    fun setPredeterminada(direccionId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.establecerDireccionPredeterminada(userId, direccionId)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message
            }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun fetchCurrentLocation() {
        viewModelScope.launch {
            _estaCargandoUbicacion.value = true
            // Comprobar permisos antes de solicitar ubicación
            val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
                _ubicacionActual.value = "Permiso denegado"
                _estaCargandoUbicacion.value = false
                return@launch
            }
            try {
                val location = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).await()

                if (location != null) {
                    val address = getAddressFromLocation(location.latitude, location.longitude)
                    _ubicacionActual.value = address ?: "Ubicación no encontrada"
                } else {
                    _ubicacionActual.value = "Error al obtener ubicación"
                }
            } catch (e: SecurityException) {
                _ubicacionActual.value = "Permiso denegado"
            } catch (e: Exception) {
                _ubicacionActual.value = "Error: ${e.message}"
            } finally {
                _estaCargandoUbicacion.value = false
            }
        }
    }

    // Inicia actualizaciones en tiempo real (requestLocationUpdates)
    fun startLocationUpdates() {
        // Comprobar permisos antes de iniciar
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine != PackageManager.PERMISSION_GRANTED && coarse != PackageManager.PERMISSION_GRANTED) {
            _ubicacionActual.value = "Permiso denegado"
            return
        }
        // Evitar múltiples callbacks
        if (locationCallback != null) return

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation
                if (loc != null) {
                    viewModelScope.launch {
                        val address = getAddressFromLocation(loc.latitude, loc.longitude)
                        _ubicacionActual.value = address ?: "Ubicación no encontrada"
                    }
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, Looper.getMainLooper())
        } catch (e: SecurityException) {
            _ubicacionActual.value = "Permiso denegado"
        } catch (e: Exception) {
            _ubicacionActual.value = "Error: ${e.message}"
        }
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
        }
    }

    private fun getAddressFromLocation(lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val street = addr.thoroughfare ?: ""
                val subLocal = addr.subLocality ?: ""
                val locality = addr.locality ?: ""
                if (street.isNotEmpty()) "$street, $locality" else locality
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
