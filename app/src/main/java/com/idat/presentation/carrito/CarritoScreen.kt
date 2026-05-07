package com.idat.presentation.carrito

import com.idat.presentation.components.ShopPeBottomNavBar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.idat.domain.model.ItemCarrito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    navController: NavHostController,
    viewModel: CarritoViewModel = hiltViewModel()
) {
    val productos by viewModel.productos.collectAsState()
    val recomendaciones by viewModel.recomendaciones.collectAsState()
    val total = productos.sumOf { it.precio * it.cantidad }
    val itemCount = productos.sumOf { it.cantidad }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Tu Bolsa", 
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp,
                        fontSize = 20.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        navController.navigate("catalogo") {
                            popUpTo("catalogo") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    Text(
                        text = "$itemCount Productos",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 8.dp
            ) {
                // Fixed Bottom Action Area (Checkout Summary)
                if (productos.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 24.dp, bottom = 32.dp)
                    ) {
                        // Summary lines
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), 
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Total", 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.ExtraBold, 
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "S/ ${String.format("%.2f", total)}", 
                                fontSize = 24.sp, 
                                fontWeight = FontWeight.Black, 
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // CTA Button
                        Button(
                            onClick = { navController.navigate("pago") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = "Proceder al Pago",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (productos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = "Bolsa vacía",
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "Tu Bolsa está vacía",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Añade algunos de nuestros mejores productos a tu colección.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 260.dp), // Extra space for checkout bottom sheet
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(productos, key = { it.id }) { item ->
                        ProductoCarritoItem(
                            item = item,
                            onEliminar = { viewModel.eliminarDelCarrito(item.id) },
                            onIncrementar = { viewModel.incrementarCantidad(item) },
                            onDecrementar = { viewModel.decrementarCantidad(item) },
                            onClick = { navController.navigate("detalle/${item.id}") }
                        )
                    }

                    if (recomendaciones.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            RecommendedSetSection(
                                recomendaciones = recomendaciones,
                                onProductClick = { id -> navController.navigate("detalle/$id") }
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun ProductoCarritoItem(
    item: ItemCarrito,
    onEliminar: () -> Unit,
    onIncrementar: () -> Unit,
    onDecrementar: () -> Unit,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Product Image
        Box(
            modifier = Modifier
                .width(128.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = item.imagen,
                contentDescription = item.nombre,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize().padding(8.dp)
            )
        }

        // Product Details
        Column(
            modifier = Modifier
                .weight(1f)
                .height(160.dp)
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top area (Title + Delete)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Talla: Estándar", // Mock variants
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                IconButton(
                    onClick = onEliminar, 
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline, 
                        contentDescription = "Eliminar", 
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Bottom area (Price + Quantity Selector)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "S/ ${item.precio}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Quantity Pill Selector
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove, 
                        contentDescription = "Minus",
                        modifier = Modifier.size(16.dp).clickable { onDecrementar() },
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${item.cantidad}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.Add, 
                        contentDescription = "Plus",
                        modifier = Modifier.size(16.dp).clickable { onIncrementar() },
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun RecommendedSetSection(
    recomendaciones: List<com.idat.domain.model.Producto>,
    onProductClick: (Int) -> Unit
) {
    Column {
        Text(
            text = "COMPLETA TU SET",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            recomendaciones.take(2).forEach { producto ->
                RecommendedItemBento(
                    modifier = Modifier.weight(1f),
                    title = producto.nombre,
                    price = "S/ ${String.format("%.2f", producto.precio)}",
                    imageUrl = producto.imagen,
                    onClick = { onProductClick(producto.id) }
                )
            }
        }
    }
}

@Composable
fun RecommendedItemBento(
    modifier: Modifier = Modifier, 
    title: String, 
    price: String, 
    imageUrl: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Column {
                Text(
                    text = title, 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = price, 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
