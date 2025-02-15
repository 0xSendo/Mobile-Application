package com.example.myacademate

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myacademate.ui.theme.MyAcademateTheme

class HomeActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels() // Initialize taskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("USERNAME") ?: ""
        Log.d("HomeActivity", "Username received: $username")

        val dbHelper = DatabaseHelper(applicationContext)

        // Fetch user data for debugging
        val user = dbHelper.getUserData(username)
        Log.d("HomeActivity", "User data fetched in HomeActivity: $user")

        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass taskViewModel to HomeScreen
                    HomeScreen(username, dbHelper, taskViewModel)
                }
            }
        }
    }
}

@Composable
fun HomeScreen(username: String, dbHelper: DatabaseHelper, taskViewModel: TaskViewModel) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val taskList = taskViewModel.taskList
    val user = dbHelper.getUserData(username)
    val latestTask = taskList.lastOrNull()

    // Handle null or missing user data safely by providing fallback values
    val firstName = user?.firstName ?: "Unknown"
    val lastName = user?.lastName ?: "User"
    val course = user?.course ?: "N/A"
    val yearLevel = user?.yearLevel ?: "N/A"

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
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "$firstName $lastName",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "$course - Year $yearLevel",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                // Handle notifications click
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notifications),
                    contentDescription = "Notifications",
                    modifier = Modifier.size(28.dp)
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
                    contentDescription = "Search Icon"
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Task Manager Section
        Text(text = "Task Manager", style = MaterialTheme.typography.headlineSmall)
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
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = latestTask.subjectName,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Due ${formatDueDate(latestTask.dueTime)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else {
            Text(text = "No tasks available", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Expense Tracker Section
        Text(text = "Expense Tracker", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable {
                    // Navigate to Expense Tracker
                },
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Track your expenses",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
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
                NavigationItem("Calendar", R.drawable.ic_calendar) {
                    // Navigate to Calendar
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
                        modifier = Modifier.size(32.dp)
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

