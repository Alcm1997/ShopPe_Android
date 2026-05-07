package com.idat.domain.repository

import com.idat.domain.model.Direccion
import com.idat.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface UsuarioRepository {
    fun getUsuario(uid: String): Flow<Usuario?>
    suspend fun guardarUsuario(usuario: Usuario): Result<Unit>
    
    // Direcciones
    fun getDirecciones(uid: String): Flow<List<Direccion>>
    suspend fun guardarDireccion(uid: String, direccion: Direccion): Result<Unit>
    suspend fun eliminarDireccion(uid: String, direccionId: String): Result<Unit>
    suspend fun establecerDireccionPredeterminada(uid: String, direccionId: String): Result<Unit>
}
