package com.example.somashare.data.model

data class Unit(
    val unitId: Int = 0,
    val unitCode: String,
    val unitName: String,
    val yearOfStudy: Int,
    val semesterOfStudy: Int,
    val department: String? = null,
    val credits: Int = 3,
    val description: String? = null,
    val lecturers: List<Lecturer> = emptyList(),
    val isFavorite: Boolean = false
)