package com.example.myacademate

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
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

class HomeActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()
    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((applicationContext as Application))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("USERNAME") ?: ""
        Log.d("HomeActivity", "Username received: $username")

        val dbHelper = DatabaseHelper(applicationContext)

        // Fetch user data for debugging
        val user = dbHelper.getUserData(username)
        Log.d("HomeActivity", "User data fetched in HomeActivity: $user")

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
                typography = MaterialTheme.typography
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = customColors.background
                ) {
                    HomeScreen(username, dbHelper, taskViewModel, expenseViewModel)
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
    expenseViewModel: ExpenseViewModel
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val taskList = taskViewModel.taskList
    val user = dbHelper.getUserData(username)
    val latestTask = taskList.lastOrNull { !it.isDone }
    val expenseList by expenseViewModel.expenseList.collectAsState()
    val latestExpense = expenseList.lastOrNull()

    // Handle null or missing user data safely by providing fallback values
    val firstName = user?.firstName ?: "Unknown"
    val lastName = user?.lastName ?: "User"
    val course = user?.course ?: "N/A"
    val yearLevel = user?.yearLevel ?: "N/A"

    // Load profile image URI from SharedPreferences
    val profileImageUri by remember {
        mutableStateOf(
            Uri.parse(
                context.getSharedPreferences("profile_prefs", Context.MODE_PRIVATE)
                    .getString("profile_image_uri_$username", null)
            )
        )
    }

    var showTaskCompleteDialog by remember { mutableStateOf(false) }
    var showTaskDeleteDialog by remember { mutableStateOf(false) }
    var showExpensePaidDialog by remember { mutableStateOf(false) }
    var showExpenseDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Profile Section
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
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "$firstName $lastName",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "$course - Year $yearLevel",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                val intent = Intent(context, NotificationsActivity::class.java)
                context.startActivity(intent)
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notifications),
                    contentDescription = "Notifications",
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                cursorColor = MaterialTheme.colorScheme.onBackground,
                focusedLabelColor = MaterialTheme.colorScheme.secondary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
            ),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Task Manager Section
        Text(
            text = "Task Manager",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (latestTask != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        val intent = Intent(context, TaskManagerActivity::class.java)
                        context.startActivity(intent)
                    },
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = latestTask.subjectName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Due ${formatDueDate(latestTask.dueTime)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        IconButton(
                            onClick = { showTaskCompleteDialog = true }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_markdone),
                                contentDescription = "Mark as Done",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                val intent = Intent(context, TaskManagerActivity::class.java)
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit Task",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { showTaskDeleteDialog = true }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Delete Task",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Task Mark as Complete Confirmation Dialog
                if (showTaskCompleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showTaskCompleteDialog = false },
                        title = { Text("Mark Task as Complete") },
                        text = { Text("Mark this task as complete?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    taskViewModel.toggleTaskStatus(latestTask.id)
                                    showTaskCompleteDialog = false
                                }
                            ) {
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showTaskCompleteDialog = false }
                            ) {
                                Text("No")
                            }
                        }
                    )
                }

                // Task Delete Confirmation Dialog
                if (showTaskDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showTaskDeleteDialog = false },
                        title = { Text("Delete Task") },
                        text = { Text("Delete this task?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    taskViewModel.deleteTask(latestTask.id)
                                    showTaskDeleteDialog = false
                                }
                            ) {
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showTaskDeleteDialog = false }
                            ) {
                                Text("No")
                            }
                        }
                    )
                }
            }
        } else {
            Text(
                text = "No tasks available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Expense Tracker Section
        Text(
            text = "Expense Tracker",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (latestExpense != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {
                        val intent = Intent(context, ExpenseActivity::class.java)
                        context.startActivity(intent)
                    },
                elevation = CardDefaults.cardElevation(4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = latestExpense.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Amount: $${latestExpense.amount}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "Status: ${if (latestExpense.completionPercentage > 0) "Partially Paid (${latestExpense.completionPercentage}%)" else "To Be Paid"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        IconButton(
                            onClick = { showExpensePaidDialog = true }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_markdone),
                                contentDescription = "Mark as Paid",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                val intent = Intent(context, ExpenseActivity::class.java)
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit Expense",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = { showExpenseDeleteDialog = true }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_delete),
                                contentDescription = "Delete Expense",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Expense Mark as Paid Confirmation Dialog
                if (showExpensePaidDialog) {
                    AlertDialog(
                        onDismissRequest = { showExpensePaidDialog = false },
                        title = { Text("Mark Expense as Paid") },
                        text = { Text("Mark this expense as paid? This will delete it.") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    expenseViewModel.deleteExpense(latestExpense)
                                    showExpensePaidDialog = false
                                }
                            ) {
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showExpensePaidDialog = false }
                            ) {
                                Text("No")
                            }
                        }
                    )
                }

                // Expense Delete Confirmation Dialog
                if (showExpenseDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showExpenseDeleteDialog = false },
                        title = { Text("Delete Expense") },
                        text = { Text("Delete this expense?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    expenseViewModel.deleteExpense(latestExpense)
                                    showExpenseDeleteDialog = false
                                }
                            ) {
                                Text("Yes")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showExpenseDeleteDialog = false }
                            ) {
                                Text("No")
                            }
                        }
                    )
                }
            }
        } else {
            Text(
                text = "No expenses added yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom Navigation
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navigationItems = listOf(
                NavigationItem("Home", R.drawable.ic_home) {},
                NavigationItem("Tasks", R.drawable.ic_tasks) {
                    val intent = Intent(context, TaskManagerActivity::class.java)
                    context.startActivity(intent)
                },
                NavigationItem("Progress", R.drawable.ic_progress) {
                    val intent = Intent(context, ProgressTrackerActivity::class.java)
                    context.startActivity(intent)
                },
                NavigationItem("Pomodoro", R.drawable.ic_pomodoro) {
                    val intent = Intent(context, PomodoroActivity::class.java)
                    context.startActivity(intent)
                },
                NavigationItem("Expense", R.drawable.ic_calendar) {
                    val intent = Intent(context, ExpenseActivity::class.java)
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
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

// Add NavigationItem data class
data class NavigationItem(
    val label: String,
    @DrawableRes val icon: Int,
    val action: () -> Unit
)

// Implement formatDueDate function
fun formatDueDate(dueTime: String): String {
    // Simple placeholder; you can improve this as needed
    return dueTime
}
