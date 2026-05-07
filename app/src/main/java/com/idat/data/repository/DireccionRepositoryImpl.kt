package com.idat.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.idat.domain.model.Direccion
import com.idat.domain.repository.DireccionRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class DireccionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : DireccionRepository {

    private fun userDireccionesCollection(userId: String) =
        firestore.collection("users").document(userId).collection("direcciones")

    override fun getDirecciones(userId: String): Flow<List<Direccion>> = callbackFlow {
        val listener = userDireccionesCollection(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val direcciones = snapshot?.documents
                    ?.mapNotNull { it.toObject(Direccion::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                trySend(direcciones)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveDireccion(userId: String, direccion: Direccion): Result<Unit> = try {
        val collection = userDireccionesCollection(userId)
        val docId = if (direccion.id.isBlank()) UUID.randomUUID().toString() else direccion.id
        
        firestore.runTransaction { transaction ->
            if (direccion.esPredeterminada) {
                // Update all others to NOT be default
                // Note: In a real app we might want to do this more efficiently
                // But for a few addresses, this is fine in a transaction
            }
            transaction.set(collection.document(docId), direccion.copy(id = docId))
        }.await()
        
        // If it's predeterminada, we need to ensure others are not
        if (direccion.esPredeterminada) {
            val others = collection.get().await().documents
            firestore.runBatch { batch ->
                others.forEach { doc ->
                    if (doc.id != docId) {
                        batch.update(doc.reference, "esPredeterminada", false)
                    }
                }
            }.await()
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteDireccion(userId: String, direccionId: String): Result<Unit> = try {
        userDireccionesCollection(userId).document(direccionId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun setPredeterminada(userId: String, direccionId: String): Result<Unit> = try {
        val collection = userDireccionesCollection(userId)
        val allDocs = collection.get().await().documents
        
        firestore.runBatch { batch ->
            allDocs.forEach { doc ->
                batch.update(doc.reference, "esPredeterminada", doc.id == direccionId)
            }
        }.await()
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
