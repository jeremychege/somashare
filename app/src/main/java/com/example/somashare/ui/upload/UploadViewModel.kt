package com.example.somashare.userinterface.upload

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.somashare.data.repository.FirebaseRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class UploadUiState(
    val isLoading: Boolean = false,
    val uploadProgress: Float = 0f,
    val resourceName: String = "",
    val selectedCourse: String = "",
    val selectedYear: Int = 1,
    val selectedSemester: Int = 1,
    val lecturerName: String = "",
    val selectedFileUri: Uri? = null,
    val selectedFileName: String = "",
    val courses: List<String> = listOf(
        "Computer Science",
        "Information Technology",
        "Software Engineering",
        "Business IT",
        "Data Science",
        "Cybersecurity"
    ),
    val error: String? = null,
    val uploadSuccess: Boolean = false
)

class UploadViewModel(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun onResourceNameChange(name: String) {
        _uiState.value = _uiState.value.copy(resourceName = name)
    }

    fun onCourseSelected(course: String) {
        _uiState.value = _uiState.value.copy(selectedCourse = course)
    }

    fun onYearSelected(year: Int) {
        _uiState.value = _uiState.value.copy(selectedYear = year)
    }

    fun onSemesterSelected(semester: Int) {
        _uiState.value = _uiState.value.copy(selectedSemester = semester)
    }

    fun onLecturerNameChange(name: String) {
        _uiState.value = _uiState.value.copy(lecturerName = name)
    }

    fun onFileSelected(uri: Uri, fileName: String) {
        _uiState.value = _uiState.value.copy(
            selectedFileUri = uri,
            selectedFileName = fileName
        )
    }

    fun clearFile() {
        _uiState.value = _uiState.value.copy(
            selectedFileUri = null,
            selectedFileName = ""
        )
    }

    fun uploadResource() {
        viewModelScope.launch {
            val state = _uiState.value

            // Validation
            if (state.resourceName.isBlank()) {
                _uiState.value = state.copy(error = "Please enter a resource name")
                return@launch
            }

            if (state.selectedCourse.isBlank()) {
                _uiState.value = state.copy(error = "Please select a course")
                return@launch
            }

            if (state.selectedFileUri == null) {
                _uiState.value = state.copy(error = "Please select a PDF file")
                return@launch
            }

            val userId = firebaseRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.value = state.copy(error = "User not logged in")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, error = null)

            try {
                // 1. Upload PDF to Firebase Storage
                val fileName = "resources/${UUID.randomUUID()}_${state.selectedFileName}"
                val storageRef = storage.reference.child(fileName)

                // Upload with progress tracking
                val uploadTask = storageRef.putFile(state.selectedFileUri)

                uploadTask.addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toFloat() / 100
                    _uiState.value = _uiState.value.copy(uploadProgress = progress)
                }

                uploadTask.await()

                // 2. Get download URL
                val downloadUrl = storageRef.downloadUrl.await().toString()

                // 3. Get file size
                val fileMetadata = storageRef.metadata.await()
                val fileSize = fileMetadata.sizeBytes

                // 4. Create resource document in Firestore
                val resourceId = UUID.randomUUID().toString()
                val resourceData = hashMapOf(
                    "resourceId" to resourceId,
                    "resourceName" to state.resourceName,
                    "course" to state.selectedCourse,
                    "yearOfStudy" to state.selectedYear,
                    "semester" to state.selectedSemester,
                    "lecturerName" to state.lecturerName.ifBlank { null },
                    "fileUrl" to downloadUrl,
                    "fileName" to state.selectedFileName,
                    "fileSize" to fileSize,
                    "uploadedBy" to userId,
                    "uploadDate" to System.currentTimeMillis(),
                    "downloadCount" to 0,
                    "verified" to false
                )

                firestore.collection("resources")
                    .document(resourceId)
                    .set(resourceData)
                    .await()

                // 5. Add to user's uploaded resources
                val uploadedResourceData = hashMapOf(
                    "resourceId" to resourceId,
                    "paperId" to 0, // You can link this to your local database if needed
                    "paperName" to state.resourceName,
                    "unitName" to state.selectedCourse,
                    "uploadDate" to System.currentTimeMillis(),
                    "userId" to userId
                )

                firestore.collection("uploaded_resources")
                    .add(uploadedResourceData)
                    .await()

                // 6. Increment user's uploaded count
                firebaseRepository.incrementUploadedCount(userId)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    uploadSuccess = true,
                    uploadProgress = 1f
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Upload failed: ${e.message}",
                    uploadProgress = 0f
                )
            }
        }
    }

    fun resetUploadState() {
        _uiState.value = UploadUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}