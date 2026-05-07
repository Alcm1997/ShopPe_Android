package com.idat.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import com.google.firebase.auth.FirebaseAuth
import com.idat.core.auth.AdminAccess
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.idat.presentation.login.LoginScreen
import com.idat.presentation.registro.RegistroScreen
import com.idat.presentation.catalogo.CatalogoScreen
import com.idat.presentation.carrito.CarritoScreen
import com.idat.presentation.detalle.DetalleScreen
import com.idat.presentation.favoritos.FavoritosScreen
import com.idat.presentation.personalizacion.PersonalizacionScreen
import com.idat.presentation.configuracion.ConfiguracionScreen
import com.idat.presentation.gestion.GestionProductosScreen
import com.idat.presentation.ayuda.AyudaScreen
import com.idat.presentation.pago.PagoScreen
import com.idat.presentation.pago.PedidoConfirmadoScreen
import com.idat.presentation.pedidos.MisPedidosScreen
import com.idat.presentation.pedidos.DetallePedidoScreen
import com.idat.presentation.pedidos.MisComprobantesScreen
import com.idat.presentation.tarjetas.MisTarjetasScreen
import com.idat.presentation.gestion.AdminComprobantesScreen
import com.idat.presentation.gestion.AdminReportesScreen
import com.idat.presentation.components.ShopPeBottomNavBar
import androidx.activity.compose.BackHandler
import androidx.navigation.NavOptionsBuilder

@Composable
fun AppNavigation(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    // Detectar si el usuario actual es Administrador
    val currentUser = FirebaseAuth.getInstance().currentUser
    val isAdmin = remember(currentUser) { 
        AdminAccess.isAdminEmail(currentUser?.email) 
    }

    // Lógica de BackStack
    val isMainScreen = currentRoute == "catalogo" || currentRoute == "login" || currentRoute == "registro" || currentRoute?.startsWith("gestion") == true
    
    BackHandler(enabled = !isMainScreen) {
        val destination = if (isAdmin) "gestion/main" else "catalogo"
        navController.navigate(destination) {
            popUpTo(destination) { inclusive = false }
            launchSingleTop = true
        }
    }

    val screensWithoutNavBar = listOf("login", "registro")
    val shouldShowNavBar = currentRoute != null && !screensWithoutNavBar.any { currentRoute.startsWith(it) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (shouldShowNavBar) {
                ShopPeBottomNavBar(
                    isAdmin = isAdmin, // Pasamos el rol a la NavBar
                    currentSelection = when {
                        currentRoute == "catalogo" -> "Explore"
                        currentRoute?.startsWith("favoritos") == true -> "Favoritos"
                        currentRoute?.startsWith("carrito") == true -> "Bag"
                        currentRoute?.startsWith("gestion") == true -> "Gestion"
                        else -> "Perfil"
                    },
                    onNavigateToCatalogo = { 
                        if (currentRoute != "catalogo") {
                            navController.navigate("catalogo") {
                                popToStartDestination(navController)
                            }
                        }
                    },
                    onNavigateToFavoritos = { 
                        if (currentRoute?.startsWith("favoritos") != true) {
                            navController.navigate("favoritos/main") {
                                popToStartDestination(navController)
                            }
                        }
                    },
                    onNavigateToCarrito = { 
                        if (currentRoute != "carrito") {
                            navController.navigate("carrito") {
                                popToStartDestination(navController)
                            }
                        }
                    },
                    onNavigateToPedidos = { 
                        navController.navigate("mis_pedidos") {
                            popToStartDestination(navController)
                        }
                    },
                    onNavigateToPersonalizacion = { 
                        navController.navigate("personalizacion/main") {
                            popToStartDestination(navController)
                        }
                    },
                    onNavigateToConfiguracion = { 
                        navController.navigate("configuracion/main") {
                            popToStartDestination(navController)
                        }
                    },
                    onNavigateToDirecciones = { 
                        navController.navigate("direcciones") {
                            popToStartDestination(navController)
                        }
                    },
                    onNavigateToAdminComprobantes = {
                        navController.navigate("admin_comprobantes") {
                            popToStartDestination(navController)
                        }
                    },
                    onNavigateToAyuda = { 
                        navController.navigate("ayuda/main") {
                            popToStartDestination(navController)
                        }
                    },
                    onNavigateToGestion = { 
                        navController.navigate("gestion/main") {
                            popToStartDestination(navController)
                        }
                    },
                    onNavigateToAdminReportes = {
                        if (currentRoute != "admin_reportes") {
                            navController.navigate("admin_reportes") {
                                popToStartDestination(navController)
                            }
                        }
                    },
                    onCerrarSesion = {
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController, 
            startDestination = "login",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") { LoginScreen(navController) }
            composable("registro") { RegistroScreen(navController) }
            composable("catalogo") { CatalogoScreen(navController) }
            composable("carrito") { CarritoScreen(navController) }
            composable("favoritos/{from}") { backStackEntry ->
                FavoritosScreen(navController) 
            }
            composable("personalizacion/{from}") { PersonalizacionScreen(navController) }
            composable("configuracion/{from}") { ConfiguracionScreen(navController) }
            composable("gestion/{from}") {
                if (isAdmin) {
                    GestionProductosScreen(navController)
                } else {
                    AdminOnlyScreen(onBackToCatalog = { navController.navigate("catalogo") })
                }
            }
            composable("ayuda/{from}") { AyudaScreen(navController) }
            composable("admin_comprobantes") {
                if (isAdmin) {
                    AdminComprobantesScreen(navController)
                } else {
                    AdminOnlyScreen(onBackToCatalog = { navController.navigate("catalogo") })
                }
            }
            composable("admin_reportes") {
                if (isAdmin) {
                    AdminReportesScreen(navController)
                } else {
                    AdminOnlyScreen(onBackToCatalog = { navController.navigate("catalogo") })
                }
            }
            composable("pago") { PagoScreen(navController) }
            composable(
                route = "pedidoConfirmado/{pedidoId}",
                arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""
                PedidoConfirmadoScreen(navController, pedidoId)
            }
            composable("mis_pedidos") { MisPedidosScreen(navController) }
            composable("mis_comprobantes") { MisComprobantesScreen(navController) }
            composable("mis_tarjetas") { MisTarjetasScreen(navController) }
            composable("direcciones") { 
                com.idat.presentation.direcciones.MisDireccionesScreen(navController) 
            }
            composable(
                route = "detalle_pedido/{pedidoId}",
                arguments = listOf(navArgument("pedidoId") { type = NavType.StringType })
            ) { backStackEntry ->
                val pedidoId = backStackEntry.arguments?.getString("pedidoId") ?: ""
                DetallePedidoScreen(navController, pedidoId)
            }
            composable(
                route = "detalle/{productoId}",
                arguments = listOf(navArgument("productoId") { type = NavType.IntType })
            ) { backStackEntry ->
                val productoId = backStackEntry.arguments?.getInt("productoId") ?: 0
                DetalleScreen(navController, productoId)
            }
        }
    }
}

private fun NavOptionsBuilder.popToStartDestination(navController: NavHostController) {
    popUpTo(navController.graph.startDestinationId) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}

@Composable
fun AdminOnlyScreen(onBackToCatalog: () -> Unit) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Panel de administración",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Solo el correo administrador puede gestionar productos.",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
            Button(
                onClick = onBackToCatalog,
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth()
            ) {
                Text("Volver al catálogo")
            }
        }
    }
}
