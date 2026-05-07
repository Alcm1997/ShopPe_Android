# Explicación Paso a Paso de ProductoRepositoryImpl

Este documento explica exactamente qué hace la clase ProductoRepositoryImpl en tu proyecto ShopPe.

## 1. Qué es ProductoRepositoryImpl

ProductoRepositoryImpl es la implementación concreta del repositorio de productos. Su función es centralizar toda la lógica de datos para:

- Productos
- Carrito
- Favoritos
- Recomendaciones
- CRUD de productos (crear, actualizar, eliminar)

Trabaja con tres dependencias principales:

```kotlin
class ProductoRepositoryImpl @Inject constructor(
    private val apiService: ProductoApiService,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ProductoRepository
```

- apiService: obtiene productos desde API externa
- auth: identifica al usuario actual
- firestore: guarda y lee datos en la nube

## 2. Estructura de datos que maneja en Firestore

Colecciones principales:

- productos
- users/{uid}/carrito
- users/{uid}/favoritos
- metadata/counters

Constantes usadas:

```kotlin
private const val PRODUCTOS = "productos"
private const val USERS = "users"
private const val CARRITO = "carrito"
private const val FAVORITOS = "favoritos"
private const val METADATA = "metadata"
private const val COUNTERS = "counters"
private const val NEXT_PRODUCT_ID = "nextProductId"
```

## 3. Funciones auxiliares clave

### 3.1 getUserId

```kotlin
private fun getUserId(): String {
    return auth.currentUser?.uid ?: "guest"
}
```

Qué hace:

- Si hay usuario logueado, devuelve su uid.
- Si no, usa guest.

### 3.2 Referencias de Firestore

```kotlin
private fun productDoc(productoId: Int) =
    firestore.collection(PRODUCTOS).document(productoId.toString())

private fun userCarritoCollection(userId: String) =
    firestore.collection(USERS).document(userId).collection(CARRITO)

private fun userFavoritosCollection(userId: String) =
    firestore.collection(USERS).document(userId).collection(FAVORITOS)
```

Qué hacen:

- Evitan repetir rutas de Firestore por todo el código.

### 3.3 Conversión de modelos

#### productoToMap

Convierte un Producto a Map para guardarlo en Firestore.

```kotlin
private fun productoToMap(producto: Producto): Map<String, Any>
```

#### mapToProducto

Convierte un documento Firestore a Producto.

```kotlin
private fun mapToProducto(data: Map<String, Any?>): Producto?
```

#### mapToItemCarrito

Convierte datos de Firestore a ItemCarrito.

```kotlin
private fun mapToItemCarrito(data: Map<String, Any?>): ItemCarrito?
```

## 4. Flujo de carga inicial de productos

### 4.1 saveApiProductsToFirestore

```kotlin
private suspend fun saveApiProductsToFirestore()
```

Paso a paso:

1. Revisa si Firestore ya tiene productos.
2. Si ya existen, no vuelve a llamar API.
3. Si no existen, llama apiService.getProducts().
4. Mapea cada dto a Producto.
5. Guarda cada producto en la colección productos.
6. Actualiza metadata/counters/nextProductId con el mayor id + 1.

Objetivo:

- Hacer una sincronización inicial segura y evitar duplicados.

## 5. Obtener productos para catálogo

### 5.1 obtenerProductos

```kotlin
override suspend fun obtenerProductos(): List<Producto>
```

Paso a paso:

1. Si cachedProductos existe, devuelve cache inmediato.
2. Si no hay cache, ejecuta saveApiProductsToFirestore().
3. Lee productos desde Firestore ordenados por id ascendente.
4. Convierte documentos a List<Producto>.
5. Guarda en cache y devuelve.

Nota:

- viewModelScopeSync está como placeholder para futura sincronización en segundo plano.

## 6. Carrito de compras

### 6.1 agregarProductoAlCarrito

```kotlin
override suspend fun agregarProductoAlCarrito(producto: Producto)
```

Paso a paso:

1. Obtiene userId.
2. Apunta al documento users/{uid}/carrito/{productoId}.
3. Ejecuta transacción Firestore.
4. Si el item ya existe, suma cantidad +1.
5. Si no existe, crea cantidad = 1.
6. Guarda id, nombre, precio, descripción, categoría, imagen y cantidad.

### 6.2 obtenerCarrito

```kotlin
override fun obtenerCarrito(): Flow<List<ItemCarrito>>
```

Paso a paso:

1. Abre listener en users/{uid}/carrito.
2. En cada cambio, mapea documentos a ItemCarrito.
3. Ordena por id.
4. Emite lista en tiempo real con callbackFlow.
5. Cuando se cierra, elimina listener con awaitClose.

### 6.3 eliminarProductoDelCarrito

Elimina un producto específico por id.

### 6.4 actualizarCantidad

- Si cantidad > 0, actualiza campo cantidad.
- Si cantidad <= 0, elimina el documento.

### 6.5 vaciarCarrito

1. Lee todos los docs del carrito.
2. Ejecuta batch delete para borrarlos juntos.

## 7. Producto por ID

### 7.1 obtenerProductoPorId

```kotlin
override suspend fun obtenerProductoPorId(productoId: Int): Producto?
```

Qué hace:

- Lee productos/{id}.
- Convierte a Producto con mapToProducto.
- Lo usa la pantalla de detalle.

## 8. Favoritos

### 8.1 agregarAFavoritos

Guarda el producto completo en users/{uid}/favoritos/{productoId}.

### 8.2 eliminarDeFavoritos

Elimina users/{uid}/favoritos/{productoId}.

### 8.3 obtenerFavoritos

Escucha cambios en favoritos en tiempo real y devuelve Flow<List<Producto>>.

### 8.4 esFavorito

Consulta si existe el documento del producto en favoritos.

## 9. Productos en tiempo real

### 9.1 obtenerProductosFlow

```kotlin
override fun obtenerProductosFlow(): Flow<List<Producto>>
```

Paso a paso:

1. Crea listener sobre colección productos.
2. Ordena por id ascendente.
3. Mapea documentos a Producto.
4. Emite lista cada vez que hay cambios.

Este método alimenta el catálogo reactivo en la UI.

## 10. CRUD de productos (gestión)

### 10.1 crearProducto

```kotlin
override suspend fun crearProducto(producto: Producto): Long
```

Flujo principal:

1. Usa transacción sobre metadata/counters.
2. Lee nextProductId actual.
3. Incrementa contador en +1.
4. Crea producto con ese id.
5. Guarda en productos/{id}.
6. Invalida cache (cachedProductos = null).
7. Devuelve id creado.

Fallback:

- Si falla por PERMISSION_DENIED al leer metadata, usa createProductWithoutMetadataCounter:
  - busca el mayor id en productos,
  - suma 1,
  - crea el nuevo producto.

### 10.2 actualizarProducto

Hace set merge del producto por id e invalida cache.

### 10.3 eliminarProducto

Borra productos/{id} e invalida cache.

## 11. Búsqueda de productos

### 11.1 buscarProductos

```kotlin
override fun buscarProductos(query: String): Flow<List<Producto>>
```

Paso a paso:

1. Normaliza query (trim + lowercase).
2. Se engancha a obtenerProductosFlow().
3. Si query está vacía, devuelve todos.
4. Si no, filtra por nombre o descripción.

## 12. Recomendaciones

### 12.1 obtenerRecomendaciones

```kotlin
override fun obtenerRecomendaciones(): Flow<List<Producto>>
```

Qué hace:

1. Escucha colección productos.
2. Ordena por calificacion descendente.
3. Limita a 5.
4. Emite lista en tiempo real.

## 13. Relación con UI (Catálogo y Detalle)

- Catálogo usa obtenerProductosFlow y obtenerProductos para mostrar lista.
- Item visual usa campo imagen para cargar con Coil AsyncImage.
- Detalle usa obtenerProductoPorId para cargar el producto seleccionado.
- Botón carrito llama agregarProductoAlCarrito.
- Favoritos usan agregarAFavoritos, eliminarDeFavoritos y esFavorito.
- Sección recomendados usa obtenerRecomendaciones.

## 14. Resumen corto

ProductoRepositoryImpl:

- Sincroniza API externa a Firestore al inicio.
- Sirve datos reactivos con Flow para la UI.
- Maneja carrito y favoritos por usuario.
- Mantiene cache en memoria para respuesta rápida.
- Implementa CRUD y búsqueda de productos.
- Genera recomendaciones por calificación.

---

Archivo fuente principal explicado: app/src/main/java/com/idat/data/repository/ProductoRepositoryImpl.kt
