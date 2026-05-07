package com.idat.domain.model



data class Producto(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val descripcion: String,
    val categoria: String,
    val imagen: String,
    val calificacion: Double = 0.0,
    val cantidadCalificaciones: Int = 0
)
