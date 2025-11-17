package com.example.somashare.ui.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: UploadViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upload Past Paper") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // File Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select PDF File",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { filePickerLauncher.launch("application/pdf") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isUploading
                    ) {
                        Icon(Icons.Default.AttachFile, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Choose PDF File")
                    }

                    if (uiState.selectedFileUri != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = uiState.selectedFileName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.clearSelectedFile() },
                                    enabled = !uiState.isUploading
                                ) {
                                    Icon(Icons.Default.Close, "Remove")
                                }
                            }
                        }
                    }
                }
            }

            // Paper Information Form
            if (uiState.selectedFileUri != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Paper Information",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = uiState.paperName,
                            onValueChange = { viewModel.updatePaperName(it) },
                            label = { Text("Paper Name") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isUploading,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.unitCode,
                            onValueChange = { viewModel.updateUnitCode(it) },
                            label = { Text("Unit Code") },
                            placeholder = { Text("e.g., CSC201") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isUploading,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = uiState.unitName,
                            onValueChange = { viewModel.updateUnitName(it) },
                            label = { Text("Unit Name") },
                            placeholder = { Text("e.g., Data Structures") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isUploading,
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.yearOfStudy,
                                onValueChange = { viewModel.updateYearOfStudy(it) },
                                label = { Text("Year") },
                                placeholder = { Text("1-4") },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isUploading,
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = uiState.semester,
                                onValueChange = { viewModel.updateSemester(it) },
                                label = { Text("Semester") },
                                placeholder = { Text("1-2") },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isUploading,
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = uiState.paperYear,
                            onValueChange = { viewModel.updatePaperYear(it) },
                            label = { Text("Paper Year") },
                            placeholder = { Text("e.g., 2024") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isUploading,
                            singleLine = true
                        )

                        // Paper Type Dropdown
                        var expanded by remember { mutableStateOf(false) }
                        val paperTypes = listOf("Final Exam", "Midterm", "CAT 1", "CAT 2", "Assignment")

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded && !uiState.isUploading }
                        ) {
                            OutlinedTextField(
                                value = uiState.paperType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Paper Type") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                enabled = !uiState.isUploading
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                paperTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            viewModel.updatePaperType(type)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Upload Progress
                if (uiState.isUploading) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Uploading...", fontWeight = FontWeight.Bold)
                            LinearProgressIndicator(
                                progress = { uiState.uploadProgress / 100f },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text("${uiState.uploadProgress}%")
                        }
                    }
                }

                // Error/Success Messages
                uiState.errorMessage?.let { error ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                            Text(error, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                if (uiState.uploadSuccess) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.tertiary)
                            Text("Upload successful!", color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }

                // Upload Button
                Button(
                    onClick = { viewModel.uploadPaper() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isUploading && viewModel.canUpload()
                ) {
                    Icon(Icons.Default.CloudUpload, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Upload Paper")
                }
            }
        }
    }
}