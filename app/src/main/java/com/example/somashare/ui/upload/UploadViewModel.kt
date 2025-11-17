package com.example.somashare.ui.upload

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.somashare.data.remote.FirebaseStorageService
import com.example.somashare.data.remote.PaperMetadata
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UploadUiState(
    val selectedFileUri: Uri? = null,
    val selectedFileName: String = "",
    val paperName: String = "",
    val unitCode: String = "",
    val unitName: String = "",
    val yearOfStudy: String = "",
    val semester: String = "",
    val paperYear: String = "",
    val paperType: String = "Final Exam",
    val isUploading: Boolean = false,
    val uploadProgress: Int = 0,
    val uploadSuccess: Boolean = false,
    val errorMessage: String? = null
)

class UploadViewModel(application: Application) : AndroidViewModel(application) {

    private val storageService = FirebaseStorageService()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun onFileSelected(uri: Uri) {
        val fileName = getFileName(uri)
        _uiState.update {
            it.copy(
                selectedFileUri = uri,
                selectedFileName = fileName,
                paperName = if (it.paperName.isEmpty()) fileName else it.paperName,
                uploadSuccess = false,
                errorMessage = null
            )
        }
    }

    fun clearSelectedFile() {
        _uiState.update {
            it.copy(
                selectedFileUri = null,
                selectedFileName = "",
                uploadSuccess = false,
                errorMessage = null
            )
        }
    }

    fun updatePaperName(value: String) {
        _uiState.update { it.copy(paperName = value) }
    }

    fun updateUnitCode(value: String) {
        _uiState.update { it.copy(unitCode = value.uppercase()) }
    }

    fun updateUnitName(value: String) {
        _uiState.update { it.copy(unitName = value) }
    }

    fun updateYearOfStudy(value: String) {
        if (value.isEmpty() || value.toIntOrNull() in 1..4) {
            _uiState.update { it.copy(yearOfStudy = value) }
        }
    }

    fun updateSemester(value: String) {
        if (value.isEmpty() || value.toIntOrNull() in 1..2) {
            _uiState.update { it.copy(semester = value) }
        }
    }

    fun updatePaperYear(value: String) {
        if (value.isEmpty() || (value.length <= 4 && value.all { it.isDigit() })) {
            _uiState.update { it.copy(paperYear = value) }
        }
    }

    fun updatePaperType(value: String) {
        _uiState.update { it.copy(paperType = value) }
    }

    fun canUpload(): Boolean {
        val state = _uiState.value
        return state.selectedFileUri != null &&
                state.paperName.isNotBlank() &&
                state.unitCode.isNotBlank() &&
                state.unitName.isNotBlank() &&
                state.yearOfStudy.isNotBlank() &&
                state.semester.isNotBlank() &&
                state.paperYear.isNotBlank()
    }

    fun uploadPaper() {
        if (!canUpload()) {
            _uiState.update {
                it.copy(errorMessage = "Please fill in all required fields")
            }
            return
        }

        val state = _uiState.value
        val fileUri = state.selectedFileUri ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploading = true,
                    uploadProgress = 0,
                    errorMessage = null,
                    uploadSuccess = false
                )
            }

            try {
                val uploadResult = storageService.uploadPastPaper(
                    fileUri = fileUri,
                    fileName = state.paperName,
                    unitCode = state.unitCode,
                    paperYear = state.paperYear.toInt(),
                    paperType = state.paperType,
                    onProgress = { progress ->
                        _uiState.update { it.copy(uploadProgress = progress) }
                    }
                )

                if (uploadResult.isSuccess) {
                    val result = uploadResult.getOrNull()!!

                    val currentUser = auth.currentUser
                    val metadata = PaperMetadata(
                        paperName = state.paperName,
                        unitCode = state.unitCode,
                        unitName = state.unitName,
                        yearOfStudy = state.yearOfStudy.toInt(),
                        semesterOfStudy = state.semester.toInt(),
                        paperYear = state.paperYear.toInt(),
                        paperType = state.paperType,
                        downloadUrl = result.downloadUrl,
                        fileSize = result.fileSize,
                        uploadDate = result.uploadTimestamp,
                        uploadedBy = currentUser?.uid ?: "anonymous",
                        uploaderName = currentUser?.displayName ?: currentUser?.email ?: "Anonymous"
                    )

                    val metadataResult = storageService.savePaperMetadataToFirestore(metadata)

                    if (metadataResult.isSuccess) {
                        _uiState.update {
                            it.copy(
                                isUploading = false,
                                uploadProgress = 100,
                                uploadSuccess = true
                            )
                        }
                        kotlinx.coroutines.delay(2000)
                        resetForm()
                    } else {
                        throw metadataResult.exceptionOrNull() ?: Exception("Failed to save metadata")
                    }
                } else {
                    throw uploadResult.exceptionOrNull() ?: Exception("Upload failed")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isUploading = false,
                        uploadProgress = 0,
                        errorMessage = "Upload failed: ${e.message}"
                    )
                }
            }
        }
    }

    private fun resetForm() {
        _uiState.update { UploadUiState() }
    }

    private fun getFileName(uri: Uri): String {
        val context = getApplication<Application>()
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "document.pdf"
    }
}