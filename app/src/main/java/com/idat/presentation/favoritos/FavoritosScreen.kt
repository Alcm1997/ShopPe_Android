package com.idat.presentation.favoritos

import com.idat.presentation.components.ShopPeBottomNavBar
import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.idat.domain.model.Producto

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FavoritosScreen(
    navController: NavHostController,
    viewModel: FavoritosViewModel = hiltViewModel()
) {
    val productos by viewModel.favoritos.collectAsState()
    val recomendaciones by viewModel.recomendaciones.collectAsState()
    
    // Usamos los colores del sistema definidos en AppTheme
    val pinkPrimary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = surfaceColor,
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            "Mis Favoritos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = onSurface,
                            letterSpacing = (-0.5).sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        navController.navigate("catalogo") {
                            popUpTo("catalogo") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = pinkPrimary)
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp)) // For centering
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor.copy(alpha = 0.8f))
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            if (productos.isEmpty()) {
                item(span = { GridItemSpan(2) }) {
                   EmptyFavoritesState()
                }
            } else {
                items(productos, key = { it.id }) { producto ->
                    FavoritoCard(
                        producto = producto,
                        onFavoriteClick = { viewModel.eliminarDeFavoritos(producto.id) },
                        onAddToCart = { viewModel.agregarAlCarrito(producto) },
                        onClick = { navController.navigate("detalle/${producto.id}") }
                    )
                }
            }

            // Discovery Section
            item(span = { GridItemSpan(2) }) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recomendaciones para ti",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = onSurface,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "Ver todo",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = pinkPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(recomendaciones) { producto ->
                            RecommendationCard(
                                title = producto.nombre,
                                price = "S/ ${String.format("%.2f", producto.precio)}",
                                imageUrl = producto.imagen,
                                onClick = { navController.navigate("detalle/${producto.id}") }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
fun FavoritoCard(
    producto: Producto,
    onFavoriteClick: () -> Unit,
    onAddToCart: () -> Unit,
    onClick: () -> Unit
) {
    val pinkPrimary = MaterialTheme.colorScheme.primary
    val pinkContainer = MaterialTheme.colorScheme.primaryContainer
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    Column(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            AsyncImage(
                model = producto.imagen,
                contentDescription = producto.nombre,
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentScale = ContentScale.Fit
            )
            
            IconButton(
                onClick = onFavoriteClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = pinkPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            producto.nombre,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = onSurface
        )
        
        Text(
            "S/ ${String.format("%.2f", producto.precio)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = pinkPrimary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Button(
            onClick = onAddToCart,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(listOf(pinkPrimary, pinkContainer))),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Añadir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun RecommendationCard(title: String, price: String, imageUrl: String, onClick: () -> Unit) {
    val pinkPrimary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .width(280.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = title,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(8.dp),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                title, 
                fontWeight = FontWeight.Bold, 
                fontSize = 14.sp, 
                color = onSurface, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis
            )
            Text(
                price, 
                fontWeight = FontWeight.ExtraBold, 
                fontSize = 16.sp, 
                color = pinkPrimary, 
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                "DESCUBRIR",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.secondary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun EmptyFavoritesState() {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFFE2BDC6)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Tu lista está vacía", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = onSurface)
        Text("Explora nuestra tienda y guarda tus productos favoritos aquí.", textAlign = TextAlign.Center, fontSize = 14.sp, color = onSurfaceVariant, modifier = Modifier.padding(start = 32.dp, top = 8.dp, end = 32.dp))
    }
}


