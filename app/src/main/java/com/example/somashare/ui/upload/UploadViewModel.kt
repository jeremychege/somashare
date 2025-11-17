package com.example.somashare.ui.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.somashare.data.model.PaperType
import com.example.somashare.data.model.PastPaper
import com.example.somashare.data.model.Unit
import com.example.somashare.data.repository.AuthRepository
import com.example.somashare.data.repository.PaperRepository
import com.example.somashare.data.repository.StorageRepository
import com.example.somashare.data.repository.UnitRepository
import com.example.somashare.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UploadUiState(
    val isLoading: Boolean = false,
    val units: List<Unit> = emptyList(),
    val selectedUnit: Unit? = null,
    val pdfUri: Uri? = null,
    val pdfName: String = "",
    val pdfSize: Long = 0,
    val paperName: String = "",
    val paperYear: Int = 2024,
    val paperType: PaperType = PaperType.FINAL_EXAM,
    val lecturerName: String = "",
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val error: String? = null,
    val uploadedPaperId: String? = null
)

class UploadViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val unitRepository = UnitRepository()
    private val paperRepository = PaperRepository()
    private val storageRepository = StorageRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    init {
        loadUnits()
    }

    private fun loadUnits() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            unitRepository.getAllUnits().collect { units ->
                _uiState.update {
                    it.copy(
                        units = units.sortedBy { unit -> unit.unitCode },
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectUnit(unit: Unit) {
        _uiState.update { it.copy(selectedUnit = unit) }
    }

    fun setPdfUri(uri: Uri, name: String, size: Long) {
        _uiState.update {
            it.copy(
                pdfUri = uri,
                pdfName = name,
                pdfSize = size,
                paperName = if (it.paperName.isEmpty()) name.removeSuffix(".pdf") else it.paperName
            )
        }
    }

    fun updatePaperName(name: String) {
        _uiState.update { it.copy(paperName = name) }
    }

    fun updatePaperYear(year: Int) {
        _uiState.update { it.copy(paperYear = year) }
    }

    fun updatePaperType(type: PaperType) {
        _uiState.update { it.copy(paperType = type) }
    }

    fun updateLecturerName(name: String) {
        _uiState.update { it.copy(lecturerName = name) }
    }

    fun uploadPaper() {
        val state = _uiState.value
        val userId = authRepository.getCurrentUserId()

        if (userId == null) {
            _uiState.update { it.copy(error = "User not authenticated") }
            return
        }

        if (state.pdfUri == null) {
            _uiState.update { it.copy(error = "Please select a PDF file") }
            return
        }

        if (state.selectedUnit == null) {
            _uiState.update { it.copy(error = "Please select a unit") }
            return
        }

        if (state.paperName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a paper name") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, error = null) }

            // Step 1: Upload PDF to Firebase Storage
            val storageResult = storageRepository.uploadPdf(
                fileUri = state.pdfUri,
                unitId = state.selectedUnit.unitId,
                fileName = state.paperName,
                onProgress = { progress ->
                    _uiState.update { it.copy(uploadProgress = progress) }
                }
            )

            if (storageResult.isFailure) {
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        uploadProgress = 0,
                        error = storageResult.exceptionOrNull()?.message ?: "Upload failed"
                    )
                }
                return@launch
            }

            val filePath = storageResult.getOrNull() ?: ""

            // Step 2: Create paper document in Firestore
            val paper = PastPaper(
                paperName = state.paperName,
                unitId = state.selectedUnit.unitId,
                unitCode = state.selectedUnit.unitCode,
                unitName = state.selectedUnit.unitName,
                yearOfStudy = state.selectedUnit.yearOfStudy,
                semesterOfStudy = state.selectedUnit.semesterOfStudy,
                paperYear = state.paperYear,
                paperType = state.paperType,
                filePath = filePath,
                fileSize = state.pdfSize,
                uploadDate = System.currentTimeMillis(),
                uploadedBy = userId,
                downloadCount = 0,
                viewCount = 0,
                isVerified = false,
                isActive = true,
                averageRating = 0f,
                ratingCount = 0
            )

            val paperResult = paperRepository.uploadPaper(paper)

            if (paperResult.isFailure) {
                // Clean up uploaded file
                storageRepository.deleteFile(filePath)

                _uiState.update {
                    it.copy(
                        isUploading = false,
                        uploadProgress = 0,
                        error = "Failed to save paper details"
                    )
                }
                return@launch
            }

            val paperId = paperResult.getOrNull() ?: ""

            // Step 3: Increment user's uploaded papers count
            userRepository.incrementUploadedPapersCount(userId)

            // Step 4: Success!
            _uiState.update {
                it.copy(
                    isUploading = false,
                    uploadProgress = 100,
                    uploadedPaperId = paperId
                )
            }
        }
    }

    fun resetUpload() {
        _uiState.update {
            UploadUiState(
                units = it.units,
                isLoading = false
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}