package com.example.somashare.data.local.dao

import androidx.room.*
import com.example.somashare.data.local.entity.PaperDownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaperDownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: PaperDownloadEntity)

    @Query("SELECT * FROM paper_downloads WHERE userId = :userId ORDER BY downloadedAt DESC")
    fun getDownloadHistoryForUser(userId: Int): Flow<List<PaperDownloadEntity>>

    @Query("SELECT COUNT(*) FROM paper_downloads WHERE paperId = :paperId")
    suspend fun getDownloadCountForPaper(paperId: Int): Int

    @Query("DELETE FROM paper_downloads WHERE userId = :userId")
    suspend fun clearDownloadHistory(userId: Int)
}