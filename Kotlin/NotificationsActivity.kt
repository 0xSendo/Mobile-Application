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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
                    color = Color(0xFF292929) // Dark Gray
                ) {
                    NotificationsScreen(taskViewModel, username)
                }
            }
        }
    }
}

@Composable
fun NotificationsScreen(taskViewModel: TaskViewModel, username: String) {
    val taskList = taskViewModel.taskList
    val currentTime = Date()
    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(currentTime)

    // Filter and map tasks to notifications
    val notifications = taskList.filter { !it.isDone }.mapNotNull { task ->
        val dueDateTime = parseDate(task.dueTime)
        val dueDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dueDateTime)
        when {
            dueDateTime.before(currentTime) -> NotificationItem(
                type = "Overdue",
                message = "${task.subjectName} (${task.courseCode})",
                dueTime = "Due: ${task.dueTime}",
                createdTime = "Created: ${task.createdTime}"
            )
            dueDateStr == todayDateStr -> NotificationItem(
                type = "Due Today",
                message = "${task.subjectName} (${task.courseCode})",
                dueTime = "Due: ${task.dueTime}",
                createdTime = "Created: ${task.createdTime}"
            )
            dueDateTime.after(currentTime) -> NotificationItem(
                type = "Due",
                message = "${task.subjectName} (${task.courseCode})",
                dueTime = "Due: ${task.dueTime}",
                createdTime = "Created: ${task.createdTime}"
            )
            else -> null
        }
    }

    // State for managing notifications
    var notificationList by remember { mutableStateOf(notifications) }

    Column(modifier = Modifier.padding(16.dp)) {
        // Top bar with title and actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notifications (${notificationList.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFFFFF)
            )
            if (notificationList.isNotEmpty()) {
                IconButton(onClick = { notificationList = emptyList() }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear All",
                        tint = Color(0xFFFFA31A)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (notificationList.isNotEmpty()) {
            LazyColumn {
                items(notificationList) { notification ->
                    NotificationCard(
                        notification = notification,
                        onDismiss = { dismissed ->
                            notificationList = notificationList.filter { it != dismissed }
                        }
                    )
                }
            }
        } else {
            Text(
                text = "No new notifications",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
                color = Color(0xFFFFFFFF)
            )
        }
    }
}

data class NotificationItem(
    val type: String,
    val message: String,
    val dueTime: String,
    val createdTime: String
)

@Composable
fun NotificationCard(notification: NotificationItem, onDismiss: (NotificationItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (notification.type) {
                "Overdue" -> Color(0xFF4A2C2A) // Dark Red for overdue
                "Due Today" -> Color(0xFF2F2F2F) // Darker Gray for due today
                else -> Color(0xFF404040) // Slightly lighter gray for future due
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon based on notification type
            Icon(
                imageVector = if (notification.type == "Overdue") Icons.Default.Warning else Icons.Default.Notifications,
                contentDescription = notification.type,
                tint = when (notification.type) {
                    "Overdue" -> Color(0xFFFFA31A) // Orange for overdue
                    "Due Today" -> Color(0xFFFFFFFF) // White for due today
                    else -> Color(0xFFB0B0B0) // Light gray for future due
                },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Notification content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.type,
                    style = MaterialTheme.typography.labelSmall,
                    color = when (notification.type) {
                        "Overdue" -> Color(0xFFFFA31A)
                        "Due Today" -> Color(0xFFFFFFFF)
                        else -> Color(0xFFB0B0B0)
                    }
                )
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF)
                )
                Text(
                    text = notification.dueTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0B0B0)
                )
                Text(
                    text = notification.createdTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF808080)
                )
            }

            // Dismiss button
            TextButton(onClick = { onDismiss(notification) }) {
                Text(
                    text = "Dismiss",
                    color = Color(0xFFFFA31A),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
