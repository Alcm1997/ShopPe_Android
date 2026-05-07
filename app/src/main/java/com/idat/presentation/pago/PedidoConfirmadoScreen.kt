package com.idat.presentation.pago

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.idat.domain.model.Pedido
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoConfirmadoScreen(
    navController: NavHostController,
    pedidoId: String,
    viewModel: PedidoConfirmadoViewModel = hiltViewModel()
) {
    val pedido by viewModel.pedido.collectAsState()
    val isGeneratingPdf by viewModel.isGeneratingPdf.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(pedidoId) {
        viewModel.cargarPedido(pedidoId)
    }

    val pinkPrimary = MaterialTheme.colorScheme.primary
    val pinkContainer = MaterialTheme.colorScheme.primaryContainer
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "ShopPe", 
                            fontWeight = FontWeight.Black, 
                            fontSize = 24.sp, 
                            letterSpacing = (-1).sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = { },
                actions = { },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        if (pedido == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = pinkPrimary)
            }
        } else {
            val currentPedido = pedido!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Confirmation Header
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = pinkPrimary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Pedido Confirmado",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Tu pedido ha sido procesado con éxito. ¡Gracias por confiar en ShopPe!",
                    fontSize = 14.sp,
                    color = onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Order Metadata Bento
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OrderInfoBox(
                        modifier = Modifier.weight(1f), 
                        label = "ORDEN NO.", 
                        value = "#${currentPedido.id.takeLast(6).uppercase()}", 
                        valueColor = pinkPrimary, 
                        surfaceColor = surfaceVariant
                    )
                    OrderInfoBox(
                        modifier = Modifier.weight(1f), 
                        label = "FECHA", 
                        value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(currentPedido.fecha)), 
                        valueColor = MaterialTheme.colorScheme.onSurface, 
                        surfaceColor = surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Shipping Address Bento
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(surfaceVariant)
                        .border(0.5.dp, outlineVariant, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, tint = pinkPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                "ENTREGA EN", 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = onSurfaceVariant.copy(alpha = 0.6f), 
                                letterSpacing = 1.sp
                            )
                            Text(
                                currentPedido.direccion.ifEmpty { "Dirección no especificada" }, 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 14.sp, 
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Articles Section
                Text(
                    "Artículos Comprados",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Real Items List
                currentPedido.items.forEach { item ->
                    PurchasedItemRow(
                        title = item.nombre,
                        price = "S/ ${String.format("%.2f", item.precio * item.cantidad)}",
                        imageUrl = item.imagen,
                        cantidad = item.cantidad
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Price Breakdown Box
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(32.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(0.5.dp, outlineVariant, RoundedCornerShape(32.dp))
                        .padding(24.dp)
                ) {
                    val subtotal = currentPedido.total / 1.18
                    val igv = currentPedido.total - subtotal

                    PriceRow("Subtotal", "S/ ${String.format("%.2f", subtotal)}", onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    PriceRow("IGV (18%)", "S/ ${String.format("%.2f", igv)}", onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = outlineVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                        Text(
                            "Total Pago", 
                            fontWeight = FontWeight.ExtraBold, 
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "S/ ${String.format("%.2f", currentPedido.total)}", 
                            fontWeight = FontWeight.Black, 
                            fontSize = 24.sp, 
                            color = pinkPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider(color = outlineVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(
                                "METODO DE PAGO", 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = onSurfaceVariant.copy(alpha = 0.6f), 
                                letterSpacing = 1.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Tarjeta de Crédito", 
                                    fontWeight = FontWeight.Bold, 
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "ESTADO", 
                                fontSize = 10.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = onSurfaceVariant.copy(alpha = 0.6f), 
                                letterSpacing = 1.sp
                            )
                            val (estadoText, estadoColor) = when(currentPedido.estado.uppercase()) {
                                "PENDIENTE" -> Pair(currentPedido.estado.uppercase(), MaterialTheme.colorScheme.tertiary)
                                "PAGADO", "COMPLETADO" -> Pair(currentPedido.estado.uppercase(), Color(0xFF4CAF50))
                                else -> Pair(currentPedido.estado.uppercase(), pinkPrimary)
                            }
                            Text(estadoText, fontWeight = FontWeight.Black, fontSize = 14.sp, color = estadoColor, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Button(
                    onClick = { 
                        viewModel.generarPdf(
                            context,
                            onComplete = { path ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("Factura generada con éxito")
                                    viewModel.abrirComprobante(context, currentPedido.id)
                                }
                            },
                            onError = { error ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(error)
                                }
                            }
                        )
                    },
                    enabled = !isGeneratingPdf,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(brush = Brush.linearGradient(colors = listOf(pinkPrimary, pinkContainer))),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isGeneratingPdf) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            else {
                                Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Descargar PDF", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("catalogo") { popUpTo("catalogo") { inclusive = true } } },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = surfaceVariant)
                ) {
                    Text("Volver a la Tienda", color = pinkPrimary, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // WhatsApp Confirmation Button
                Button(
                    onClick = {
                        val message = "Hola, acabo de realizar un pedido con ID: ${currentPedido.id}. Adjunto mi comprobante para confirmación."
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/51947837554?text=${Uri.encode(message)}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(id = android.R.drawable.stat_notify_chat), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviar pedido por WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun OrderInfoBox(modifier: Modifier, label: String, value: String, valueColor: Color, surfaceColor: Color) {
    Column(
        modifier = modifier
            .clip(CircleShape)
            .background(surfaceColor)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label, 
            fontSize = 10.sp, 
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), 
            letterSpacing = 1.sp
        )
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = valueColor)
    }
}

@Composable
fun PurchasedItemRow(title: String, price: String, imageUrl: String, cantidad: Int = 1) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(6.dp)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                fontWeight = FontWeight.Bold, 
                fontSize = 14.sp, 
                maxLines = 2, 
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Unidad x $cantidad", 
                fontSize = 12.sp, 
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        Text(
            text = price, 
            fontWeight = FontWeight.Black, 
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PriceRow(label: String, value: String, color: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = color)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ShopPeBottomNavBar(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, start = 12.dp, end = 12.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            NavBarItem(icon = Icons.Default.Home, label = "Inicio", active = false, onClick = { navController.navigate("catalogo") })
            NavBarItem(icon = Icons.Default.Storefront, label = "Tienda", active = false, onClick = { navController.navigate("catalogo") })
            NavBarItem(icon = Icons.Default.ReceiptLong, label = "Mis Compras", active = true, onClick = { /* Already here */ })
            NavBarItem(icon = Icons.Default.Person, label = "Perfil", active = false, onClick = { /* Navigate to profile */ })
        }
    }
}

@Composable
fun NavBarItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, active: Boolean, onClick: () -> Unit = {}) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) Color(0xFFFFE8ED) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = if (active) Color(0xFFAB005A) else Color.Gray, modifier = Modifier.size(24.dp))
        Text(label, fontSize = 10.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.SemiBold, color = if (active) Color(0xFFAB005A) else Color.Gray)
    }
}
