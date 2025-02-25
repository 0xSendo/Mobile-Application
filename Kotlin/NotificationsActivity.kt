package com.example.myacademate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myacademate.ui.theme.MyAcademateTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929) // background: Dark Gray #292929
                ) {
                    NotificationsScreen(taskViewModel, username)
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(taskViewModel: TaskViewModel, username: String) {
    val taskList = taskViewModel.taskList // Synced with TaskManager
    val currentTime = Date()
    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime)

    // Filter and map tasks to notifications with "Overdue" and "Due" tags
    val notifications = taskList.filter { !it.isDone }.mapNotNull { task ->
        val dueDateTime = parseDate(task.dueTime)
        val createdDateTime = parseDate(task.createdTime)
        val dueDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dueDateTime)

        when {
            dueDateTime.before(currentTime) -> // Past due date
                "Overdue: ${task.subjectName} (${task.courseCode}) - Due: ${task.dueTime} (Created: ${task.createdTime})"
            dueDateStr == todayDateStr -> // Due today
                "Due Today: ${task.subjectName} (${task.courseCode}) - Due: ${task.dueTime} (Created: ${task.createdTime})"
            dueDateTime.after(currentTime) -> // Due in the future
                "Due: ${task.subjectName} (${task.courseCode}) - Due: ${task.dueTime} (Created: ${task.createdTime})"
            else -> null // Invalid or completed tasks
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Notifications",
            style = androidx.compose.material3.Typography().titleLarge,
            color = Color(0xFFFFFFFF) // onBackground: White #ffffff
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (notifications.isNotEmpty()) {
            LazyColumn {
                items(notifications) { notification ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                notification.startsWith("Overdue") -> Color(0xFF808080) // Gray for overdue
                                notification.startsWith("Due Today") -> Color(0xFF1B1B1B) // Darker Gray for due today
                                notification.startsWith("Due") -> Color(0xFF404040) // Slightly lighter gray for future due
                                else -> Color(0xFF1B1B1B) // Default
                            }
                        )
                    ) {
                        Text(
                            text = notification,
                            style = androidx.compose.material3.Typography().bodyLarge,
                            modifier = Modifier.padding(16.dp),
                            color = when {
                                notification.startsWith("Overdue") -> Color(0xFFFFA31A) // Orange for overdue
                                notification.startsWith("Due Today") -> Color(0xFFFFFFFF) // White for due today
                                notification.startsWith("Due") -> Color(0xFFB0B0B0) // Light gray for future due
                                else -> Color(0xFFFFFFFF) // Default white
                            }
                        )
                    }
                }
            }
        } else {
            Text(
                text = "No new notifications",
                style = androidx.compose.material3.Typography().bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = Color(0xFFFFFFFF) // onBackground: White #ffffff
            )
        }
    }

    fun parseDate(dateString: String): Date {
        return try {
            SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).parse(dateString) ?: Date(0)
        } catch (e: Exception) {
            Date(0) // Fallback for invalid dates
        }
    }
}


