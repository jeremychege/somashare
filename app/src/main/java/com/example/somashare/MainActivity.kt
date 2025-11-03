package com.example.somashare

// MainActivity.kt


import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AuthApp()
            }
        }
    }
}

@Composable
fun AuthApp() {
    var currentScreen by remember { mutableStateOf("login") }
    var userEmail by remember { mutableStateOf("") }
    var userId by remember { mutableStateOf("") }

    // Check if user is already logged in
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && currentUser.isEmailVerified) {
            userEmail = currentUser.email ?: ""
            userId = currentUser.uid
            currentScreen = "home"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (currentScreen) {
            "login" -> LoginScreen(
                onNavigateToSignup = { currentScreen = "signup" },
                onLoginSuccess = { email, uid ->
                    userEmail = email
                    userId = uid
                    currentScreen = "2fa"
                }
            )
            "signup" -> SignupScreen(
                onNavigateToLogin = { currentScreen = "login" },
                onSignupSuccess = { email, uid ->
                    userEmail = email
                    userId = uid
                    currentScreen = "2fa"
                }
            )
            "2fa" -> TwoFactorScreen(
                email = userEmail,
                userId = userId,
                onVerificationSuccess = { currentScreen = "home" }
            )
            "home" -> HomeScreen(
                email = userEmail,
                userId = userId,
                onLogout = { currentScreen = "login" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToSignup: () -> Unit,
    onLoginSuccess: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to SomaShare",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }
                isLoading = true
                errorMessage = ""

                scope.launch {
                    try {
                        val authResult = FirebaseAuth.getInstance()
                            .signInWithEmailAndPassword(email, password)
                            .await()

                        val user = authResult.user
                        if (user != null) {
                            Log.d("SomaShare", "Login successful for: ${user.email}")
                            isLoading = false
                            onLoginSuccess(user.email ?: email, user.uid)
                        } else {
                            errorMessage = "Login failed. Please try again."
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        Log.e("SomaShare", "Login error", e)
                        errorMessage = when {
                            e.message?.contains("password") == true -> "Invalid email or password"
                            e.message?.contains("network") == true -> "Network error. Check your connection."
                            else -> e.message ?: "Login failed. Please try again."
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToSignup) {
            Text("Don't have an account? Sign up")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: (String, String) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Join SomaShare",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                when {
                    fullName.isBlank() || email.isBlank() || password.isBlank() -> {
                        errorMessage = "Please fill in all fields"
                    }
                    password != confirmPassword -> {
                        errorMessage = "Passwords do not match"
                    }
                    password.length < 6 -> {
                        errorMessage = "Password must be at least 6 characters"
                    }
                    else -> {
                        isLoading = true
                        errorMessage = ""

                        scope.launch {
                            try {
                                // Create user in Firebase Auth
                                val authResult = FirebaseAuth.getInstance()
                                    .createUserWithEmailAndPassword(email, password)
                                    .await()

                                val user = authResult.user
                                if (user != null) {
                                    // Save user data to Firestore
                                    val userData = hashMapOf(
                                        "fullName" to fullName,
                                        "email" to email,
                                        "createdAt" to System.currentTimeMillis(),
                                        "twoFactorEnabled" to true
                                    )

                                    FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(user.uid)
                                        .set(userData)
                                        .await()

                                    Log.d("SomaShare", "User created successfully: ${user.email}")
                                    isLoading = false
                                    onSignupSuccess(user.email ?: email, user.uid)
                                } else {
                                    errorMessage = "Signup failed. Please try again."
                                    isLoading = false
                                }
                            } catch (e: Exception) {
                                Log.e("SomaShare", "Signup error", e)
                                errorMessage = when {
                                    e.message?.contains("email") == true -> "Email already in use or invalid"
                                    e.message?.contains("network") == true -> "Network error. Check your connection."
                                    else -> e.message ?: "Signup failed. Please try again."
                                }
                                isLoading = false
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign Up")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TwoFactorScreen(
    email: String,
    userId: String,
    onVerificationSuccess: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var storedCode by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    // Generate and store 2FA code when screen loads
    LaunchedEffect(Unit) {
        val generatedCode = Random.nextInt(100000, 999999).toString()
        storedCode = generatedCode

        try {
            // Store the 2FA code in Firestore with timestamp
            val codeData = hashMapOf(
                "code" to generatedCode,
                "email" to email,
                "timestamp" to System.currentTimeMillis(),
                "expiresAt" to (System.currentTimeMillis() + 300000) // 5 minutes
            )

            FirebaseFirestore.getInstance()
                .collection("verification_codes")
                .document(userId)
                .set(codeData)
                .await()

            Log.d("SomaShare", "2FA code generated: $generatedCode for $email")
            successMessage = "Code sent! (Check console in production, code: $generatedCode)"

            // In production, you would send this via email using a service like:
            // - Firebase Cloud Functions with SendGrid
            // - Your backend API

        } catch (e: Exception) {
            Log.e("SomaShare", "Error generating 2FA code", e)
            errorMessage = "Failed to send verification code"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Two-Factor Authentication",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Enter the 6-digit code sent to $email",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = code,
            onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) code = it },
            label = { Text("Verification Code") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (successMessage.isNotEmpty()) {
            Text(
                text = successMessage,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (code.length != 6) {
                    errorMessage = "Please enter a 6-digit code"
                    return@Button
                }
                isLoading = true
                errorMessage = ""
                successMessage = ""

                scope.launch {
                    try {
                        // Retrieve the stored code from Firestore
                        val doc = FirebaseFirestore.getInstance()
                            .collection("verification_codes")
                            .document(userId)
                            .get()
                            .await()

                        val savedCode = doc.getString("code")
                        val expiresAt = doc.getLong("expiresAt") ?: 0L

                        when {
                            savedCode == null -> {
                                errorMessage = "Verification code not found. Please request a new one."
                            }
                            System.currentTimeMillis() > expiresAt -> {
                                errorMessage = "Code expired. Please request a new one."
                            }
                            code == savedCode -> {
                                // Code is valid - delete it and proceed
                                FirebaseFirestore.getInstance()
                                    .collection("verification_codes")
                                    .document(userId)
                                    .delete()
                                    .await()

                                // Update user's verification status
                                FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(userId)
                                    .update("verified", true)
                                    .await()

                                Log.d("SomaShare", "2FA verification successful")
                                delay(500)
                                onVerificationSuccess()
                            }
                            else -> {
                                errorMessage = "Invalid code. Please try again."
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("SomaShare", "Verification error", e)
                        errorMessage = "Verification failed. Please try again."
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Verify")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                scope.launch {
                    try {
                        // Generate new code
                        val newCode = Random.nextInt(100000, 999999).toString()
                        storedCode = newCode

                        val codeData = hashMapOf(
                            "code" to newCode,
                            "email" to email,
                            "timestamp" to System.currentTimeMillis(),
                            "expiresAt" to (System.currentTimeMillis() + 300000)
                        )

                        FirebaseFirestore.getInstance()
                            .collection("verification_codes")
                            .document(userId)
                            .set(codeData)
                            .await()

                        Log.d("SomaShare", "New 2FA code: $newCode")
                        successMessage = "New code sent! (Check console, code: $newCode)"
                        errorMessage = ""
                    } catch (e: Exception) {
                        Log.e("SomaShare", "Error resending code", e)
                        errorMessage = "Failed to resend code"
                    }
                }
            }
        ) {
            Text("Resend Code")
        }
    }
}

@Composable
fun HomeScreen(
    email: String,
    userId: String,
    onLogout: () -> Unit
) {
    var userName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Load user data from Firestore
    LaunchedEffect(userId) {
        try {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .await()

            userName = doc.getString("fullName") ?: "User"
            isLoading = false
        } catch (e: Exception) {
            Log.e("SomaShare", "Error loading user data", e)
            userName = "User"
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Text(
                text = "Welcome to SomaShare!",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = email,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = {
                    scope.launch {
                        try {
                            FirebaseAuth.getInstance().signOut()
                            Log.d("SomaShare", "User logged out")
                            onLogout()
                        } catch (e: Exception) {
                            Log.e("SomaShare", "Logout error", e)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}