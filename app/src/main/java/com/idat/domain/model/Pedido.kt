package com.idat.domain.model

import java.util.Date

data class Pedido(
    val id: String = "",
    val numComprobante: String = "", // Ejemplo: B001-000001
    val fecha: Long = System.currentTimeMillis(),
    val estado: String = "Pendiente",
    val total: Double = 0.0,
    val items: List<ItemCarrito> = emptyList(),
    val userId: String = "",
    val clienteEmail: String = "",
    val clienteNombre: String = "",
    val direccion: String = "",
    val dni: String = ""
)
