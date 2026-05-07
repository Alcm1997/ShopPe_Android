package com.idat.presentation.components

import com.google.firebase.auth.FirebaseAuth
import com.idat.core.auth.AdminAccess
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShopPeBottomNavBar(
    modifier: Modifier = Modifier,
    currentSelection: String = "Explore",
    isAdmin: Boolean = false, // Parámetro añadido
    onTabSelected: (String) -> Unit = {},
    // Navbar Actions
    onNavigateToCatalogo: () -> Unit = {},
    onNavigateToFavoritos: () -> Unit = {},
    onNavigateToCarrito: () -> Unit = {},
    onNavigateToGestion: () -> Unit = {},
    onNavigateToPedidos: () -> Unit = {},
    onNavigateToAyuda: () -> Unit = {},
    onNavigateToConfiguracion: () -> Unit = {},
    onNavigateToPersonalizacion: () -> Unit = {},
    onNavigateToDirecciones: () -> Unit = {},
    onNavigateToAdminComprobantes: () -> Unit = {},
    onNavigateToAdminReportes: () -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val pinkPrimary = Color(0xFFAB005A)
    val inactiveColor = Color(0xFF8E6F77)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp, start = 12.dp, end = 12.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = pinkPrimary.copy(alpha = 0.1f),
                spotColor = pinkPrimary.copy(alpha = 0.1f)
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // "Descubrir" solo para clientes o como vista previa para admin
            NavItem(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Explore,
                label = "Descubrir",
                isActive = currentSelection == "Explore",
                onClick = { onTabSelected("Explore"); onNavigateToCatalogo() }
            )

            if (!isAdmin) {
                // Secciones exclusivas de CLIENTES
                NavItem(
                    modifier = Modifier.weight(1f),
                    icon = if (currentSelection == "Favoritos") Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    label = "Favoritos",
                    isActive = currentSelection == "Favoritos",
                    onClick = { onTabSelected("Favoritos"); onNavigateToFavoritos() }
                )
                NavItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.ShoppingBag,
                    label = "Bolsa",
                    isActive = currentSelection == "Bag",
                    onClick = { onTabSelected("Bag"); onNavigateToCarrito() }
                )
            } else {
                // Secciones exclusivas de ADMINISTRADOR
                NavItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Inventory,
                    label = "Inventario",
                    isActive = currentSelection == "Gestion",
                    onClick = { onTabSelected("Gestion"); onNavigateToGestion() }
                )
                
                NavItem(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Assessment,
                    label = "Reportes",
                    isActive = currentSelection == "Reportes",
                    onClick = { onTabSelected("Reportes"); onNavigateToAdminReportes() }
                )
            }
            
            // Perfil dinámico
            var showProfileMenu by remember { mutableStateOf(false) }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                NavItem(
                    icon = Icons.Default.Person,
                    label = "Perfil",
                    isActive = currentSelection == "Perfil" || showProfileMenu,
                    onClick = { showProfileMenu = true }
                )
                
                DropdownMenu(
                    expanded = showProfileMenu,
                    onDismissRequest = { showProfileMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    DropdownMenuItem(
                        text = { Text("Mi Perfil", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = { showProfileMenu = false; onNavigateToConfiguracion() },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = pinkPrimary) }
                    )

                    if (!isAdmin) {
                        // Opciones solo para Clientes
                        DropdownMenuItem(
                            text = { Text("Mis Pedidos", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { showProfileMenu = false; onNavigateToPedidos() },
                            leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = pinkPrimary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Personalización", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { showProfileMenu = false; onNavigateToPersonalizacion() },
                            leadingIcon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = pinkPrimary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Mis Direcciones", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { showProfileMenu = false; onNavigateToDirecciones() },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = pinkPrimary) }
                        )
                    } else {
                        // Opciones solo para Administradores
                        DropdownMenuItem(
                            text = { Text("Gestión de Pedidos", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { showProfileMenu = false; onNavigateToAdminComprobantes() },
                            leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = pinkPrimary) }
                        )
                        DropdownMenuItem(
                            text = { Text("Personalización", color = MaterialTheme.colorScheme.onSurface) },
                            onClick = { showProfileMenu = false; onNavigateToPersonalizacion() },
                            leadingIcon = { Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = pinkPrimary) }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Ayuda", color = MaterialTheme.colorScheme.onSurface) },
                        onClick = { showProfileMenu = false; onNavigateToAyuda() },
                        leadingIcon = { Icon(Icons.Default.HelpOutline, contentDescription = null, tint = pinkPrimary) }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    DropdownMenuItem(
                        text = { Text("Cerrar Sesión", color = MaterialTheme.colorScheme.primary) },
                        onClick = { showProfileMenu = false; onCerrarSesion() },
                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }
            }
        }
    }
}

@Composable
fun NavItem(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val pinkPrimary = Color(0xFFAB005A)
    val inactiveColor = Color(0xFF8E6F77)
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) Color(0xFFFFE8ED) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isActive) pinkPrimary else inactiveColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isActive) pinkPrimary else inactiveColor,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
