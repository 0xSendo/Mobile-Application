package com.example.myacademate

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    private val BACK_PRESS_INTERVAL = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929)
                ) {
                    TaskManagerScreen(taskViewModel, username, this)
                }
            }
        }
    }

    override fun onBackPressed() {
        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
            finishAffinity()
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagerScreen(taskViewModel: TaskViewModel, username: String, context: Context) {
    var highlightedNavItem by remember { mutableStateOf<String?>(null) }

    Scaffold(
        bottomBar = { NavigationBar(username, highlightedNavItem, context) },
        containerColor = Color(0xFF292929),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Task Manager",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1B1B)),
                actions = {
                    IconButton(onClick = { /* Could add info dialog here */ }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xFFFFA31A)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        TaskManagerContent(taskViewModel, username, paddingValues, context)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagerContent(
    taskViewModel: TaskViewModel,
    username: String,
    paddingValues: PaddingValues,
    context: Context
) {
    val taskList = taskViewModel.taskList
    val editingTaskId = taskViewModel.editingTaskId

    var taskName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var currentSorting by remember { mutableStateOf(Sorting.ClosestDeadline) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showCompletedTasksDialog by remember { mutableStateOf(false) }

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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    OutlinedTextField(
                        value = taskName,
                        onValueChange = { taskName = it },
                        label = { Text("Subject Name", color = Color(0xFFB0B0B0)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFA31A),
                            unfocusedBorderColor = Color(0xFF404040),
                            cursorColor = Color.White,
                            focusedLabelColor = Color(0xFFFFA31A),
                            unfocusedLabelColor = Color(0xFFB0B0B0)
                        ),
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Course Code", color = Color(0xFFB0B0B0)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFA31A),
                            unfocusedBorderColor = Color(0xFF404040),
                            cursorColor = Color.White,
                            focusedLabelColor = Color(0xFFFFA31A),
                            unfocusedLabelColor = Color(0xFFB0B0B0)
                        ),
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = dueTime,
                        onValueChange = { dueTime = it },
                        label = { Text("Due Time", color = Color(0xFFB0B0B0)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val calendar = Calendar.getInstance()
                                DatePickerDialog(
                                    context,
                                    { _, year, month, day ->
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                calendar.set(year, month, day, hour, minute)
                                                dueTime = SimpleDateFormat(
                                                    "MMM dd, yyyy hh:mm a",
                                                    Locale.getDefault()
                                                ).format(calendar.time)
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
                            disabledBorderColor = Color(0xFF404040),
                            disabledTextColor = Color.White,
                            disabledLabelColor = Color(0xFFB0B0B0)
                        ),
                        textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_calendar),
                                contentDescription = "Calendar",
                                tint = Color(0xFFFFA31A)
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (taskName.isNotEmpty() && courseCode.isNotEmpty() && dueTime.isNotEmpty()) {
                                if (editingTaskId == null) {
                                    val newTask = DatabaseHelper.Task(
                                        id = TaskRepository.taskList.size + 1,
                                        subjectName = taskName,
                                        courseCode = courseCode,
                                        dueTime = dueTime,
                                        createdTime = SimpleDateFormat(
                                            "MMM dd, yyyy hh:mm a",
                                            Locale.getDefault()
                                        ).format(Date())
                                    )
                                    taskViewModel.addTask(newTask)
                                    Toast.makeText(context, "Task added successfully", Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(context, "Task updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                taskName = ""
                                courseCode = ""
                                dueTime = ""
                                taskViewModel.clearEditingTask()
                            } else {
                                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFA31A),
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(4.dp)
                    ) {
                        Text(
                            text = if (editingTaskId == null) "Add Task" else "Update Task",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Tasks (${sortedUpcomingTasks.size})",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showFilterDialog = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_sort),
                        contentDescription = "Sort",
                        tint = Color(0xFFFFA31A)
                    )
                }
            }
        }

        if (sortedUpcomingTasks.isNotEmpty()) {
            items(sortedUpcomingTasks, key = { it.id }) { task ->
                TaskCard(task, taskViewModel, context) {
                    taskName = task.subjectName
                    courseCode = task.courseCode
                    dueTime = task.dueTime
                    taskViewModel.startEditingTask(task.id)
                }
            }
        } else {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
                ) {
                    Text(
                        text = "No upcoming tasks yet.\nAdd one above!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFB0B0B0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        }

        item {
            OutlinedButton(
                onClick = { showCompletedTasksDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFFA31A)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFA31A))
            ) {
                Text(
                    "View Completed Tasks (${completedTasks.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = {
                Text(
                    "Sort Tasks",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Sorting.values().forEach { sort ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentSorting = sort
                                    showFilterDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentSorting == sort,
                                onClick = null,
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color(0xFFFFA31A),
                                    unselectedColor = Color(0xFFB0B0B0)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = sort.name.replace("([A-Z])".toRegex(), " $1").trim(),
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Divider(color = Color(0xFF404040), thickness = 1.dp)
                    Text(
                        text = "Current sorting: ${currentSorting.name.replace("([A-Z])".toRegex(), " $1").trim()}",
                        color = Color(0xFFB0B0B0),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Cancel", color = Color(0xFFFFA31A), fontSize = 16.sp)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        )
    }

    if (showCompletedTasksDialog) {
        AlertDialog(
            onDismissRequest = { showCompletedTasksDialog = false },
            title = {
                Text(
                    "Completed Tasks (${completedTasks.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                if (completedTasks.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(completedTasks, key = { it.id }) { task ->
                            CompletedTaskItem(task, taskViewModel, context) { showCompletedTasksDialog = false }
                        }
                    }
                } else {
                    Text(
                        text = "No completed tasks yet.\nKeep working!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFB0B0B0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompletedTasksDialog = false }) {
                    Text("Close", color = Color(0xFFFFA31A), fontSize = 16.sp)
                }
            },
            confirmButton = {},
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun NavigationBar(username: String, highlightedNavItem: String?, context: Context) {
    Surface(
        color = Color(0xFF1B1B1B),
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(80.dp),
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
                // Remove highlight for "Tasks" button in TaskManagerActivity
                val isHighlighted = highlightedNavItem == item.label && item.label != "Tasks"
                IconButton(
                    onClick = { item.action() },
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            if (isHighlighted) Color(0xFFFFA31A).copy(alpha = 0.2f) else Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(28.dp),
                        tint = if (isHighlighted) Color(0xFFFFA31A) else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCard(
    task: DatabaseHelper.Task,
    taskViewModel: TaskViewModel,
    context: Context,
    onEditTask: () -> Unit
) {
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val timeLeft = calculateTimeLeft(task.dueTime)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Course: ${task.courseCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0B0B0)
                )
                Text(
                    text = "Due: ${task.dueTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0B0B0)
                )
                Text(
                    text = timeLeft,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (timeLeft.startsWith("-")) Color(0xFFFF4444) else Color(0xFF4CAF50),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = { showCompleteDialog = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_markdone),
                        contentDescription = "Complete",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onEditTask) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Edit",
                        tint = Color(0xFFFFA31A),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Delete",
                        tint = Color(0xFFFF4444),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = {
                Text(
                    "Complete Task",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        "Mark '${task.subjectName}' as complete?",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        "Due: ${task.dueTime}",
                        color = Color(0xFFB0B0B0),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        taskViewModel.toggleTaskStatus(task.id)
                        showCompleteDialog = false
                        Toast.makeText(context, "Task marked as complete", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Complete", fontSize = 14.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancel", color = Color(0xFFFFA31A), fontSize = 14.sp)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Delete Task",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Column {
                    Text(
                        "Delete '${task.subjectName}'?",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Text(
                        "This action cannot be undone.",
                        color = Color(0xFFFF4444),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        taskViewModel.deleteTask(task.id)
                        showDeleteDialog = false
                        Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF4444),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", fontSize = 14.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = Color(0xFFFFA31A), fontSize = 14.sp)
                }
            },
            containerColor = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun CompletedTaskItem(
    task: DatabaseHelper.Task,
    taskViewModel: TaskViewModel,
    context: Context,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Course: ${task.courseCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0B0B0)
                )
                Text(
                    text = "Completed: ${task.dueTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFB0B0B0)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = {
                    taskViewModel.toggleTaskStatus(task.id)
                    onDismiss()
                    Toast.makeText(context, "Task retrieved", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_undo),
                        contentDescription = "Retrieve",
                        tint = Color(0xFFFFA31A),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = {
                    taskViewModel.deleteTask(task.id)
                    onDismiss()
                    Toast.makeText(context, "Task removed", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Delete",
                        tint = Color(0xFFFF4444),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

fun parseDate(dateString: String): Date {
    return try {
        SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).parse(dateString) ?: Date(0)
    } catch (e: Exception) {
        Date(0)
    }
}

fun calculateTimeLeft(dueTime: String): String {
    val dueDate = parseDate(dueTime)
    val currentTime = Date()
    val diff = dueDate.time - currentTime.time
    val days = diff / (1000 * 60 * 60 * 24)
    val hours = (diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)

    return when {
        diff < 0 -> "-${-days}d ${-hours}h overdue"
        days > 0 -> "${days}d ${hours}h left"
        hours > 0 -> "${hours}h left"
        else -> "Due soon"
    }
}

@Preview(showBackground = true)
@Composable
fun TaskManagerScreenPreview() {
    MyAcademateTheme {
        val previewViewModel = TaskViewModel().apply {
            addTask(DatabaseHelper.Task(1, "Math Assignment", "MATH101", "Feb 24, 2025 02:30 PM"))
            addTask(DatabaseHelper.Task(2, "Science Project", "SCI202", "Nov 15, 2025 09:00 AM", isDone = true))
            addTask(DatabaseHelper.Task(3, "History Essay", "HIST303", "Mar 01, 2025 10:00 AM"))
            addTask(DatabaseHelper.Task(4, "English Paper", "ENG401", "Apr 10, 2025 03:00 PM", isDone = true))
        }
        // For preview, we'll use a dummy context
    }
}
