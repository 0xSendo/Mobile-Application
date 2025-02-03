package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myacademate.ui.theme.MyAcademateTheme

class RegisterActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)

        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RegisterScreen(
                        onRegisterSuccess = {
                            Log.d("RegisterActivity", "Registration successful.")
                            // Navigate to login screen or home
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        dbHelper = dbHelper,
                        onNavigateToLogin = {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish() // Optional: to close RegisterActivity
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    dbHelper: DatabaseHelper,
    onNavigateToLogin: () -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var yearLevel by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Register",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // First Name
        OutlinedTextField(
            value = firstName,
            onValueChange = {
                firstName = it
                errorMessage = "" // Clear error message when user starts typing
            },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Last Name
        OutlinedTextField(
            value = lastName,
            onValueChange = {
                lastName = it
                errorMessage = ""
            },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Username
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                errorMessage = ""
            },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Course
        OutlinedTextField(
            value = course,
            onValueChange = {
                course = it
                errorMessage = ""
            },
            label = { Text("Course") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Year Level
        OutlinedTextField(
            value = yearLevel,
            onValueChange = {
                yearLevel = it
                errorMessage = ""
            },
            label = { Text("Year Level") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Confirm Password
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = ""
            },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Register Button
        Button(
            onClick = {
                // Trim all input fields
                val trimmedFirstName = firstName.trim()
                val trimmedLastName = lastName.trim()
                val trimmedUsername = username.trim()
                val trimmedCourse = course.trim()
                val trimmedYearLevel = yearLevel.trim()
                val trimmedPassword = password.trim()
                val trimmedConfirmPassword = confirmPassword.trim()

                // Validate inputs
                when {
                    trimmedFirstName.isEmpty() || trimmedLastName.isEmpty() || trimmedUsername.isEmpty() ||
                            trimmedCourse.isEmpty() || trimmedYearLevel.isEmpty() || trimmedPassword.isEmpty() ||
                            trimmedConfirmPassword.isEmpty() -> {
                        errorMessage = "All fields must be filled."
                        Log.d("RegisterActivity", "Registration failed: Fields are empty.")
                    }
                    trimmedPassword.length < 6 -> {
                        errorMessage = "Password must be at least 6 characters."
                        Log.d("RegisterActivity", "Registration failed: Password too short.")
                    }
                    trimmedPassword != trimmedConfirmPassword -> {
                        errorMessage = "Passwords do not match."
                        Log.d("RegisterActivity", "Registration failed: Passwords do not match.")
                    }
                    else -> {
                        val result = dbHelper.addUser(
                            trimmedFirstName,
                            trimmedLastName,
                            trimmedCourse,
                            trimmedYearLevel,
                            trimmedUsername,
                            trimmedPassword
                        )
                        if (result == -1L) {
                            errorMessage = "Username already taken."
                            Log.d("RegisterActivity", "Registration failed: Username already taken.")
                        } else {
                            Log.d("RegisterActivity", "Registration successful for user: $trimmedUsername")
                            onRegisterSuccess()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Register")
        }

        // Error Message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // "Already have an account? Log in here" button
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onNavigateToLogin() // Navigate to Login screen
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Already have an account? Log in here")
        }
    }
}
