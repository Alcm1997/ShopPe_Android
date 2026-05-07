package com.idat.presentation.gestion

import com.idat.presentation.components.*
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.idat.domain.model.Producto
import kotlinx.coroutines.launch

enum class GestionMode { LIST, ADD, EDIT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionProductosScreen(
    navController: NavHostController,
    viewModel: GestionProductosViewModel = hiltViewModel()
) {
    val productos by viewModel.productos.collectAsState()
    var currentMode by remember { mutableStateOf(GestionMode.LIST) }
    var selectedProducto by remember { mutableStateOf<Producto?>(null) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val pinkPrimary = MaterialTheme.colorScheme.primary
    val pinkContainer = MaterialTheme.colorScheme.primaryContainer

    Scaffold(
        topBar = {
            if (currentMode == GestionMode.LIST) {
                TopAppBar(
                    title = { Text("Gestión de Productos", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = pinkPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("admin_comprobantes") }) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = "Comprobantes", tint = pinkPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentMode == GestionMode.LIST) {
                FloatingActionButton(
                    onClick = { currentMode = GestionMode.ADD },
                    shape = RoundedCornerShape(20.dp),
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(brush = Brush.linearGradient(colors = listOf(pinkPrimary, pinkContainer))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar producto", modifier = Modifier.size(32.dp))
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Crossfade(targetState = currentMode, label = "ScreenTransition") { mode ->
            when (mode) {
                GestionMode.LIST -> {
                    GestionListaView(
                        paddingValues = paddingValues,
                        productos = productos,
                        onEdit = { 
                            selectedProducto = it
                            currentMode = GestionMode.EDIT
                        },
                        onDelete = { producto ->
                            viewModel.eliminarProducto(producto.id, onSuccess = {
                                scope.launch { snackbarHostState.showSnackbar("Producto eliminado correctamente") }
                            }, onError = { error ->
                                scope.launch { snackbarHostState.showSnackbar(error) }
                            })
                        },
                        onAddClick = { currentMode = GestionMode.ADD }
                    )
                }
                GestionMode.ADD -> {
                    ProductFormView(
                        titulo = "Nuevo Producto",
                        producto = null,
                        onBack = { currentMode = GestionMode.LIST },
                        onSave = { nombre, precio, desc, cat, img, cal, cant ->
                            viewModel.crearProducto(nombre, precio, desc, cat, img, cal, cant, onSuccess = {
                                currentMode = GestionMode.LIST
                                scope.launch { snackbarHostState.showSnackbar("Producto creado") }
                            }, onError = { error ->
                                scope.launch { snackbarHostState.showSnackbar(error) }
                            })
                        }
                    )
                }
                GestionMode.EDIT -> {
                    ProductFormView(
                        titulo = "Editar Producto",
                        producto = selectedProducto,
                        onBack = { currentMode = GestionMode.LIST },
                        onSave = { nombre, precio, desc, cat, img, cal, cant ->
                            selectedProducto?.let { old ->
                                val updated = old.copy(nombre = nombre, precio = precio, descripcion = desc, categoria = cat, imagen = img, calificacion = cal, cantidadCalificaciones = cant)
                                viewModel.actualizarProducto(updated, onSuccess = {
                                    currentMode = GestionMode.LIST
                                    scope.launch { snackbarHostState.showSnackbar("Producto actualizado") }
                                }, onError = { error ->
                                    scope.launch { snackbarHostState.showSnackbar(error) }
                                })
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GestionListaView(
    paddingValues: PaddingValues,
    productos: List<Producto>,
    onEdit: (Producto) -> Unit,
    onDelete: (Producto) -> Unit,
    onAddClick: () -> Unit
) {
    val pinkPrimary = MaterialTheme.colorScheme.primary
    val pinkContainer = MaterialTheme.colorScheme.primaryContainer

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Inventario General", 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Medium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Mis Productos", 
                fontSize = 32.sp, 
                fontWeight = FontWeight.ExtraBold, 
                color = MaterialTheme.colorScheme.onSurface, 
                letterSpacing = (-1).sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = pinkPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${productos.size} Items", 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(productos) { producto ->
            ProductItemPill(producto, onEdit = { onEdit(producto) }, onDelete = { onDelete(producto) })
        }

        item {
            // Suggestion Box (Bento Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .clickable { onAddClick() }
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddCircle, 
                        contentDescription = null, 
                        tint = pinkPrimary.copy(alpha = 0.5f), 
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¿Tienes algo nuevo?", 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Agrega un producto más a tu catálogo", 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onAddClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .background(brush = Brush.linearGradient(colors = listOf(pinkPrimary, pinkContainer)))
                                .padding(horizontal = 32.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Subir Producto", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProductItemPill(producto: Producto, onEdit: () -> Unit, onDelete: () -> Unit) {
    val pinkPrimary = MaterialTheme.colorScheme.primary
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(32.dp), // Más redondeado estilo Bento
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Contenedor de Imagen con Verticalidad y Trasfondo
            Box(
                modifier = Modifier
                    .width(100.dp) // Ancho fijo
                    .height(130.dp) // Mayor altura para verticalidad
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)), // Trasfondo más sólido
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = producto.imagen,
                    contentDescription = producto.nombre,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp), // Margen interno para que el producto respire
                    contentScale = ContentScale.Fit // Muestra el producto COMPLETO
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = pinkPrimary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "ID: #SP-${producto.id.toString().takeLast(4)}", 
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.ExtraBold, 
                        color = pinkPrimary, 
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = producto.nombre, 
                    fontWeight = FontWeight.Bold, 
                    fontSize = 18.sp, 
                    lineHeight = 22.sp,
                    maxLines = 2, 
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "S/ ${producto.precio}", 
                    fontWeight = FontWeight.Black, 
                    fontSize = 22.sp, 
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            // Acciones laterales estilizadas
            Column(
                modifier = Modifier.padding(start = 8.dp), 
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FilledTonalIconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = pinkPrimary.copy(alpha = 0.1f),
                        contentColor = pinkPrimary
                    )
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp))
                }
                
                FilledTonalIconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormView(
    titulo: String,
    producto: Producto?,
    onBack: () -> Unit,
    onSave: (String, Double, String, String, String, Double, Int) -> Unit
) {
    var nombre by remember { mutableStateOf(producto?.nombre ?: "") }
    var precio by remember { mutableStateOf(producto?.precio?.toString() ?: "") }
    var descripcion by remember { mutableStateOf(producto?.descripcion ?: "") }
    var categoria by remember { mutableStateOf(producto?.categoria ?: "Hogar y Decoración") }
    var imagen by remember { mutableStateOf(producto?.imagen ?: "") }
    var calificacion by remember { mutableStateOf(producto?.calificacion?.toString() ?: "5.0") }
    var cantidadCalificaciones by remember { mutableStateOf(producto?.cantidadCalificaciones?.toString() ?: "0") }

    val pinkPrimary = MaterialTheme.colorScheme.primary
    val pinkContainer = MaterialTheme.colorScheme.primaryContainer

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Productos", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = pinkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column {
                Text(
                    text = titulo, 
                    fontSize = 32.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    letterSpacing = (-1).sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Complete los detalles a continuación para el catálogo.", 
                    fontSize = 14.sp, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Identity Card (Bento Style)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FormFieldLabel("Nombre del Producto")
                FormTextField(value = nombre, onValueChange = { nombre = it }, placeholder = "Ej. Jarrón de Cerámica")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel("Precio (S/)")
                        FormTextField(value = precio, onValueChange = { precio = it }, placeholder = "0.00", isNumber = true, prefix = "S/")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormFieldLabel("Categoría")
                        CategorySelector(selected = categoria, onSelect = { categoria = it })
                    }
                }
            }

            // Description & Image Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FormFieldLabel("Descripción")
                FormTextField(value = descripcion, onValueChange = { descripcion = it }, placeholder = "Detalles del producto...", singleLine = false, minLines = 4)

                FormFieldLabel("URL de Imagen")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FormTextField(value = imagen, onValueChange = { imagen = it }, placeholder = "https://...")
                    }
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        AsyncImage(model = imagen, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
            }

            // Save Button
            Button(
                onClick = { 
                    val p = precio.toDoubleOrNull() ?: 0.0
                    val c = calificacion.toDoubleOrNull() ?: 5.0
                    val r = cantidadCalificaciones.toIntOrNull() ?: 0
                    onSave(nombre, p, descripcion, categoria, imagen, c, r)
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brush = Brush.linearGradient(colors = listOf(pinkPrimary, pinkContainer))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Guardar Producto", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            
            Text(
                text = "Este producto será visible inmediatamente en el catálogo.", 
                textAlign = TextAlign.Center, 
                fontSize = 11.sp, 
                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            )
        }
    }
}
