package com.example.somashare.data.local.dao

import androidx.room.*
import com.example.somashare.data.local.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnit(unit: UnitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<UnitEntity>)

    @Update
    suspend fun updateUnit(unit: UnitEntity)

    @Delete
    suspend fun deleteUnit(unit: UnitEntity)

    @Query("SELECT * FROM units WHERE unitId = :unitId")
    fun getUnitById(unitId: Int): Flow<UnitEntity?>

    @Query("SELECT * FROM units WHERE unitCode = :unitCode LIMIT 1")
    suspend fun getUnitByCode(unitCode: String): UnitEntity?

    @Query("SELECT * FROM units")
    fun getAllUnits(): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE yearOfStudy = :year AND semesterOfStudy = :semester")
    fun getUnitsByYearAndSemester(year: Int, semester: Int): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE yearOfStudy <= :year")
    fun getUnitsUpToYear(year: Int): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE department = :department")
    fun getUnitsByDepartment(department: String): Flow<List<UnitEntity>>

    @Query("SELECT * FROM units WHERE unitName LIKE '%' || :searchQuery || '%' OR unitCode LIKE '%' || :searchQuery || '%'")
    fun searchUnits(searchQuery: String): Flow<List<UnitEntity>>
}
