package com.example.somashare.data.model


data class Units(
    val unitId: String = "",
    val unitCode: String = "",
    val unitName: String = "",
    val yearOfStudy: Int = 0,
    val semesterOfStudy: Int = 0,
    val department: String = "",
    val credits: Int = 3,
    val description: String = "",
    val paperCount: Int = 0,
    val isFavorite: Boolean = false
)