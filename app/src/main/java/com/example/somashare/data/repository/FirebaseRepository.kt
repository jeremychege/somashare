package com.example.somashare.data.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class FirebaseUserProfile(
    val userId: String = "",
    val email: String = "",
    val fullName: String = "",
    val course: String = "",
    val yearOfStudy: Int = 1,
    val photoUrl: String? = null,
    val uploadedResourcesCount: Int = 0,
    val downloadedResourcesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

data class UploadedResource(
    val resourceId: String = "",
    val paperId: Int = 0,
    val paperName: String = "",
    val unitName: String = "",
    val uploadDate: Long = System.currentTimeMillis()
)

data class DownloadedResource(
    val resourceId: String = "",
    val paperId: Int = 0,
    val paperName: String = "",
    val unitName: String = "",
    val downloadDate: Long = System.currentTimeMillis()
)

class FirebaseRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    // Get current user ID
    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Get user profile from Firestore
    suspend fun getUserProfile(userId: String): Result<FirebaseUserProfile> {
        return try {
            val document = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val profile = document.toObject(FirebaseUserProfile::class.java)
            if (profile != null) {
                Result.success(profile)
            } else {
                Result.failure(Exception("Profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update user profile
    suspend fun updateUserProfile(
        userId: String,
        fullName: String,
        course: String,
        yearOfStudy: Int
    ): Result<Unit> {
        return try {
            val updates = hashMapOf<String, Any>(
                "fullName" to fullName,
                "course" to course,
                "yearOfStudy" to yearOfStudy
            )

            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload profile photo to Firebase Storage
    suspend fun uploadProfilePhoto(userId: String, imageUri: Uri): Result<String> {
        return try {
            val fileName = "profile_photos/${userId}_${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(fileName)

            // Upload the file
            storageRef.putFile(imageUri).await()

            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await().toString()

            // Update Firestore with new photo URL
            firestore.collection("users")
                .document(userId)
                .update("photoUrl", downloadUrl)
                .await()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete profile photo
    suspend fun deleteProfilePhoto(userId: String, photoUrl: String): Result<Unit> {
        return try {
            // Delete from Storage
            val storageRef = storage.getReferenceFromUrl(photoUrl)
            storageRef.delete().await()

            // Update Firestore
            firestore.collection("users")
                .document(userId)
                .update("photoUrl", null)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get uploaded resources
    suspend fun getUploadedResources(userId: String): Result<List<UploadedResource>> {
        return try {
            val snapshot = firestore.collection("uploaded_resources")
                .whereEqualTo("userId", userId)
                .orderBy("uploadDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val resources = snapshot.documents.mapNotNull { doc ->
                doc.toObject(UploadedResource::class.java)
            }

            Result.success(resources)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get downloaded resources
    suspend fun getDownloadedResources(userId: String): Result<List<DownloadedResource>> {
        return try {
            val snapshot = firestore.collection("downloaded_resources")
                .whereEqualTo("userId", userId)
                .orderBy("downloadDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val resources = snapshot.documents.mapNotNull { doc ->
                doc.toObject(DownloadedResource::class.java)
            }

            Result.success(resources)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create initial user profile (call this after registration)
    suspend fun createUserProfile(
        userId: String,
        email: String,
        fullName: String,
        course: String,
        yearOfStudy: Int
    ): Result<Unit> {
        return try {
            val profile = FirebaseUserProfile(
                userId = userId,
                email = email,
                fullName = fullName,
                course = course,
                yearOfStudy = yearOfStudy
            )

            firestore.collection("users")
                .document(userId)
                .set(profile)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Increment uploaded resources count
    suspend fun incrementUploadedCount(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("uploadedResourcesCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Increment downloaded resources count
    suspend fun incrementDownloadedCount(userId: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .update("downloadedResourcesCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}