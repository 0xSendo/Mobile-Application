package com.example.myacademate

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import com.example.myacademate.ui.theme.MyAcademateTheme
import java.util.Calendar

class ProfileActivity : ComponentActivity() {
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val dbHelper = DatabaseHelper(applicationContext)
        val username = intent.getStringExtra("USERNAME") ?: ""

        // Load initial profile image URI
        profileViewModel.loadProfileImageUri(this, username)

        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929) // background: Dark Gray #292929
                ) {
                    ProfileScreen(username, dbHelper, profileViewModel)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    username: String,
    dbHelper: DatabaseHelper,
    profileViewModel: ProfileViewModel
) {
    var isEditing by remember { mutableStateOf(false) }
    val user by remember { mutableStateOf(dbHelper.getUserData(username)) }
    var firstName by remember { mutableStateOf(user?.firstName ?: "") }
    var course by remember { mutableStateOf(user?.course ?: "") }
    var birthdate by remember { mutableStateOf(user?.birthdate ?: "") }
    val imageUri by remember { mutableStateOf(profileViewModel.profileImageUri) }
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPictureOptionsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    // Image picker launcher
    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            profileViewModel.setProfileImageUri(context, username, it)
            Log.d("ProfileActivity", "Image URI saved: $it")
        } ?: Log.e("ProfileActivity", "No URI returned")
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            getContent.launch("image/*")
        } else {
            Log.e("ProfileActivity", "Permission denied")
        }
    }

    if (showDatePicker) {
        val datePicker = DatePickerDialog(
            context,
            { _, year, month, day ->
                birthdate = "$day/${month + 1}/$year"
                showDatePicker = false
            },
            Calendar.getInstance().get(Calendar.YEAR),
            Calendar.getInstance().get(Calendar.MONTH),
            Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Profile Picture Section
        Box(
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = imageUri,
                        placeholder = painterResource(id = R.drawable.ic_profile),
                        error = painterResource(id = R.drawable.ic_error),
                        onSuccess = { Log.d("ProfileActivity", "Image loaded successfully") },
                        onError = { Log.e("ProfileActivity", "Image load failed: ${it.result.throwable}") }
                    ),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.size(120.dp),
                    tint = Color(0xFFFFA31A) // primary: Orange #ffa31a
                )
            }
        }

        Button(
            onClick = { showPictureOptionsDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
            )
        ) {
            Text("Change Profile Picture")
        }

        // Editable Fields
        TextField(
            value = firstName,
            onValueChange = { if (isEditing) firstName = it },
            label = { Text("First Name", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFFFFF)) // onSurface: White #ffffff
        )

        TextField(
            value = course,
            onValueChange = { if (isEditing) course = it },
            label = { Text("Course", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            enabled = isEditing,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFFFFF)) // onSurface: White #ffffff
        )

        TextField(
            value = birthdate,
            onValueChange = { if (isEditing) birthdate = it },
            label = { Text("Birthdate", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clickable(enabled = isEditing) { showDatePicker = true },
            enabled = isEditing,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text("DD/MM/YYYY", color = Color(0xFF808080)) }, // secondary: Gray #808080
            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFFFFF)) // onSurface: White #ffffff
        )

        // Buttons Section
        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (isEditing) {
                        // Save changes to database
                        dbHelper.updateUser(
                            user?.id ?: 0,
                            firstName,
                            user?.lastName ?: "",
                            course,
                            user?.yearLevel ?: "",
                            birthdate
                        )
                    }
                    isEditing = !isEditing
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                    contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                )
            ) {
                Text(if (isEditing) "Save" else "Edit Profile")
            }

            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF808080), // secondary: Gray #808080 (replacing error)
                    contentColor = Color(0xFFFFFFFF) // onSecondary: White #ffffff
                )
            ) {
                Text("Log Out")
            }

            Button(
                onClick = {
                    val intent = Intent(context, SettingsActivity::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF808080), // secondary: Gray #808080
                    contentColor = Color(0xFFFFFFFF) // onSecondary: White #ffffff
                )
            ) {
                Text("Settings")
            }

            Button(
                onClick = { showDeleteAccountDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a (replacing red)
                    contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                )
            ) {
                Text("Delete Account")
            }
        }
    }

    // Picture Options Dialog
    if (showPictureOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPictureOptionsDialog = false },
            title = { Text("Profile Picture Options", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
            text = {
                Column {
                    TextButton(
                        onClick = {
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                                permissionLauncher.launch(permission)
                            } else {
                                getContent.launch("image/*")
                            }
                            showPictureOptionsDialog = false
                        }
                    ) {
                        Text("Upload New Picture", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                    }
                    TextButton(
                        onClick = {
                            profileViewModel.setProfileImageUri(context, username, null)
                            Log.d("ProfileActivity", "Profile picture removed")
                            showPictureOptionsDialog = false
                        }
                    ) {
                        Text("Remove Picture", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPictureOptionsDialog = false }) {
                    Text("Cancel", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                }
            },
            containerColor = Color(0xFF1B1B1B) // surface: Darker Gray #1b1b1b
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF808080), // secondary: Gray #808080
                        contentColor = Color(0xFFFFFFFF) // onSecondary: White #ffffff
                    )
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Log Out", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
            text = { Text("Are you sure you want to log out?", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
            containerColor = Color(0xFF1B1B1B) // surface: Darker Gray #1b1b1b
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
            confirmButton = {
                TextButton(
                    onClick = {
                        val deleted = dbHelper.deleteUser(username)
                        if (deleted) {
                            Log.d("ProfileActivity", "Account deleted for username: $username")
                            profileViewModel.setProfileImageUri(context, username, null)
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            (context as ComponentActivity).finish()
                        } else {
                            Log.e("ProfileActivity", "Failed to delete account for username: $username")
                        }
                        showDeleteAccountDialog = false
                    }
                ) {
                    Text("Yes", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false }
                ) {
                    Text("No", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                }
            },
            containerColor = Color(0xFF1B1B1B) // surface: Darker Gray #1b1b1b
        )
    }

    // Bottom Navigation
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter), // Align Row at the bottom
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navigationItems = listOf(
                NavigationItem("Home", R.drawable.ic_home) {
                    val intent = Intent(context, HomeActivity::class.java)
                    intent.putExtra("USERNAME", username) // Pass username
                    context.startActivity(intent)
                },
                NavigationItem("Tasks", R.drawable.ic_tasks) {
                    val intent = Intent(context, TaskManagerActivity::class.java)
                    intent.putExtra("USERNAME", username) // Pass username
                    context.startActivity(intent)
                },
                NavigationItem("Progress", R.drawable.ic_progress) {
                    val intent = Intent(context, ProgressTrackerActivity::class.java)
                    intent.putExtra("USERNAME", username) // Pass username
                    context.startActivity(intent)
                },
                NavigationItem("Pomodoro", R.drawable.ic_pomodoro) {
                    val intent = Intent(context, PomodoroActivity::class.java)
                    intent.putExtra("USERNAME", username) // Pass username
                    context.startActivity(intent)
                },
                NavigationItem("Expense", R.drawable.ic_calendar) {
                    val intent = Intent(context, ExpenseActivity::class.java)
                    intent.putExtra("USERNAME", username) // Pass username
                    context.startActivity(intent)
                }
            )
            navigationItems.forEach { item ->
                IconButton(
                    onClick = { item.action() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFFFFFFF)
                    )
                }
            }
        }
    }
}
