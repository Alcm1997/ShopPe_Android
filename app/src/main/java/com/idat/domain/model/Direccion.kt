package com.idat.domain.model

data class Direccion(
    val id: String = "",
    val nombreLugar: String = "", // "Casa", "Oficina", "Depa"
    val calle: String = "",
    val receptor: String = "",
    val esPredeterminada: Boolean = false,
    val latitud: Double? = null,
    val longitud: Double? = null
)
