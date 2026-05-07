package com.idat.presentation.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class DetalleViewModel @Inject constructor(
    private val repository: ProductoRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _producto = MutableStateFlow<Producto?>(null)
    val producto: StateFlow<Producto?> = _producto

    private val _esFavorito = MutableStateFlow(false)
    val esFavorito: StateFlow<Boolean> = _esFavorito

    val isDarkTheme = userPreferencesManager.isDarkTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val recomendaciones: StateFlow<List<Producto>> = repository.obtenerRecomendaciones()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private var productoActual: Producto? = null

    fun cargarProducto(productoId: Int) {
        viewModelScope.launch {
            val prod = repository.obtenerProductoPorId(productoId)
            _producto.value = prod
            productoActual = prod
            
            prod?.let {
                _esFavorito.value = repository.esFavorito(productoId)
            }
        }
    }

    fun toggleFavorito() {
        viewModelScope.launch {
            productoActual?.let { prod ->
                if (_esFavorito.value) {
                    repository.eliminarDeFavoritos(prod.id)
                    _esFavorito.value = false
                } else {
                    repository.agregarAFavoritos(prod)
                    _esFavorito.value = true
                }
            }
        }
    }

    fun agregarAlCarrito(producto: Producto) {
        viewModelScope.launch {
            repository.agregarProductoAlCarrito(producto)
        }
    }
}
