package com.idat.presentation.detalle

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.idat.presentation.ui.details.ShopPeProductDetailsScreen
import kotlinx.coroutines.launch

@Composable
fun DetalleScreen(
    navController: NavHostController,
    productoId: Int,
    viewModel: DetalleViewModel = hiltViewModel()
) {
    val producto by viewModel.producto.collectAsState()
    val recomendaciones by viewModel.recomendaciones.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(productoId) {
        viewModel.cargarProducto(productoId)
    }

    producto?.let { prod ->
        Box(modifier = Modifier.fillMaxSize()) {
            ShopPeProductDetailsScreen(
                producto = prod,
                recomendaciones = recomendaciones,
                onBackClick = { navController.popBackStack() },
                onAddToCart = {
                    viewModel.agregarAlCarrito(prod)
                    scope.launch {
                        snackbarHostState.showSnackbar("Producto agregado al carrito", duration = SnackbarDuration.Short)
                    }
                },
                onProductClick = { newId ->
                    navController.navigate("detalle/$newId")
                }
            )
            SnackbarHost(
                hostState = snackbarHostState, 
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
