package com.example.somashare.userinterface.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.somashare.data.repository.DownloadedResource
import com.example.somashare.data.repository.FirebaseRepository
import com.example.somashare.data.repository.FirebaseUserProfile
import com.example.somashare.data.repository.UploadedResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: FirebaseUserProfile? = null,
    val uploadedResources: List<UploadedResource> = emptyList(),
    val downloadedResources: List<DownloadedResource> = emptyList(),
    val error: String? = null,
    val isEditMode: Boolean = false,
    val uploadingPhoto: Boolean = false,
    val showResourcesTab: ResourcesTab = ResourcesTab.UPLOADED
)

enum class ResourcesTab {
    UPLOADED,
    DOWNLOADED
}

class ProfileViewModel(
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = firebaseRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not logged in"
                )
                return@launch
            }

            // Load profile
            val profileResult = firebaseRepository.getUserProfile(userId)
            if (profileResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = profileResult.exceptionOrNull()?.message ?: "Failed to load profile"
                )
                return@launch
            }

            val profile = profileResult.getOrNull()

            // Load uploaded resources
            val uploadedResult = firebaseRepository.getUploadedResources(userId)
            val uploadedResources = uploadedResult.getOrNull() ?: emptyList()

            // Load downloaded resources
            val downloadedResult = firebaseRepository.getDownloadedResources(userId)
            val downloadedResources = downloadedResult.getOrNull() ?: emptyList()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                profile = profile,
                uploadedResources = uploadedResources,
                downloadedResources = downloadedResources
            )
        }
    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = !_uiState.value.isEditMode
        )
    }

    fun updateProfile(fullName: String, course: String, year: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = firebaseRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "User not logged in"
                )
                return@launch
            }

            val result = firebaseRepository.updateUserProfile(
                userId = userId,
                fullName = fullName,
                course = course,
                yearOfStudy = year
            )

            if (result.isSuccess) {
                loadProfile()
                _uiState.value = _uiState.value.copy(isEditMode = false)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to update profile"
                )
            }
        }
    }

    fun uploadProfilePhoto(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(uploadingPhoto = true, error = null)

            val userId = firebaseRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    uploadingPhoto = false,
                    error = "User not logged in"
                )
                return@launch
            }

            val result = firebaseRepository.uploadProfilePhoto(userId, imageUri)

            if (result.isSuccess) {
                loadProfile()
            } else {
                _uiState.value = _uiState.value.copy(
                    uploadingPhoto = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to upload photo"
                )
            }
        }
    }

    fun deleteProfilePhoto() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(uploadingPhoto = true, error = null)

            val userId = firebaseRepository.getCurrentUserId()
            val photoUrl = _uiState.value.profile?.photoUrl

            if (userId == null || photoUrl == null) {
                _uiState.value = _uiState.value.copy(
                    uploadingPhoto = false,
                    error = "Cannot delete photo"
                )
                return@launch
            }

            val result = firebaseRepository.deleteProfilePhoto(userId, photoUrl)

            if (result.isSuccess) {
                loadProfile()
            } else {
                _uiState.value = _uiState.value.copy(
                    uploadingPhoto = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to delete photo"
                )
            }
        }
    }

    fun switchResourcesTab(tab: ResourcesTab) {
        _uiState.value = _uiState.value.copy(showResourcesTab = tab)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}