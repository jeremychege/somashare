package com.example.somashare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "unit_lecturers",
    foreignKeys = [
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["unitId"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = LecturerEntity::class,
            parentColumns = ["lecturerId"],
            childColumns = ["lecturerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["unitId"]), Index(value = ["lecturerId"])]
)
data class UnitLecturerEntity(
    @PrimaryKey(autoGenerate = true)
    val unitLecturerId: Int = 0,
    val unitId: Int,
    val lecturerId: Int,
    val isPrimary: Boolean = false,
    val academicYear: String?
)