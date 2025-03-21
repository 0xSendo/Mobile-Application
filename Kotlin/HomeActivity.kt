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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberAsyncImagePainter
import com.example.myacademate.ui.theme.MyAcademateTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((applicationContext as Application))
    }
    private val profileViewModel: ProfileViewModel by viewModels()

    // Variables for back press handling
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000L // 2 seconds interval

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

    // Override onBackPressed to implement double press to exit
    override fun onBackPressed() {
        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
            finishAffinity() // Close all activities and exit app
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

    Scaffold(
        bottomBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val navigationItems = listOf(
                        NavigationItem("Home", R.drawable.ic_home) { /* Already on Home screen */ },
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
                        val isHighlighted = highlightedNavItem == item.label
                        IconButton(
                            onClick = { item.action() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (isHighlighted) Color(0xFFFFA31A).copy(alpha = 0.3f) else Color.Transparent,
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.label,
                                modifier = Modifier.size(32.dp),
                                tint = if (isHighlighted) Color(0xFFFFA31A) else Color(0xFFFFFFFF)
                            )
                        }
                    }
                }
            }
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
            // Profile Section
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
                                text = "Welcome, $firstName $lastName!",
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

            // Search Bar
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

            // Today Section (Merged Weather and Date)
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
                                painter = painterResource(id = R.drawable.ic_weather), // Add your weather icon
                                contentDescription = "Weather",
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFFFFA31A)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Sunny, 25°C", // Placeholder, integrate with weather API
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFFFA31A)
                            )
                        }
                        Text(
                            text = SimpleDateFormat("EEE, MMM dd", Locale.getDefault()).format(Date()), // Shortened format
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFFFFFF)
                        )
                    }
                }
            }

            // Task Manager
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
                                    text = "Due ${formatDueDate(latestTask.dueTime)}",
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

            // Expense Tracker
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

            // Progress Snapshot
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

            // Games Section
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
                            GameCard("Math Challenge") {
                                context.startActivity(Intent(context, MathChallengeActivity::class.java).putExtra("USERNAME", username))
                            }
                            GameCard("Guess the Number") {
                                context.startActivity(Intent(context, GuessNumberActivity::class.java).putExtra("USERNAME", username))
                            }
                        }
                    }
                }
            }

            // Quick Actions
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

            // Recent Activity
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

            // Study Tips
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

            // Motivational Quote
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

            // Spacer at the bottom
            item {
                Spacer(modifier = Modifier.height(16.dp))
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
