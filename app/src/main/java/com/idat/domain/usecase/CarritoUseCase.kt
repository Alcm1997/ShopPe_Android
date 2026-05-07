package com.idat.domain.usecase

import com.idat.domain.repository.ProductoRepository
import com.idat.domain.model.ItemCarrito
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CarritoUseCase @Inject constructor(
    private val repository: ProductoRepository
) {
    suspend fun obtenerCarrito(): Flow<List<ItemCarrito>> {
        return repository.obtenerCarrito()
    }

    suspend fun eliminarDelCarrito(productoId: Int) {
        repository.eliminarProductoDelCarrito(productoId)
    }

    suspend fun actualizarCantidad(productoId: Int, cantidad: Int) {
        repository.actualizarCantidad(productoId, cantidad)
    }

    fun obtenerRecomendaciones() = repository.obtenerRecomendaciones()
}
