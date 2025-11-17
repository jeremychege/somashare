package com.example.somashare.data.model

data class Favorite(
    val favoriteId: String = "",
    val userId: String = "",
    val unitId: String = "",
    val unitCode: String = "",
    val unitName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)