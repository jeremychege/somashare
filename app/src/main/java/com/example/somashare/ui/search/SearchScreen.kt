package com.example.somashare.ui.search

import TopBarWithAction
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.somashare.data.model.PaperType
import com.example.somashare.data.model.PastPaper
import com.example.somashare.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopBarWithAction(
                title = "Search Papers",
                onNavigateBack = { navController.navigateUp() },
                actionIcon = if (uiState.showFilters) Icons.Default.FilterAltOff else Icons.Default.FilterAlt,
                actionContentDescription = "Toggle Filters",
                onActionClick = { viewModel.toggleFilterVisibility() }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )

            // Filters Section
            AnimatedVisibility(visible = uiState.showFilters) {
                FiltersSection(
                    filters = uiState.filters,
                    onYearChange = { viewModel.onYearFilterChange(it) },
                    onSemesterChange = { viewModel.onSemesterFilterChange(it) },
                    onPaperTypeChange = { viewModel.onPaperTypeFilterChange(it) },
                    onPaperYearChange = { viewModel.onPaperYearFilterChange(it) },
                    onClearFilters = { viewModel.clearFilters() },
                    paperYears = viewModel.getSortedPaperYears()
                )
            }

            // Content
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingScreen(message = "Searching papers...")
                    }
                }

                uiState.error != null -> {
                    ErrorScreen(
                        message = uiState.error!!,
                        onRetry = { viewModel.refresh() }
                    )
                }

                uiState.filteredPapers.isEmpty() -> {
                    EmptyStateScreen(
                        icon = Icons.Default.SearchOff,
                        title = "No papers found",
                        message = if (uiState.searchQuery.isNotBlank() || hasActiveFilters(uiState.filters)) {
                            "Try adjusting your search or filters"
                        } else {
                            "No past papers available yet"
                        },
                        actionText = if (hasActiveFilters(uiState.filters)) "Clear Filters" else null,
                        onActionClick = if (hasActiveFilters(uiState.filters)) {
                            { viewModel.clearFilters() }
                        } else null
                    )
                }

                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Results count
                        item {
                            Text(
                                text = "${uiState.filteredPapers.size} ${if (uiState.filteredPapers.size == 1) "paper" else "papers"} found",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // Papers list
                        items(uiState.filteredPapers) { paper ->
                            PaperSearchCard(
                                paper = paper,
                                onCardClick = {
                                    navController.navigate("pdf_viewer/${paper.paperId}")
                                },
                                onUnitClick = {
                                    navController.navigate("unit_details/${paper.unitId}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search papers, units...") },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search")
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                }
            }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4F46E5),
            unfocusedBorderColor = Color(0xFFE5E7EB)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSection(
    filters: SearchFilters,
    onYearChange: (Int?) -> Unit,
    onSemesterChange: (Int?) -> Unit,
    onPaperTypeChange: (PaperType?) -> Unit,
    onPaperYearChange: (Int?) -> Unit,
    onClearFilters: () -> Unit,
    paperYears: List<Int>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filters",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClearFilters) {
                    Text("Clear All")
                }
            }

            // Year of Study Filter
            var yearExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = yearExpanded,
                onExpandedChange = { yearExpanded = !yearExpanded }
            ) {
                OutlinedTextField(
                    value = filters.yearOfStudy?.toString() ?: "All Years",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Year of Study") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = yearExpanded,
                    onDismissRequest = { yearExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Years") },
                        onClick = {
                            onYearChange(null)
                            yearExpanded = false
                        }
                    )
                    (1..6).forEach { year ->
                        DropdownMenuItem(
                            text = { Text("Year $year") },
                            onClick = {
                                onYearChange(year)
                                yearExpanded = false
                            }
                        )
                    }
                }
            }

            // Semester Filter
            var semesterExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = semesterExpanded,
                onExpandedChange = { semesterExpanded = !semesterExpanded }
            ) {
                OutlinedTextField(
                    value = filters.semesterOfStudy?.let { "Semester $it" } ?: "All Semesters",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Semester") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = semesterExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = semesterExpanded,
                    onDismissRequest = { semesterExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Semesters") },
                        onClick = {
                            onSemesterChange(null)
                            semesterExpanded = false
                        }
                    )
                    listOf(1, 2).forEach { semester ->
                        DropdownMenuItem(
                            text = { Text("Semester $semester") },
                            onClick = {
                                onSemesterChange(semester)
                                semesterExpanded = false
                            }
                        )
                    }
                }
            }

            // Paper Type Filter
            var typeExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded }
            ) {
                OutlinedTextField(
                    value = filters.paperType?.displayName ?: "All Types",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Paper Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Types") },
                        onClick = {
                            onPaperTypeChange(null)
                            typeExpanded = false
                        }
                    )
                    PaperType.values().forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = {
                                onPaperTypeChange(type)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            // Paper Year Filter
            var paperYearExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = paperYearExpanded,
                onExpandedChange = { paperYearExpanded = !paperYearExpanded }
            ) {
                OutlinedTextField(
                    value = filters.paperYear?.toString() ?: "All Years",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Paper Year") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paperYearExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = paperYearExpanded,
                    onDismissRequest = { paperYearExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All Years") },
                        onClick = {
                            onPaperYearChange(null)
                            paperYearExpanded = false
                        }
                    )
                    paperYears.forEach { year ->
                        DropdownMenuItem(
                            text = { Text(year.toString()) },
                            onClick = {
                                onPaperYearChange(year)
                                paperYearExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaperSearchCard(
    paper: PastPaper,
    onCardClick: () -> Unit,
    onUnitClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCardClick,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Unit badge (clickable)
            Surface(
                color = Color(0xFFEEF2FF),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.clickable(onClick = onUnitClick)
            ) {
                Text(
                    text = "${paper.unitCode} - ${paper.unitName}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4F46E5)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Paper name
            Text(
                text = paper.paperName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetadataChip(
                        icon = Icons.Default.CalendarToday,
                        text = paper.paperYear.toString()
                    )
                    MetadataChip(
                        icon = Icons.Default.Description,
                        text = paper.paperType.displayName
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF6B7280)
                    )
                    Text(
                        text = paper.downloadCount.toString(),
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
    }
}

@Composable
fun MetadataChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color(0xFF6B7280)
        )
    }
}

private fun hasActiveFilters(filters: SearchFilters): Boolean {
    return filters.yearOfStudy != null ||
            filters.semesterOfStudy != null ||
            filters.paperType != null ||
            filters.paperYear != null
}
