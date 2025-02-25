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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
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

// Sorting enum
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
                    color = MaterialTheme.colorScheme.background
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

    // Apply custom color scheme
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFFA31A),  // #ffa31a (255,163,26) Orange
            secondary = Color(0xFF808080),  // #808080 (128,128,128) Gray
            background = Color(0xFF292929),  // #292929 (41,41,41) Dark Gray
            surface = Color(0xFF1B1B1B),  // #1b1b1b (27,27,27) Darker Gray
            onPrimary = Color(0xFFFFFFFF),  // #ffffff (255,255,255) White
            onSecondary = Color(0xFFFFFFFF),  // #ffffff (255,255,255) White
            onBackground = Color(0xFFFFFFFF),  // #ffffff (255,255,255) White
            onSurface = Color(0xFFFFFFFF),  // #ffffff (255,255,255) White
            error = Color(0xFFCF6679),  // Red (unchanged)
            onError = Color.Black  // Black (unchanged)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Task Manager Title
            Text(
                text = "Task Manager",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Task Name Input Field
            OutlinedTextField(
                value = taskName,
                onValueChange = { taskName = it },
                label = { Text("Subject Name", color = MaterialTheme.colorScheme.onBackground) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.onBackground,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Course Code Input Field
            OutlinedTextField(
                value = courseCode,
                onValueChange = { courseCode = it },
                label = { Text("Course Code", color = MaterialTheme.colorScheme.onBackground) },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    cursorColor = MaterialTheme.colorScheme.onBackground,
                    focusedLabelColor = MaterialTheme.colorScheme.secondary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Due Time Input Field with Date and Time Picker (12-hour with AM/PM)
            OutlinedTextField(
                value = dueTime,
                onValueChange = { dueTime = it },
                label = { Text("Due Time", color = MaterialTheme.colorScheme.onBackground) },
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
                                            .format(calendar.time) // e.g., "2025-02-24 02:30 PM"
                                    },
                                    calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE),
                                    false // Use 12-hour clock (false = 12-hour, true = 24-hour)
                                ).show()
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                enabled = false, // Disable manual text input
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = MaterialTheme.colorScheme.secondary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    disabledBorderColor = MaterialTheme.colorScheme.onBackground,
                    disabledTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledLabelColor = MaterialTheme.colorScheme.onBackground
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onBackground)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Save or Update Task Button
            Button(
                onClick = {
                    if (taskName.isNotEmpty() && courseCode.isNotEmpty() && dueTime.isNotEmpty()) {
                        if (editingTaskId == null) {
                            // Create a new task
                            val newTask = DatabaseHelper.Task(
                                id = TaskRepository.taskList.size + 1,
                                subjectName = taskName,
                                courseCode = courseCode,
                                dueTime = dueTime,
                                createdTime = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).format(Date())
                            )
                            taskViewModel.addTask(newTask)
                        } else {
                            // Update existing task (preserve original createdTime)
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
                        // Clear inputs after adding/updating task
                        taskName = ""
                        courseCode = ""
                        dueTime = ""
                        taskViewModel.clearEditingTask()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = if (editingTaskId == null) "Save Task" else "Update Task", color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Upcoming Tasks Section
            Text(
                text = "Upcoming Tasks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Single Filter Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showFilterDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Filter (Current: ${currentSorting.name})", color = MaterialTheme.colorScheme.onSecondary)
                }
            }

            // Filter Dialog
            if (showFilterDialog) {
                AlertDialog(
                    onDismissRequest = { showFilterDialog = false },
                    title = { Text("Sort Tasks By", color = MaterialTheme.colorScheme.onBackground) },
                    text = {
                        Column {
                            TextButton(onClick = {
                                currentSorting = Sorting.ClosestDeadline
                                showFilterDialog = false
                            }) { Text("Closest Deadline", color = MaterialTheme.colorScheme.onBackground) }
                            TextButton(onClick = {
                                currentSorting = Sorting.FarthestDeadline
                                showFilterDialog = false
                            }) { Text("Farthest Deadline", color = MaterialTheme.colorScheme.onBackground) }
                            TextButton(onClick = {
                                currentSorting = Sorting.AZ
                                showFilterDialog = false
                            }) { Text("A-Z", color = MaterialTheme.colorScheme.onBackground) }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showFilterDialog = false }) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            // LazyColumn for scrollable task list
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sortedUpcomingTasks) { task ->
                    TaskCard(task, taskViewModel, onEditTask = {
                        taskName = task.subjectName
                        courseCode = task.courseCode
                        dueTime = task.dueTime
                        taskViewModel.startEditingTask(task.id)
                    })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Completed Tasks Section
            Text(
                text = "Completed Tasks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(10.dp))

            // List of Completed Tasks
            if (completedTasks.isNotEmpty()) {
                completedTasks.forEach { task ->
                    TaskCard(task, taskViewModel, onEditTask = {
                        taskName = task.subjectName
                        courseCode = task.courseCode
                        dueTime = task.dueTime
                        taskViewModel.startEditingTask(task.id)
                    })
                }
            } else {
                Text(
                    text = "No completed tasks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.subjectName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Course: ${task.courseCode}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Due: ${task.dueTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mark as done icon with confirmation dialog
                IconButton(
                    onClick = { showCompleteDialog = true }
                ) {
                    Icon(
                        painter = painterResource(id = if (task.isDone) R.drawable.ic_markdone else R.drawable.ic_markdone),
                        contentDescription = "Mark as Done",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Edit icon
                IconButton(onClick = onEditTask) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit Task",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Remove task icon with confirmation dialog
                IconButton(
                    onClick = { showDeleteDialog = true }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Remove Task",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Mark as Complete Confirmation Dialog
            if (showCompleteDialog) {
                AlertDialog(
                    onDismissRequest = { showCompleteDialog = false },
                    title = { Text("Mark Task as Complete", color = MaterialTheme.colorScheme.onBackground) },
                    text = { Text("Mark this task as complete?", color = MaterialTheme.colorScheme.onBackground) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                taskViewModel.toggleTaskStatus(task.id)
                                showCompleteDialog = false
                            }
                        ) {
                            Text("Yes", color = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showCompleteDialog = false }
                        ) {
                            Text("No", color = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }

            // Delete Confirmation Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Task", color = MaterialTheme.colorScheme.onBackground) },
                    text = { Text("Delete this task?", color = MaterialTheme.colorScheme.onBackground) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                taskViewModel.deleteTask(task.id)
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Yes", color = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("No", color = MaterialTheme.colorScheme.onBackground)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

// Helper function to parse date strings with AM/PM
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
        // Create a preview TaskViewModel with sample data
        val previewViewModel = TaskViewModel().apply {
            addTask(DatabaseHelper.Task(1, "Math Assignment", "MATH101", "2025-02-24 02:30 PM"))
            addTask(DatabaseHelper.Task(2, "Science Project", "SCI202", "2025-11-15 09:00 AM", isDone = true))
        }
        TaskManagerScreen(previewViewModel)
    }
}
