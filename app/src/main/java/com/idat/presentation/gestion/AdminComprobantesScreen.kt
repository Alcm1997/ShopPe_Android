package com.idat.presentation.gestion

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.idat.presentation.pago.PedidoConfirmadoViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminComprobantesScreen(
    navController: NavHostController,
    adminViewModel: AdminComprobantesViewModel = hiltViewModel(),
    pdfViewModel: PedidoConfirmadoViewModel = hiltViewModel()
) {
    val filteredPedidos by adminViewModel.filteredPedidos.collectAsState()
    val searchQuery by adminViewModel.searchQuery.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val pinkPrimary = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Pedidos", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = pinkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            val statusFilter by adminViewModel.statusFilter.collectAsState()
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Historial de Ventas", 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Medium, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Gestión de Pedidos", 
                fontSize = 32.sp, 
                fontWeight = FontWeight.ExtraBold, 
                color = MaterialTheme.colorScheme.onSurface, 
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { adminViewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Buscar por cliente o boleta...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = pinkPrimary) },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = pinkPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf("Pendiente", "Confirmado", "Entregado")
                statuses.forEach { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { adminViewModel.onStatusFilterChange(status) },
                        label = { Text(status, fontSize = 12.sp) },
                        shape = CircleShape,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = pinkPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = statusFilter == status,
                            borderColor = Color.Transparent,
                            selectedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (filteredPedidos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No se encontraron pedidos registrados", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(filteredPedidos) { pedido ->
                        PedidoAdminItem(
                            pedido = pedido, 
                            onAction = {
                                scope.launch {
                                    pdfViewModel.cargarPedido(pedido.id)
                                    pdfViewModel.generarPdf(
                                        context,
                                        onComplete = { pdfViewModel.abrirComprobante(context, pedido.id) },
                                        onError = { /* handle error */ }
                                    )
                                }
                            },
                            onUpdateStatus = { newStatus ->
                                adminViewModel.updatePedidoStatus(
                                    pedidoId = pedido.id,
                                    newStatus = newStatus,
                                    onSuccess = {
                                        scope.launch { snackbarHostState.showSnackbar("Pedido actualizado a $newStatus") }
                                    },
                                    onError = { error ->
                                        scope.launch { snackbarHostState.showSnackbar(error) }
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PedidoAdminItem(
    pedido: com.idat.domain.model.Pedido, 
    onAction: () -> Unit,
    onUpdateStatus: (String) -> Unit
) {
    val pinkPrimary = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pedido.numComprobante.ifEmpty { "Boleta Pendiente" },
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = pedido.clienteNombre,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = pinkPrimary
                    )
                    Text(
                        text = pedido.clienteEmail,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botón para PDF
                    IconButton(
                        onClick = onAction,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(pinkPrimary.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = "Ver Boleta", tint = pinkPrimary, modifier = Modifier.size(20.dp))
                    }
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp), 
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(pedido.fecha)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(CircleShape)
                            .background(
                                when(pedido.estado) {
                                    "Pendiente" -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                                    "Entregado" -> Color(0xFFE8F5E9)
                                    else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = pedido.estado.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when(pedido.estado) {
                                "Pendiente" -> MaterialTheme.colorScheme.error 
                                "Entregado" -> Color(0xFF2E7D32)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
                
                // Botones de acción de estado
                if (pedido.estado == "Pendiente") {
                    Button(
                        onClick = { onUpdateStatus("Entregado") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("MARCAR ENTREGADO", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
