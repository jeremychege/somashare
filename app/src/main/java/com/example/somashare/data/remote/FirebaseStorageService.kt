package com.example.somashare.data.remote

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class UploadResult(
    val downloadUrl: String,
    val fileName: String,
    val fileSize: Long,
    val uploadTimestamp: Long
)

data class PaperMetadata(
    val paperId: String = "",
    val paperName: String = "",
    val unitId: Int = 0,
    val unitCode: String = "",
    val unitName: String = "",
    val yearOfStudy: Int = 0,
    val semesterOfStudy: Int = 0,
    val paperYear: Int = 0,
    val paperType: String = "",
    val downloadUrl: String = "",
    val fileSize: Long = 0,
    val uploadDate: Long = System.currentTimeMillis(),
    val uploadedBy: String = "",
    val uploaderName: String = "",
    val downloadCount: Int = 0,
    val isVerified: Boolean = false,
    val isActive: Boolean = true
)

class FirebaseStorageService {
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storageRef: StorageReference = storage.reference

    suspend fun uploadPastPaper(
        fileUri: Uri,
        fileName: String,
        unitCode: String,
        paperYear: Int,
        paperType: String,
        onProgress: (Int) -> Unit = {}
    ): Result<UploadResult> {
        return try {
            val timestamp = System.currentTimeMillis()
            val sanitizedFileName = sanitizeFileName(fileName)
            val storagePath = "past_papers/$unitCode/$paperYear/$paperType/$sanitizedFileName"

            val fileRef = storageRef.child(storagePath)
            val uploadTask = fileRef.putFile(fileUri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            val taskSnapshot = uploadTask.await()
            val downloadUrl = fileRef.downloadUrl.await().toString()
            val fileSize = taskSnapshot.metadata?.sizeBytes ?: 0L

            Result.success(
                UploadResult(
                    downloadUrl = downloadUrl,
                    fileName = sanitizedFileName,
                    fileSize = fileSize,
                    uploadTimestamp = timestamp
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun savePaperMetadataToFirestore(metadata: PaperMetadata): Result<String> {
        return try {
            val paperId = metadata.paperId.ifEmpty { UUID.randomUUID().toString() }
            val paperData = metadata.copy(paperId = paperId)

            firestore.collection("past_papers")
                .document(paperId)
                .set(paperData)
                .await()

            Result.success(paperId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllPapers(): Result<List<PaperMetadata>> {
        return try {
            val snapshot = firestore.collection("past_papers")
                .whereEqualTo("isActive", true)
                .orderBy("uploadDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val papers = snapshot.documents.mapNotNull { doc ->
                doc.toObject(PaperMetadata::class.java)
            }

            Result.success(papers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementDownloadCount(paperId: String): Result<Unit> {
        return try {
            firestore.collection("past_papers")
                .document(paperId)
                .update("downloadCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sanitizeFileName(fileName: String): String {
        return fileName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
    }
}