// ProgressTrackerActivity.kt

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myacademate.ui.theme.MyAcademateTheme

class ProgressTrackerActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProgressTrackerScreen(taskViewModel)
                }
            }
        }
    }
}

@Composable
fun ProgressTrackerScreen(taskViewModel: TaskViewModel) {
    val taskList = taskViewModel.taskList

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Progress Tracker", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(20.dp))

        // Display progress of tasks
        Text(text = "Progress List", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(10.dp))

        if (taskList.isNotEmpty()) {
            LazyColumn {
                items(taskList) { task ->
                    ProgressCard(task = task, taskViewModel = taskViewModel)
                }
            }
        } else {
            Text("No tasks available", style = MaterialTheme.typography.bodyMedium)
        }
    }
}



@Composable
fun ProgressCard(task: DatabaseHelper.Task, taskViewModel: TaskViewModel) {
    val progressStatus = if (task.isDone) "Completed" else "Pending"
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Task: ${task.subjectName}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Status: $progressStatus", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Course: ${task.courseCode}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Due: ${task.dueTime}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            // Optional: Mark as Done/Undone button
            Button(
                onClick = { taskViewModel.toggleTaskStatus(task.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (task.isDone) "Mark as Pending" else "Mark as Completed")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressTrackerScreenPreview() {
    MyAcademateTheme {
        val previewViewModel = TaskViewModel().apply {
            addTask(DatabaseHelper.Task(1, "Math Assignment", "MATH101", "Tomorrow"))
            addTask(DatabaseHelper.Task(2, "Science Project", "SCI202", "Next Week", isDone = true))
        }
        ProgressTrackerScreen(previewViewModel)
    }
}
