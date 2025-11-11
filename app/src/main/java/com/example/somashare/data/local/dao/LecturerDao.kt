package com.example.somashare.data.local.dao

import androidx.room.*
import com.example.somashare.data.local.entity.LecturerEntity
import com.example.somashare.data.local.entity.UnitLecturerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LecturerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLecturer(lecturer: LecturerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLecturers(lecturers: List<LecturerEntity>)

    @Update
    suspend fun updateLecturer(lecturer: LecturerEntity)

    @Delete
    suspend fun deleteLecturer(lecturer: LecturerEntity)

    @Query("SELECT * FROM lecturers WHERE lecturerId = :lecturerId")
    fun getLecturerById(lecturerId: Int): Flow<LecturerEntity?>

    @Query("SELECT * FROM lecturers")
    fun getAllLecturers(): Flow<List<LecturerEntity>>

    // Get lecturers for a specific unit
    @Query("""
        SELECT l.* FROM lecturers l
        INNER JOIN unit_lecturers ul ON l.lecturerId = ul.lecturerId
        WHERE ul.unitId = :unitId
    """)
    fun getLecturersForUnit(unitId: Int): Flow<List<LecturerEntity>>

    // Unit-Lecturer relationship operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnitLecturer(unitLecturer: UnitLecturerEntity)

    @Query("DELETE FROM unit_lecturers WHERE unitId = :unitId AND lecturerId = :lecturerId")
    suspend fun removeUnitLecturer(unitId: Int, lecturerId: Int)
}
