package com.example.somashare.data.repository

import com.example.somashare.data.local.dao.UserDao
import com.example.somashare.data.local.entity.UserEntity
import com.example.somashare.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(private val userDao: UserDao) {

    fun getUserById(userId: Int): Flow<User?> {
        return userDao.getUserById(userId).map { it?.toModel() }
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toModel()
    }

    fun getAllActiveUsers(): Flow<List<User>> {
        return userDao.getAllActiveUsers().map { list -> list.map { it.toModel() } }
    }

    suspend fun insertUser(user: User, password: String): Long {
        val entity = user.toEntity(password)
        return userDao.insertUser(entity)
    }

    suspend fun updateUser(user: User) {
        val existingUser = userDao.getUserByEmail(user.email)
        existingUser?.let {
            val updated = it.copy(
                fullName = user.fullName,
                yearOfStudy = user.yearOfStudy,
                semesterOfStudy = user.semesterOfStudy,
                department = user.department,
                updatedAt = System.currentTimeMillis()
            )
            userDao.updateUser(updated)
        }
    }

    suspend fun updateYearAndSemester(userId: Int, year: Int, semester: Int) {
        userDao.updateUserYearAndSemester(userId, year, semester)
    }

    suspend fun authenticateUser(email: String, password: String): User? {
        val user = userDao.getUserByEmail(email)
        return if (user != null && user.passwordHash == hashPassword(password)) {
            user.toModel()
        } else {
            null
        }
    }

    private fun hashPassword(password: String): String {
        // TODO: Implement proper password hashing (bcrypt, argon2, etc.)
        // For now, using simple hash (NOT SECURE - replace in production!)
        return password.hashCode().toString()
    }

    private fun UserEntity.toModel() = User(
        userId = userId,
        email = email,
        fullName = fullName,
        yearOfStudy = yearOfStudy,
        semesterOfStudy = semesterOfStudy,
        department = department,
        isActive = isActive
    )

    private fun User.toEntity(password: String) = UserEntity(
        userId = userId,
        email = email,
        passwordHash = hashPassword(password),
        fullName = fullName,
        yearOfStudy = yearOfStudy,
        semesterOfStudy = semesterOfStudy,
        department = department,
        isActive = isActive
    )


}
