package com.idat.presentation.ui.home

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idat.domain.model.Categoria
import com.idat.domain.model.Producto
import com.idat.presentation.components.ProductItem
import com.idat.presentation.components.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ShopPeHomeScreen(
    isLoading: Boolean = true,
    viewMode: String = "grid",
    products: List<Producto> = emptyList(),
    categorias: List<Categoria> = emptyList(),
    categoriaSeleccionada: String = "",
    textoBusqueda: String = "",
    onSearchTextChanged: (String) -> Unit = {},
    onCategorySelected: (Categoria) -> Unit = {},
    onProductClick: (Producto) -> Unit = {},
    onProductFavorite: (Producto) -> Unit = {},
    onAddToCart: (Producto) -> Unit = {},
    isProductFavorite: suspend (Int) -> Boolean = { false },
    // Navbar Actions
    onNavigateToFavoritos: () -> Unit = {},
    onNavigateToGestion: () -> Unit = {},
    onNavigateToAyuda: () -> Unit = {},
    onNavigateToCarrito: () -> Unit = {},
    onNavigateToPedidos: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onNavigateToPersonalizacion: () -> Unit = {},
    onNavigateToDirecciones: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    // Usamos los colores definidos en AppTheme
    val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
    val configuration = LocalConfiguration.current
    val isCompactWidth = configuration.screenWidthDp < 360
    val isCompactHeight = configuration.screenHeightDp < 700

    val horizontalPadding = if (isCompactWidth) 16.dp else 24.dp
    val topSpacing = if (isCompactHeight) 8.dp else 16.dp
    val searchToChipsSpacing = if (isCompactHeight) 16.dp else 24.dp
    val chipsToProductsSpacing = if (isCompactHeight) 20.dp else 32.dp

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ShopPe",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        letterSpacing = (-1).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = { },
                actions = {
                    IconButton(onClick = { /* Open Search */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = horizontalPadding)
        ) {
            Spacer(modifier = Modifier.height(topSpacing))

            // Search Bar
            val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
            TextField(
                value = textoBusqueda,
                onValueChange = onSearchTextChanged,
                placeholder = { Text("Buscar colección...", color = outlineColor) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon", tint = outlineColor)
                },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { 
                        focusManager.clearFocus()
                        // Aquí podrías disparar una acción de búsqueda explícita si fuera necesario
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(searchToChipsSpacing))

            // Category Chips
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categorias.forEach { categoria ->
                    CategoryChip(
                        text = categoria.nombre, 
                        isSelected = categoriaSeleccionada == categoria.id,
                        onClick = { onCategorySelected(categoria) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(chipsToProductsSpacing))

            // Product View
            if (isLoading && products.isEmpty()) {
                if (viewMode == "list") {
                    ShimmerProductList()
                } else {
                    ShimmerProductGrid()
                }
            } else if (products.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("No se encontraron productos", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                if (viewMode == "list") {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        itemsIndexed(products) { index, producto ->
                            var isFav by remember { mutableStateOf(false) }
                            LaunchedEffect(producto.id) { isFav = isProductFavorite(producto.id) }
                            val showTag = index == 0 || index == 3
                            val tagText = if (index == 0) "LIMITED" else "NEW IN"
                            
                            ProductItem(
                                producto = producto,
                                isFavorite = isFav,
                                onFavoriteClick = {
                                    onProductFavorite(producto)
                                    isFav = !isFav
                                },
                                onClick = { onProductClick(producto) },
                                onAddToCart = { onAddToCart(producto) },
                                showTag = showTag,
                                tagText = tagText
                            )
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(32.dp),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        itemsIndexed(products) { index, producto ->
                            var isFav by remember { mutableStateOf(false) }
                            LaunchedEffect(producto.id) { isFav = isProductFavorite(producto.id) }
                            val showTag = index == 0 || index == 3
                            val tagText = if (index == 0) "LIMITED" else "NEW IN"
                            
                            ProductItem(
                                producto = producto,
                                isFavorite = isFav,
                                onFavoriteClick = {
                                    onProductFavorite(producto)
                                    isFav = !isFav
                                },
                                onClick = { onProductClick(producto) },
                                onAddToCart = { onAddToCart(producto) },
                                showTag = showTag,
                                tagText = tagText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryChip(text: String, isSelected: Boolean = false, onClick: () -> Unit = {}) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .background(containerColor)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun ShimmerProductGrid() {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        items(4) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(3f / 4f).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(60.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                }
            }
        }
    }
}

@Composable
fun ShimmerProductList() {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(32.dp),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        items(3) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.width(60.dp).height(20.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    Box(modifier = Modifier.size(24.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                }
            }
        }
    }
}
