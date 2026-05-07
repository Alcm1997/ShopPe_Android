package com.idat.domain.repository

import com.idat.domain.model.Tarjeta
import kotlinx.coroutines.flow.Flow

interface TarjetaRepository {
    fun getTarjetas(userId: String): Flow<List<Tarjeta>>
    suspend fun saveTarjeta(userId: String, tarjeta: Tarjeta): Result<Unit>
    suspend fun deleteTarjeta(userId: String, tarjetaId: String): Result<Unit>
}
