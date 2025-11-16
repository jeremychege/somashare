package com.example.somashare.data.model

data class PastPaper(
    val paperId: String = "",
    val paperName: String = "",
    val unitId: String = "",
    val unitCode: String = "",
    val unitName: String = "",
    val yearOfStudy: Int = 0,
    val semesterOfStudy: Int = 0,
    val paperYear: Int = 0,
    val paperType: PaperType = PaperType.FINAL_EXAM,
    val filePath: String = "",
    val fileSize: Long = 0,
    val uploadDate: Long = System.currentTimeMillis(),
    val uploadedBy: String = "",
    val downloadCount: Int = 0,
    val viewCount: Int = 0,
    val isVerified: Boolean = false,
    val isActive: Boolean = true,
    val averageRating: Float = 0f,
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