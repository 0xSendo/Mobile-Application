package com.example.myacademate

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val imageUri by profileViewModel::profileImageUri
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPictureOptionsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            profileViewModel.setProfileImageUri(context, username, it)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) getContent.launch("image/*")
    }

    if (showDatePicker) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, day ->
                birthdate = "$day/${month + 1}/$year"
                showDatePicker = false
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(username, context) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF292929), Color(0xFF1B1B1B)),
                        startY = 0f,
                        endY = 1000f
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Username
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFF1B1B1B), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${user?.firstName ?: "User"} ${user?.lastName ?: ""}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Profile Picture
            Card(
                modifier = Modifier
                    .size(130.dp)
                    .padding(top = 16.dp)
                    .shadow(8.dp, CircleShape)
                    .clickable { showPictureOptionsDialog = true },
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
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
                            modifier = Modifier.size(130.dp),
                            tint = Color(0xFFFFA31A)
                        )
                    }
                    this@Card.AnimatedVisibility(
                        visible = !isEditing,
                        enter = fadeIn(animationSpec = tween(300)),
                        exit = fadeOut(animationSpec = tween(300)),
                        modifier = Modifier.align(Alignment.BottomEnd)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFA31A), CircleShape)
                                .padding(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "Change Profile Picture",
                color = Color(0xFFFFA31A),
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable { showPictureOptionsDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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

            Spacer(modifier = Modifier.height(32.dp))

            // Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AnimatedVisibility(visible = isEditing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        PrimaryButton(
                            text = "Save",
                            onClick = {
                                dbHelper.updateUser(
                                    user?.id ?: 0,
                                    firstName,
                                    user?.lastName ?: "",
                                    course,
                                    user?.yearLevel ?: "",
                                    birthdate
                                )
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SecondaryButton(
                            text = "Cancel",
                            onClick = {
                                firstName = user?.firstName ?: ""
                                course = user?.course ?: ""
                                birthdate = user?.birthdate ?: ""
                                isEditing = false
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                AnimatedVisibility(visible = !isEditing) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PrimaryButton(
                            text = "Edit Profile",
                            onClick = { isEditing = true }
                        )
                        SecondaryButton(
                            text = "Settings",
                            onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) }
                        )
                        SecondaryButton(
                            text = "Log Out",
                            onClick = { showLogoutDialog = true }
                        )
                        OutlinedButton(
                            onClick = { showDeleteAccountDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF4444)),
                            border = BorderStroke(1.dp, Color(0xFFFF4444)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Delete Account", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Dialogs with enhanced UI
    if (showPictureOptionsDialog) {
        CustomDialog(
            title = "Profile Picture Options",
            onDismiss = { showPictureOptionsDialog = false },
            content = {
                Column {
                    TextButton(onClick = {
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
                    }) {
                        Text("Upload New Picture", color = Color(0xFFFFA31A), fontSize = 16.sp)
                    }
                    TextButton(onClick = {
                        profileViewModel.setProfileImageUri(context, username, null)
                        showPictureOptionsDialog = false
                    }) {
                        Text("Remove Picture", color = Color(0xFFFFA31A), fontSize = 16.sp)
                    }
                }
            }
        )
    }

    if (showLogoutDialog) {
        CustomDialog(
            title = "Log Out",
            text = "Are you sure you want to log out?",
            onDismiss = { showLogoutDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA31A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Confirm", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFFFFA31A))
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        CustomDialog(
            title = "Delete Account",
            text = "Are you sure you want to delete your account? This action cannot be undone.",
            onDismiss = { showDeleteAccountDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (dbHelper.deleteUser(username)) {
                            profileViewModel.setProfileImageUri(context, username, null)
                            val intent = Intent(context, MainActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                            (context as ComponentActivity).finish()
                        }
                        showDeleteAccountDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", color = Color(0xFFFFA31A))
                }
            }
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
        label = { Text(label, color = Color.White, fontSize = 14.sp) },
        modifier = Modifier.fillMaxWidth(),
        enabled = isEditing,
        readOnly = onClick != null,
        placeholder = placeholder?.let { { Text(it, color = Color(0xFF808080), fontSize = 14.sp) } },
        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (label == "Birthdate") KeyboardType.Number else KeyboardType.Text
        ),
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Color(0xFFFFA31A),
            unfocusedBorderColor = Color(0xFF404040),
            cursorColor = Color(0xFFFFA31A),
            disabledBorderColor = Color(0xFF404040),
            disabledTextColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp),
        interactionSource = remember { MutableInteractionSource() }.also { interactionSource ->
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
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFA31A),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF404040),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun BottomNavigationBar(username: String, context: Context) {
    Surface(
        color = Color(0xFF1B1B1B),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navigationItems = listOf(
                NavigationItem("Home", R.drawable.ic_home) {
                    context.startActivity(Intent(context, HomeActivity::class.java).putExtra("USERNAME", username))
                },
                NavigationItem("Tasks", R.drawable.ic_tasks) {
                    context.startActivity(Intent(context, TaskManagerActivity::class.java).putExtra("USERNAME", username))
                },
                NavigationItem("Progress", R.drawable.ic_progress) {
                    context.startActivity(Intent(context, ProgressTrackerActivity::class.java).putExtra("USERNAME", username))
                },
                NavigationItem("Pomodoro", R.drawable.ic_pomodoro) {
                    context.startActivity(Intent(context, PomodoroActivity::class.java).putExtra("USERNAME", username))
                },
                NavigationItem("Expense", R.drawable.ic_calendar) {
                    context.startActivity(Intent(context, ExpenseActivity::class.java).putExtra("USERNAME", username))
                }
            )
            navigationItems.forEach { item ->
                IconButton(
                    onClick = { item.action() },
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                ) {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CustomDialog(
    title: String,
    text: String? = null,
    onDismiss: () -> Unit,
    content: @Composable (() -> Unit)? = null,
    confirmButton: @Composable (() -> Unit)? = null,
    dismissButton: @Composable (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            if (text != null) {
                Text(text, color = Color(0xFFB0B0B0), fontSize = 16.sp)
            } else {
                content?.invoke()
            }
        },
        confirmButton = { confirmButton?.invoke() ?: Unit },
        dismissButton = { dismissButton?.invoke() ?: Unit },
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(16.dp)
    )
}
