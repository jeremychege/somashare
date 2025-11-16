package com.example.somashare.data.model

data class User(
    val userId: String = "",
    val email: String = "",
    val fullName: String = "",
    val yearOfStudy: Int = 0,
    val semesterOfStudy: Int = 0,
    val department: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
