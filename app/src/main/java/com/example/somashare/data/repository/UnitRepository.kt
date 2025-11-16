package com.example.somashare.data.repository

import com.example.somashare.data.model.Unit
import com.example.somashare.data.model.Lecturer
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UnitRepository {
    private val firestore = FirebaseFirestore.getInstance()

    // Get all units (real-time)
    fun getAllUnits(): Flow<List<Unit>> = callbackFlow {
        val listener = firestore.collection("units")
            .orderBy("unitCode")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val units = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Unit::class.java)?.copy(unitId = doc.id)
                } ?: emptyList()

                trySend(units)
            }

        awaitClose { listener.remove() }
    }

    // Get units by year and semester (real-time)
    fun getUnitsByYearAndSemester(year: Int, semester: Int): Flow<List<Unit>> = callbackFlow {
        val listener = firestore.collection("units")
            .whereEqualTo("yearOfStudy", year)
            .whereEqualTo("semesterOfStudy", semester)
            .orderBy("unitCode")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val units = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Unit::class.java)?.copy(unitId = doc.id)
                } ?: emptyList()

                trySend(units)
            }

        awaitClose { listener.remove() }
    }

    // Get unit by ID
    fun getUnitById(unitId: String): Flow<Unit?> = callbackFlow {
        val listener = firestore.collection("units")
            .document(unitId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val unit = snapshot?.toObject(Unit::class.java)?.copy(unitId = snapshot.id)
                trySend(unit)
            }

        awaitClose { listener.remove() }
    }

    // Search units
    fun searchUnits(query: String): Flow<List<Unit>> = callbackFlow {
        val listener = firestore.collection("units")
            .orderBy("unitName")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val allUnits = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Unit::class.java)?.copy(unitId = doc.id)
                } ?: emptyList()

                // Client-side filtering (Firestore doesn't support LIKE)
                val filtered = if (query.isBlank()) {
                    allUnits
                } else {
                    allUnits.filter { unit ->
                        unit.unitCode.contains(query, ignoreCase = true) ||
                                unit.unitName.contains(query, ignoreCase = true)
                    }
                }

                trySend(filtered)
            }

        awaitClose { listener.remove() }
    }

    // Get lecturers for a unit
    fun getLecturersForUnit(unitId: String): Flow<List<Lecturer>> = callbackFlow {
        val listener = firestore.collection("units")
            .document(unitId)
            .collection("lecturers")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val lecturers = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Lecturer::class.java)?.copy(lecturerId = doc.id)
                } ?: emptyList()

                trySend(lecturers)
            }

        awaitClose { listener.remove() }
    }

    // Add new unit
    suspend fun addUnit(unit: Unit): Result<String> {
        return try {
            val docRef = firestore.collection("units").add(unit).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
