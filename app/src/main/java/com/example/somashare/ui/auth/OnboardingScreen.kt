package com.example.somashare.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var currentStep by remember { mutableStateOf(1) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }
    var selectedSemester by remember { mutableStateOf<Int?>(null) }
    var selectedDepartment by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    // Navigate on success
    LaunchedEffect(uiState.isOnboardingComplete) {
        if (uiState.isOnboardingComplete) {
            onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = currentStep / 3f,
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF4F46E5)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Tell us about yourself",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Step $currentStep of 3",
            fontSize = 14.sp,
            color = Color(0xFF6B7280)
        )

        Spacer(modifier = Modifier.height(48.dp))

        when (currentStep) {
            1 -> YearSelection(
                selectedYear = selectedYear,
                onYearSelected = { selectedYear = it }
            )
            2 -> SemesterSelection(
                selectedSemester = selectedSemester,
                onSemesterSelected = { selectedSemester = it }
            )
            3 -> DepartmentSelection(
                selectedDepartment = selectedDepartment,
                onDepartmentSelected = { selectedDepartment = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Error Message
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentStep > 1) {
                OutlinedButton(
                    onClick = { currentStep-- },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("BACK")
                }
            }

            Button(
                onClick = {
                    if (currentStep < 3) {
                        currentStep++
                    } else {
                        // Complete onboarding
                        if (selectedYear != null && selectedSemester != null && selectedDepartment.isNotEmpty()) {
                            viewModel.completeOnboarding(
                                selectedYear!!,
                                selectedSemester!!,
                                selectedDepartment
                            )
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = when (currentStep) {
                    1 -> selectedYear != null
                    2 -> selectedSemester != null
                    3 -> selectedDepartment.isNotEmpty() && !uiState.isLoading
                    else -> false
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4F46E5)
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(if (currentStep == 3) "GET STARTED" else "NEXT")
                }
            }
        }
    }
}

@Composable
fun YearSelection(
    selectedYear: Int?,
    onYearSelected: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "What year are you in?",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            (1..6).chunked(3).forEach { rowYears ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowYears.forEach { year ->
                        YearCard(
                            year = year,
                            isSelected = selectedYear == year,
                            onClick = { onYearSelected(year) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun YearCard(
    year: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .selectable(selected = isSelected, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4F46E5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = year.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else Color(0xFF111827)
            )
        }
    }
}

@Composable
fun SemesterSelection(
    selectedSemester: Int?,
    onSemesterSelected: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Which semester?",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SemesterCard(
                semester = 1,
                isSelected = selectedSemester == 1,
                onClick = { onSemesterSelected(1) },
                modifier = Modifier.weight(1f)
            )
            SemesterCard(
                semester = 2,
                isSelected = selectedSemester == 2,
                onClick = { onSemesterSelected(2) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun SemesterCard(
    semester: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(150.dp)
            .selectable(selected = isSelected, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF4F46E5) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 8.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Semester",
                    fontSize = 16.sp,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF6B7280)
                )
                Text(
                    text = semester.toString(),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color(0xFF111827)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentSelection(
    selectedDepartment: String,
    onDepartmentSelected: (String) -> Unit
) {
    val departments = listOf(
        "Computer Science",
        "Information Technology",
        "Software Engineering",
        "Business Information Technology",
        "Mathematics",
        "Statistics",
        "Other"
    )

    var expanded by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Select your department",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF111827)
        )

        Spacer(modifier = Modifier.height(32.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedDepartment,
                onValueChange = {},
                readOnly = true,
                label = { Text("Department") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                colors = OutlinedTextFieldDefaults.colors()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                departments.forEach { department ->
                    DropdownMenuItem(
                        text = { Text(department) },
                        onClick = {
                            onDepartmentSelected(department)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}