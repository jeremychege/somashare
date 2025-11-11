package com.example.somashare.data.model

data class User(
    val userId: Int = 0,
    val email: String,
    val fullName: String,
    val yearOfStudy: Int,
    val semesterOfStudy: Int,
    val department: String? = null,
    val isActive: Boolean = true
)