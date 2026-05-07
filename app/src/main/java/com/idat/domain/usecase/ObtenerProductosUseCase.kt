package com.idat.domain.usecase

import com.idat.domain.repository.ProductoRepository
import com.idat.domain.model.Producto
import javax.inject.Inject

class ObtenerProductosUseCase @Inject constructor(
    private val repository: ProductoRepository
) {
    suspend fun ejecutar(): List<Producto> {
        return repository.obtenerProductos()
    }
}
