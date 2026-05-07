package com.idat.domain.repository

import com.idat.domain.model.Pedido
import kotlinx.coroutines.flow.Flow

interface PedidoRepository {
    suspend fun savePedido(pedido: Pedido): Result<Unit>
    fun getPedidosByUser(userId: String): Flow<List<Pedido>>
    fun getAllPedidos(): Flow<List<Pedido>>
    fun getPedidoById(pedidoId: String): Flow<Pedido?>
    suspend fun updatePedidoStatus(pedidoId: String, newStatus: String): Result<Unit>
}
