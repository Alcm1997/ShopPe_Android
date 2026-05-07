package com.idat.presentation.catalogo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.idat.data.local.preferences.UserPreferencesManager
import com.idat.domain.model.Categoria
import com.idat.domain.model.Producto
import com.idat.domain.repository.ProductoRepository
import com.idat.domain.usecase.ObtenerProductosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogoViewModel @Inject constructor(
    private val useCase: ObtenerProductosUseCase,
    private val repository: ProductoRepository,
    private val firebaseAuth: FirebaseAuth,
    private val userPreferencesManager: UserPreferencesManager,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _todosLosProductos = MutableStateFlow<List<Producto>>(emptyList())
    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    private val _categoriaSeleccionada = MutableStateFlow("Todas")
    val categoriaSeleccionada: StateFlow<String> = _categoriaSeleccionada

    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda: StateFlow<String> = _textoBusqueda

    private val _categorias = MutableStateFlow<List<Categoria>>(listOf(Categoria("Todas", "Todas", 0, true)))
    val categorias: StateFlow<List<Categoria>> = _categorias

    init {
        Log.d("CATALOGO_DEBUG", "ViewModel Iniciado")
        
        viewModelScope.launch {
            repository.obtenerProductosFlow().collect { productos ->
                Log.d("CATALOGO_DEBUG", "Productos recibidos: ${productos.size}")
                _todosLosProductos.value = productos
                aplicarFiltros()
            }
        }
        cargarCategorias()
        cargarProductos()
    }

    private fun cargarCategorias() {
        firestore.collection("categorias")
            .orderBy("orden")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("CATALOGO_DEBUG", "Error al cargar categorías: ${error.message}")
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val catList = snapshot.documents.mapNotNull { doc ->
                        try {
                            // Mapeo manual para asegurar que no falle por tipos
                            Categoria(
                                id = doc.getString("id") ?: "",
                                nombre = doc.getString("nombre") ?: "",
                                orden = doc.getLong("orden")?.toInt() ?: 0,
                                activo = doc.getBoolean("activo") ?: true
                            )
                        } catch (e: Exception) {
                            Log.e("CATALOGO_DEBUG", "Error mapeando categoría: ${e.message}")
                            null
                        }
                    }.filter { it.activo }
                    
                    Log.d("CATALOGO_DEBUG", "Categorías cargadas: ${catList.size}")
                    _categorias.value = listOf(Categoria("Todas", "Todas", 0, true)) + catList
                }
            }
    }

    val isDarkTheme = userPreferencesManager.isDarkTheme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val viewMode = userPreferencesManager.viewMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "grid"
    )

    fun cargarProductos() {
        viewModelScope.launch { useCase.ejecutar() }
    }

    fun seleccionarCategoria(categoria: Categoria) {
        Log.d("CATALOGO_DEBUG", "Categoría seleccionada: ${categoria.nombre} (ID: ${categoria.id})")
        _categoriaSeleccionada.value = categoria.id
        aplicarFiltros()
    }

    fun actualizarBusqueda(texto: String) {
        _textoBusqueda.value = texto
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        var productosFiltrados = _todosLosProductos.value

        if (_categoriaSeleccionada.value != "Todas") {
            productosFiltrados = productosFiltrados.filter { producto ->
                producto.categoria.equals(_categoriaSeleccionada.value, ignoreCase = true)
            }
        }

        if (_textoBusqueda.value.isNotBlank()) {
            productosFiltrados = productosFiltrados.filter {
                it.nombre.contains(_textoBusqueda.value, ignoreCase = true)
            }
        }

        _productos.value = productosFiltrados
    }

    fun agregarAlCarrito(producto: Producto) {
        viewModelScope.launch { repository.agregarProductoAlCarrito(producto) }
    }

    fun toggleFavorito(producto: Producto) {
        viewModelScope.launch {
            if (repository.esFavorito(producto.id)) {
                repository.eliminarDeFavoritos(producto.id)
            } else {
                repository.agregarAFavoritos(producto)
            }
        }
    }

    suspend fun esFavorito(productoId: Int): Boolean = repository.esFavorito(productoId)

    fun cerrarSesion() { firebaseAuth.signOut() }
}
