package com.example.somashare.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.somashare.data.model.PastPaper
import com.example.somashare.ui.components.BottomNavBar
import com.example.somashare.ui.components.ErrorScreen
import com.example.somashare.ui.components.LoadingScreen
import com.example.somashare.ui.components.UnitCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToSearch: () -> Unit = {},
    onNavigateToRecommendations: () -> Unit = {},
    onNavigateToRecentlyOpened: () -> Unit = {},
    onNavigateToUnitDetails: (String) -> Unit = {},
    onNavigateToPdfViewer: (String) -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToUpload: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4F46E5),
                    titleContentColor = Color.White
                ),
                title = {
                    Column {
                        Text(
                            text = if (uiState.user != null) {
                                "${uiState.greeting}, ${uiState.user!!.fullName.split(" ").firstOrNull() ?: ""}!"
                            } else {
                                uiState.greeting
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.user != null) {
                            Text(
                                text = "Year ${uiState.user!!.yearOfStudy} Student",
                                fontSize = 14.sp,
                                color = Color(0xFFC7D2FE)
                            )
                        }
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF6366F1)),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingScreen()
            }

            uiState.error != null -> {
                ErrorScreen(
                    message = uiState.error!!,
                    onRetry = { viewModel.refresh() }
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Recommended Units Section
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recommended Units",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF111827)
                                )
                                TextButton(onClick = onNavigateToRecommendations) {
                                    Text("See all", color = Color(0xFF4F46E5))
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (uiState.recommendedUnit.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF3F4F6)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No units available",
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                            } else {
                                uiState.recommendedUnit.chunked(2).forEach { rowUnits ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        rowUnits.forEach { unit ->
                                            UnitCard(
                                                unit = unit,
                                                onCardClick = {
                                                        id -> onNavigateToUnitDetails(id)
                                                },
                                                onFavoriteClick = {
                                                    viewModel.toggleFavorite(unit)
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowUnits.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Recently Opened Section
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Schedule,
                                        null,
                                        tint = Color(0xFF374151),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Recently Opened",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF111827)
                                    )
                                }

                                if (uiState.recentPapers.isNotEmpty()) {
                                    TextButton(onClick = onNavigateToRecentlyOpened) {
                                        Text("See all", color = Color(0xFF4F46E5))
                                        Icon(
                                            Icons.Default.ChevronRight,
                                            null,
                                            tint = Color(0xFF4F46E5),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (uiState.recentPapers.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF3F4F6)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Default.Description,
                                                null,
                                                modifier = Modifier.size(48.dp),
                                                tint = Color(0xFF9CA3AF)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("No recent papers", color = Color(0xFF6B7280))
                                        }
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    uiState.recentPapers.forEach { paper ->
                                        RecentPaperCard(
                                            paper = paper,
                                            timeAgo = viewModel.getTimeAgo(paper.uploadDate),
                                            onCardClick = {
                                                viewModel.onPaperClick(paper)
                                                onNavigateToPdfViewer(paper.paperId)
                                            },
                                            onDownloadClick = { /* Handle download */ },
                                            onMoreClick = { /* Show options */ }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Browse Button
                    item {
                        Button(
                            onClick = onNavigateToSearch,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4F46E5)
                            )
                        ) {
                            Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Browse All Past Papers", fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecentPaperCard(
    paper: PastPaper,
    timeAgo: String,
    onCardClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    TODO("Not yet implemented")
}