package com.idat.presentation.gestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idat.domain.model.Pedido
import com.idat.domain.repository.PedidoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminComprobantesViewModel @Inject constructor(
    private val repository: PedidoRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter = _statusFilter.asStateFlow()

    private val _allPedidos = repository.getAllPedidos()
        .retryWhen { cause, attempt ->
            if (cause is java.io.IOException || cause.message?.contains("UNAVAILABLE") == true) {
                val delayTime = (attempt + 1) * 2000L
                kotlinx.coroutines.delay(minOf(delayTime, 30000L))
                true
            } else {
                false
            }
        }
        .map { pedidos -> pedidos.sortedByDescending { it.fecha } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPedidos = combine(_allPedidos, _searchQuery, _statusFilter) { pedidos, query, status ->
        var list = pedidos
        if (status != null) {
            list = list.filter { it.estado.equals(status, ignoreCase = true) }
        }
        if (query.isNotEmpty()) {
            list = list.filter { 
                it.numComprobante.contains(query, ignoreCase = true) || 
                it.clienteNombre.contains(query, ignoreCase = true) ||
                it.clienteEmail.contains(query, ignoreCase = true)
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onStatusFilterChange(status: String?) {
        _statusFilter.value = if (_statusFilter.value == status) null else status
    }

    fun updatePedidoStatus(pedidoId: String, newStatus: String, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            repository.updatePedidoStatus(pedidoId, newStatus)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "Error al actualizar estado") }
        }
    }
}
