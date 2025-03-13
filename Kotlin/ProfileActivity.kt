package com.example.myacademate

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.TextStyle
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

        profileViewModel.loadProfileImageUri(this, username)

        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929)
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

    Scaffold(
        bottomBar = {
            BottomNavigationBar(username, context)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { showPictureOptionsDialog = true },
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                model = imageUri,
                                placeholder = painterResource(id = R.drawable.ic_profile),
                                error = painterResource(id = R.drawable.ic_error)
                            ),
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(120.dp),
                            tint = Color(0xFFFFA31A)
                        )
                    }
                    if (!isEditing) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Change Profile Picture",
                color = Color(0xFFFFA31A),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { showPictureOptionsDialog = true },
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileTextField(
                        value = firstName,
                        onValueChange = { if (isEditing) firstName = it },
                        label = "First Name",
                        isEditing = isEditing
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileTextField(
                        value = course,
                        onValueChange = { if (isEditing) course = it },
                        label = "Course",
                        isEditing = isEditing
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ProfileTextField(
                        value = birthdate,
                        onValueChange = { if (isEditing) birthdate = it },
                        label = "Birthdate",
                        isEditing = isEditing,
                        onClick = { showDatePicker = true },
                        placeholder = "DD/MM/YYYY"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(
                    text = if (isEditing) "Save" else "Edit Profile",
                    onClick = {
                        if (isEditing) {
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
                    }
                )
                SecondaryButton(
                    text = "Settings",
                    onClick = {
                        context.startActivity(Intent(context, SettingsActivity::class.java))
                    }
                )
                SecondaryButton(
                    text = "Log Out",
                    onClick = { showLogoutDialog = true }
                )
                OutlinedButton(
                    onClick = { showDeleteAccountDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFFFA31A)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFFA31A))
                ) {
                    Text("Delete Account")
                }
            }
        }
    }

    if (showPictureOptionsDialog) {
        AlertDialog(
            onDismissRequest = { showPictureOptionsDialog = false },
            title = { Text("Profile Picture Options", color = Color(0xFFFFFFFF)) },
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
                        Text("Upload New Picture", color = Color(0xFFFFA31A))
                    }
                    TextButton(
                        onClick = {
                            profileViewModel.setProfileImageUri(context, username, null)
                            Log.d("ProfileActivity", "Profile picture removed")
                            showPictureOptionsDialog = false
                        }
                    ) {
                        Text("Remove Picture", color = Color(0xFFFFA31A))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPictureOptionsDialog = false }) {
                    Text("Cancel", color = Color(0xFFFFA31A))
                }
            },
            containerColor = Color(0xFF1B1B1B)
        )
    }

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
                        containerColor = Color(0xFFFFA31A),
                        contentColor = Color(0xFFFFFFFF)
                    )
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF808080),
                        contentColor = Color(0xFFFFFFFF)
                    )
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Log Out", color = Color(0xFFFFFFFF)) },
            text = { Text("Are you sure you want to log out?", color = Color(0xFFFFFFFF)) },
            containerColor = Color(0xFF1B1B1B)
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text("Delete Account", color = Color(0xFFFFFFFF)) },
            text = { Text("Are you sure you want to delete your account? This action cannot be undone.", color = Color(0xFFFFFFFF)) },
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
                    Text("Yes", color = Color(0xFFFFA31A))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteAccountDialog = false }
                ) {
                    Text("No", color = Color(0xFFFFA31A))
                }
            },
            containerColor = Color(0xFF1B1B1B)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isEditing: Boolean,
    onClick: (() -> Unit)? = null,
    placeholder: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFFFFFFFF)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = isEditing,
        readOnly = onClick != null,
        placeholder = placeholder?.let { { Text(it, color = Color(0xFF808080)) } },
        textStyle = TextStyle(color = Color(0xFFFFFFFF)),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (label == "Birthdate") KeyboardType.Number else KeyboardType.Text
        ),
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFFFFA31A),
            unfocusedBorderColor = Color(0xFF808080)
        ),
        interactionSource = remember { MutableInteractionSource() }
            .also { interactionSource ->
                LaunchedEffect(interactionSource) {
                    interactionSource.interactions.collect { interaction ->
                        if (interaction is PressInteraction.Release && isEditing && onClick != null) {
                            onClick()
                        }
                    }
                }
            }
    )
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFA31A),
            contentColor = Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF808080),
            contentColor = Color(0xFFFFFFFF)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun BottomNavigationBar(username: String, context: Context) {
    Surface(
        color = Color(0xFF1B1B1B),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navigationItems = listOf(
                NavigationItem("Home", R.drawable.ic_home) {
                    val intent = Intent(context, HomeActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    context.startActivity(intent)
                },
                NavigationItem("Tasks", R.drawable.ic_tasks) {
                    val intent = Intent(context, TaskManagerActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    context.startActivity(intent)
                },
                NavigationItem("Progress", R.drawable.ic_progress) {
                    val intent = Intent(context, ProgressTrackerActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    context.startActivity(intent)
                },
                NavigationItem("Pomodoro", R.drawable.ic_pomodoro) {
                    val intent = Intent(context, PomodoroActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    context.startActivity(intent)
                },
                NavigationItem("Expense", R.drawable.ic_calendar) {
                    val intent = Intent(context, ExpenseActivity::class.java)
                    intent.putExtra("USERNAME", username)
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
