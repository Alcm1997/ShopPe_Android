package com.idat.domain.repository

import com.idat.domain.model.Producto
import com.idat.domain.model.ItemCarrito
import kotlinx.coroutines.flow.Flow

interface ProductoRepository {
    // ========== LEER (Read) ==========
    suspend fun obtenerProductos(): List<Producto>
    suspend fun obtenerProductoPorId(productoId: Int): Producto?
    fun obtenerProductosFlow(): Flow<List<Producto>>
    fun buscarProductos(query: String): Flow<List<Producto>>
    
    // ========== CREAR (Create) ==========
    suspend fun crearProducto(producto: Producto): Long
    
    // ========== ACTUALIZAR (Update) ==========
    suspend fun actualizarProducto(producto: Producto)
    
    // ========== ELIMINAR (Delete) ==========
    suspend fun eliminarProducto(productoId: Int)
    
    // Carrito
    suspend fun agregarProductoAlCarrito(producto: Producto)
    fun obtenerCarrito(): Flow<List<ItemCarrito>>
    suspend fun eliminarProductoDelCarrito(productoId: Int)
    suspend fun actualizarCantidad(productoId: Int, cantidad: Int)
    suspend fun vaciarCarrito()
    
    // Favoritos
    suspend fun agregarAFavoritos(producto: Producto)
    suspend fun eliminarDeFavoritos(productoId: Int)
    fun obtenerFavoritos(): Flow<List<Producto>>
    suspend fun esFavorito(productoId: Int): Boolean
    
    // Recomendaciones
    fun obtenerRecomendaciones(): Flow<List<Producto>>
}
