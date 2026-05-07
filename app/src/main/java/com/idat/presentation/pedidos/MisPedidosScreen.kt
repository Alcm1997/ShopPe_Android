package com.idat.presentation.pedidos

import android.annotation.SuppressLint
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.idat.domain.model.Pedido
import com.idat.presentation.components.ShopPeBottomNavBar
import com.idat.presentation.pago.PedidoConfirmadoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MisPedidosScreen(
    navController: NavHostController,
    viewModel: MisPedidosViewModel = hiltViewModel(),
    pdfViewModel: PedidoConfirmadoViewModel = hiltViewModel()
) {
    val filteredPedidos by viewModel.filteredPedidos.collectAsState(emptyList())
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val totalMes by viewModel.totalGastoMes.collectAsState(0.0)
    val pedidosAnio by viewModel.pedidosEsteAnio.collectAsState("0")
    val puntosShoppe by viewModel.puntosShoppe.collectAsState("0")
    val uiError by viewModel.uiError.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estado para comprobantes locales
    val comprobantesLocales = remember { mutableStateListOf<File>() }

    fun cargarComprobantesLocales() {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        comprobantesLocales.clear()
        dir?.listFiles { file -> file.extension == "pdf" }?.let {
            comprobantesLocales.addAll(it.sortedByDescending { f -> f.lastModified() })
        }
    }

    LaunchedEffect(Unit) {
        cargarComprobantesLocales()
    }

    val pinkPrimary = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.background
    val onSurface = MaterialTheme.colorScheme.onSurface

    Scaffold(
        containerColor = surfaceColor,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (isAdmin) "Gestión de Pedidos (ADMIN)" else "Mis Pedidos", 
                        fontWeight = FontWeight.ExtraBold, 
                        fontSize = 22.sp, 
                        letterSpacing = (-0.5).sp,
                        color = onSurface
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        navController.navigate("catalogo") {
                            popUpTo("catalogo") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = pinkPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { cargarComprobantesLocales() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = pinkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = surfaceColor.copy(alpha = 0.8f))
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
        ) {
            // Quick Filters
            item {
                FilterTabs(
                    selected = selectedFilter,
                    onSelect = { viewModel.setFilter(it) }
                )
            }

            // Purchase Summary Bento
            item {
                PurchaseSummarySection(totalMes, pedidosAnio, puntosShoppe)
            }

            item {
                Text(
                    "Pedidos Recientes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Order List / Error Message / Empty state
            if (uiError != null) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFFFFDAD6))
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiError!!,
                            color = Color(0xFF410002),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (filteredPedidos.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 30.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(60.dp), tint = Color.Gray.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No se encontraron pedidos", color = Color.Gray)
                        }
                    }
                }
            } else {
                items(filteredPedidos) { pedido ->
                    OrderCard(
                        pedido = pedido, 
                        navController = navController,
                        isAdmin = isAdmin,
                        pdfViewModel = pdfViewModel,
                        snackbarHostState = snackbarHostState,
                        scope = scope,
                        onUpdateStatus = { status ->
                            viewModel.actualizarEstadoPedido(pedido.id, status)
                        },
                        onPdfGenerated = { cargarComprobantesLocales() }
                    )
                }
            }

            // --- SECCIÓN DE COMPROBANTES LOCALES ---
            if (comprobantesLocales.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                        Text(
                            "Comprobantes Guardados",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Archivos PDF guardados en este dispositivo.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                items(comprobantesLocales) { archivo ->
                    LocalReceiptCard(
                        file = archivo,
                        onOpen = { pdfViewModel.abrirArchivoPDF(context, archivo) },
                        onDelete = { 
                            archivo.delete()
                            cargarComprobantesLocales()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    pedido: Pedido,
    navController: NavHostController,
    isAdmin: Boolean = false,
    pdfViewModel: PedidoConfirmadoViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onUpdateStatus: (String) -> Unit = {},
    onPdfGenerated: () -> Unit = {}
) {
    val pinkPrimary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    val (statusLabel, statusBg, statusText) = when (pedido.estado.lowercase()) {
        "entregado" -> Triple("Entregado", MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), MaterialTheme.colorScheme.primary)
        "pendiente" -> Triple("Pendiente", MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f), MaterialTheme.colorScheme.secondary)
        "procesando" -> Triple("Procesando", MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f), MaterialTheme.colorScheme.tertiary)
        "cancelado" -> Triple("Cancelado", MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f), MaterialTheme.colorScheme.error)
        else -> Triple("En Camino", MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f), MaterialTheme.colorScheme.primary)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("detalle_pedido/${pedido.id}") },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header: ID and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "#SP-${pedido.id.takeLast(6).uppercase()}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = onSurface
                )
                Surface(
                    color = statusBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        statusLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusText
                    )
                }
            }

            Text(
                SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(pedido.fecha)),
                fontSize = 12.sp,
                color = onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Content: Image and Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                val firstItemImage = if (pedido.items.isNotEmpty()) pedido.items[0].imagen else ""
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    if (firstItemImage.isNotEmpty()) {
                        AsyncImage(
                            model = firstItemImage,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(4.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val productName = if (pedido.items.isNotEmpty()) pedido.items[0].nombre else "Pedido ShopPe"
                    Text(
                        productName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Total: S/ ${String.format("%.2f", pedido.total)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = pinkPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Actions: Main Button and Utility Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { navController.navigate("detalle_pedido/${pedido.id}") },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = pinkPrimary)
                ) {
                    Text("Detalles", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                }

                val context = LocalContext.current
                // PDF Button
                OutlinedIconButton(
                    onClick = {
                        pdfViewModel.generarPdfConPedido(
                            context,
                            pedido,
                            onComplete = {
                                onPdfGenerated()
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "PDF generado",
                                        actionLabel = "ABRIR",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        pdfViewModel.abrirComprobante(context, pedido.id)
                                    }
                                }
                            },
                            onError = { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
                        )
                    },
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(
                        painter = painterResource(id = com.idat.R.drawable.ic_pdf_logo),
                        contentDescription = "PDF",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // WhatsApp Button
                OutlinedIconButton(
                    onClick = {
                        val phoneNumber = "+51947837554"
                        val message = "Ayuda con pedido #SP-${pedido.id.takeLast(6).uppercase()}"
                        try {
                            val url = "https://api.whatsapp.com/send?phone=$phoneNumber&text=${java.net.URLEncoder.encode(message, "UTF-8")}"
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse(url)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Icon(
                        painter = painterResource(id = com.idat.R.drawable.ic_whatsapp_logo),
                        contentDescription = "WhatsApp",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LocalReceiptCard(
    file: File,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    val pinkPrimary = MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(pinkPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = pinkPrimary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = onSurface
                )
                Text(
                    text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(file.lastModified())),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onOpen) {
                Icon(Icons.Default.OpenInNew, contentDescription = "Abrir", tint = pinkPrimary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun FilterTabs(selected: String, onSelect: (String) -> Unit) {
    val filters = listOf("Todos", "Pendiente", "Entregado")
    val pinkPrimary = MaterialTheme.colorScheme.primary
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        items(filters) { filter ->
            val isSelected = filter == selected
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isSelected) pinkPrimary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Text(filter, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium, color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PurchaseSummarySection(totalMes: Double, pedidosAnio: String, puntosShoppe: String) {
    val pinkPrimary = MaterialTheme.colorScheme.primary
    val pinkContainer = MaterialTheme.colorScheme.primaryContainer
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Resumen de Compras", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary, 
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(28.dp)
        ) {
            Column {
                Text(
                    "GASTO TOTAL DEL MES", 
                    fontSize = 11.sp, 
                    fontWeight = FontWeight.Bold, 
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), 
                    letterSpacing = 1.5.sp
                )
                Text(
                    "S/ ${String.format("%.2f", totalMes)}", 
                    fontSize = 36.sp, 
                    fontWeight = FontWeight.Black, 
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(20.dp))
                Surface(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), 
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp), 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.TrendingUp, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.onPrimary, 
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (totalMes > 0) "Activo este mes" else "Sin actividad", 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold, 
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatBox(
                modifier = Modifier.weight(1f), 
                label = "PEDIDOS ESTE AÑO", 
                value = pedidosAnio, 
                icon = Icons.Default.ShoppingBasket, 
                iconColor = MaterialTheme.colorScheme.primary
            )
            StatBox(
                modifier = Modifier.weight(1f), 
                label = "PUNTOS SHOPPE", 
                value = puntosShoppe, 
                icon = Icons.Default.Loyalty, 
                iconColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
fun StatBox(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconColor: Color) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(32.dp))
            .padding(24.dp), 
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
    }
}
