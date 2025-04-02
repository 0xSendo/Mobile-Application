package com.example.myacademate

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberAsyncImagePainter
import com.example.myacademate.ui.theme.MyAcademateTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory(application)
    }
    private val profileViewModel: ProfileViewModel by viewModels()

    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        Log.d("HomeActivity", "Username received: $username")

        val dbHelper = DatabaseHelper(applicationContext)
        val user = dbHelper.getUserData(username)
        Log.d("HomeActivity", "User data fetched in HomeActivity: $user")

        profileViewModel.loadProfileImageUri(this, username)

        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929)
                ) {
                    HomeScreen(username, dbHelper, taskViewModel, expenseViewModel, profileViewModel)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
            finishAffinity()
        } else {
            Toast.makeText(this, "Press back again to exit the app", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}

class ExpenseViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel.getInstance(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun QuickActionButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFA31A),
            contentColor = Color(0xFFFFFFFF)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String,
    dbHelper: DatabaseHelper,
    taskViewModel: TaskViewModel,
    expenseViewModel: ExpenseViewModel,
    profileViewModel: ProfileViewModel
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var highlightedNavItem by remember { mutableStateOf<String?>(null) }
    val taskList = taskViewModel.taskList
    val user = dbHelper.getUserData(username)
    val latestTask = taskList.lastOrNull { !it.isDone }
    val expenseList by expenseViewModel.expenseList.collectAsState()
    val latestExpense = expenseList.lastOrNull()

    val firstName = user?.firstName ?: "Unknown"
    val lastName = user?.lastName ?: "User"
    val course = user?.course ?: "N/A"
    val yearLevel = user?.yearLevel ?: "N/A"
    val profileImageUri by profileViewModel::profileImageUri

    var showTaskCompleteDialog by remember { mutableStateOf(false) }
    var showTaskDeleteDialog by remember { mutableStateOf(false) }
    var showExpensePaidDialog by remember { mutableStateOf(false) }
    var showExpenseDeleteDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(searchQuery) {
        highlightedNavItem = when {
            searchQuery.isBlank() -> null
            "task manager".contains(searchQuery.lowercase()) || "tasks".contains(searchQuery.lowercase()) -> "Tasks"
            "progress".contains(searchQuery.lowercase()) -> "Progress"
            "pomodoro".contains(searchQuery.lowercase()) -> "Pomodoro"
            "expense".contains(searchQuery.lowercase()) -> "Expense"
            "home".contains(searchQuery.lowercase()) -> "Home"
            else -> null
        }
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
                        Pair("Home", R.drawable.ic_home) to {},
                        Pair("Tasks", R.drawable.ic_tasks) to {
                            context.startActivity(Intent(context, TaskManagerActivity::class.java).putExtra("USERNAME", username))
                            (context as? ComponentActivity)?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        },
                        Pair("Progress", R.drawable.ic_progress) to {
                            context.startActivity(Intent(context, ProgressTrackerActivity::class.java).putExtra("USERNAME", username))
                            (context as? ComponentActivity)?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        },
                        Pair("Pomodoro", R.drawable.ic_pomodoro) to {
                            context.startActivity(Intent(context, PomodoroActivity::class.java).putExtra("USERNAME", username))
                            (context as? ComponentActivity)?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        },
                        Pair("Expense", R.drawable.ic_calendar) to {
                            context.startActivity(Intent(context, ExpenseActivity::class.java).putExtra("USERNAME", username))
                            (context as? ComponentActivity)?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                    )

                    navItems.forEach { (pair, action) ->
                        val (label, icon) = pair
                        val isHighlighted = highlightedNavItem == label || label == "Home"
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
                    title = { Text("Home", color = Color(0xFFFFFFFF)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color(0xFFFFFFFF)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1B1B1B)
                    )
                )
            },
            containerColor = Color(0xFF292929)
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clickable {
                                val intent = Intent(context, ProfileActivity::class.java).apply {
                                    putExtra("USERNAME", username)
                                    putExtra("FIRST_NAME", firstName)
                                    putExtra("LAST_NAME", lastName)
                                    putExtra("COURSE", course)
                                    putExtra("YEAR_LEVEL", yearLevel)
                                }
                                context.startActivity(intent)
                                (context as? ComponentActivity)?.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            if (profileImageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        model = profileImageUri,
                                        placeholder = painterResource(id = R.drawable.ic_profile),
                                        error = painterResource(id = R.drawable.ic_profile),
                                        onError = { Log.e("HomeActivity", "Image load failed: ${it.result.throwable}") }
                                    ),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Default Profile Picture",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    tint = Color(0xFFFFFFFF)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "$firstName $lastName",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFFFFF)
                                )
                                Text(
                                    text = "$course - Year $yearLevel",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFFFA31A)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                context.startActivity(Intent(context, NotificationsActivity::class.java).putExtra("USERNAME", username))
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_notifications),
                                    contentDescription = "Notifications",
                                    modifier = Modifier.size(28.dp),
                                    tint = Color(0xFFFFFFFF)
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search", color = Color(0xFF808080)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = Color(0xFFFFFFFF)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFA31A),
                            unfocusedBorderColor = Color(0xFF808080),
                            cursorColor = Color(0xFFFFFFFF),
                            focusedLabelColor = Color(0xFFFFA31A),
                            unfocusedLabelColor = Color(0xFF808080)
                        ),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFFFFFFF)),
                        singleLine = true
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_weather),
                                    contentDescription = "Weather",
                                    modifier = Modifier.size(32.dp),
                                    tint = Color(0xFFFFA31A)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sunny, 25°C",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFFFFA31A)
                                )
                            }
                            Text(
                                text = SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date()),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFFFFFFF)
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Task Manager",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFFFFFFF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (latestTask != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            context.startActivity(Intent(context, TaskManagerActivity::class.java).putExtra("USERNAME", username))
                                        }
                                ) {
                                    Text(
                                        text = latestTask.subjectName,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        color = Color(0xFFFFFFFF)
                                    )
                                    Text(
                                        text = "Due: ${latestTask.dueTime}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF808080)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        IconButton(onClick = { showTaskCompleteDialog = true }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_markdone),
                                                contentDescription = "Mark as Done",
                                                modifier = Modifier.size(24.dp),
                                                tint = Color(0xFFFFFFFF)
                                            )
                                        }
                                        IconButton(onClick = {
                                            context.startActivity(Intent(context, TaskManagerActivity::class.java).putExtra("USERNAME", username))
                                        }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_edit),
                                                contentDescription = "Edit Task",
                                                modifier = Modifier.size(24.dp),
                                                tint = Color(0xFFFFFFFF)
                                            )
                                        }
                                        IconButton(onClick = { showTaskDeleteDialog = true }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_delete),
                                                contentDescription = "Delete Task",
                                                modifier = Modifier.size(24.dp),
                                                tint = Color(0xFFFFFFFF)
                                            )
                                        }
                                    }
                                }

                                if (showTaskCompleteDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showTaskCompleteDialog = false },
                                        title = { Text("Mark Task as Complete", color = Color(0xFFFFFFFF), fontWeight = FontWeight.Bold) },
                                        text = { Text("Mark this task as complete?", color = Color(0xFFFFFFFF)) },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                taskViewModel.toggleTaskStatus(latestTask.id)
                                                showTaskCompleteDialog = false
                                            }) { Text("Yes", color = Color(0xFFFFA31A)) }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showTaskCompleteDialog = false }) { Text("No", color = Color(0xFFFFA31A)) }
                                        },
                                        containerColor = Color(0xFF1B1B1B),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                if (showTaskDeleteDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showTaskDeleteDialog = false },
                                        title = { Text("Delete Task", color = Color(0xFFFFFFFF), fontWeight = FontWeight.Bold) },
                                        text = { Text("Delete this task?", color = Color(0xFFFFFFFF)) },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                taskViewModel.deleteTask(latestTask.id)
                                                showTaskDeleteDialog = false
                                            }) { Text("Yes", color = Color(0xFFFFA31A)) }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showTaskDeleteDialog = false }) { Text("No", color = Color(0xFFFFA31A)) }
                                        },
                                        containerColor = Color(0xFF1B1B1B),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "No tasks available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF808080)
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Expense Tracker",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFFFFFFF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (latestExpense != null) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            context.startActivity(Intent(context, ExpenseActivity::class.java).putExtra("USERNAME", username))
                                        }
                                ) {
                                    Text(
                                        text = latestExpense.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                        color = Color(0xFFFFFFFF)
                                    )
                                    Text(
                                        text = "Amount: $${String.format("%.2f", latestExpense.amount)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF808080)
                                    )
                                    Text(
                                        text = "Status: ${if (latestExpense.completionPercentage > 0) "Partially Paid (${latestExpense.completionPercentage}%)" else "To Be Paid"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFF808080)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.align(Alignment.End)
                                    ) {
                                        IconButton(onClick = { showExpensePaidDialog = true }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_markdone),
                                                contentDescription = "Mark as Paid",
                                                modifier = Modifier.size(24.dp),
                                                tint = Color(0xFFFFFFFF)
                                            )
                                        }
                                        IconButton(onClick = {
                                            context.startActivity(Intent(context, ExpenseActivity::class.java).putExtra("USERNAME", username))
                                        }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_edit),
                                                contentDescription = "Edit Expense",
                                                modifier = Modifier.size(24.dp),
                                                tint = Color(0xFFFFFFFF)
                                            )
                                        }
                                        IconButton(onClick = { showExpenseDeleteDialog = true }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_delete),
                                                contentDescription = "Delete Expense",
                                                modifier = Modifier.size(24.dp),
                                                tint = Color(0xFFFFFFFF)
                                            )
                                        }
                                    }
                                }

                                if (showExpensePaidDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showExpensePaidDialog = false },
                                        title = { Text("Mark Expense as Paid", color = Color(0xFFFFFFFF), fontWeight = FontWeight.Bold) },
                                        text = { Text("Mark this expense as paid? This will delete it.", color = Color(0xFFFFFFFF)) },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                expenseViewModel.deleteExpense(latestExpense)
                                                showExpensePaidDialog = false
                                            }) { Text("Yes", color = Color(0xFFFFA31A)) }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showExpensePaidDialog = false }) { Text("No", color = Color(0xFFFFA31A)) }
                                        },
                                        containerColor = Color(0xFF1B1B1B),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }

                                if (showExpenseDeleteDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showExpenseDeleteDialog = false },
                                        title = { Text("Delete Expense", color = Color(0xFFFFFFFF), fontWeight = FontWeight.Bold) },
                                        text = { Text("Delete this expense?", color = Color(0xFFFFFFFF)) },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                expenseViewModel.deleteExpense(latestExpense)
                                                showExpenseDeleteDialog = false
                                            }) { Text("Yes", color = Color(0xFFFFA31A)) }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showExpenseDeleteDialog = false }) { Text("No", color = Color(0xFFFFA31A)) }
                                        },
                                        containerColor = Color(0xFF1B1B1B),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = "No expenses added yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF808080)
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Progress Snapshot",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFFFFFFF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Tasks Completed",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color(0xFFFFFFFF)
                                    )
                                    Text(
                                        text = "${taskList.count { it.isDone }} / ${taskList.size}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFFFA31A)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Expenses Paid",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color(0xFFFFFFFF)
                                    )
                                    Text(
                                        text = "${expenseList.count { it.completionPercentage == 100 }} / ${expenseList.size}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color(0xFFFFA31A)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Games",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFFFFFFF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Card(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clickable {
                                            context.startActivity(Intent(context, MathChallengeActivity::class.java).putExtra("USERNAME", username))
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF292929)),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Math Challenge",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                            color = Color(0xFFFFFFFF),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                                Card(
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clickable {
                                            context.startActivity(Intent(context, GuessNumberActivity::class.java).putExtra("USERNAME", username))
                                        },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF292929)),
                                    elevation = CardDefaults.cardElevation(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Guess the Number",
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                            color = Color(0xFFFFFFFF),
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Quick Actions",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFFFFFFF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                QuickActionButton("Add Task") {
                                    context.startActivity(Intent(context, TaskManagerActivity::class.java).putExtra("USERNAME", username))
                                }.run { Modifier.weight(1f) }
                                QuickActionButton("Add Expense") {
                                    context.startActivity(Intent(context, ExpenseActivity::class.java).putExtra("USERNAME", username))
                                }.run { Modifier.weight(1f) }
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Recent Activity",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFFFFFFF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (latestTask != null) {
                                Text(
                                    text = "Added Task: ${latestTask.subjectName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF808080)
                                )
                            }
                            if (latestExpense != null) {
                                Text(
                                    text = "Added Expense: ${latestExpense.name} ($${String.format("%.2f", latestExpense.amount)})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF808080)
                                )
                            }
                            if (latestTask == null && latestExpense == null) {
                                Text(
                                    text = "No recent activity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF808080)
                                )
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Study Tip of the Day",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFFFFFFF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Break your study sessions into 25-minute chunks with 5-minute breaks (Pomodoro Technique).",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFFFA31A)
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Daily Motivation",
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFFFFFFFF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "\"The only way to do great work is to love what you do.\" - Steve Jobs",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFFFA31A),
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun GameCard(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF292929)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFFFFFFFF),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

data class NavigationItem(
    val label: String,
    @DrawableRes val icon: Int,
    val action: () -> Unit
)

fun formatDueDate(dueTime: String): String {
    return dueTime // Add proper date formatting if needed
}
