package com.example.somashare.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.somashare.data.repository.AuthRepository
import com.example.somashare.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val isRegisterSuccess: Boolean = false,
    val isOnboardingComplete: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.login(email, password)

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoginSuccess = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Login failed"
                    )
                }
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Register with temporary data, will be completed in onboarding
            val result = authRepository.register(
                email = email,
                password = password,
                fullName = fullName,
                yearOfStudy = 1, // Temporary
                semesterOfStudy = 1, // Temporary
                department = "" // Will be set in onboarding
            )

            if (result.isSuccess) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRegisterSuccess = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Registration failed"
                    )
                }
            }
        }
    }

    fun completeOnboarding(year: Int, semester: Int, department: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val userId = authRepository.getCurrentUserId()
            if (userId != null) {
                val result = userRepository.updateUser(
                    userId,
                    mapOf(
                        "yearOfStudy" to year,
                        "semesterOfStudy" to semester,
                        "department" to department
                    )
                )

                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isOnboardingComplete = true
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Failed to save profile"
                        )
                    }
                }
            }
        }
    }
}