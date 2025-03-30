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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myacademate.ui.theme.MyAcademateTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

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
        super.onBackPressed()
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
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(250.dp),
                drawerContainerColor = Color(0xFF1B1B1B)
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "MyAcademate",
                    color = Color(0xFFFFA31A),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Spacer(Modifier.height(16.dp))

                val navItems = listOf(
                    Pair("Home", R.drawable.ic_home) to {
                        context.startActivity(Intent(context, HomeActivity::class.java).putExtra("USERNAME", username))
                    },
                    Pair("Tasks", R.drawable.ic_tasks) to { /* Current screen */ },
                    Pair("Progress", R.drawable.ic_progress) to {
                        context.startActivity(Intent(context, ProgressTrackerActivity::class.java).putExtra("USERNAME", username))
                    },
                    Pair("Pomodoro", R.drawable.ic_pomodoro) to {
                        context.startActivity(Intent(context, PomodoroActivity::class.java).putExtra("USERNAME", username))
                    },
                    Pair("Expense", R.drawable.ic_calendar) to {
                        context.startActivity(Intent(context, ExpenseActivity::class.java).putExtra("USERNAME", username))
                    }
                )

                navItems.forEach { (pair, action) ->
                    val (label, icon) = pair
                    val isHighlighted = highlightedNavItem == label || label == "Tasks"
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = label,
                                color = if (isHighlighted) Color(0xFFFFA31A) else Color(0xFFFFFFFF)
                            )
                        },
                        selected = isHighlighted,
                        onClick = {
                            action()
                            scope.launch { drawerState.close() }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = label,
                                tint = if (isHighlighted) Color(0xFFFFA31A) else Color(0xFFFFFFFF)
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
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
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color(0xFFFFA31A)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Add info dialog */ }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = Color(0xFFFFA31A)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1B1B))
                )
            },
            containerColor = Color(0xFF292929)
        ) { paddingValues ->
            TaskManagerContent(taskViewModel, username, paddingValues, context)
        }
    }
}

@Composable
fun TaskManagerContent(
    taskViewModel: TaskViewModel,
    username: String,
    paddingValues: PaddingValues,
    context: Context
) {
    var taskName by remember { mutableStateOf("") }
    var courseCode by remember { mutableStateOf("") }
    var dueTime by remember { mutableStateOf("") }
    var currentSorting by remember { mutableStateOf(Sorting.ClosestDeadline) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showCompletedTasksDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var page by remember { mutableStateOf(1) }
    val pageSize = 20 // Number of tasks per page

    val taskList = taskViewModel.taskList
    val upcomingTasks = taskList.filter { !it.isDone }
    val completedTasks = taskList.filter { it.isDone }

    // Filter and sort tasks
    val filteredTasks = upcomingTasks.filter {
        it.subjectName.contains(searchQuery, ignoreCase = true) ||
                it.courseCode.contains(searchQuery, ignoreCase = true)
    }
    val sortedUpcomingTasks = when (currentSorting) {
        Sorting.ClosestDeadline -> filteredTasks.sortedBy { parseDate(it.dueTime) }
        Sorting.FarthestDeadline -> filteredTasks.sortedByDescending { parseDate(it.dueTime) }
        Sorting.AZ -> filteredTasks.sortedBy { it.subjectName }
    }

    // Paginate tasks
    val paginatedTasks = sortedUpcomingTasks.take(page * pageSize)

    // Use Column with weights to ensure button stays at bottom
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Task Input and Upcoming Tasks take available space
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TaskInputSection(
                taskName = taskName,
                courseCode = courseCode,
                dueTime = dueTime,
                onTaskNameChange = { taskName = it },
                onCourseCodeChange = { courseCode = it },
                onDueTimeChange = { dueTime = it },
                taskViewModel = taskViewModel,
                context = context
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchBar(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            UpcomingTasksSection(
                paginatedTasks = paginatedTasks,
                currentSorting = currentSorting,
                onSortClick = { showFilterDialog = true },
                taskViewModel = taskViewModel,
                context = context,
                onEditTaskClicked = { task ->
                    taskName = task.subjectName
                    courseCode = task.courseCode
                    dueTime = task.dueTime
                    taskViewModel.startEditingTask(task.id)
                },
                onLoadMore = {
                    if (paginatedTasks.size < sortedUpcomingTasks.size) {
                        page += 1
                    }
                }
            )
        }

        // View Completed Tasks Button stays at bottom
        OutlinedButton(
            onClick = { showCompletedTasksDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(top = 16.dp),
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
    }

    if (showFilterDialog) {
        FilterDialog(
            currentSorting = currentSorting,
            onSortChange = { currentSorting = it },
            onDismiss = { showFilterDialog = false }
        )
    }

    if (showCompletedTasksDialog) {
        CompletedTasksDialog(
            completedTasks = completedTasks,
            taskViewModel = taskViewModel,
            context = context,
            onDismiss = { showCompletedTasksDialog = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskInputSection(
    taskName: String,
    courseCode: String,
    dueTime: String,
    onTaskNameChange: (String) -> Unit,
    onCourseCodeChange: (String) -> Unit,
    onDueTimeChange: (String) -> Unit,
    taskViewModel: TaskViewModel,
    context: Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            OutlinedTextField(
                value = taskName,
                onValueChange = onTaskNameChange,
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
                onValueChange = onCourseCodeChange,
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
                onValueChange = onDueTimeChange,
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
                                        onDueTimeChange(
                                            SimpleDateFormat(
                                                "MMM dd, yyyy hh:mm a",
                                                Locale.getDefault()
                                            ).format(calendar.time)
                                        )
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
                        val editingTaskId = taskViewModel.editingTaskId
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
                            val existingTask = taskViewModel.taskList.first { it.id == editingTaskId }
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
                        onTaskNameChange("")
                        onCourseCodeChange("")
                        onDueTimeChange("")
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
                    text = if (taskViewModel.editingTaskId == null) "Add Task" else "Update Task",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(searchQuery: String, onSearchQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = { Text("Search Tasks", color = Color(0xFFB0B0B0)) },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFFB0B0B0)
            )
        },
        trailingIcon = {
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color(0xFFB0B0B0)
                    )
                }
            }
        },
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
}

@Composable
fun UpcomingTasksSection(
    paginatedTasks: List<DatabaseHelper.Task>,
    currentSorting: Sorting,
    onSortClick: () -> Unit,
    taskViewModel: TaskViewModel,
    context: Context,
    onEditTaskClicked: (DatabaseHelper.Task) -> Unit,
    onLoadMore: () -> Unit
) {
    val lazyListState = rememberLazyListState()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Upcoming Tasks (${paginatedTasks.size})",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onSortClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_sort),
                    contentDescription = "Sort",
                    tint = Color(0xFFFFA31A)
                )
            }
        }

        if (paginatedTasks.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Takes available space but respects parent constraints
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(paginatedTasks, key = { it.id }) { task ->
                    var isVisible by remember { mutableStateOf(true) }
                    AnimatedVisibility(
                        visible = isVisible,
                        enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(300)) +
                                fadeIn(animationSpec = tween(300)),
                        exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(300)) +
                                fadeOut(animationSpec = tween(300))
                    ) {
                        TaskCard(
                            task = task,
                            taskViewModel = taskViewModel,
                            context = context,
                            onEditTask = { onEditTaskClicked(task) },
                            onComplete = {
                                taskViewModel.toggleTaskStatus(task.id)
                                Toast.makeText(context, "Task marked as complete", Toast.LENGTH_SHORT).show()
                                isVisible = false
                            },
                            onDelete = {
                                taskViewModel.deleteTask(task.id)
                                Toast.makeText(context, "Task deleted", Toast.LENGTH_SHORT).show()
                                isVisible = false
                            }
                        )
                    }
                }
                item {
                    if (paginatedTasks.size < taskViewModel.taskList.filter { !it.isDone }.size) {
                        Button(
                            onClick = onLoadMore,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF404040),
                                contentColor = Color.White
                            )
                        ) {
                            Text("Load More", fontSize = 14.sp)
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tasks),
                        contentDescription = null,
                        tint = Color(0xFFB0B0B0),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "No upcoming tasks yet.\nAdd one above!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFFB0B0B0),
                        textAlign = TextAlign.Center
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
    onEditTask: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    val timeLeft = calculateTimeLeft(task.dueTime)

    // Swipe gesture handling
    val swipeThreshold = 150f // Pixels to trigger swipe action
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .offset(x = offsetX.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (abs(offsetX) > swipeThreshold) {
                            if (offsetX > 0) {
                                onComplete()
                            } else {
                                onDelete()
                            }
                        }
                        offsetX = 0f
                    },
                    onDragCancel = { offsetX = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                        offsetX = offsetX.coerceIn(-swipeThreshold, swipeThreshold)
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = when {
                offsetX > 0 -> Color(0xFF4CAF50).copy(alpha = abs(offsetX) / swipeThreshold)
                offsetX < 0 -> Color(0xFFFF4444).copy(alpha = abs(offsetX) / swipeThreshold)
                else -> Color(0xFF1E1E1E)
            }
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_tasks),
                        contentDescription = null,
                        tint = Color(0xFFFFA31A),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.subjectName,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
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
                        onComplete()
                        showCompleteDialog = false
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
                        onDelete()
                        showDeleteDialog = false
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
fun FilterDialog(
    currentSorting: Sorting,
    onSortChange: (Sorting) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                                onSortChange(sort)
                                onDismiss()
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
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFFFA31A), fontSize = 16.sp)
            }
        },
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun CompletedTasksDialog(
    completedTasks: List<DatabaseHelper.Task>,
    taskViewModel: TaskViewModel,
    context: Context,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                        CompletedTaskItem(task, taskViewModel, context) { onDismiss() }
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
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFFFA31A), fontSize = 16.sp)
            }
        },
        confirmButton = {},
        containerColor = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(16.dp)
    )
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
