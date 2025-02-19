fix the placement of the profile and i also have double "save changes" button remove the other one and fix the birthdate field where i can actually input dates 

package com.example.myacademate

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.myacademate.ui.theme.MyAcademateTheme

class ProfileActivity : ComponentActivity() {
    private var imageUri: Uri? by mutableStateOf(null)
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

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
                    ProfileScreen(username, dbHelper, imageUri, onImageSelected = { getContent.launch("image/*") })
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(username: String, dbHelper: DatabaseHelper, imageUri: Uri?, onImageSelected: () -> Unit) {
    var isEditing by remember { mutableStateOf(false) }
    val user by remember { mutableStateOf(dbHelper.getUserData(username)) }
    var name by remember { mutableStateOf(user?.firstName ?: "") }
    var course by remember { mutableStateOf(user?.course ?: "") }
    var birthdate by remember { mutableStateOf(user?.birthdate ?: "") }
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(bottom = 12.dp)
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberImagePainter(imageUri),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Button(
            onClick = onImageSelected,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Change Profile Picture")
        }

        TextField(
            value = name,
            onValueChange = { if (isEditing) name = it },
            label = { Text("Name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = isEditing,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
        )

        TextField(
            value = course,
            onValueChange = { if (isEditing) course = it },
            label = { Text("Course/Year") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = isEditing,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text)
        )

        TextField(
            value = birthdate,
            onValueChange = { if (isEditing) birthdate = it },
            label = { Text("Birthdate") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = isEditing,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { isEditing = !isEditing },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = if (isEditing) "Save Changes" else "Edit Profile")
            }

            if (isEditing) {
                Button(
                    onClick = {
                        dbHelper.updateUser(user?.id ?: 0, name, user?.lastName ?: "", course, user?.yearLevel ?: "", birthdate)
                        isEditing = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text(text = "Save Changes")
                }
            }

            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = "Log Out")
            }

            Button(
                onClick = {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text(text = "Settings")
            }

            Button(
                onClick = { /* Handle Delete Account Action */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text(text = "Delete Account")
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showDialog = false
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") }
        )
    }
}
