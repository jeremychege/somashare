package com.example.somashare.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lecturers")
data class LecturerEntity(
    @PrimaryKey(autoGenerate = true)
    val lecturerId: Int = 0,
    val fullName: String,
    val email: String?,
    val department: String?,
    val phoneNumber: String?,
    val createdAt: Long = System.currentTimeMillis()
)