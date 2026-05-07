package com.idat.presentation.catalogo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.idat.presentation.ui.home.ShopPeHomeScreen
import kotlinx.coroutines.launch

@Composable
fun CatalogoScreen(
    navController: NavHostController,
    viewModel: CatalogoViewModel = hiltViewModel(),
) {
    val productos by viewModel.productos.collectAsState()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()
    val textoBusqueda by viewModel.textoBusqueda.collectAsState()
    val categorias by viewModel.categorias.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    
    var mostrarDialogoCerrarSesion by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.cargarProductos()
    }
    
    // Log para depuración
    LaunchedEffect(categorias) {
        println("DEBUG_CATEGORIAS: Recibidas ${categorias.size} categorías: $categorias")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ShopPeHomeScreen(
                isLoading = productos.isEmpty() && textoBusqueda.isEmpty(),
                viewMode = viewMode,
                products = productos,
                categorias = categorias,
                categoriaSeleccionada = categoriaSeleccionada,
                textoBusqueda = textoBusqueda,
                onSearchTextChanged = { viewModel.actualizarBusqueda(it) },
                onCategorySelected = { viewModel.seleccionarCategoria(it) },
                onProductClick = { producto -> navController.navigate("detalle/${producto.id}") },
                onProductFavorite = { producto -> viewModel.toggleFavorito(producto) },
                onAddToCart = { producto ->
                    viewModel.agregarAlCarrito(producto)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "${producto.nombre} añadido al carrito",
                            duration = SnackbarDuration.Short
                        )
                    }
                },
                isProductFavorite = { id -> viewModel.esFavorito(id) },
                onNavigateToFavoritos = { navController.navigate("favoritos/fromDrawer") },
                onNavigateToPersonalizacion = { navController.navigate("personalizacion/fromDrawer") },
                onNavigateToConfiguracion = { navController.navigate("configuracion/fromDrawer") },
                onNavigateToGestion = { navController.navigate("gestion/fromDrawer") },
                onNavigateToAyuda = { navController.navigate("ayuda/fromDrawer") },
                onNavigateToCarrito = { navController.navigate("carrito") },
                onNavigateToPedidos = { navController.navigate("mis_pedidos") },
                onNavigateToDirecciones = { navController.navigate("direcciones") },
                onCerrarSesion = { mostrarDialogoCerrarSesion = true }
            )
        }
    }

    if (mostrarDialogoCerrarSesion) {
        DialogoConfirmacionCerrarSesion(
            onConfirmar = {
                mostrarDialogoCerrarSesion = false
                viewModel.cerrarSesion()
                navController.navigate("login") { popUpTo("catalogo") { inclusive = true } }
            },
            onCancelar = { mostrarDialogoCerrarSesion = false }
        )
    }
}

@Composable
fun DialogoConfirmacionCerrarSesion(onConfirmar: () -> Unit, onCancelar: () -> Unit) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text("Cerrar Sesión", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold) },
        text = { Text("¿Estás seguro de que deseas cerrar sesión?", style = MaterialTheme.typography.bodyMedium) },
        confirmButton = { 
            Button(
                onClick = onConfirmar, 
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), 
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cerrar Sesión") } 
        },
        dismissButton = { 
            OutlinedButton(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") } 
        },
        shape = RoundedCornerShape(16.dp)
    )
}
