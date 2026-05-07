package com.idat.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.idat.domain.model.Direccion
import com.idat.domain.model.Usuario
import com.idat.domain.repository.UsuarioRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UsuarioRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : UsuarioRepository {

    override fun getUsuario(uid: String): Flow<Usuario?> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val usuario = snapshot?.toObject(Usuario::class.java)
                trySend(usuario)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun guardarUsuario(usuario: Usuario): Result<Unit> {
        return try {
            firestore.collection("users").document(usuario.uid)
                .set(usuario)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDirecciones(uid: String): Flow<List<Direccion>> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .collection("direcciones")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val direcciones = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Direccion::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(direcciones)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun guardarDireccion(uid: String, direccion: Direccion): Result<Unit> {
        return try {
            val collection = firestore.collection("users").document(uid).collection("direcciones")
            if (direccion.id.isEmpty()) {
                collection.add(direccion).await()
            } else {
                collection.document(direccion.id).set(direccion).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarDireccion(uid: String, direccionId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid)
                .collection("direcciones").document(direccionId)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun establecerDireccionPredeterminada(uid: String, direccionId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val collection = firestore.collection("users").document(uid).collection("direcciones")
            
            val querySnapshot = collection.get().await()
            for (doc in querySnapshot.documents) {
                batch.update(doc.reference, "esPredeterminada", doc.id == direccionId)
            }
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
