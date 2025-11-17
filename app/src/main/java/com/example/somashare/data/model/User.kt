package com.example.somashare.data.model

data class User(
    val userId: String = "",
    val email: String = "",
    val fullName: String = "",
    val yearOfStudy: Int = 0,
    val semesterOfStudy: Int = 0,
    val department: String = "",
    val course: String = "",
    val profilePhotoUrl: String = "",
    val uploadedPapersCount: Int = 0,
    val downloadedPapersCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)