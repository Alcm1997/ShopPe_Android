package com.idat.presentation.ayuda

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AyudaScreen(
    navController: NavHostController,
    viewModel: AyudaViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val expandedFaqId by viewModel.expandedFaqId.collectAsState()

    val pinkPrimary = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centro de Ayuda", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = { 
                        navController.navigate("catalogo") {
                            popUpTo("catalogo") { inclusive = true }
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Volver",
                            tint = pinkPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = pinkPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Help,
                        contentDescription = "Ayuda",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "¿En qué podemos ayudarte?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Text(
                "Contacto Rápido", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = pinkPrimary
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ContactCard(
                    icon = Icons.Default.Email,
                    title = "Email",
                    subtitle = "yeffercastillovega24@gmail.com",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = pinkPrimary,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:yeffercastillovega24@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Consulta desde la app ShopPe")
                        }
                        context.startActivity(intent)
                    }
                )
                ContactCard(
                    icon = Icons.Default.Phone,
                    title = "Teléfono",
                    subtitle = "947 837 554",
                    modifier = Modifier.weight(1f),
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = pinkPrimary,
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:947837554") }
                        context.startActivity(intent)
                    }
                )
            }

            Text(
                "Preguntas Frecuentes", 
                style = MaterialTheme.typography.titleMedium, 
                fontWeight = FontWeight.Bold,
                color = pinkPrimary
            )

            // Lista de FAQs
            val faqs = listOf(
                1 to "¿Cómo puedo realizar un pedido?" to "Para realizar un pedido, navega por nuestro catálogo, selecciona los artículos que desees, agrégalos al carrito y sigue el proceso de compra. Necesitarás una cuenta registrada.",
                2 to "¿Cuáles son los métodos de pago?" to "Aceptamos tarjetas de crédito/débito y transferencias. El pago contra entrega no está disponible.",
                3 to "¿Cuánto tiempo tarda el envío?" to "Los envíos estándar tardan entre 3-5 días hábiles. Recibirás un número de seguimiento una vez que tu pedido sea enviado.",
                4 to "¿Puedo devolver un producto?" to "Sí, aceptamos devoluciones dentro de los 30 días posteriores a la compra. El producto debe estar en su estado original.",
                5 to "¿Cómo cambio mi contraseña?" to "Ve a Configuración > Seguridad, luego selecciona 'Cambiar Contraseña'. Deberás ingresar tu contraseña actual y la nueva."
            )
            faqs.forEach { (faq, content) ->
                FaqItem(
                    id = faq.first,
                    question = faq.second,
                    answer = content,
                    isExpanded = expandedFaqId == faq.first,
                    onToggle = { viewModel.toggleFaq(faq.first) },
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = pinkPrimary
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("¿No encontraste lo que buscabas?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Nuestro equipo de soporte está disponible para ayudarte con cualquier consulta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("https://wa.me/51947837554?text=Hola,%20necesito%20ayuda%20con%20la%20app%20ShopPe") }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Chat, "WhatsApp")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contactar por WhatsApp")
                    }
                }
            }
        }
    }
}

@Composable
fun ContactCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(32.dp), tint = iconColor)
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun FaqItem(
    id: Int,
    question: String,
    answer: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    backgroundColor: Color,
    iconColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = question, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Contraer" else "Expandir",
                    tint = iconColor
                )
            }
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = answer, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f), lineHeight = 20.sp)
            }
        }
    }
}
