package com.idat.domain.model

data class Categoria(
    val id: String = "",
    val nombre: String = "",
    val orden: Int = 0,
    val activo: Boolean = true
)
