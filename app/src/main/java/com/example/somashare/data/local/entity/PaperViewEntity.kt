package com.example.somashare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "paper_views",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PastPaperEntity::class,
            parentColumns = ["paperId"],
            childColumns = ["paperId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["paperId"])]
)
data class PaperViewEntity(
    @PrimaryKey(autoGenerate = true)
    val viewId: Int = 0,
    val userId: Int,
    val paperId: Int,
    val viewedAt: Long = System.currentTimeMillis()
)
