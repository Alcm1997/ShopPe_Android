package com.idat.presentation.pago

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.idat.domain.model.Pedido
import com.idat.domain.model.Tarjeta
import com.idat.domain.model.Direccion
import com.idat.domain.repository.UsuarioRepository
import com.idat.domain.repository.PedidoRepository
import com.idat.domain.repository.ProductoRepository
import com.idat.domain.repository.TarjetaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PagoViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository,
    private val productoRepository: ProductoRepository,
    private val usuarioRepository: UsuarioRepository,
    private val tarjetaRepository: TarjetaRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _selectedDireccion = MutableStateFlow<Direccion?>(null)
    val selectedDireccion: StateFlow<Direccion?> = _selectedDireccion.asStateFlow()

    val direcciones = auth.currentUser?.uid?.let { userId ->
        usuarioRepository.getDirecciones(userId)
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems = productoRepository.obtenerCarrito().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val totalAmount: StateFlow<Double> = cartItems.map { items ->
        items.sumOf { it.precio * it.cantidad }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val tarjetas = auth.currentUser?.uid?.let { userId ->
        tarjetaRepository.getTarjetas(userId)
    }?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun seleccionarDireccion(direccion: Direccion) {
        _selectedDireccion.value = direccion
    }

    fun procesarPago(
        cardNumber: String? = null,
        expiryDate: String? = null,
        cardHolderName: String? = null,
        onSuccess: (String) -> Unit, 
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) {
                onError("El carrito está vacío")
                return@launch
            }

            val direccionFinal = _selectedDireccion.value
            if (direccionFinal == null) {
                onError("Debe seleccionar una dirección de envío")
                return@launch
            }

            val user = auth.currentUser
            val userId = user?.uid ?: ""
            
            // Obtener datos reales del usuario desde el repositorio
            val usuarioCompleto = usuarioRepository.getUsuario(userId).firstOrNull()
            
            val customerEmail = usuarioCompleto?.email ?: user?.email ?: ""
            val customerName = if (usuarioCompleto != null) {
                "${usuarioCompleto.nombre} ${usuarioCompleto.apellido}".trim()
            } else {
                user?.displayName ?: customerEmail.substringBefore("@")
            }
            val customerDni = usuarioCompleto?.dni ?: ""
            
            // Mask and Save card if provided
            if (cardNumber != null && cardNumber.isNotBlank()) {
                val last4 = if (cardNumber.length >= 4) cardNumber.takeLast(4) else cardNumber
                val masked = "**** **** **** $last4"
                val card = Tarjeta(
                    numeroEnmascarado = masked,
                    vencimiento = expiryDate ?: "",
                    titular = cardHolderName ?: customerName,
                    tipo = if (cardNumber.startsWith("4")) "Visa" else if (cardNumber.startsWith("5")) "Mastercard" else "Debito/Credito"
                )
                tarjetaRepository.saveTarjeta(userId, card)
            }

            val randomNum = (100000..999999).random()
            val invoiceNum = "B001-$randomNum"

            val pedido = Pedido(
                id = "SP-${UUID.randomUUID().toString().take(6).uppercase()}",
                numComprobante = invoiceNum,
                total = totalAmount.value,
                estado = "Pendiente",
                items = items,
                userId = userId,
                clienteEmail = customerEmail,
                clienteNombre = customerName,
                direccion = "${direccionFinal.nombreLugar}: ${direccionFinal.calle}",
                dni = customerDni // Necesitaremos agregar este campo al modelo Pedido
            )

            val resultSave = pedidoRepository.savePedido(pedido)
            if (resultSave.isSuccess) {
                productoRepository.vaciarCarrito()
                onSuccess(pedido.id)
            } else {
                onError("Error al guardar el pedido: ${resultSave.exceptionOrNull()?.message}")
            }
        }
    }
}
