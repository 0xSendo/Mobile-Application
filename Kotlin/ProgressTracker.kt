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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.myacademate.ui.theme.MyAcademateTheme

class ProgressTrackerActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("username") ?: "default_user"
        taskViewModel.loadTasks(username)

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
fun ProgressTrackerScreen(viewModel: TaskViewModel) {
    val taskList = viewModel.taskList // Get the tasks from ViewModel

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Progress Tracker", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Task Progress", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(10.dp))

        if (taskList.isNotEmpty()) {
            taskList.forEach { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "${task.subjectName} (${task.courseCode})", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Due: ${task.dueTime}", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (task.isDone) "Status: Completed ✅" else "Status: In Progress ⏳",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            Text("No tasks available", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

