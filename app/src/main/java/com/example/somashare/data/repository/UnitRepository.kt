package com.example.somashare.data.repository

import com.example.somashare.data.local.dao.LecturerDao
import com.example.somashare.data.local.dao.UnitDao
import com.example.somashare.data.local.dao.UserFavoriteDao
import com.example.somashare.data.local.entity.UnitEntity
import com.example.somashare.data.model.Lecturer
import com.example.somashare.data.model.Unit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class UnitRepository(
    private val unitDao: UnitDao,
    private val lecturerDao: LecturerDao,
    private val favoriteDao: UserFavoriteDao
) {

    fun getUnitById(unitId: Int, userId: Int): Flow<Unit?> {
        return combine(
            unitDao.getUnitById(unitId),
            lecturerDao.getLecturersForUnit(unitId)
        ) { unit, lecturers ->
            unit?.let {
                val isFavorite = favoriteDao.isFavorite(userId, unitId)
                it.toModel(lecturers.map { lec -> lec.toModel() }, isFavorite)
            }
        }
    }

    fun getAllUnits(userId: Int): Flow<List<Unit>> {
        return unitDao.getAllUnits().map { list ->
            list.map { unit ->
                val isFavorite = favoriteDao.isFavorite(userId, unit.unitId)
                unit.toModel(emptyList(), isFavorite)
            }
        }
    }

    fun getUnitsByYearAndSemester(year: Int, semester: Int, userId: Int): Flow<List<Unit>> {
        return unitDao.getUnitsByYearAndSemester(year, semester).map { list ->
            list.map { unit ->
                val isFavorite = favoriteDao.isFavorite(userId, unit.unitId)
                unit.toModel(emptyList(), isFavorite)
            }
        }
    }

    fun getUnitsUpToYear(year: Int, userId: Int): Flow<List<Unit>> {
        return unitDao.getUnitsUpToYear(year).map { list ->
            list.map { unit ->
                val isFavorite = favoriteDao.isFavorite(userId, unit.unitId)
                unit.toModel(emptyList(), isFavorite)
            }
        }
    }

    fun searchUnits(query: String, userId: Int): Flow<List<Unit>> {
        return unitDao.searchUnits(query).map { list ->
            list.map { unit ->
                val isFavorite = favoriteDao.isFavorite(userId, unit.unitId)
                unit.toModel(emptyList(), isFavorite)
            }
        }
    }

    suspend fun insertUnit(unit: Unit): Long {
        return unitDao.insertUnit(unit.toEntity())
    }

    suspend fun insertUnits(units: List<Unit>) {
        unitDao.insertUnits(units.map { it.toEntity() })
    }

    private fun UnitEntity.toModel(lecturers: List<Lecturer>, isFavorite: Boolean) = Unit(
        unitId = unitId,
        unitCode = unitCode,
        unitName = unitName,
        yearOfStudy = yearOfStudy,
        semesterOfStudy = semesterOfStudy,
        department = department,
        credits = credits,
        description = description,
        lecturers = lecturers,
        isFavorite = isFavorite
    )

    private fun Unit.toEntity() = UnitEntity(
        unitId = unitId,
        unitCode = unitCode,
        unitName = unitName,
        yearOfStudy = yearOfStudy,
        semesterOfStudy = semesterOfStudy,
        department = department,
        credits = credits,
        description = description
    )

    private fun com.example.somashare.data.local.entity.LecturerEntity.toModel() = Lecturer(
        lecturerId = lecturerId,
        fullName = fullName,
        email = email,
        department = department,
        phoneNumber = phoneNumber
    )
}