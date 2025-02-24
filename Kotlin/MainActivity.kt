package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    private lateinit var dbHelper: DatabaseHelper
    private val taskViewModel: TaskViewModel by viewModels() // Initialize ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseHelper(this)

        setContent {
            val customColors = darkColorScheme(
                primary = Color(0xFFFFA31A),  // Orange
                secondary = Color(0xFF808080),  // Gray
                background = Color(0xFF292929),  // Dark Gray
                surface = Color(0xFF1B1B1B),  // Darker Gray
                onPrimary = Color(0xFFFFFFFF),  // White
                onSecondary = Color(0xFFFFFFFF),  // White
                onBackground = Color(0xFFFFFFFF),  // White
                onSurface = Color(0xFFFFFFFF),  // White
                error = Color(0xFFCF6679),  // Red
                onError = Color.Black
            )

            MaterialTheme(
                colorScheme = customColors,
                typography = Typography()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = customColors.background
                ) {
                    LoginScreen(
                        onLoginSuccess = { username -> // Receive username here
                            Log.d("MainActivity", "Login successful for: $username")
                            val intent = Intent(this@MainActivity, HomeActivity::class.java).apply {
                                putExtra("USERNAME", username) // Pass username to HomeActivity
                            }
                            startActivity(intent)
                            finish()
                        },
                        onRegister = {
                            startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
                        },
                        dbHelper = dbHelper
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit, // Modify to accept username as parameter
    onRegister: () -> Unit,
    dbHelper: DatabaseHelper
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to MyAcademate",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
        )
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary
            )
        } else {
            Button(
                onClick = {
                    isLoading = true
                    if (dbHelper.checkUserCredentials(username, password)) {
                        onLoginSuccess(username) // Pass the username here
                    } else {
                        errorMessage = "Invalid credentials. Try again."
                        Log.e("LoginActivity", "Failed login attempt: $username")
                    }
                    isLoading = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Login", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
        Button(
            onClick = { onRegister() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(text = "Don't have an account? Register here", color = MaterialTheme.colorScheme.onSecondary)
        }
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
