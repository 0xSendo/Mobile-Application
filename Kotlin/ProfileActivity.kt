package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myacademate.ui.theme.MyAcademateTheme

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = DatabaseHelper(applicationContext)
        val username = intent.getStringExtra("USERNAME") ?: ""
        Log.d("HomeActivity", "Username received: $username")

        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen(username, dbHelper)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(username: String, dbHelper: DatabaseHelper) {
    var isEditing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    // Fetch user data from the database
    val user = dbHelper.getUserData(username)
    var name by remember { mutableStateOf("") }
    var course by remember { mutableStateOf("") }
    var birthdate by remember { mutableStateOf("") }
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile Picture (Use Image or Icon for Profile Image)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Editable Name (Pre-filled)
        TextField(
            value = name,
            onValueChange = { if (isEditing) name = it },
            label = { Text("Name") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            enabled = isEditing,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
        )

        // Editable Course/Section (Pre-filled)
        TextField(
            value = course,
            onValueChange = { if (isEditing) course = it },
            label = { Text("Course/Section") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            enabled = isEditing,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
        )

        // Editable Birthdate (Pre-filled)
        TextField(
            value = birthdate,
            onValueChange = { if (isEditing) birthdate = it },
            label = { Text("Birthdate") },
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            enabled = isEditing,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Buttons Section
        Column(modifier = Modifier.fillMaxWidth()) {
            // Edit Profile Button
            Button(
                onClick = { isEditing = !isEditing },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = if (isEditing) "Save Changes" else "Edit Profile")
            }

            // Save Profile Button (Only Visible When Editing)
            if (isEditing) {
                Button(
                    onClick = { /* Handle Save Action */ },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(text = if (isEditing) "Cancel edit" else "Edit Profile")
                }
            }

            // Log Out Button
            Button(
                onClick = {

                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = "Log Out")
            }


            // Delete Account Button
            Button(
                onClick = { /* Handle Delete Account Action */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "Delete Account")
            }
        }
    }
}


