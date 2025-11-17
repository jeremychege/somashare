package com.example.somashare.userinterface.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    var editedName by remember { mutableStateOf("") }
    var editedCourse by remember { mutableStateOf("") }
    var editedYear by remember { mutableIntStateOf(1) }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadProfilePhoto(it) }
    }

    // Show delete photo confirmation dialog
    var showDeletePhotoDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.profile) {
        uiState.profile?.let { profile ->
            editedName = profile.fullName
            editedCourse = profile.course
            editedYear = profile.yearOfStudy
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (!uiState.isEditMode) {
                        IconButton(onClick = { viewModel.toggleEditMode() }) {
                            Icon(Icons.Default.Edit, "Edit Profile")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.profile != null -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Profile Photo Section
                        item {
                            ProfilePhotoSection(
                                photoUrl = uiState.profile?.photoUrl,
                                isUploading = uiState.uploadingPhoto,
                                onPhotoClick = { imagePickerLauncher.launch("image/*") },
                                onDeleteClick = { showDeletePhotoDialog = true }
                            )
                        }

                        // Profile Info Section
                        item {
                            if (uiState.isEditMode) {
                                EditProfileSection(
                                    name = editedName,
                                    course = editedCourse,
                                    year = editedYear,
                                    onNameChange = { editedName = it },
                                    onCourseChange = { editedCourse = it },
                                    onYearChange = { editedYear = it },
                                    onSave = {
                                        viewModel.updateProfile(editedName, editedCourse, editedYear)
                                    },
                                    onCancel = { viewModel.toggleEditMode() }
                                )
                            } else {
                                ProfileInfoCard(profile = uiState.profile!!)
                            }
                        }

                        // Stats Section
                        item {
                            StatsCard(
                                uploadedCount = uiState.uploadedResources.size,
                                downloadedCount = uiState.downloadedResources.size
                            )
                        }

                        // Resources Tabs
                        item {
                            ResourcesTabs(
                                selectedTab = uiState.showResourcesTab,
                                onTabSelected = { viewModel.switchResourcesTab(it) }
                            )
                        }

                        // Resources List
                        when (uiState.showResourcesTab) {
                            ResourcesTab.UPLOADED -> {
                                items(uiState.uploadedResources) { resource ->
                                    UploadedResourceItem(resource = resource)
                                }
                            }
                            ResourcesTab.DOWNLOADED -> {
                                items(uiState.downloadedResources) { resource ->
                                    DownloadedResourceItem(resource = resource)
                                }
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = uiState.error ?: "Unable to load profile",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        // Error Snackbar
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show snackbar (simplified)
                viewModel.clearError()
            }
        }

        // Delete Photo Confirmation Dialog
        if (showDeletePhotoDialog) {
            AlertDialog(
                onDismissRequest = { showDeletePhotoDialog = false },
                title = { Text("Delete Profile Photo") },
                text = { Text("Are you sure you want to delete your profile photo?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteProfilePhoto()
                            showDeletePhotoDialog = false
                        }
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeletePhotoDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfilePhotoSection(
    photoUrl: String?,
    isUploading: Boolean,
    onPhotoClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(120.dp)
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Photo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable(onClick = onPhotoClick),
                    contentScale = ContentScale.Crop
                )

                // Delete button
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.error, CircleShape)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Delete Photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable(onClick = onPhotoClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Add Photo",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (photoUrl != null) "Tap to change photo" else "Tap to add photo",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ProfileInfoCard(profile: com.example.somashare.data.repository.FirebaseUserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProfileInfoRow(
                icon = Icons.Default.Person,
                label = "Name",
                value = profile.fullName
            )

            ProfileInfoRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = profile.email
            )

            ProfileInfoRow(
                icon = Icons.Default.School,
                label = "Course",
                value = profile.course
            )

            ProfileInfoRow(
                icon = Icons.Default.DateRange,
                label = "Year of Study",
                value = "Year ${profile.yearOfStudy}"
            )
        }
    }
}

@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EditProfileSection(
    name: String,
    course: String,
    year: Int,
    onNameChange: (String) -> Unit,
    onCourseChange: (String) -> Unit,
    onYearChange: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = course,
                onValueChange = onCourseChange,
                label = { Text("Course") },
                leadingIcon = { Icon(Icons.Default.School, null) },
                modifier = Modifier.fillMaxWidth()
            )

            // Year selector
            var expandedYear by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedYear,
                onExpandedChange = { expandedYear = it }
            ) {
                OutlinedTextField(
                    value = "Year $year",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Year of Study") },
                    leadingIcon = { Icon(Icons.Default.DateRange, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedYear,
                    onDismissRequest = { expandedYear = false }
                ) {
                    (1..5).forEach { yearOption ->
                        DropdownMenuItem(
                            text = { Text("Year $yearOption") },
                            onClick = {
                                onYearChange(yearOption)
                                expandedYear = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
fun StatsCard(uploadedCount: Int, downloadedCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = Icons.Default.Upload,
                label = "Uploaded",
                count = uploadedCount
            )

            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp)
            )

            StatItem(
                icon = Icons.Default.Download,
                label = "Downloaded",
                count = downloadedCount
            )
        }
    }
}

@Composable
fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ResourcesTabs(
    selectedTab: ResourcesTab,
    onTabSelected: (ResourcesTab) -> Unit
) {
    TabRow(selectedTabIndex = selectedTab.ordinal) {
        Tab(
            selected = selectedTab == ResourcesTab.UPLOADED,
            onClick = { onTabSelected(ResourcesTab.UPLOADED) },
            text = { Text("Uploaded") }
        )
        Tab(
            selected = selectedTab == ResourcesTab.DOWNLOADED,
            onClick = { onTabSelected(ResourcesTab.DOWNLOADED) },
            text = { Text("Downloaded") }
        )
    }
}

@Composable
fun UploadedResourceItem(
    resource: com.example.somashare.data.repository.UploadedResource
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.paperName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = resource.unitName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(resource.uploadDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DownloadedResourceItem(
    resource: com.example.somashare.data.repository.DownloadedResource
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Download,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(40.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resource.paperName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = resource.unitName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDate(resource.downloadDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}