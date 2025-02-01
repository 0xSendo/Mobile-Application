package com.example.myacademate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myacademate.ui.theme.MyAcademateTheme

data class Task(
    val id: Int,
    var subjectName: String,
    var courseCode: String,
    var dueTime: String,
    var isDone: Boolean = false
)

class TaskManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskManagerScreen()
                }
            }
        }
    }
}

@Composable
fun TaskManagerScreen() {
    var taskName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var taskList by remember { mutableStateOf(listOf<Task>()) }
    var editingTaskId by remember { mutableStateOf<Int?>(null) }
    var filterType by remember { mutableStateOf("Pending") } // Filter state

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Task Manager", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

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

        Button(
            onClick = {
                if (taskName.isNotEmpty() && courseCode.isNotEmpty() && dueTime.isNotEmpty()) {
                    if (editingTaskId == null) {
                        val newTask = Task(
                            id = taskList.size + 1,
                            subjectName = taskName,
                            courseCode = courseCode,
                            dueTime = dueTime
                        )
                        taskList = taskList + newTask
                    } else {
                        val updatedTask = taskList.find { it.id == editingTaskId }
                        updatedTask?.let {
                            it.subjectName = taskName
                            it.courseCode = courseCode
                            it.dueTime = dueTime
                        }
                    }
                    taskName = ""
                    courseCode = ""
                    dueTime = ""
                    editingTaskId = null
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (editingTaskId == null) "Save Task" else "Update Task")
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Row with "Upcoming Tasks" Text and Filter Button
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Upcoming Tasks", style = MaterialTheme.typography.bodyMedium)

            var expanded by remember { mutableStateOf(false) }
            Box {
                Button(onClick = { expanded = true }) {
                    Text("Filter: $filterType")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(text = { Text("Pending") }, onClick = { filterType = "Pending"; expanded = false })
                    DropdownMenuItem(text = { Text("Finished") }, onClick = { filterType = "Finished"; expanded = false })
                    DropdownMenuItem(text = { Text("Closest Deadline") }, onClick = { filterType = "Closest Deadline"; expanded = false })
                    DropdownMenuItem(text = { Text("Farthest Deadline") }, onClick = { filterType = "Farthest Deadline"; expanded = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val filteredTasks = when (filterType) {
            "Pending" -> taskList.filter { !it.isDone }
            "Finished" -> taskList.filter { it.isDone }
            "Closest Deadline" -> taskList.sortedBy { it.dueTime }
            "Farthest Deadline" -> taskList.sortedByDescending { it.dueTime }
            else -> taskList
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredTasks) { task ->
                TaskCard(task, onUpdateTask = { updatedTask ->
                    taskList = taskList.map { if (it.id == updatedTask.id) updatedTask else it }
                }, onDeleteTask = {
                    taskList = taskList.filter { it.id != task.id }
                })
            }
        }
    }
}

@Composable
fun TaskCard(task: Task, onUpdateTask: (Task) -> Unit, onDeleteTask: () -> Unit) {
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
                IconButton(onClick = {
                    onUpdateTask(task.copy(isDone = !task.isDone))
                }) {
                    Icon(
                        painter = painterResource(id = if (task.isDone) R.drawable.ic_markdone else R.drawable.ic_markdone),
                        contentDescription = "Mark as Done",
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onDeleteTask) {
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

@Preview(showBackground = true)
@Composable
fun TaskManagerScreenPreview() {
    MyAcademateTheme {
        TaskManagerScreen()
    }
}
