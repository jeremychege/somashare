package com.example.somashare.data.local.dao

import androidx.room.*
import com.example.somashare.data.local.entity.UnitEntity
import com.example.somashare.data.local.entity.UserFavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserFavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: UserFavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: UserFavoriteEntity)

    @Query("DELETE FROM user_favorites WHERE userId = :userId AND unitId = :unitId")
    suspend fun removeFavorite(userId: Int, unitId: Int)

    @Query("SELECT * FROM user_favorites WHERE userId = :userId")
    fun getFavoritesForUser(userId: Int): Flow<List<UserFavoriteEntity>>

    // Get favorite units with full unit details
    @Query("""
        SELECT u.* FROM units u
        INNER JOIN user_favorites uf ON u.unitId = uf.unitId
        WHERE uf.userId = :userId
        ORDER BY uf.createdAt DESC
    """)
    fun getFavoriteUnitsForUser(userId: Int): Flow<List<UnitEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE userId = :userId AND unitId = :unitId)")
    suspend fun isFavorite(userId: Int, unitId: Int): Boolean

    @Query("SELECT COUNT(*) FROM user_favorites WHERE userId = :userId")
    suspend fun getFavoriteCount(userId: Int): Int
}
