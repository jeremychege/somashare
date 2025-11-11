package com.example.somashare.data.model

data class PastPaper(
    val paperId: Int = 0,
    val paperName: String,
    val unit: Unit,
    val yearOfStudy: Int,
    val semesterOfStudy: Int,
    val paperYear: Int,
    val paperType: PaperType,
    val filePath: String,
    val fileSize: Long? = null,
    val uploadDate: Long = System.currentTimeMillis(),
    val downloadCount: Int = 0,
    val isVerified: Boolean = false,
    val averageRating: Float? = null,
    val ratingCount: Int = 0
)

enum class PaperType(val displayName: String) {
    FINAL_EXAM("Final Exam"),
    MIDTERM("Midterm"),
    CAT_1("CAT 1"),
    CAT_2("CAT 2"),
    ASSIGNMENT("Assignment");

    companion object {
        fun fromString(value: String): PaperType {
            return PaperType.entries.find { it.displayName == value } ?: FINAL_EXAM
        }
    }
}