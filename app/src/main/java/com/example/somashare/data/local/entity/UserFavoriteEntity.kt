package com.example.somashare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_favorites",
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
data class UserFavoriteEntity(
    @PrimaryKey(autoGenerate = true)
    val favoriteId: Int = 0,
    val userId: Int,
    val unitId: Int,
    val createdAt: Long = System.currentTimeMillis()
)