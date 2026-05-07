package com.idat.domain.repository

import com.idat.domain.model.Direccion
import kotlinx.coroutines.flow.Flow

interface DireccionRepository {
    fun getDirecciones(userId: String): Flow<List<Direccion>>
    suspend fun saveDireccion(userId: String, direccion: Direccion): Result<Unit>
    suspend fun deleteDireccion(userId: String, direccionId: String): Result<Unit>
    suspend fun setPredeterminada(userId: String, direccionId: String): Result<Unit>
}
