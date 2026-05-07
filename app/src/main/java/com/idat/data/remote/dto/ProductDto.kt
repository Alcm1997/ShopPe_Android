package com.idat.data.remote.dto

data class RatingDto(
    val rate: Double,
    val count: Int
)

data class ProductDto(
    val id: Int,
    val title: String,
    val price: Double,
    val description: String?,
    val category: String?,
    val image: String?,
    val rating: RatingDto?
)
