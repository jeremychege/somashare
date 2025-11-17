package com.example.somashare.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class StorageRepository {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    // Upload profile photo
    suspend fun uploadProfilePhoto(
        fileUri: Uri,
        userId: String,
        onProgress: (Int) -> Unit = {}
    ): Result<String> {
        return try {
            val timestamp = System.currentTimeMillis()
            val path = "profile_photos/$userId/profile_$timestamp.jpg"
            val fileRef = storageRef.child(path)

            val uploadTask = fileRef.putFile(fileUri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            uploadTask.await()
            val downloadUrl = fileRef.downloadUrl.await().toString()

            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Upload PDF file
    suspend fun uploadPdf(
        fileUri: Uri,
        unitId: String,
        fileName: String,
        onProgress: (Int) -> Unit = {}
    ): Result<String> {
        return try {
            val timestamp = System.currentTimeMillis()
            val sanitizedName = fileName.replace(" ", "_").replace("[^a-zA-Z0-9._-]".toRegex(), "")
            val path = "papers/$unitId/${sanitizedName}_$timestamp.pdf"
            val fileRef = storageRef.child(path)

            val uploadTask = fileRef.putFile(fileUri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            uploadTask.await()

            Result.success(path)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get download URL
    suspend fun getDownloadUrl(filePath: String): Result<String> {
        return try {
            val url = storageRef.child(filePath).downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Delete file
    suspend fun deleteFile(filePath: String): Result<Unit> {
        return try {
            storageRef.child(filePath).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get file size
    suspend fun getFileSize(filePath: String): Result<Long> {
        return try {
            val metadata = storageRef.child(filePath).metadata.await()
            Result.success(metadata.sizeBytes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}