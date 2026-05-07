package com.idat.domain.model

data class ItemCarrito(
    val id: Int = 0,
    val nombre: String = "",
    val precio: Double = 0.0,
    val descripcion: String = "",
    val categoria: String = "",
    val imagen: String = "",
    val cantidad: Int = 1
)
