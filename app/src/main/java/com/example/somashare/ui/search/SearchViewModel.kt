package com.example.somashare.ui.search


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.somashare.data.model.PastPaper
import com.example.somashare.data.model.PaperType
import com.example.somashare.data.repository.PaperRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*


data class SearchFilters(
    val yearOfStudy: Int? = null,
    val semesterOfStudy: Int? = null,
    val paperType: PaperType? = null,
    val paperYear: Int? = null
)

data class SearchUiState(
    val searchQuery: String = "",
    val papers: List<PastPaper> = emptyList(),
    val filteredPapers: List<PastPaper> = emptyList(),
    val filters: SearchFilters = SearchFilters(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showFilters: Boolean = false
)

class SearchViewModel : ViewModel() {
    private val paperRepository = PaperRepository()

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        loadPapers()
    }

    private fun loadPapers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Start with all papers
                paperRepository.getAllPapers(limit = 100)
                    .catch { e ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "Failed to load papers: ${e.message}"
                            )
                        }
                    }
                    .collect { papers ->
                        _uiState.update {
                            it.copy(
                                papers = papers,
                                filteredPapers = papers,
                                isLoading = false
                            )
                        }
                        // Apply any existing filters
                        applyFilters()
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load papers"
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onYearFilterChange(year: Int?) {
        _uiState.update {
            it.copy(filters = it.filters.copy(yearOfStudy = year))
        }
        applyFilters()
    }

    fun onSemesterFilterChange(semester: Int?) {
        _uiState.update {
            it.copy(filters = it.filters.copy(semesterOfStudy = semester))
        }
        applyFilters()
    }

    fun onPaperTypeFilterChange(type: PaperType?) {
        _uiState.update {
            it.copy(filters = it.filters.copy(paperType = type))
        }
        applyFilters()
    }

    fun onPaperYearFilterChange(year: Int?) {
        _uiState.update {
            it.copy(filters = it.filters.copy(paperYear = year))
        }
        applyFilters()
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(
                filters = SearchFilters(),
                searchQuery = ""
            )
        }
        applyFilters()
    }

    fun toggleFilterVisibility() {
        _uiState.update { it.copy(showFilters = !it.showFilters) }
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        var filtered = currentState.papers

        // Apply search query
        if (currentState.searchQuery.isNotBlank()) {
            val query = currentState.searchQuery.lowercase()
            filtered = filtered.filter { paper ->
                paper.paperName.lowercase().contains(query) ||
                        paper.unitCode.lowercase().contains(query) ||
                        paper.unitName.lowercase().contains(query)
            }
        }

        // Apply year of study filter
        currentState.filters.yearOfStudy?.let { year ->
            filtered = filtered.filter { it.yearOfStudy == year }
        }

        // Apply semester filter
        currentState.filters.semesterOfStudy?.let { semester ->
            filtered = filtered.filter { it.semesterOfStudy == semester }
        }

        // Apply paper type filter
        currentState.filters.paperType?.let { type ->
            filtered = filtered.filter { it.paperType == type }
        }

        // Apply paper year filter
        currentState.filters.paperYear?.let { year ->
            filtered = filtered.filter { it.paperYear == year }
        }

        _uiState.update { it.copy(filteredPapers = filtered) }
    }

    fun refresh() {
        loadPapers()
    }

    fun getSortedPaperYears(): List<Int> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        return (currentYear downTo currentYear - 10).toList()
    }
}
