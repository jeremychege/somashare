package com.example.somashare.data.repository

import com.example.somashare.data.local.dao.*
import com.example.somashare.data.local.entity.PaperDownloadEntity
import com.example.somashare.data.local.entity.PaperViewEntity
import com.example.somashare.data.local.entity.PastPaperEntity
import com.example.somashare.data.model.PastPaper
import com.example.somashare.data.model.PaperType
import com.example.somashare.data.model.Unit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PastPaperRepository(
    private val paperDao: PastPaperDao,
    private val unitDao: UnitDao,
    private val paperViewDao: PaperViewDao,
    private val paperDownloadDao: PaperDownloadDao,
    private val paperRatingDao: PaperRatingDao
) {

    fun getPapersForUnit(unitId: Int): Flow<List<PastPaper>> {
        return paperDao.getPapersForUnit(unitId).map { papers ->
            papers.map { paper ->
                val unit = unitDao.getUnitById(paper.unitId)
                val avgRating = paperRatingDao.getAverageRatingForPaper(paper.paperId)
                val ratingCount = paperRatingDao.getRatingCountForPaper(paper.paperId)
                // paper.toModel(unit, avgRating, ratingCount) // Simplified for now
                paper.toSimpleModel()
            }
        }
    }

    fun getRecentlyViewedPapers(userId: Int, limit: Int = 10): Flow<List<PastPaper>> {
        return paperViewDao.getRecentlyViewedPapers(userId, limit).map { papers ->
            papers.map { it.toSimpleModel() }
        }
    }

    fun getPapersByYearAndSemester(year: Int, semester: Int): Flow<List<PastPaper>> {
        return paperDao.getPapersByYearAndSemester(year, semester).map { papers ->
            papers.map { it.toSimpleModel() }
        }
    }

    fun searchPapers(query: String): Flow<List<PastPaper>> {
        return paperDao.searchPapers(query).map { papers ->
            papers.map { it.toSimpleModel() }
        }
    }

    suspend fun insertPaper(paper: PastPaper): Long {
        return paperDao.insertPaper(paper.toEntity())
    }

    suspend fun recordPaperView(userId: Int, paperId: Int) {
        val view = PaperViewEntity(
            userId = userId,
            paperId = paperId
        )
        paperViewDao.insertView(view)
    }

    suspend fun recordPaperDownload(userId: Int, paperId: Int) {
        val download = PaperDownloadEntity(
            userId = userId,
            paperId = paperId
        )
        paperDownloadDao.insertDownload(download)
        paperDao.incrementDownloadCount(paperId)
    }

    private fun PastPaperEntity.toSimpleModel() = PastPaper(
        paperId = paperId,
        paperName = paperName,
        unit = Unit(unitId, "", "", 0, 0), // Simplified - load unit separately if needed
        yearOfStudy = yearOfStudy,
        semesterOfStudy = semesterOfStudy,
        paperYear = paperYear,
        paperType = PaperType.fromString(paperType),
        filePath = filePath,
        fileSize = fileSize,
        uploadDate = uploadDate,
        downloadCount = downloadCount,
        isVerified = isVerified
    )

    private fun PastPaper.toEntity() = PastPaperEntity(
        paperId = paperId,
        paperName = paperName,
        unitId = unit.unitId,
        yearOfStudy = yearOfStudy,
        semesterOfStudy = semesterOfStudy,
        paperYear = paperYear,
        paperType = paperType.displayName,
        filePath = filePath,
        fileSize = fileSize,
        uploadDate = uploadDate,
        uploadedBy = null,
        downloadCount = downloadCount,
        isVerified = isVerified
    )
}