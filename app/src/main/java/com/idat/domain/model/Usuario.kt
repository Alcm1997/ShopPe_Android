package com.idat.domain.model

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val dni: String = "",
    val telefono: String = "",
    val direccion: String = "",
    val distrito: String = "",
    val departamento: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val fotoUrl: String = ""
)
