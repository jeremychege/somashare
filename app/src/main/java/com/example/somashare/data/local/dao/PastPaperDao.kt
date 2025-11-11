package com.example.somashare.data.local.dao

import androidx.room.*
import com.example.somashare.data.local.entity.PastPaperEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PastPaperDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaper(paper: PastPaperEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPapers(papers: List<PastPaperEntity>)

    @Update
    suspend fun updatePaper(paper: PastPaperEntity)

    @Delete
    suspend fun deletePaper(paper: PastPaperEntity)

    @Query("SELECT * FROM past_papers WHERE paperId = :paperId")
    fun getPaperById(paperId: Int): Flow<PastPaperEntity?>

    @Query("SELECT * FROM past_papers WHERE isActive = 1")
    fun getAllActivePapers(): Flow<List<PastPaperEntity>>

    @Query("SELECT * FROM past_papers WHERE unitId = :unitId AND isActive = 1")
    fun getPapersForUnit(unitId: Int): Flow<List<PastPaperEntity>>

    @Query("SELECT * FROM past_papers WHERE unitId = :unitId AND paperType = :paperType AND isActive = 1")
    fun getPapersForUnitByType(unitId: Int, paperType: String): Flow<List<PastPaperEntity>>

    @Query("SELECT * FROM past_papers WHERE yearOfStudy = :year AND semesterOfStudy = :semester AND isActive = 1")
    fun getPapersByYearAndSemester(year: Int, semester: Int): Flow<List<PastPaperEntity>>

    @Query("SELECT * FROM past_papers WHERE yearOfStudy <= :year AND isActive = 1")
    fun getPapersUpToYear(year: Int): Flow<List<PastPaperEntity>>

    @Query("SELECT * FROM past_papers WHERE paperYear = :year AND isActive = 1")
    fun getPapersByYear(year: Int): Flow<List<PastPaperEntity>>

    @Query("UPDATE past_papers SET downloadCount = downloadCount + 1 WHERE paperId = :paperId")
    suspend fun incrementDownloadCount(paperId: Int)

    @Query("UPDATE past_papers SET isVerified = :verified WHERE paperId = :paperId")
    suspend fun updateVerificationStatus(paperId: Int, verified: Boolean)

    @Query("""
        SELECT * FROM past_papers 
        WHERE paperName LIKE '%' || :searchQuery || '%' 
        AND isActive = 1
    """)
    fun searchPapers(searchQuery: String): Flow<List<PastPaperEntity>>
}
