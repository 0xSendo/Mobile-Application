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
import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch
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
                ProfileScreen(username, dbHelper, profileViewModel, this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: String,
    dbHelper: DatabaseHelper,
    profileViewModel: ProfileViewModel,
    context: Context
) {
    var isEditing by remember { mutableStateOf(false) }
    val user by remember { mutableStateOf(dbHelper.getUserData(username)) }
    var firstName by remember { mutableStateOf(user?.firstName ?: "") }
    var course by remember { mutableStateOf(user?.course ?: "") }
    var birthdate by remember { mutableStateOf(user?.birthdate ?: "") }
    val imageUri by remember { mutableStateOf(profileViewModel.profileImageUri) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showPictureOptionsDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var highlightedNavItem by remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

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

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color(0xFF1B1B1B),
                drawerContentColor = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1B1B1B), Color(0xFF2A2A2A))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFFA31A), Color(0xFFFFC107))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "MyAcademate",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    val navItems = listOf(
                        Pair("Home", R.drawable.ic_home) to {
                            context.startActivity(Intent(context, HomeActivity::class.java).putExtra("USERNAME", username))
                        },
                        Pair("Tasks", R.drawable.ic_tasks) to {
                            context.startActivity(Intent(context, TaskManagerActivity::class.java).putExtra("USERNAME", username))
                        },
                        Pair("Progress", R.drawable.ic_progress) to {
                            context.startActivity(Intent(context, ProgressTrackerActivity::class.java).putExtra("USERNAME", username))
                        },
                        Pair("Pomodoro", R.drawable.ic_pomodoro) to {
                            context.startActivity(Intent(context, PomodoroActivity::class.java).putExtra("USERNAME", username))
                        },
                        Pair("Expense", R.drawable.ic_calendar) to {
                            context.startActivity(Intent(context, ExpenseActivity::class.java).putExtra("USERNAME", username))
                        },
                        Pair("Profile", R.drawable.ic_profile) to { /* Current screen */ }
                    )

                    navItems.forEach { (pair, action) ->
                        val (label, icon) = pair
                        val isHighlighted = highlightedNavItem == label || label == "Profile"
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    action()
                                    scope.launch { drawerState.close() }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isHighlighted) Color(0xFFFFA31A).copy(alpha = 0.1f) else Color.Transparent
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = icon),
                                    contentDescription = label,
                                    tint = if (isHighlighted) Color(0xFFFFA31A) else Color(0xFFFFFFFF),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = label,
                                    color = if (isHighlighted) Color(0xFFFFA31A) else Color(0xFFFFFFFF),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Profile",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color(0xFFFFA31A)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Add info dialog */ }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = Color(0xFFFFA31A)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1B1B))
                )
            },
            containerColor = Color(0xFF292929)
        ) { paddingValues ->
            ProfileContent(
                username = username,
                dbHelper = dbHelper,
                profileViewModel = profileViewModel,
                paddingValues = paddingValues,
                isEditing = isEditing,
                setIsEditing = { isEditing = it },
                firstName = firstName,
                setFirstName = { firstName = it },
                course = course,
                setCourse = { course = it },
                birthdate = birthdate,
                setBirthdate = { birthdate = it },
                showLogoutDialog = showLogoutDialog,
                setShowLogoutDialog = { showLogoutDialog = it },
                showDatePicker = showDatePicker,
                setShowDatePicker = { showDatePicker = it },
                showPictureOptionsDialog = showPictureOptionsDialog,
                setShowPictureOptionsDialog = { showPictureOptionsDialog = it },
                showDeleteAccountDialog = showDeleteAccountDialog,
                setShowDeleteAccountDialog = { showDeleteAccountDialog = it },
                getContent = getContent,
                permissionLauncher = permissionLauncher
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    username: String,
    dbHelper: DatabaseHelper,
    profileViewModel: ProfileViewModel,
    paddingValues: PaddingValues,
    isEditing: Boolean,
    setIsEditing: (Boolean) -> Unit,
    firstName: String,
    setFirstName: (String) -> Unit,
    course: String,
    setCourse: (String) -> Unit,
    birthdate: String,
    setBirthdate: (String) -> Unit,
    showLogoutDialog: Boolean,
    setShowLogoutDialog: (Boolean) -> Unit,
    showDatePicker: Boolean,
    setShowDatePicker: (Boolean) -> Unit,
    showPictureOptionsDialog: Boolean,
    setShowPictureOptionsDialog: (Boolean) -> Unit,
    showDeleteAccountDialog: Boolean,
    setShowDeleteAccountDialog: (Boolean) -> Unit,
    getContent: ActivityResultLauncher<String>,
    permissionLauncher: ActivityResultLauncher<String>
) {
    val user by remember { mutableStateOf(dbHelper.getUserData(username)) }
    val imageUri by remember { mutableStateOf(profileViewModel.profileImageUri) }
    val context = LocalContext.current

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

        Card(
            modifier = Modifier
                .size(130.dp)
                .padding(top = 16.dp)
                .shadow(8.dp, CircleShape)
                .clickable { setShowPictureOptionsDialog(true) },
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
                .clickable { setShowPictureOptionsDialog(true) }
        )

        Spacer(modifier = Modifier.height(24.dp))

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
                    onValueChange = { if (isEditing) setFirstName(it) },
                    label = "First Name",
                    isEditing = isEditing
                )
                Spacer(modifier = Modifier.height(16.dp))
                ProfileTextField(
                    value = course,
                    onValueChange = { if (isEditing) setCourse(it) },
                    label = "Course",
                    isEditing = isEditing
                )
                Spacer(modifier = Modifier.height(16.dp))
                ProfileTextField(
                    value = birthdate,
                    onValueChange = { if (isEditing) setBirthdate(it) },
                    label = "Birthdate",
                    isEditing = isEditing,
                    onClick = { setShowDatePicker(true) },
                    placeholder = "DD/MM/YYYY"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

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
                            setIsEditing(false)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SecondaryButton(
                        text = "Cancel",
                        onClick = {
                            setFirstName(user?.firstName ?: "")
                            setCourse(user?.course ?: "")
                            setBirthdate(user?.birthdate ?: "")
                            setIsEditing(false)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            AnimatedVisibility(visible = !isEditing) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PrimaryButton(
                        text = "Edit Profile",
                        onClick = { setIsEditing(true) }
                    )
                    SecondaryButton(
                        text = "Settings",
                        onClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) }
                    )
                    SecondaryButton(
                        text = "Log Out",
                        onClick = { setShowLogoutDialog(true) }
                    )
                    OutlinedButton(
                        onClick = { setShowDeleteAccountDialog(true) },
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

    if (showPictureOptionsDialog) {
        CustomDialog(
            title = "Profile Picture Options",
            onDismiss = { setShowPictureOptionsDialog(false) },
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
                        setShowPictureOptionsDialog(false)
                    }) {
                        Text("Upload New Picture", color = Color(0xFFFFA31A), fontSize = 16.sp)
                    }
                    TextButton(onClick = {
                        profileViewModel.setProfileImageUri(context, username, null)
                        setShowPictureOptionsDialog(false)
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
            onDismiss = { setShowLogoutDialog(false) },
            confirmButton = {
                Button(
                    onClick = {
                        setShowLogoutDialog(false)
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
                TextButton(onClick = { setShowLogoutDialog(false) }) {
                    Text("Cancel", color = Color(0xFFFFA31A))
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        CustomDialog(
            title = "Delete Account",
            text = "Are you sure you want to delete your account? This action cannot be undone.",
            onDismiss = { setShowDeleteAccountDialog(false) },
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
                        setShowDeleteAccountDialog(false)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { setShowDeleteAccountDialog(false) }) {
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
