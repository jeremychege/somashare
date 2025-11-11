package com.example.somashare.data.local.dao

import androidx.room.*
import com.example.somashare.data.local.entity.UnitEntity
import com.example.somashare.data.local.entity.UserEnrollmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserEnrollmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollment(enrollment: UserEnrollmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnrollments(enrollments: List<UserEnrollmentEntity>)

    @Update
    suspend fun updateEnrollment(enrollment: UserEnrollmentEntity)

    @Delete
    suspend fun deleteEnrollment(enrollment: UserEnrollmentEntity)

    @Query("SELECT * FROM user_enrollments WHERE userId = :userId AND status = 'active'")
    fun getActiveEnrollmentsForUser(userId: Int): Flow<List<UserEnrollmentEntity>>

    // Get enrolled units for a user with full unit details
    @Query("""
        SELECT u.* FROM units u
        INNER JOIN user_enrollments ue ON u.unitId = ue.unitId
        WHERE ue.userId = :userId AND ue.status = 'active'
    """)
    fun getEnrolledUnitsForUser(userId: Int): Flow<List<UnitEntity>>

    @Query("UPDATE user_enrollments SET status = :status WHERE enrollmentId = :enrollmentId")
    suspend fun updateEnrollmentStatus(enrollmentId: Int, status: String)

    @Query("SELECT EXISTS(SELECT 1 FROM user_enrollments WHERE userId = :userId AND unitId = :unitId AND status = 'active')")
    suspend fun isUserEnrolledInUnit(userId: Int, unitId: Int): Boolean
}
