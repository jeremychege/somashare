package com.example.somashare.data.repository

import com.example.somashare.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Get user by ID (real-time)
    fun getUserById(userId: String): Flow<User?> = callbackFlow {
        val listener = firestore.collection("users")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val user = snapshot?.toObject(User::class.java)
                trySend(user)
            }

        awaitClose { listener.remove() }
    }

    // Update user profile
    suspend fun updateUser(userId: String, updates: Map<String, Any>): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update profile photo
    suspend fun updateProfilePhoto(userId: String, photoUrl: String): Result<Unit> {
        return updateUser(userId, mapOf("profilePhotoUrl" to photoUrl))
    }

    // Update year and semester
    suspend fun updateYearAndSemester(
        userId: String,
        year: Int,
        semester: Int
    ): Result<Unit> {
        return updateUser(
            userId,
            mapOf(
                "yearOfStudy" to year,
                "semesterOfStudy" to semester
            )
        )
    }
    // Increment uploaded papers count
    suspend fun incrementUploadedPapersCount(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("uploadedPapersCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Increment downloaded papers count
    suspend fun incrementDownloadedPapersCount(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("downloadedPapersCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

