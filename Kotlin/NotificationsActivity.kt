package com.example.myacademate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NotificationsScreen(taskViewModel)
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(taskViewModel: TaskViewModel) {
    val taskList = taskViewModel.taskList // Synced with TaskManager
    val currentTime = Date()
    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime)

    // Filter tasks to show all overdue and due-today notifications
    val notifications = taskList.filter { task ->
        !task.isDone && run {
            val dueDateTime = parseDate(task.dueTime)
            val dueDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dueDateTime)
            dueDateTime.before(currentTime) || dueDateStr == todayDateStr
        }
    }.map { task ->
        val dueDateTime = parseDate(task.dueTime)
        val dueDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dueDateTime)
        val message = if (dueDateTime.before(currentTime)) {
            "Overdue: ${task.subjectName} (${task.courseCode}) - Due: ${task.dueTime} (Created: ${task.createdTime})"
        } else {
            "Due Today: ${task.subjectName} (${task.courseCode}) - Due: ${task.dueTime} (Created: ${task.createdTime})"
        }
        Pair(task, message) // Pair task with its notification message
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Notifications", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))

        if (notifications.isNotEmpty()) {
            LazyColumn {
                items(notifications, key = { it.first.id }) { (task, notification) ->
                    NotificationCard(
                        task = task,
                        notification = notification,
                        onMarkAsRead = { taskViewModel.toggleTaskStatus(task.id) },
                        onDelete = { taskViewModel.deleteTask(task.id) }
                    )
                }
            }
        } else {
            Text(
                text = "No new notifications",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun NotificationCard(
    task: DatabaseHelper.Task,
    notification: String,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                notification.startsWith("Overdue") -> Color(0xFFFFCDD2) // Light red
                notification.startsWith("Due Today") -> Color(0xFFFFF9C4) // Light yellow
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notification,
                style = MaterialTheme.typography.bodyLarge,
                color = when {
                    notification.startsWith("Overdue") -> Color(0xFFD32F2F) // Darker red
                    notification.startsWith("Due Today") -> Color(0xFF455A64) // Dark gray-blue
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = onMarkAsRead,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF388E3C), // Green
                        contentColor = Color.White
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Mark as Read")
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F), // Red
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            }
        }
        
    }
}

fun isSameDay(date1: Date, date2: Date): Boolean {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(date1) == sdf.format(date2)
}
