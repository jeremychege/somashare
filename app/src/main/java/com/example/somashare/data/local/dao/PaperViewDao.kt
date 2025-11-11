package com.example.somashare.data.local.dao

import androidx.room.*
import com.example.somashare.data.local.entity.PaperViewEntity
import com.example.somashare.data.local.entity.PastPaperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaperViewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertView(view: PaperViewEntity)

    @Query("SELECT * FROM paper_views WHERE userId = :userId ORDER BY viewedAt DESC")
    fun getViewHistoryForUser(userId: Int): Flow<List<PaperViewEntity>>

    // Get recently viewed papers with full paper details
    @Query("""
        SELECT pp.* FROM past_papers pp
        INNER JOIN paper_views pv ON pp.paperId = pv.paperId
        WHERE pv.userId = :userId
        GROUP BY pp.paperId
        ORDER BY MAX(pv.viewedAt) DESC
        LIMIT :limit
    """)
    fun getRecentlyViewedPapers(userId: Int, limit: Int = 10): Flow<List<PastPaperEntity>>

    @Query("DELETE FROM paper_views WHERE userId = :userId")
    suspend fun clearViewHistory(userId: Int)
}