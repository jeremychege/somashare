package com.example.somashare.ui.upload

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.somashare.data.model.PaperType
import com.example.somashare.ui.components.LoadingScreen
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    navController: NavController,
    viewModel: UploadViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onUploadSuccess: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showUnitPicker by remember { mutableStateOf(false) }
    var showPaperTypePicker by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    // PDF picker launcher
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                val sizeIndex = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
                c.moveToFirst()
                val name = c.getString(nameIndex)
                val size = c.getLong(sizeIndex)
                viewModel.setPdfUri(it, name, size)
            }
        }
    }

    // Navigate on success
    LaunchedEffect(uiState.uploadedPaperId) {
        uiState.uploadedPaperId?.let { paperId ->
            onUploadSuccess(paperId)
        }
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
                    containerColor = Color(0xFF4F46E5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingScreen()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Upload Instructions
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFEEF2FF)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF4F46E5)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Share your past papers to help fellow students. Make sure to fill in all required details.",
                            fontSize = 14.sp,
                            color = Color(0xFF1E40AF)
                        )
                    }
                }

                Divider()

                // Step 1: Select PDF
                Text(
                    text = "Step 1: Select PDF File",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                if (uiState.pdfUri == null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clickable { pdfPickerLauncher.launch("application/pdf") },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF3F4F6)
                        ),
                        border = BorderStroke(2.dp, Color(0xFFD1D5DB))
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = Color(0xFF6B7280)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to select PDF",
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF374151)
                                )
                                Text(
                                    text = "Max size: 50MB",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFFEEF2FF),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .size(32.dp),
                                    tint = Color(0xFFEF4444)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = uiState.pdfName,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                Text(
                                    text = formatFileSize(uiState.pdfSize),
                                    fontSize = 12.sp,
                                    color = Color(0xFF6B7280)
                                )
                            }

                            IconButton(onClick = { viewModel.setPdfUri(Uri.EMPTY, "", 0) }) {
                                Icon(Icons.Default.Close, "Remove")
                            }
                        }
                    }
                }

                Divider()

                // Step 2: Paper Details
                Text(
                    text = "Step 2: Paper Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                // Paper Name
                OutlinedTextField(
                    value = uiState.paperName,
                    onValueChange = { viewModel.updatePaperName(it) },
                    label = { Text("Paper Name *") },
                    placeholder = { Text("e.g., CSC 101 Final Exam 2023") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isUploading
                )

                // Select Unit
                OutlinedTextField(
                    value = uiState.selectedUnit?.let { "${it.unitCode} - ${it.unitName}" } ?: "",
                    onValueChange = {},
                    label = { Text("Select Unit *") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showUnitPicker = true },
                    enabled = false,
                    readOnly = true,
                    trailingIcon = {
                        Icon(Icons.Default.ArrowDropDown, null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Paper Year
                    OutlinedTextField(
                        value = uiState.paperYear.toString(),
                        onValueChange = {},
                        label = { Text("Year *") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showYearPicker = true },
                        enabled = false,
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // Paper Type
                    OutlinedTextField(
                        value = uiState.paperType.displayName,
                        onValueChange = {},
                        label = { Text("Type *") },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showPaperTypePicker = true },
                        enabled = false,
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Lecturer Name (Optional)
                OutlinedTextField(
                    value = uiState.lecturerName,
                    onValueChange = { viewModel.updateLecturerName(it) },
                    label = { Text("Lecturer Name (Optional)") },
                    placeholder = { Text("e.g., Dr. Smith") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isUploading
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Error message
                if (uiState.error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFEE2E2)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color(0xFFDC2626)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.error!!,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }

                // Upload Progress
                if (uiState.isUploading) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFEEF2FF)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Uploading...",
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${uiState.uploadProgress}%",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = uiState.uploadProgress / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF4F46E5)
                            )
                        }
                    }
                }

                // Upload Button
                Button(
                    onClick = { viewModel.uploadPaper() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isUploading &&
                            uiState.pdfUri != null &&
                            uiState.selectedUnit != null &&
                            uiState.paperName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4F46E5)
                    )
                ) {
                    if (uiState.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Upload, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Paper", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Unit Picker Dialog
        if (showUnitPicker) {
            Dialog(onDismissRequest = { showUnitPicker = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp)
                ) {
                    Column {
                        Text(
                            text = "Select Unit",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        LazyColumn {
                            items(uiState.units) { unit ->
                                ListItem(
                                    headlineContent = { Text("${unit.unitCode} - ${unit.unitName}") },
                                    supportingContent = { Text("Year ${unit.yearOfStudy} • Semester ${unit.semesterOfStudy}") },
                                    modifier = Modifier.clickable {
                                        viewModel.selectUnit(unit)
                                        showUnitPicker = false
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
            }
        }

        // Paper Type Picker Dialog
        if (showPaperTypePicker) {
            Dialog(onDismissRequest = { showPaperTypePicker = false }) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "Select Paper Type",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        PaperType.entries.forEach { type ->
                            ListItem(
                                headlineContent = { Text(type.displayName) },
                                modifier = Modifier.clickable {
                                    viewModel.updatePaperType(type)
                                    showPaperTypePicker = false
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        }

        // Year Picker Dialog
        if (showYearPicker) {
            Dialog(onDismissRequest = { showYearPicker = false }) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Text(
                            text = "Select Year",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        LazyColumn {
                            items((2015..2025).reversed().toList()) { year ->
                                ListItem(
                                    headlineContent = { Text(year.toString()) },
                                    modifier = Modifier.clickable {
                                        viewModel.updatePaperYear(year)
                                        showYearPicker = false
                                    }
                                )
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatFileSize(size: Long): String {
    val df = DecimalFormat("#.##")
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${df.format(size / 1024.0)} KB"
        else -> "${df.format(size / (1024.0 * 1024.0))} MB"
    }
}