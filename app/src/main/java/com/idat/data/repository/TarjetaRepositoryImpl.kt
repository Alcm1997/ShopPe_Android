package com.idat.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.idat.domain.model.Tarjeta
import com.idat.domain.repository.TarjetaRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class TarjetaRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : TarjetaRepository {

    private fun userTarjetasCollection(userId: String) =
        firestore.collection("users").document(userId).collection("tarjetas")

    override fun getTarjetas(userId: String): Flow<List<Tarjeta>> = callbackFlow {
        val listener = userTarjetasCollection(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val tarjetas = snapshot?.documents
                    ?.mapNotNull { it.toObject(Tarjeta::class.java)?.copy(id = it.id) }
                    ?: emptyList()
                trySend(tarjetas)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveTarjeta(userId: String, tarjeta: Tarjeta): Result<Unit> = try {
        val collection = userTarjetasCollection(userId)
        val docId = if (tarjeta.id.isBlank()) UUID.randomUUID().toString() else tarjeta.id
        collection.document(docId).set(tarjeta.copy(id = docId)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteTarjeta(userId: String, tarjetaId: String): Result<Unit> = try {
        userTarjetasCollection(userId).document(tarjetaId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
