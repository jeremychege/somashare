package com.example.somashare.data.local.entity


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_enrollments",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["unitId"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["unitId"])]
)
data class UserEnrollmentEntity(
    @PrimaryKey(autoGenerate = true)
    val enrollmentId: Int = 0,
    val userId: Int,
    val unitId: Int,
    val academicYear: String,
    val enrollmentDate: Long = System.currentTimeMillis(),
    val status: String = "active" // "active", "completed", "dropped"
)
