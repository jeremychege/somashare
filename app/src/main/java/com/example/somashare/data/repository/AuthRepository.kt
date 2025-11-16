package com.example.somashare.data.repository

import com.example.somashare.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Current user as Flow
    val currentUser: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    // Register new user
    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        yearOfStudy: Int,
        semesterOfStudy: Int,
        department: String
    ): Result<User> {
        return try {
            // Create auth user
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("User ID is null")

            // Create user document in Firestore
            val user = User(
                userId = userId,
                email = email,
                fullName = fullName,
                yearOfStudy = yearOfStudy,
                semesterOfStudy = semesterOfStudy,
                department = department,
                createdAt = System.currentTimeMillis(),
                isActive = true
            )

            firestore.collection("users")
                .document(userId)
                .set(user)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Login
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user ?: throw Exception("Login failed")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Logout
    fun logout() {
        auth.signOut()
    }

    // Get current user ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Reset password
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}