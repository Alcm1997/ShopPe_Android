package com.idat.presentation.gestion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.idat.domain.model.Pedido
import com.idat.domain.repository.PedidoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ReportState(
    val totalVentas: Double = 0.0,
    val totalPedidos: Int = 0,
    val pedidosPendientes: Int = 0,
    val productoEstrella: String = "N/A",
    val clienteTop: String = "N/A",
    val isLoading: Boolean = true
)

@HiltViewModel
class AdminReportesViewModel @Inject constructor(
    private val repository: PedidoRepository
) : ViewModel() {

    val reportState = repository.getAllPedidos()
        .map { pedidos ->
            if (pedidos.isEmpty()) return@map ReportState(isLoading = false)

            val confirmados = pedidos.filter { it.estado != "Cancelado" }
            val totalDinero = confirmados.sumOf { it.total }
            val pendientes = pedidos.count { it.estado == "Pendiente" }

            // Calcular producto más vendido
            val itemCounts = pedidos.flatMap { it.items }
                .groupBy { it.nombre }
                .mapValues { it.value.sumOf { item -> item.cantidad } }
            val topProduct = itemCounts.maxByOrNull { it.value }?.key ?: "Sin datos"

            // Calcular mejor cliente
            val clienteGastos = pedidos.groupBy { it.clienteNombre }
                .mapValues { it.value.sumOf { p -> p.total } }
            val topCliente = clienteGastos.maxByOrNull { it.value }?.key ?: "Sin datos"

            ReportState(
                totalVentas = totalDinero,
                totalPedidos = pedidos.size,
                pedidosPendientes = pendientes,
                productoEstrella = topProduct,
                clienteTop = topCliente,
                isLoading = false
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportState())
}
