package com.example.somashare.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.somashare.data.model.PastPaper
import com.example.somashare.data.model.User
import com.example.somashare.data.repository.AuthRepository
import com.example.somashare.data.repository.PaperRepository
import com.example.somashare.data.repository.StorageRepository
import com.example.somashare.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val uploadedPapers: List<PastPaper> = emptyList(),
    val downloadedPapers: List<PastPaper> = emptyList(),
    val isUploadingPhoto: Boolean = false,
    val uploadPhotoProgress: Int = 0,
    val error: String? = null,
    val successMessage: String? = null
)

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val paperRepository = PaperRepository()
    private val storageRepository = StorageRepository()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load user data
            userRepository.getUserById(userId).collect { user ->
                _uiState.update { it.copy(user = user, isLoading = false) }
            }
        }

        // Load uploaded papers
        viewModelScope.launch {
            paperRepository.getAllPapers(100).collect { papers ->
                val userPapers = papers.filter { it.uploadedBy == userId }
                _uiState.update { it.copy(uploadedPapers = userPapers) }
            }
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, error = null) }

            val uploadResult = storageRepository.uploadProfilePhoto(
                fileUri = uri,
                userId = userId,
                onProgress = { progress ->
                    _uiState.update { it.copy(uploadPhotoProgress = progress) }
                }
            )

            if (uploadResult.isSuccess) {
                val photoUrl = uploadResult.getOrNull() ?: ""
                val updateResult = userRepository.updateProfilePhoto(userId, photoUrl)

                if (updateResult.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            uploadPhotoProgress = 0,
                            successMessage = "Profile photo updated successfully"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isUploadingPhoto = false,
                            uploadPhotoProgress = 0,
                            error = "Failed to update profile photo"
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        uploadPhotoProgress = 0,
                        error = uploadResult.exceptionOrNull()?.message ?: "Upload failed"
                    )
                }
            }
        }
    }

    fun updateProfile(fullName: String, course: String, year: Int, semester: Int, department: String) {
        val userId = authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val updates = mapOf(
                "fullName" to fullName,
                "course" to course,
                "yearOfStudy" to year,
                "semesterOfStudy" to semester,
                "department" to department
            )

            val result = userRepository.updateUser(userId, updates)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Profile updated successfully"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Update failed"
                    )
                }
            }
        }
    }

    fun logout() {
        authRepository.logout()
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}