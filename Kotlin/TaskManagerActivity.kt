package com.example.myacademate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myacademate.ui.theme.MyAcademateTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class Sorting {
    ClosestDeadline,
    FarthestDeadline,
    AZ
}

class TaskManagerActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929) // background: Dark Gray #292929
                ) {
                    TaskManagerScreen(taskViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagerScreen(taskViewModel: TaskViewModel) {
    val taskList = taskViewModel.taskList
    val editingTaskId = taskViewModel.editingTaskId

    var taskName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var currentSorting by remember { mutableStateOf(Sorting.ClosestDeadline) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showCompletedTasksDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Separate tasks into upcoming and completed
    val upcomingTasks = taskList.filter { !it.isDone }
    val completedTasks = taskList.filter { it.isDone }

    // Sort upcoming tasks based on current sorting
    val sortedUpcomingTasks = when (currentSorting) {
        Sorting.ClosestDeadline -> upcomingTasks.sortedBy { parseDate(it.dueTime) }
        Sorting.FarthestDeadline -> upcomingTasks.sortedByDescending { parseDate(it.dueTime) }
        Sorting.AZ -> upcomingTasks.sortedBy { it.subjectName }
    }

    LazyColumn(modifier = Modifier.padding(16.dp)) {
        // Task Manager Title
        item {
            Text(
                text = "Task Manager",
                style = androidx.compose.material3.Typography().titleLarge,
                color = Color(0xFFFFFFFF) // onBackground: White #ffffff
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Task Input Fields
        item {
            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Subject Name", color = Color(0xFFFFFFFF)) }, // onBackground: White #ffffff
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF808080), // secondary: Gray #808080
                    unfocusedBorderColor = Color(0xFFFFFFFF), // onBackground: White #ffffff
                    cursorColor = Color(0xFFFFFFFF), // onBackground: White #ffffff
                    focusedLabelColor = Color(0xFF808080), // secondary: Gray #808080
                    unfocusedLabelColor = Color(0xFFFFFFFF) // onBackground: White #ffffff
                ),
                textStyle = androidx.compose.material3.Typography().bodyLarge.copy(color = Color(0xFFFFFFFF)) // onBackground: White #ffffff
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = courseCode,
                onValueChange = { courseCode = it },
                label = { Text("Course Code", color = Color(0xFFFFFFFF)) }, // onBackground: White #ffffff
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF808080), // secondary: Gray #808080
                    unfocusedBorderColor = Color(0xFFFFFFFF), // onBackground: White #ffffff
                    cursorColor = Color(0xFFFFFFFF), // onBackground: White #ffffff
                    focusedLabelColor = Color(0xFF808080), // secondary: Gray #808080
                    unfocusedLabelColor = Color(0xFFFFFFFF) // onBackground: White #ffffff
                ),
                textStyle = androidx.compose.material3.Typography().bodyLarge.copy(color = Color(0xFFFFFFFF)) // onBackground: White #ffffff
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            OutlinedTextField(
                value = dueTime,
                onValueChange = { dueTime = it },
                label = { Text("Due Time", color = Color(0xFFFFFFFF)) }, // onBackground: White #ffffff
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val calendar = Calendar.getInstance()
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        calendar.set(year, month, day, hourOfDay, minute)
                                        dueTime = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                                            .format(calendar.time)
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                enabled = false,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF808080), // secondary: Gray #808080
                    unfocusedBorderColor = Color(0xFFFFFFFF), // onBackground: White #ffffff
                    disabledBorderColor = Color(0xFFFFFFFF), // onBackground: White #ffffff
                    disabledTextColor = Color(0xFFFFFFFF), // onBackground: White #ffffff
                    disabledLabelColor = Color(0xFFFFFFFF) // onBackground: White #ffffff
                ),
                textStyle = androidx.compose.material3.Typography().bodyLarge.copy(color = Color(0xFFFFFFFF)) // onBackground: White #ffffff
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Save or Update Task Button
        item {
            Button(
                onClick = {
                    if (taskName.isNotEmpty() && courseCode.isNotEmpty() && dueTime.isNotEmpty()) {
                        if (editingTaskId == null) {
                            val newTask = DatabaseHelper.Task(
                                id = TaskRepository.taskList.size + 1,
                                subjectName = taskName,
                                courseCode = courseCode,
                                dueTime = dueTime,
                                createdTime = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
                            )
                            taskViewModel.addTask(newTask)
                        } else {
                            val existingTask = taskList.first { it.id == editingTaskId }
                            val updatedTask = DatabaseHelper.Task(
                                id = editingTaskId,
                                subjectName = taskName,
                                courseCode = courseCode,
                                dueTime = dueTime,
                                isDone = existingTask.isDone,
                                completionPercentage = existingTask.completionPercentage,
                                createdTime = existingTask.createdTime
                            )
                            taskViewModel.updateTask(updatedTask)
                        }
                        taskName = ""
                        courseCode = ""
                        dueTime = ""
                        taskViewModel.clearEditingTask()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                    contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                )
            ) {
                Text(if (editingTaskId == null) "Save Task" else "Update Task")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Upcoming Tasks Section
        item {
            Text(
                text = "Upcoming Tasks",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFFFFFFFF) // onBackground: White #ffffff
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showFilterDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF808080), // secondary: Gray #808080
                        contentColor = Color(0xFFFFFFFF) // onSecondary: White #ffffff
                    )
                ) {
                    Text("Filter (Current: ${currentSorting.name})")
                }
            }
        }

        // Upcoming Tasks List
        if (sortedUpcomingTasks.isNotEmpty()) {
            items(sortedUpcomingTasks) { task ->
                TaskCard(task, taskViewModel, onEditTask = {
                    taskName = task.subjectName
                    courseCode = task.courseCode
                    dueTime = task.dueTime
                    taskViewModel.startEditingTask(task.id)
                })
            }
        } else {
            item {
                Text(
                    text = "No upcoming tasks",
                    style = androidx.compose.material3.Typography().bodyMedium,
                    color = Color(0xFFFFFFFF) // onBackground: White #ffffff
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Completed Tasks Section (Button)
        item {
            Button(
                onClick = { showCompletedTasksDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF808080), // secondary: Gray #808080
                    contentColor = Color(0xFFFFFFFF) // onSecondary: White #ffffff
                )
            ) {
                Text("Completed Tasks")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Sort Tasks By", color = Color(0xFFFFFFFF)) }, // onBackground: White #ffffff
            text = {
                Column {
                    TextButton(onClick = {
                        currentSorting = Sorting.ClosestDeadline
                        showFilterDialog = false
                    }) { Text("Closest Deadline", color = Color(0xFFFFFFFF)) } // onBackground: White #ffffff
                    TextButton(onClick = {
                        currentSorting = Sorting.FarthestDeadline
                        showFilterDialog = false
                    }) { Text("Farthest Deadline", color = Color(0xFFFFFFFF)) } // onBackground: White #ffffff
                    TextButton(onClick = {
                        currentSorting = Sorting.AZ
                        showFilterDialog = false
                    }) { Text("A-Z", color = Color(0xFFFFFFFF)) } // onBackground: White #ffffff
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Cancel", color = Color(0xFFFFFFFF)) // onBackground: White #ffffff
                }
            },
            containerColor = Color(0xFF1B1B1B) // surface: Darker Gray #1b1b1b
        )
    }

    // Completed Tasks Dialog
    if (showCompletedTasksDialog) {
        AlertDialog(
            onDismissRequest = { showCompletedTasksDialog = false },
            title = { Text("Completed Tasks", color = Color(0xFFFFFFFF)) }, // onBackground: White #ffffff
            text = {
                if (completedTasks.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.height(300.dp)) { // Fixed height for scrollable list
                        items(completedTasks) { task ->
                            CompletedTaskItem(task, taskViewModel) { showCompletedTasksDialog = false }
                        }
                    }
                } else {
                    Text(
                        text = "No completed tasks",
                        style = androidx.compose.material3.Typography().bodyMedium,
                        color = Color(0xFFFFFFFF) // onBackground: White #ffffff
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletedTasksDialog = false }) {
                    Text("Close", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                }
            },
            confirmButton = {},
            containerColor = Color(0xFF1B1B1B) // surface: Darker Gray #1b1b1b
        )
    }
}

@Composable
fun TaskCard(task: DatabaseHelper.Task, taskViewModel: TaskViewModel, onEditTask: () -> Unit) {
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)) // surface: Darker Gray #1b1b1b
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.subjectName,
                style = androidx.compose.material3.Typography().bodyLarge,
                color = Color(0xFFFFFFFF) // onSurface: White #ffffff
            )
            Text(
                text = "Course: ${task.courseCode}",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFFFFFFFF) // onSurface: White #ffffff
            )
            Text(
                text = "Due: ${task.dueTime}",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFFFFFFFF) // onSurface: White #ffffff
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { showCompleteDialog = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_markdone),
                        contentDescription = "Mark as Done",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFFA31A) // primary: Orange #ffa31a
                    )
                }

                IconButton(onClick = onEditTask) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit Task",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFFA31A) // primary: Orange #ffa31a
                    )
                }

                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Remove Task",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFFA31A) // primary: Orange #ffa31a
                    )
                }
            }

            if (showCompleteDialog) {
                AlertDialog(
                    onDismissRequest = { showCompleteDialog = false },
                    title = { Text("Mark Task as Complete", color = Color(0xFFFFFFFF)) }, // onBackground: White #ffffff
                    text = { Text("Mark this task as complete?", color = Color(0xFFFFFFFF)) }, // onBackground: White #ffffff
                    confirmButton = {
                        TextButton(
                            onClick = {
                                taskViewModel.toggleTaskStatus(task.id)
                                showCompleteDialog = false
                            }
                        ) {
                            Text("Yes", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCompleteDialog = false }
                        ) {
                            Text("No", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                        }
                    },
                    containerColor = Color(0xFF1B1B1B) // surface: Darker Gray #1b1b1b
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Task", color = Color(0xFFFFFFFF)) }, // onBackground: White #ffffff
                    text = { Text("Delete this task?", color = Color(0xFFFFFFFF)) }, // onBackground: White #ffffff
                    confirmButton = {
                        TextButton(
                            onClick = {
                                taskViewModel.deleteTask(task.id)
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Yes", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("No", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                        }
                    },
                    containerColor = Color(0xFF1B1B1B) // surface: Darker Gray #1b1b1b
                )
            }
        }
    }
}

@Composable
fun CompletedTaskItem(task: DatabaseHelper.Task, taskViewModel: TaskViewModel, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)) // surface: Darker Gray #1b1b1b
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = task.subjectName,
                style = androidx.compose.material3.Typography().bodyLarge,
                color = Color(0xFFFFFFFF) // onSurface: White #ffffff
            )
            Text(
                text = "Course: ${task.courseCode}",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFFFFFFFF) // onSurface: White #ffffff
            )
            Text(
                text = "Due: ${task.dueTime}",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFFFFFFFF) // onSurface: White #ffffff
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Button(
                    onClick = {
                        taskViewModel.toggleTaskStatus(task.id) // Retrieve task (set isDone to false)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("Retrieve")
                }
                Button(
                    onClick = {
                        taskViewModel.deleteTask(task.id) // Remove task permanently
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF808080), // secondary: Gray #808080
                        contentColor = Color(0xFFFFFFFF) // onSecondary: White #ffffff
                    )
                ) {
                    Text("Remove")
                }
            }
        }
    }
}

fun parseDate(dateString: String): Date {
    return try {
        SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).parse(dateString) ?: Date(0)
    } catch (e: Exception) {
        Date(0) // Handle invalid dates as epoch time
    }
}

@Preview(showBackground = true)
@Composable
fun TaskManagerScreenPreview() {
    MyAcademateTheme {
        val previewViewModel = TaskViewModel().apply {
            addTask(DatabaseHelper.Task(1, "Math Assignment", "MATH101", "2025-02-24 02:30 PM"))
            addTask(DatabaseHelper.Task(2, "Science Project", "SCI202", "2025-11-15 09:00 AM", isDone = true))
            addTask(DatabaseHelper.Task(3, "History Essay", "HIST303", "2025-03-01 10:00 AM"))
            addTask(DatabaseHelper.Task(4, "English Paper", "ENG401", "2025-04-10 03:00 PM", isDone = true))
        }
        TaskManagerScreen(previewViewModel)
    }
}
