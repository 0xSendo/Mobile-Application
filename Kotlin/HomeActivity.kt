package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    
    // Log the fetched user
    Log.d("HomeScreen", "User fetched: $user")

    // Handle null or missing user data safely by providing fallback values
    val firstName = user?.firstName ?: "Unknown"
    val lastName = user?.lastName ?: "User"
    val course = user?.course ?: "N/A"
    val yearLevel = user?.yearLevel ?: "N/A"

    Column(modifier = Modifier.padding(16.dp)) {
        // Profile Section (existing code)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(context, ProfileActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.putExtra("FIRST_NAME", firstName)
                    intent.putExtra("LAST_NAME", lastName)
                    intent.putExtra("COURSE", course)
                    intent.putExtra("YEAR_LEVEL", yearLevel)
                    context.startActivity(intent)
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_profile),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "$firstName $lastName",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "$course - $yearLevel",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.ic_notifications),
                contentDescription = "Notifications",
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search") },
            modifier = Modifier.fillMaxWidth()
        )

        // Replace the hardcoded card with dynamic content
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Task Manager", style = MaterialTheme.typography.titleLarge)


        if (latestTask != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "${latestTask.subjectName} - Due ${formatDueDate(latestTask.dueTime)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = latestTask.dueTime,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            Text(text = "No tasks available", style = MaterialTheme.typography.bodyMedium)
        }


        Spacer(modifier = Modifier.height(20.dp))
        Text(text = "Expense Tracker", style = MaterialTheme.typography.titleLarge)
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                /* Text(text = "Total Spent: $$totalSpent", style = MaterialTheme.typography.bodyLarge)*/
            }
        }

        // Bottom Navigation
        Row(
            modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Pair(R.drawable.ic_home, "Home") to {},
                Pair(R.drawable.ic_tasks, "Tasks") to {
                    val intent = Intent(context, TaskManagerActivity::class.java)
                    context.startActivity(intent)
                },
                Pair(R.drawable.ic_progress, "Progress") to {
                    val intent = Intent(context, ProgressTrackerActivity::class.java)
                    context.startActivity(intent)
                },
                Pair(R.drawable.ic_pomodoro, "Pomodoro") to {
                    val intent = Intent(context, PomodoroActivity::class.java)
                    context.startActivity(intent)
                },
                Pair(R.drawable.ic_calendar, "Calendar") to {}
            ).forEach { (icon, action) ->
                Image(
                    painter = painterResource(id = icon.first),
                    contentDescription = icon.second,
                    modifier = Modifier.weight(1f).size(60.dp).clickable { action() }
                )
            }
        }
    }
}

fun formatDueDate(dueTime: String): String {
    
    return dueTime
}
