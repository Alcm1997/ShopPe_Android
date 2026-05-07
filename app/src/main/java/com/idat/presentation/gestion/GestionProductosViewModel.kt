package com.idat.presentation.gestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.idat.core.auth.AdminAccess
import com.idat.data.local.preferences.UserPreferencesManager
import com.idat.domain.model.Producto
import com.idat.domain.repository.ProductoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GestionProductosViewModel @Inject constructor(
    private val repository: ProductoRepository,
    private val userPreferencesManager: UserPreferencesManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    // Lista de productos desde Firestore (se actualiza automáticamente)
    val productos = repository.obtenerProductosFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda

    private val _productosFiltrados = MutableStateFlow<List<Producto>>(emptyList())
    val productosFiltrados: StateFlow<List<Producto>> = _productosFiltrados

    val isDarkTheme = userPreferencesManager.isDarkTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        // Observar cambios en búsqueda
        viewModelScope.launch {
            busqueda.collect { query ->
                if (query.isBlank()) {
                    _productosFiltrados.value = productos.value
                } else {
                    repository.buscarProductos(query).collect { resultados ->
                        _productosFiltrados.value = resultados
                    }
                }
            }
        }
    }

    fun setBusqueda(query: String) {
        _busqueda.value = query
    }

    private fun permissionDeniedMessage(): String {
        val email = auth.currentUser?.email ?: "sin-email"
        val uid = auth.currentUser?.uid ?: "sin-uid"
        return "Firestore denego permisos. Sesion actual: $email ($uid). Verifica reglas para /productos y /metadata."
    }

    private fun mapError(defaultMessage: String, e: Exception): String {
        return if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
            permissionDeniedMessage()
        } else {
            e.message ?: defaultMessage
        }
    }

    private fun ensureAdminOrError(onError: (String) -> Unit): Boolean {
        val currentEmail = auth.currentUser?.email
        if (AdminAccess.isAdminEmail(currentEmail)) {
            return true
        }
        onError("Acceso denegado: solo el administrador puede gestionar productos.")
        return false
    }

    // ========== CREAR ==========
    fun crearProducto(
        nombre: String,
        precio: Double,
        descripcion: String,
        categoria: String,
        imagen: String,
        calificacion: Double,
        cantidadCalificaciones: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!ensureAdminOrError(onError)) return@launch

                if (nombre.isBlank() || precio <= 0) {
                    onError("Nombre y precio son obligatorios")
                    return@launch
                }

                val producto = Producto(
                    id = 0, // Auto-increment
                    nombre = nombre,
                    precio = precio,
                    descripcion = descripcion,
                    categoria = categoria,
                    imagen = imagen,
                    calificacion = calificacion,
                    cantidadCalificaciones = cantidadCalificaciones
                )
                repository.crearProducto(producto)
                onSuccess()
            } catch (e: Exception) {
                onError(mapError("Error al crear producto", e))
            }
        }
    }

    // ========== ACTUALIZAR ==========
    fun actualizarProducto(
        producto: Producto,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!ensureAdminOrError(onError)) return@launch

                if (producto.nombre.isBlank() || producto.precio <= 0) {
                    onError("Nombre y precio son obligatorios")
                    return@launch
                }
                repository.actualizarProducto(producto)
                onSuccess()
            } catch (e: Exception) {
                onError(mapError("Error al actualizar producto", e))
            }
        }
    }

    // ========== ELIMINAR ==========
    fun eliminarProducto(
        productoId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (!ensureAdminOrError(onError)) return@launch

                repository.eliminarProducto(productoId)
                onSuccess()
            } catch (e: Exception) {
                onError(mapError("Error al eliminar producto", e))
            }
        }
    }
}
