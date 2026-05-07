package com.idat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import com.idat.domain.model.Producto
import com.idat.domain.model.ItemCarrito
import com.idat.domain.repository.ProductoRepository
import com.idat.data.remote.service.ProductoApiService
import android.util.Log
import javax.inject.Inject

class ProductoRepositoryImpl @Inject constructor(
    private val apiService: ProductoApiService,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ProductoRepository {
    
    private var cachedProductos: List<Producto>? = null


    companion object {
        private const val PRODUCTOS = "productos"
        private const val USERS = "users"
        private const val CARRITO = "carrito"
        private const val FAVORITOS = "favoritos"
        private const val METADATA = "metadata"
        private const val COUNTERS = "counters"
        private const val NEXT_PRODUCT_ID = "nextProductId"
    }

    private fun getUserId(): String {
        return auth.currentUser?.uid ?: "guest"
    }

    private fun productDoc(productoId: Int) =
        firestore.collection(PRODUCTOS).document(productoId.toString())

    private fun userCarritoCollection(userId: String) =
        firestore.collection(USERS).document(userId).collection(CARRITO)

    private fun userFavoritosCollection(userId: String) =
        firestore.collection(USERS).document(userId).collection(FAVORITOS)

    private fun productoToMap(producto: Producto): Map<String, Any> {
        return mapOf(
            "id" to producto.id,
            "nombre" to producto.nombre,
            "precio" to producto.precio,
            "descripcion" to producto.descripcion,
            "categoria" to producto.categoria,
            "imagen" to producto.imagen,
            "calificacion" to producto.calificacion,
            "cantidadCalificaciones" to producto.cantidadCalificaciones
        )
    }

    private fun mapToProducto(data: Map<String, Any?>): Producto? {
        val id = (data["id"] as? Number)?.toInt() ?: return null
        val nombre = data["nombre"] as? String ?: ""
        val precio = (data["precio"] as? Number)?.toDouble() ?: 0.0
        val descripcion = data["descripcion"] as? String ?: ""
        val categoria = data["categoria"] as? String ?: ""
        val imagen = data["imagen"] as? String ?: ""
        val calificacion = (data["calificacion"] as? Number)?.toDouble() ?: 0.0
        val cantidadCalificaciones = (data["cantidadCalificaciones"] as? Number)?.toInt() ?: 0

        return Producto(
            id = id,
            nombre = nombre,
            precio = precio,
            descripcion = descripcion,
            categoria = categoria,
            imagen = imagen,
            calificacion = calificacion,
            cantidadCalificaciones = cantidadCalificaciones
        )
    }

    private fun mapToItemCarrito(data: Map<String, Any?>): ItemCarrito? {
        val id = (data["id"] as? Number)?.toInt() ?: return null
        val nombre = data["nombre"] as? String ?: ""
        val precio = (data["precio"] as? Number)?.toDouble() ?: 0.0
        val descripcion = data["descripcion"] as? String ?: ""
        val categoria = data["categoria"] as? String ?: ""
        val imagen = data["imagen"] as? String ?: ""
        val cantidad = (data["cantidad"] as? Number)?.toInt() ?: 1

        return ItemCarrito(
            id = id,
            nombre = nombre,
            precio = precio,
            descripcion = descripcion,
            categoria = categoria,
            imagen = imagen,
            cantidad = cantidad
        )
    }

    private suspend fun saveApiProductsToFirestore() {
        try {
            // Check if we already have products in Firestore to avoid redundant API calls
            val snapshot = firestore.collection(PRODUCTOS).limit(1).get().await()
            if (!snapshot.isEmpty) {
                Log.d("ProductoRepositoryImpl", "Firestore ya tiene productos, omitiendo sincronización API.")
                return
            }

            Log.d("ProductoRepositoryImpl", "Sincronizando productos desde API externa...")
            val productosDto = apiService.getProducts()
            var maxId = 0
            for (dto in productosDto) {
                maxId = maxOf(maxId, dto.id)
                val producto = Producto(
                    id = dto.id,
                    nombre = dto.title,
                    precio = dto.price,
                    descripcion = dto.description ?: "",
                    categoria = dto.category ?: "",
                    imagen = dto.image ?: "",
                    calificacion = dto.rating?.rate ?: 0.0,
                    cantidadCalificaciones = dto.rating?.count ?: 0
                )
                productDoc(producto.id).set(productoToMap(producto), SetOptions.merge()).await()
            }

            val countersRef = firestore.collection(METADATA).document(COUNTERS)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(countersRef)
                val current = snapshot.getLong(NEXT_PRODUCT_ID) ?: 1L
                val required = (maxId + 1).toLong()
                if (required > current) {
                    transaction.set(countersRef, mapOf(NEXT_PRODUCT_ID to required), SetOptions.merge())
                }
            }.await()
        } catch (e: Exception) {
            Log.e("ProductoRepositoryImpl", "Error syncing products from API: ${e.message}")
        }
    }

    override suspend fun obtenerProductos(): List<Producto> {
        // Return memory cache if available for instant UI
        cachedProductos?.let { 
            // Still try to sync in background if needed
            viewModelScopeSync() 
            return it 
        }

        // If no cache, we must sync at least once or get from Firestore
        saveApiProductsToFirestore()
        val snapshot = firestore.collection(PRODUCTOS).orderBy("id", Query.Direction.ASCENDING).get().await()
        val productos = snapshot.documents.mapNotNull { mapToProducto(it.data ?: emptyMap()) }
        
        cachedProductos = productos
        return productos
    }

    private fun viewModelScopeSync() {
        // Helper to trigger sync without blocking the main flow
        // In a real app, this would be a WorkManager or a specific scope
        // For now, we rely on the caller's scope or just run it as a side effect
    }

    override suspend fun agregarProductoAlCarrito(producto: Producto) {
        val userId = getUserId()
        val docRef = userCarritoCollection(userId).document(producto.id.toString())

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            val cantidadActual = (snapshot.getLong("cantidad") ?: 0L).toInt()
            val nuevaCantidad = if (snapshot.exists()) cantidadActual + 1 else 1

            val item = mapOf(
                "id" to producto.id,
                "nombre" to producto.nombre,
                "precio" to producto.precio,
                "descripcion" to producto.descripcion,
                "categoria" to producto.categoria,
                "imagen" to producto.imagen,
                "cantidad" to nuevaCantidad
            )
            transaction.set(docRef, item)
        }.await()
    }

    override fun obtenerCarrito(): Flow<List<ItemCarrito>> {
        val userId = getUserId()
        return callbackFlow {
            val listener = userCarritoCollection(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents
                        ?.mapNotNull { mapToItemCarrito(it.data ?: emptyMap()) }
                        ?.sortedBy { it.id }
                        ?: emptyList()
                    trySend(items)
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun eliminarProductoDelCarrito(productoId: Int) {
        val userId = getUserId()
        userCarritoCollection(userId).document(productoId.toString()).delete().await()
    }

    override suspend fun actualizarCantidad(productoId: Int, cantidad: Int) {
        val userId = getUserId()
        val docRef = userCarritoCollection(userId).document(productoId.toString())

        if (cantidad > 0) {
            docRef.update("cantidad", cantidad).await()
        } else {
            docRef.delete().await()
        }
    }

    override suspend fun vaciarCarrito() {
        val userId = getUserId()
        val snapshot = userCarritoCollection(userId).get().await()
        firestore.runBatch { batch ->
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
        }.await()
    }

    override suspend fun obtenerProductoPorId(productoId: Int): Producto? {
        val snapshot = productDoc(productoId).get().await()
        return mapToProducto(snapshot.data ?: emptyMap())
    }

    override suspend fun agregarAFavoritos(producto: Producto) {
        val userId = getUserId()
        val data = mapOf(
            "id" to producto.id,
            "nombre" to producto.nombre,
            "precio" to producto.precio,
            "descripcion" to producto.descripcion,
            "categoria" to producto.categoria,
            "imagen" to producto.imagen,
            "calificacion" to producto.calificacion,
            "cantidadCalificaciones" to producto.cantidadCalificaciones
        )
        userFavoritosCollection(userId).document(producto.id.toString()).set(data).await()
    }

    override suspend fun eliminarDeFavoritos(productoId: Int) {
        val userId = getUserId()
        userFavoritosCollection(userId).document(productoId.toString()).delete().await()
    }

    override fun obtenerFavoritos(): Flow<List<Producto>> {
        val userId = getUserId()
        return callbackFlow {
            val listener = userFavoritosCollection(userId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val favoritos = snapshot?.documents
                        ?.mapNotNull { mapToProducto(it.data ?: emptyMap()) }
                        ?.sortedBy { it.id }
                        ?: emptyList()
                    trySend(favoritos)
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun esFavorito(productoId: Int): Boolean {
        val userId = getUserId()
        return userFavoritosCollection(userId).document(productoId.toString()).get().await().exists()
    }

    override fun obtenerProductosFlow(): Flow<List<Producto>> {
        return callbackFlow {
            val listener = firestore.collection(PRODUCTOS)
                .orderBy("id", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val productos = snapshot?.documents
                        ?.mapNotNull { mapToProducto(it.data ?: emptyMap()) }
                        ?: emptyList()
                    trySend(productos)
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun crearProducto(producto: Producto): Long {
        val counterRef = firestore.collection(METADATA).document(COUNTERS)
        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(counterRef)
                val currentId = (snapshot.getLong(NEXT_PRODUCT_ID) ?: 1L).toInt()
                transaction.set(counterRef, mapOf(NEXT_PRODUCT_ID to (currentId + 1)), SetOptions.merge())

                val nuevoProducto = producto.copy(id = currentId)
                transaction.set(productDoc(currentId), productoToMap(nuevoProducto))
                cachedProductos = null // Invalidate cache
                currentId.toLong()
            }.await()
        } catch (e: FirebaseFirestoreException) {
            if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                createProductWithoutMetadataCounter(producto)
            } else {
                throw e
            }
        }
    }

    private suspend fun createProductWithoutMetadataCounter(producto: Producto): Long {
        val lastSnapshot = firestore.collection(PRODUCTOS)
            .orderBy("id", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        val lastId = (lastSnapshot.documents.firstOrNull()?.getLong("id") ?: 0L).toInt()
        val newId = lastId + 1
        val nuevoProducto = producto.copy(id = newId)
        productDoc(newId).set(productoToMap(nuevoProducto)).await()
        cachedProductos = null // Invalidate cache
        return newId.toLong()
    }

    override suspend fun actualizarProducto(producto: Producto) {
        productDoc(producto.id).set(productoToMap(producto), SetOptions.merge()).await()
        cachedProductos = null // Invalidate cache
    }

    override suspend fun eliminarProducto(productoId: Int) {
        productDoc(productoId).delete().await()
        cachedProductos = null // Invalidate cache
    }

    override fun buscarProductos(query: String): Flow<List<Producto>> {
        val normalized = query.trim().lowercase()
        return obtenerProductosFlow().map { productos ->
            if (normalized.isBlank()) {
                productos
            } else {
                productos.filter {
                    it.nombre.lowercase().contains(normalized) ||
                        it.descripcion.lowercase().contains(normalized)
                }
            }
        }
    }

    override fun obtenerRecomendaciones(): Flow<List<Producto>> {
        return callbackFlow {
            val listener = firestore.collection(PRODUCTOS)
                .orderBy("calificacion", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("ProductoRepositoryImpl", "Error obteniendo recomendaciones: ${error.message}")
                        close(error)
                        return@addSnapshotListener
                    }
                    val productos = snapshot?.documents
                        ?.mapNotNull { mapToProducto(it.data ?: emptyMap()) }
                        ?: emptyList()
                    trySend(productos)
                }
            awaitClose { listener.remove() }
        }
    }
}
