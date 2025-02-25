package com.example.myacademate

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import coil.compose.rememberAsyncImagePainter
import com.example.myacademate.ui.theme.MyAcademateTheme

class HomeActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((applicationContext as Application))
    }
    private val profileViewModel: ProfileViewModel by viewModels()

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
    var highlightedNavItem by remember { mutableStateOf<String?>(null) } // Track highlighted nav item
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

    // Search logic
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile Section (unchanged)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(context, ProfileActivity::class.java).apply {
                        putExtra("USERNAME", username)
                        putExtra("FIRST_NAME", firstName)
                        putExtra("LAST_NAME", lastName)
                        putExtra("COURSE", course)
                        putExtra("YEAR_LEVEL", yearLevel)
                    }
                    context.startActivity(intent)
                }
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
                    style = androidx.compose.material3.Typography().titleLarge,
                    color = Color(0xFFFFFFFF)
                )
                Text(
                    text = "$course - Year $yearLevel",
                    style = androidx.compose.material3.Typography().titleMedium,
                    color = Color(0xFFFFA31A)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                val intent = Intent(context, NotificationsActivity::class.java)
                intent.putExtra("USERNAME", username)
                context.startActivity(intent)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notifications),
                    contentDescription = "Notifications",
                    modifier = Modifier.size(28.dp),
                    tint = Color(0xFFFFFFFF)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search", color = Color(0xFFFFFFFF)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = Color(0xFFFFFFFF)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF808080),
                unfocusedBorderColor = Color(0xFFFFFFFF),
                cursorColor = Color(0xFFFFFFFF),
                focusedLabelColor = Color(0xFF808080),
                unfocusedLabelColor = Color(0xFFFFFFFF)
            ),
            textStyle = androidx.compose.material3.Typography().bodyLarge.copy(color = Color(0xFFFFFFFF))
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Task Manager Section (unchanged)
        Text(
            text = "Task Manager",
            style = androidx.compose.material3.Typography().headlineSmall,
            color = Color(0xFFFFFFFF)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (latestTask != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        val intent = Intent(context, TaskManagerActivity::class.java)
                        intent.putExtra("USERNAME", username)
                        context.startActivity(intent)
                    },
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = latestTask.subjectName,
                        style = androidx.compose.material3.Typography().bodyLarge,
                        color = Color(0xFFFFFFFF)
                    )
                    Text(
                        text = "Due ${formatDueDate(latestTask.dueTime)}",
                        style = androidx.compose.material3.Typography().bodyMedium,
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
                            val intent = Intent(context, TaskManagerActivity::class.java)
                            intent.putExtra("USERNAME", username)
                            context.startActivity(intent)
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
                        title = { Text("Mark Task as Complete", color = Color(0xFFFFFFFF)) },
                        text = { Text("Mark this task as complete?", color = Color(0xFFFFFFFF)) },
                        confirmButton = {
                            TextButton(onClick = {
                                taskViewModel.toggleTaskStatus(latestTask.id)
                                showTaskCompleteDialog = false
                            }) {
                                Text("Yes", color = Color(0xFFFFA31A))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTaskCompleteDialog = false }) {
                                Text("No", color = Color(0xFFFFA31A))
                            }
                        },
                        containerColor = Color(0xFF1B1B1B)
                    )
                }

                if (showTaskDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showTaskDeleteDialog = false },
                        title = { Text("Delete Task", color = Color(0xFFFFFFFF)) },
                        text = { Text("Delete this task?", color = Color(0xFFFFFFFF)) },
                        confirmButton = {
                            TextButton(onClick = {
                                taskViewModel.deleteTask(latestTask.id)
                                showTaskDeleteDialog = false
                            }) {
                                Text("Yes", color = Color(0xFFFFA31A))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTaskDeleteDialog = false }) {
                                Text("No", color = Color(0xFFFFA31A))
                            }
                        },
                        containerColor = Color(0xFF1B1B1B)
                    )
                }
            }
        } else {
            Text(
                text = "No tasks available",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFFFFFFFF)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Expense Tracker Section (unchanged)
        Text(
            text = "Expense Tracker",
            style = androidx.compose.material3.Typography().headlineSmall,
            color = Color(0xFFFFFFFF)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (latestExpense != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        val intent = Intent(context, ExpenseActivity::class.java)
                        intent.putExtra("USERNAME", username)
                        context.startActivity(intent)
                    },
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = latestExpense.name,
                        style = androidx.compose.material3.Typography().bodyLarge,
                        color = Color(0xFFFFFFFF)
                    )
                    Text(
                        text = "Amount: $${latestExpense.amount}",
                        style = androidx.compose.material3.Typography().bodyMedium,
                        color = Color(0xFF808080)
                    )
                    Text(
                        text = "Status: ${if (latestExpense.completionPercentage > 0) "Partially Paid (${latestExpense.completionPercentage}%)" else "To Be Paid"}",
                        style = androidx.compose.material3.Typography().bodyMedium,
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
                            val intent = Intent(context, ExpenseActivity::class.java)
                            intent.putExtra("USERNAME", username)
                            context.startActivity(intent)
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
                        title = { Text("Mark Expense as Paid", color = Color(0xFFFFFFFF)) },
                        text = { Text("Mark this expense as paid? This will delete it.", color = Color(0xFFFFFFFF)) },
                        confirmButton = {
                            TextButton(onClick = {
                                expenseViewModel.deleteExpense(latestExpense)
                                showExpensePaidDialog = false
                            }) {
                                Text("Yes", color = Color(0xFFFFA31A))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExpensePaidDialog = false }) {
                                Text("No", color = Color(0xFFFFA31A))
                            }
                        },
                        containerColor = Color(0xFF1B1B1B)
                    )
                }

                if (showExpenseDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showExpenseDeleteDialog = false },
                        title = { Text("Delete Expense", color = Color(0xFFFFFFFF)) },
                        text = { Text("Delete this expense?", color = Color(0xFFFFFFFF)) },
                        confirmButton = {
                            TextButton(onClick = {
                                expenseViewModel.deleteExpense(latestExpense)
                                showExpenseDeleteDialog = false
                            }) {
                                Text("Yes", color = Color(0xFFFFA31A))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExpenseDeleteDialog = false }) {
                                Text("No", color = Color(0xFFFFA31A))
                            }
                        },
                        containerColor = Color(0xFF1B1B1B)
                    )
                }
            }
        } else {
            Text(
                text = "No expenses added yet",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFFFFFFFF)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Games Section (unchanged)
        Text(
            text = "Games",
            style = androidx.compose.material3.Typography().headlineSmall,
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
                        val intent = Intent(context, MathChallengeActivity::class.java)
                        intent.putExtra("USERNAME", username)
                        context.startActivity(intent)
                    },
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Math Challenge",
                        style = androidx.compose.material3.Typography().bodyLarge,
                        color = Color(0xFFFFFFFF)
                    )
                }
            }

            Card(
                modifier = Modifier
                    .size(150.dp)
                    .clickable {
                        val intent = Intent(context, GuessNumberActivity::class.java)
                        intent.putExtra("USERNAME", username)
                        context.startActivity(intent)
                    },
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B))
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Guess the Number",
                        style = androidx.compose.material3.Typography().bodyLarge,
                        color = Color(0xFFFFFFFF)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Navigation Bar with Highlighting
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navigationItems = listOf(
                NavigationItem("Home", R.drawable.ic_home) { /* Current screen */ },
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
}

data class NavigationItem(
    val label: String,
    @DrawableRes val icon: Int,
    val action: () -> Unit
)

fun formatDueDate(dueTime: String): String {
    return dueTime
}
