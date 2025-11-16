package com.example.somashare.data.repository

import com.example.somashare.data.model.PastPaper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PaperRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Get all papers (real-time)
    fun getAllPapers(limit: Int = 50): Flow<List<PastPaper>> = callbackFlow {
        val listener = firestore.collection("papers")
            .whereEqualTo("isActive", true)
            .orderBy("uploadDate", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val papers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PastPaper::class.java)?.copy(paperId = doc.id)
                } ?: emptyList()

                trySend(papers)
            }

        awaitClose { listener.remove() }
    }

    // Get papers for a unit (real-time)
    fun getPapersForUnit(unitId: String): Flow<List<PastPaper>> = callbackFlow {
        val listener = firestore.collection("papers")
            .whereEqualTo("unitId", unitId)
            .whereEqualTo("isActive", true)
            .orderBy("paperYear", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val papers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PastPaper::class.java)?.copy(paperId = doc.id)
                } ?: emptyList()

                trySend(papers)
            }

        awaitClose { listener.remove() }
    }

    // Get papers by filters
    fun getPapersByFilters(
        yearOfStudy: Int? = null,
        semesterOfStudy: Int? = null,
        paperType: String? = null
    ): Flow<List<PastPaper>> = callbackFlow {
        var query = firestore.collection("papers")
            .whereEqualTo("isActive", true) as Query

        yearOfStudy?.let { query = query.whereEqualTo("yearOfStudy", it) }
        semesterOfStudy?.let { query = query.whereEqualTo("semesterOfStudy", it) }
        paperType?.let { query = query.whereEqualTo("paperType", it) }

        val listener = query
            .orderBy("uploadDate", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val papers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PastPaper::class.java)?.copy(paperId = doc.id)
                } ?: emptyList()

                trySend(papers)
            }

        awaitClose { listener.remove() }
    }

    // Get paper by ID
    fun getPaperById(paperId: String): Flow<PastPaper?> = callbackFlow {
        val listener = firestore.collection("papers")
            .document(paperId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val paper = snapshot?.toObject(PastPaper::class.java)?.copy(paperId = snapshot.id)
                trySend(paper)
            }

        awaitClose { listener.remove() }
    }

    // Upload paper
    suspend fun uploadPaper(paper: PastPaper): Result<String> {
        return try {
            val docRef = firestore.collection("papers").add(paper).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Increment download count
    suspend fun incrementDownloadCount(paperId: String): Result<Unit> {
        return try {
            firestore.collection("papers")
                .document(paperId)
                .update("downloadCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Record paper view
    suspend fun recordView(userId: String, paperId: String, paperName: String, unitCode: String): Result<Unit> {
        return try {
            val view = hashMapOf(
                "userId" to userId,
                "paperId" to paperId,
                "paperName" to paperName,
                "unitCode" to unitCode,
                "viewedAt" to System.currentTimeMillis()
            )

            firestore.collection("paperViews").add(view).await()

            // Also increment view count on paper
            firestore.collection("papers")
                .document(paperId)
                .update("viewCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get recently viewed papers
    fun getRecentlyViewedPapers(userId: String, limit: Int = 10): Flow<List<PastPaper>> = callbackFlow {
        val listener = firestore.collection("paperViews")
            .whereEqualTo("userId", userId)
            .orderBy("viewedAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val paperIds = snapshot?.documents?.mapNotNull { it.getString("paperId") }
                    ?.distinct() ?: emptyList()

                if (paperIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                // Fetch paper details (max 10 for Firestore 'in' query limit)
                firestore.collection("papers")
                    .whereIn("__name__", paperIds.take(10))
                    .get()
                    .addOnSuccessListener { papersSnapshot ->
                        val papers = papersSnapshot.documents.mapNotNull { doc ->
                            doc.toObject(PastPaper::class.java)?.copy(paperId = doc.id)
                        }
                        trySend(papers)
                    }
            }

        awaitClose { listener.remove() }
    }
}
