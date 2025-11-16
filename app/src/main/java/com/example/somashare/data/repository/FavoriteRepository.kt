package com.example.somashare.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FavoriteRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Add to favorites
    suspend fun addFavorite(userId: String, unitId: String, unitCode: String, unitName: String): Result<Unit> {
        return try {
            val favorite = hashMapOf(
                "userId" to userId,
                "unitId" to unitId,
                "unitCode" to unitCode,
                "unitName" to unitName,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("favorites").add(favorite).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Remove from favorites
    suspend fun removeFavorite(userId: String, unitId: String): Result<Unit> {
        return try {
            val snapshot = firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("unitId", unitId)
                .get()
                .await()

            snapshot.documents.forEach { it.reference.delete().await() }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Check if favorited
    suspend fun isFavorite(userId: String, unitId: String): Boolean {
        return try {
            val snapshot = firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("unitId", unitId)
                .get()
                .await()

            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // Get favorite units
    fun getFavoriteUnits(userId: String): Flow<List<String>> = callbackFlow {
        val listener = firestore.collection("favorites")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val unitIds = snapshot?.documents?.mapNotNull {
                    it.getString("unitId")
                } ?: emptyList()

                trySend(unitIds)
            }

        awaitClose { listener.remove() }
    }
}