package com.example.somashare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "past_papers",
    foreignKeys = [
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["unitId"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["uploadedBy"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["unitId"]), Index(value = ["uploadedBy"])]
)
data class PastPaperEntity(
    @PrimaryKey(autoGenerate = true)
    val paperId: Int = 0,
    val paperName: String,
    val unitId: Int,
    val yearOfStudy: Int,
    val semesterOfStudy: Int,
    val paperYear: Int,
    val paperType: String, // "Final Exam", "Midterm", "CAT 1", "CAT 2"
    val filePath: String,
    val fileSize: Long?,
    val uploadDate: Long = System.currentTimeMillis(),
    val uploadedBy: Int?,
    val downloadCount: Int = 0,
    val isVerified: Boolean = false,
    val isActive: Boolean = true
)