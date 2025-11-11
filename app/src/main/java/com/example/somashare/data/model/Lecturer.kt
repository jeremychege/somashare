package com.example.somashare.data.model


data class Lecturer(
    val lecturerId: Int = 0,
    val fullName: String,
    val email: String? = null,
    val department: String? = null,
    val phoneNumber: String? = null
)
