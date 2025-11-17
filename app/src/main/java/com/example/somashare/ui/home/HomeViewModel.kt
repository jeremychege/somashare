package com.example.somashare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.somashare.data.model.PastPaper
import com.example.somashare.data.model.Units
import com.example.somashare.data.model.User
import com.example.somashare.data.repository.AuthRepository
import com.example.somashare.data.repository.FavoriteRepository
import com.example.somashare.data.repository.PaperRepository
import com.example.somashare.data.repository.UnitRepository
import com.example.somashare.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

data class HomeUiState(
    val user: User? = null,
    val greeting: String = "",
    val recommendedUnit: List<Units> = emptyList(),
    val recentPapers: List<PastPaper> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val unitRepository = UnitRepository()
    private val paperRepository = PaperRepository()
    private val favoriteRepository = FavoriteRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val currentUserId = authRepository.getCurrentUserId()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                if (currentUserId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Please log in to continue"
                        )
                    }
                    return@launch
                }

                // Set greeting immediately
                _uiState.update { it.copy(greeting = getGreeting()) }

                // Load user data
                userRepository.getUserById(currentUserId)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load user data: ${e.message}"
                            )
                        }
                    }
                    .collect { user ->
                        if (user != null) {
                            _uiState.update { it.copy(user = user) }

                            // Load recommended units for user's year/semester
                            loadRecommendedUnits(user.yearOfStudy, user.semesterOfStudy)

                            // Load recent papers
                            loadRecentPapers()
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "User profile not found"
                                )
                            }
                        }
                    }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    private fun loadRecommendedUnits(year: Int, semester: Int) {
        viewModelScope.launch {
            try {
                // Combine units with favorites
                combine(
                    unitRepository.getUnitsByYearAndSemester(year, semester),
                    favoriteRepository.getFavoriteUnits(currentUserId ?: "")
                ) { units: List<Units>, favoriteIds: List<String> ->
                    units.map { unit ->
                        unit.copy(isFavorite = favoriteIds.contains(unit.unitId))
                    }.take(4) // Show top 4 on home screen
                }
                    .catch { e ->
                        _uiState.update {
                            it.copy(error = "Failed to load units: ${e.message}")
                        }
                    }
                    .collect { units ->
                        _uiState.update {
                            it.copy(
                                recommendedUnit = units, // Fixed typo: was "recommendedUnit"
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load units"
                    )
                }
            }
        }
    }

    private fun loadRecentPapers() {
        viewModelScope.launch {
            try {
                paperRepository.getRecentlyViewedPapers(currentUserId ?: "", limit = 3)
                    .catch { e ->
                        // Silently fail for recent papers as it's not critical
                        emit(emptyList())
                    }
                    .collect { papers ->
                        _uiState.update { it.copy(recentPapers = papers) }
                    }
            } catch (e: Exception) {
                // Don't show error for recent papers
            }
        }
    }

    fun toggleFavorite(unit: Units) {
        viewModelScope.launch {
            try {
                if (currentUserId == null) return@launch

                if (unit.isFavorite) {
                    favoriteRepository.removeFavorite(currentUserId, unit.unitId)
                } else {
                    favoriteRepository.addFavorite(
                        currentUserId,
                        unit.unitId,
                        unit.unitCode,
                        unit.unitName
                    )
                }

                // Update local state immediately for better UX
                _uiState.update { state ->
                    state.copy(
                        recommendedUnit = state.recommendedUnit.map { u ->
                            if (u.unitId == unit.unitId) u.copy(isFavorite = !u.isFavorite)
                            else u
                        }
                    )
                }
            } catch (e: Exception) {
                // Handle error - could show a snackbar
            }
        }
    }

    fun onPaperClick(paper: PastPaper) {
        viewModelScope.launch {
            try {
                if (currentUserId == null) return@launch

                paperRepository.recordView(
                    currentUserId,
                    paper.paperId,
                    paper.paperName,
                    paper.unitCode
                )
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }

    fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            hours < 24 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            days < 7 -> "$days ${if (days == 1L) "day" else "days"} ago"
            else -> "${days / 7} ${if (days / 7 == 1L) "week" else "weeks"} ago"
        }
    }

    private fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    fun refresh() {
        loadHomeData()
    }
}
