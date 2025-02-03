package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import com.example.myacademate.ui.theme.MyAcademateTheme

class TaskManagerActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskManagerScreen(taskViewModel)
                }
            }
        }
    }
}

@Composable
fun TaskManagerScreen(taskViewModel: TaskViewModel) {
    var taskName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("Pending") }

    // Handle the UI for editing or adding tasks
    val editingTaskId = taskViewModel.editingTaskId
    val editingTask = taskViewModel.taskList.find { it.id == editingTaskId }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Task Manager", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        // Input Fields for Task Information (Subject Name, Course Code, Due Time)
        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Subject Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = courseCode,
            onValueChange = { courseCode = it },
            label = { Text("Course Code") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = dueTime,
            onValueChange = { dueTime = it },
            label = { Text("Due Time") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Save or Update Task Button
        Button(onClick = {
            if (taskName.isNotEmpty() && courseCode.isNotEmpty() && dueTime.isNotEmpty()) {
                val newTask = Task(
                    id = taskViewModel.taskList.size + 1,
                    subjectName = taskName,
                    courseCode = courseCode,
                    dueTime = dueTime
                )
                if (editingTask == null) {
                    taskViewModel.addTask(newTask)
                } else {
                    taskViewModel.updateTask(newTask)
                }
                taskName = ""
                courseCode = ""
                dueTime = ""
                taskViewModel.clearEditingTask()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text(text = if (editingTask == null) "Save Task" else "Update Task")
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Filter Tasks
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Filter Tasks", style = MaterialTheme.typography.bodyMedium)
            var expanded by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expanded = true }) {
                    Text("Filter: $filterType")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Pending") }, onClick = {
                        filterType = "Pending"
                        expanded = false
                    })
                    DropdownMenuItem(text = { Text("Finished") }, onClick = {
                        filterType = "Finished"
                        expanded = false
                    })
                    DropdownMenuItem(text = { Text("Closest Deadline") }, onClick = {
                        filterType = "Closest Deadline"
                        expanded = false
                    })
                    DropdownMenuItem(text = { Text("Farthest Deadline") }, onClick = {
                        filterType = "Farthest Deadline"
                        expanded = false
                    })
                    DropdownMenuItem(text = { Text("A-Z") }, onClick = {
                        filterType = "A-Z"
                        expanded = false
                    })
                }
            }
        }

        // Apply filtering logic
        val filteredTasks = when (filterType) {
            "Pending" -> taskViewModel.taskList.filter { !it.isDone }
            "Finished" -> taskViewModel.taskList.filter { it.isDone }
            "Closest Deadline" -> taskViewModel.taskList.sortedBy { it.dueTime }
            "Farthest Deadline" -> taskViewModel.taskList.sortedByDescending { it.dueTime }
            "A-Z" -> taskViewModel.taskList.sortedBy { it.subjectName }
            else -> taskViewModel.taskList
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task List Section
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredTasks) { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = task.subjectName, style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Course: ${task.courseCode}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Due: ${task.dueTime}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Mark as done icon
                            IconButton(onClick = {
                                taskViewModel.toggleTaskStatus(task.id)
                            }) {
                                Icon(
                                    painter = painterResource(id = if (task.isDone) R.drawable.ic_markdone else R.drawable.ic_markdone),
                                    contentDescription = "Mark as Done", modifier = Modifier.size(24.dp)
                                )
                            }
                            // Edit icon
                            IconButton(onClick = {
                                taskViewModel.startEditingTask(task.id)
                                taskName = task.subjectName
                                courseCode = task.courseCode
                                dueTime = task.dueTime
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_edit),
                                    contentDescription = "Edit Task",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            // Remove task icon
                            IconButton(onClick = {
                                taskViewModel.deleteTask(task.id)
                            }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_delete),
                                    contentDescription = "Remove Task",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        val context = LocalContext.current

        val menuItems = listOf(
            Pair(R.drawable.ic_home, "Home") to {
                val intent = Intent(context, HomeActivity::class.java)
                context.startActivity(intent)
            },
            Pair(R.drawable.ic_tasks, "Tasks") to {
                // No action needed as you are already in Task Manager
            },
            Pair(R.drawable.ic_progress, "Progress") to {
                val intent = Intent(context, ProgressTrackerActivity::class.java)
                context.startActivity(intent)
            },
            Pair(R.drawable.ic_pomodoro, "Pomodoro") to {
                // Pomodoro button action (optional)
            },
            Pair(R.drawable.ic_calendar, "Calendar") to {
                // Calendar button action (optional)
            }
        )

        // Bottom Navigation Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)  // Increased height for better visibility
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            menuItems.forEach { (icon, action) ->
                Image(
                    painter = painterResource(id = icon.first),
                    contentDescription = icon.second,
                    modifier = Modifier
                        .weight(1f)
                        .size(80.dp)  // Increased size for better visibility
                        .clickable { action() }
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TaskManagerScreenPreview() {
    MyAcademateTheme {
        TaskManagerScreen(taskViewModel = TaskViewModel(SavedStateHandle()))
    }
}
