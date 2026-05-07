package com.idat.presentation.pago

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PagoScreen(
    navController: NavHostController,
    viewModel: PagoViewModel = hiltViewModel()
) {
    var selectedMethod by remember { mutableStateOf("card") } // "card" or "yape"
    
    val totalAmount by viewModel.totalAmount.collectAsState()
    
    // Form State
    var cardNumber by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardHolderName by remember { mutableStateOf("") }
    
    var showLoading by remember { mutableStateOf(false) }

    val selectedDireccion by viewModel.selectedDireccion.collectAsState()
    
    // Validation
    val isFormValid = (if (selectedMethod == "card") {
        cardNumber.filter { it.isDigit() }.length >= 16 && 
        expiryDate.length >= 4 &&
        cvv.length >= 3 && 
        cardHolderName.isNotBlank()
    } else {
        true // Yape is always "valid" as it's just showing a QR
    }) && selectedDireccion != null

    val isDark = MaterialTheme.colorScheme.surface == Color(0xFF1E1E1E)

    val focusManager = LocalFocusManager.current

    // Loading Simulation
    if (showLoading) {
        LaunchedEffect(Unit) {
            viewModel.procesarPago(
                cardNumber = if (selectedMethod == "card") cardNumber else null,
                expiryDate = if (selectedMethod == "card") expiryDate else null,
                cardHolderName = if (selectedMethod == "card") cardHolderName else null,
                onSuccess = { id ->
                    showLoading = false
                    navController.navigate("pedidoConfirmado/$id")
                },
                onError = { error ->
                    showLoading = false
                    // Handle error (e.g. snackbar)
                }
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Método de Pago",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                // Shipping Address Section
                val direcciones by viewModel.direcciones?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList()) }
                val selectedDireccion by viewModel.selectedDireccion.collectAsState()

                Text(
                    "Dirección de Envío",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (direcciones.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f))
                            .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
                            .clickable { navController.navigate("direcciones") }
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Help, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "No tienes direcciones registradas. Toca aquí para añadir una.",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                } else {
                    direcciones.forEach { dir ->
                        val isDirSelected = selectedDireccion?.id == dir.id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDirSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                                .border(1.dp, if (isDirSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, RoundedCornerShape(20.dp))
                                .clickable { viewModel.seleccionarDireccion(dir) }
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = isDirSelected, onClick = { viewModel.seleccionarDireccion(dir) })
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(dir.nombreLugar, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(dir.calle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                    
                    TextButton(
                        onClick = { navController.navigate("direcciones") },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("+ Gestionar direcciones")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Payment Selection
                Text(
                    "Selecciona cómo pagar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                PaymentMethodCard(
                    title = "Visa / Mastercard",
                    subtitle = "Crédito o Débito",
                    icon = Icons.Default.Contactless,
                    isSelected = selectedMethod == "card",
                    onClick = { selectedMethod = "card" }
                )

                Spacer(modifier = Modifier.height(16.dp))

                PaymentMethodCard(
                    title = "Pago con QR (Yape)",
                    subtitle = "Escanea y paga al instante",
                    icon = Icons.Default.QrCode2,
                    isSelected = selectedMethod == "yape",
                    onClick = { selectedMethod = "yape" }
                )

                Spacer(modifier = Modifier.height(32.dp))

                AnimatedVisibility(visible = selectedMethod == "card") {
                    CardFormArea(
                        cardNumber, { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length <= 16) cardNumber = digits
                        },
                        expiryDate, { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length <= 4) expiryDate = digits
                        },
                        cvv, { input ->
                            val digits = input.filter { it.isDigit() }
                            if (digits.length <= 3) cvv = digits
                        },
                        cardHolderName, { cardHolderName = it },
                        focusManager = focusManager
                    )
                }

                AnimatedVisibility(visible = selectedMethod == "yape") {
                    YapeInstructionArea()
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Order Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f))
                        .border(1.dp, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(24.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Total a pagar", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("S/ ${String.format("%.2f", totalAmount)}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primaryContainer, letterSpacing = (-1).sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("1 ITEM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primaryContainer, letterSpacing = 1.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(160.dp))
            }

            // Bottom Action Area
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .padding(24.dp)
                ) {
                    // Progress lines
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.height(4.dp).width(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.height(4.dp).width(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.height(4.dp).width(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer))
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.height(4.dp).width(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { if (isFormValid) showLoading = true },
                        enabled = isFormValid,
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = if (isFormValid) 
                                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                                        else 
                                            listOf(Color.Gray, Color.LightGray)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Confirmar Pago", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Loading Modal Overlay
            if (showLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Procesando Pago...", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentMethodCard(
    title: String, subtitle: String, icon: ImageVector,
    isSelected: Boolean, onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(2.dp, borderColor, RoundedCornerShape(32.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Radio button custom
            Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).border(2.dp, if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardFormArea(
    cardNumber: String, onCardNumberChange: (String) -> Unit,
    expiryDate: String, onExpiryDateChange: (String) -> Unit,
    cvv: String, onCvvChange: (String) -> Unit,
    cardHolderName: String, onCardHolderNameChange: (String) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp)
    ) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Detalles de la Tarjeta", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Default.Contactless, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("NÚMERO DE TARJETA", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = cardNumber,
                onValueChange = onCardNumberChange,
                placeholder = { Text("0000 0000 0000 0000", color = MaterialTheme.colorScheme.outlineVariant) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedBorderColor = Color.Transparent, focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                singleLine = true,
                visualTransformation = { text ->
                    val trimmed = if (text.text.length >= 16) text.text.substring(0..15) else text.text
                    var out = ""
                    for (i in trimmed.indices) {
                        out += trimmed[i]
                        if (i % 4 == 3 && i != 15) out += " "
                    }

                    val offsetMapping = object : OffsetMapping {
                        override fun originalToTransformed(offset: Int): Int {
                            if (offset <= 3) return offset
                            if (offset <= 7) return offset + 1
                            if (offset <= 11) return offset + 2
                            if (offset <= 16) return offset + 3
                            return 19
                        }

                        override fun transformedToOriginal(offset: Int): Int {
                            if (offset <= 4) return offset
                            if (offset <= 9) return offset - 1
                            if (offset <= 14) return offset - 2
                            if (offset <= 19) return offset - 3
                            return 16
                        }
                    }
                    TransformedText(AnnotatedString(out), offsetMapping)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("VENCIMIENTO", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = expiryDate,
                        onValueChange = onExpiryDateChange,
                        placeholder = { Text("MM/YY", color = MaterialTheme.colorScheme.outlineVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
                        visualTransformation = { text ->
                            val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
                            var out = ""
                            for (i in trimmed.indices) {
                                out += trimmed[i]
                                if (i == 1) out += "/"
                            }

                            val offsetMapping = object : OffsetMapping {
                                override fun originalToTransformed(offset: Int): Int {
                                    if (offset <= 1) return offset
                                    if (offset <= 4) return offset + 1
                                    return 5
                                }

                                override fun transformedToOriginal(offset: Int): Int {
                                    if (offset <= 1) return offset
                                    if (offset <= 5) return offset - 1
                                    return 4
                                }
                            }
                            TransformedText(AnnotatedString(out), offsetMapping)
                        }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("CVV", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = onCvvChange,
                        placeholder = { Text("***", color = MaterialTheme.colorScheme.outlineVariant) },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.Help, tint = MaterialTheme.colorScheme.outlineVariant, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("NOMBRE EN LA TARJETA", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = cardHolderName,
                onValueChange = onCardHolderNameChange,
                placeholder = { Text("Ej: JUAN PEREZ", color = MaterialTheme.colorScheme.outlineVariant) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text("PAGO SEGURO ENCRIPTADO POR SHOPPE SECURE", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun YapeInstructionArea() {
    val qrBackground = if (MaterialTheme.colorScheme.surface == Color(0xFF1E1E1E)) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.size(160.dp).background(qrBackground, RoundedCornerShape(16.dp)).padding(16.dp), contentAlignment = Alignment.Center) {
                Image(painter = painterResource(id = com.idat.R.drawable.yape_qr), contentDescription = "QR Code Yape", modifier = Modifier.fillMaxSize())
            }
            Text("Escanea el código", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Abre tu app Yape, selecciona 'Yapear con QR' y escanea este código para finalizar.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
