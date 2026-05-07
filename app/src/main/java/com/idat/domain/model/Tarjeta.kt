package com.idat.domain.model

data class Tarjeta(
    val id: String = "",
    val numeroEnmascarado: String = "", // Ejemplo: **** **** **** 1234
    val vencimiento: String = "",
    val titular: String = "",
    val tipo: String = "Desconocida" // Visa, Mastercard, etc.
)
