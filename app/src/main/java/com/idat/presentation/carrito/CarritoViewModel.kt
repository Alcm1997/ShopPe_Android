package com.idat.presentation.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idat.data.local.preferences.UserPreferencesManager
import com.idat.domain.model.ItemCarrito
import com.idat.domain.usecase.CarritoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarritoViewModel @Inject constructor(
    private val useCase: CarritoUseCase,
    private val userPreferencesManager: UserPreferencesManager
) : ViewModel() {

    private val _productos = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val productos: StateFlow<List<ItemCarrito>> = _productos

    val isDarkTheme = userPreferencesManager.isDarkTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val recomendaciones = useCase.obtenerRecomendaciones().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            useCase.obtenerCarrito().collect { items ->
                _productos.value = items
            }
        }
    }

    fun eliminarDelCarrito(productoId: Int) {
        viewModelScope.launch {
            useCase.eliminarDelCarrito(productoId)
        }
    }

    fun incrementarCantidad(item: ItemCarrito) {
        viewModelScope.launch {
            useCase.actualizarCantidad(item.id, item.cantidad + 1)
        }
    }

    fun decrementarCantidad(item: ItemCarrito) {
        viewModelScope.launch {
            if (item.cantidad > 1) {
                useCase.actualizarCantidad(item.id, item.cantidad - 1)
            } else {
                eliminarDelCarrito(item.id)
            }
        }
    }
}
