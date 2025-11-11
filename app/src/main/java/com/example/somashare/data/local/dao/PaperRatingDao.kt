package com.example.somashare.data.local.dao

import androidx.room.*
import com.example.somashare.data.local.entity.PaperRatingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaperRatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: PaperRatingEntity)

    @Update
    suspend fun updateRating(rating: PaperRatingEntity)

    @Delete
    suspend fun deleteRating(rating: PaperRatingEntity)

    @Query("SELECT * FROM paper_ratings WHERE paperId = :paperId")
    fun getRatingsForPaper(paperId: Int): Flow<List<PaperRatingEntity>>

    @Query("SELECT * FROM paper_ratings WHERE userId = :userId AND paperId = :paperId LIMIT 1")
    suspend fun getUserRatingForPaper(userId: Int, paperId: Int): PaperRatingEntity?

    @Query("SELECT AVG(rating) FROM paper_ratings WHERE paperId = :paperId")
    suspend fun getAverageRatingForPaper(paperId: Int): Float?

    @Query("SELECT COUNT(*) FROM paper_ratings WHERE paperId = :paperId")
    suspend fun getRatingCountForPaper(paperId: Int): Int

    @Query("UPDATE paper_ratings SET helpfulCount = helpfulCount + 1 WHERE ratingId = :ratingId")
    suspend fun incrementHelpfulCount(ratingId: Int)
}