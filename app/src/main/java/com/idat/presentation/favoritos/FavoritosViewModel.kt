package com.idat.presentation.favoritos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idat.data.local.preferences.UserPreferencesManager
import com.idat.domain.model.Producto
import com.idat.domain.repository.ProductoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritosViewModel @Inject constructor(
    private val repository: ProductoRepository,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    val favoritos: StateFlow<List<Producto>> = repository.obtenerFavoritos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

    fun eliminarDeFavoritos(productoId: Int) {
        viewModelScope.launch {
            repository.eliminarDeFavoritos(productoId)
        }
    }

    fun agregarAlCarrito(producto: Producto) {
        viewModelScope.launch {
            repository.agregarProductoAlCarrito(producto)
        }
    }
}
