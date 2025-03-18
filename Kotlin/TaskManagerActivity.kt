package com.example.myacademate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000L // 2 seconds interval

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929)
                ) {
                    TaskManagerScreen(taskViewModel, username)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
            finishAffinity() // This will close all activities and exit the app
        } else {
            Toast.makeText(this, "Press back again to exit the app", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagerScreen(taskViewModel: TaskViewModel, username: String) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var highlightedNavItem by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchQuery) {
        highlightedNavItem = when {
            searchQuery.isBlank() -> null
            "task manager".contains(searchQuery.lowercase()) || "tasks".contains(searchQuery.lowercase()) -> "Tasks"
            "progress".contains(searchQuery.lowercase()) -> "Progress"
            "pomodoro".contains(searchQuery.lowercase()) -> "Pomodoro"
            "expense".contains(searchQuery.lowercase()) -> "Expense"
            "home".contains(searchQuery.lowercase()) -> "Home"
            else -> null
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(username, highlightedNavItem)
        },
        containerColor = Color(0xFF292929)
    ) { paddingValues ->
        TaskManagerContent(taskViewModel, username, paddingValues)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagerContent(taskViewModel: TaskViewModel, username: String, paddingValues: PaddingValues) {
    val taskList = taskViewModel.taskList
    val editingTaskId = taskViewModel.editingTaskId

    var taskName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var currentSorting by remember { mutableStateOf(Sorting.ClosestDeadline) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showCompletedTasksDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val upcomingTasks = taskList.filter { !it.isDone }
    val completedTasks = taskList.filter { it.isDone }

    val sortedUpcomingTasks = when (currentSorting) {
        Sorting.ClosestDeadline -> upcomingTasks.sortedBy { parseDate(it.dueTime) }
        Sorting.FarthestDeadline -> upcomingTasks.sortedByDescending { parseDate(it.dueTime) }
        Sorting.AZ -> upcomingTasks.sortedBy { it.subjectName }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Task Manager",
                style = androidx.compose.material3.Typography().titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color(0xFFFFFFFF),
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        label = { Text("Subject Name", color = Color(0xFF808080)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFA31A),
                            unfocusedBorderColor = Color(0xFF808080),
                            cursorColor = Color(0xFFFFFFFF),
                            focusedLabelColor = Color(0xFFFFA31A),
                            unfocusedLabelColor = Color(0xFF808080)
                        ),
                        textStyle = androidx.compose.material3.Typography().bodyLarge.copy(color = Color(0xFFFFFFFF))
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Course Code", color = Color(0xFF808080)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFA31A),
                            unfocusedBorderColor = Color(0xFF808080),
                            cursorColor = Color(0xFFFFFFFF),
                            focusedLabelColor = Color(0xFFFFA31A),
                            unfocusedLabelColor = Color(0xFF808080)
                        ),
                        textStyle = androidx.compose.material3.Typography().bodyLarge.copy(color = Color(0xFFFFFFFF))
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Due Time", color = Color(0xFF808080)) },
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
                            focusedBorderColor = Color(0xFFFFA31A),
                            unfocusedBorderColor = Color(0xFF808080),
                            disabledBorderColor = Color(0xFF808080),
                            disabledTextColor = Color(0xFFFFFFFF),
                            disabledLabelColor = Color(0xFF808080)
                        ),
                        textStyle = androidx.compose.material3.Typography().bodyLarge.copy(color = Color(0xFFFFFFFF))
                    )
                    Spacer(modifier = Modifier.height(16.dp))

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA31A),
                            contentColor = Color(0xFFFFFFFF)
                        )
                    ) {
                        Text(
                            text = if (editingTaskId == null) "Save Task" else "Update Task",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Upcoming Tasks",
                style = androidx.compose.material3.Typography().bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = Color(0xFFFFFFFF)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { showFilterDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF808080),
                        contentColor = Color(0xFFFFFFFF)
                    )
                ) {
                    Text("Filter (Current: ${currentSorting.name})", fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Text(
                        text = "No upcoming tasks",
                        style = androidx.compose.material3.Typography().bodyMedium,
                        color = Color(0xFF808080),
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        item {
            Button(
                onClick = { showCompletedTasksDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF808080),
                    contentColor = Color(0xFFFFFFFF)
                )
            ) {
                Text("Completed Tasks", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Sort Tasks By", color = Color(0xFFFFFFFF), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    TextButton(onClick = {
                        currentSorting = Sorting.ClosestDeadline
                        showFilterDialog = false
                    }) { Text("Closest Deadline", color = Color(0xFFFFA31A)) }
                    TextButton(onClick = {
                        currentSorting = Sorting.FarthestDeadline
                        showFilterDialog = false
                    }) { Text("Farthest Deadline", color = Color(0xFFFFA31A)) }
                    TextButton(onClick = {
                        currentSorting = Sorting.AZ
                        showFilterDialog = false
                    }) { Text("A-Z", color = Color(0xFFFFA31A)) }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Cancel", color = Color(0xFFFFA31A))
                }
            },
            containerColor = Color(0xFF1B1B1B),
            shape = RoundedCornerShape(12.dp)
        )
    }

    if (showCompletedTasksDialog) {
        AlertDialog(
            onDismissRequest = { showCompletedTasksDialog = false },
            title = { Text("Completed Tasks", color = Color(0xFFFFFFFF), fontWeight = FontWeight.Bold) },
            text = {
                if (completedTasks.isNotEmpty()) {
                    LazyColumn(modifier = Modifier.height(300.dp)) {
                        items(completedTasks) { task ->
                            CompletedTaskItem(task, taskViewModel) { showCompletedTasksDialog = false }
                        }
                    }
                } else {
                    Text(
                        text = "No completed tasks",
                        style = androidx.compose.material3.Typography().bodyMedium,
                        color = Color(0xFF808080)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletedTasksDialog = false }) {
                    Text("Close", color = Color(0xFFFFA31A))
                }
            },
            confirmButton = {},
            containerColor = Color(0xFF1B1B1B),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun NavigationBar(username: String, highlightedNavItem: String?) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navigationItems = listOf(
                NavigationItem("Home", R.drawable.ic_home) {
                    val intent = Intent(context, HomeActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                },
                NavigationItem("Tasks", R.drawable.ic_tasks) { /* Current screen, no action */ },
                NavigationItem("Progress", R.drawable.ic_progress) {
                    val intent = Intent(context, ProgressTrackerActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                },
                NavigationItem("Pomodoro", R.drawable.ic_pomodoro) {
                    val intent = Intent(context, PomodoroActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                },
                NavigationItem("Expense", R.drawable.ic_calendar) {
                    val intent = Intent(context, ExpenseActivity::class.java)
                    intent.putExtra("USERNAME", username)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                }
            )

            navigationItems.forEach { item ->
                val isHighlighted = highlightedNavItem == item.label
                IconButton(
                    onClick = { item.action() },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (isHighlighted) Color(0xFFFFA31A).copy(alpha = 0.3f) else Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(32.dp),
                        tint = if (isHighlighted) Color(0xFFFFA31A) else Color(0xFFFFFFFF)
                    )
                }
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.subjectName,
                style = androidx.compose.material3.Typography().bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFFFFFFFF)
            )
            Text(
                text = "Course: ${task.courseCode}",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFF808080)
            )
            Text(
                text = "Due: ${task.dueTime}",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFF808080)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                IconButton(onClick = { showCompleteDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_markdone),
                        contentDescription = "Mark as Done",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFFA31A)
                    )
                }
                IconButton(onClick = onEditTask) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_edit),
                        contentDescription = "Edit Task",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFFA31A)
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Remove Task",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFFFFA31A)
                    )
                }
            }
        }

        if (showCompleteDialog) {
            AlertDialog(
                onDismissRequest = { showCompleteDialog = false },
                title = { Text("Mark Task as Complete", color = Color(0xFFFFFFFF), fontWeight = FontWeight.Bold) },
                text = { Text("Mark this task as complete?", color = Color(0xFFFFFFFF)) },
                confirmButton = {
                    TextButton(onClick = {
                        taskViewModel.toggleTaskStatus(task.id)
                        showCompleteDialog = false
                    }) {
                        Text("Yes", color = Color(0xFFFFA31A))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCompleteDialog = false }) {
                        Text("No", color = Color(0xFFFFA31A))
                    }
                },
                containerColor = Color(0xFF1B1B1B),
                shape = RoundedCornerShape(12.dp)
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Task", color = Color(0xFFFFFFFF), fontWeight = FontWeight.Bold) },
                text = { Text("Delete this task?", color = Color(0xFFFFFFFF)) },
                confirmButton = {
                    TextButton(onClick = {
                        taskViewModel.deleteTask(task.id)
                        showDeleteDialog = false
                    }) {
                        Text("Yes", color = Color(0xFFFFA31A))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("No", color = Color(0xFFFFA31A))
                    }
                },
                containerColor = Color(0xFF1B1B1B),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun CompletedTaskItem(task: DatabaseHelper.Task, taskViewModel: TaskViewModel, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = task.subjectName,
                style = androidx.compose.material3.Typography().bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFFFFFFFF)
            )
            Text(
                text = "Course: ${task.courseCode}",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFF808080)
            )
            Text(
                text = "Due: ${task.dueTime}",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFF808080)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Button(
                    onClick = {
                        taskViewModel.toggleTaskStatus(task.id)
                        onDismiss()
                    },
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A),
                        contentColor = Color(0xFFFFFFFF)
                    )
                ) {
                    Text("Retrieve", fontSize = 14.sp)
                }
                Button(
                    onClick = {
                        taskViewModel.deleteTask(task.id)
                        onDismiss()
                    },
                    modifier = Modifier.height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF808080),
                        contentColor = Color(0xFFFFFFFF)
                    )
                ) {
                    Text("Remove", fontSize = 14.sp)
                }
            }
        }
    }
}

fun parseDate(dateString: String): Date {
    return try {
        SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault()).parse(dateString) ?: Date(0)
    } catch (e: Exception) {
        Date(0)
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
        TaskManagerScreen(previewViewModel, "previewUser")
    }
}
