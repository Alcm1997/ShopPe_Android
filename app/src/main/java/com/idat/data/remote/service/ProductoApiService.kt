package com.idat.data.remote.service

import com.idat.data.remote.dto.ProductDto
import retrofit2.http.GET

interface ProductoApiService {
    @GET("products")
    suspend fun getProducts(): List<ProductDto>
}
