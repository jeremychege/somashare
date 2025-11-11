package com.example.somashare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey(autoGenerate = true)
    val unitId: Int = 0,
    val unitCode: String,
    val unitName: String,
    val yearOfStudy: Int,
    val semesterOfStudy: Int,
    val department: String?,
    val credits: Int = 3,
    val description: String?,
    val createdAt: Long = System.currentTimeMillis()
)