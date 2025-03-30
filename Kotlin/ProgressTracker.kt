package com.example.myacademate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myacademate.ui.theme.MyAcademateTheme
import kotlinx.coroutines.launch

class ProgressTrackerActivity : ComponentActivity() {
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
                    ProgressTrackerScreen(taskViewModel, username, this)
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
            finishAffinity() // Close all activities and exit app
        } else {
            Toast.makeText(this, "Press back again to exit the app", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressTrackerScreen(taskViewModel: TaskViewModel, username: String, context: Context) {
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
                    Pair("Tasks", R.drawable.ic_tasks) to {
                        context.startActivity(Intent(context, TaskManagerActivity::class.java).putExtra("USERNAME", username))
                    },
                    Pair("Progress", R.drawable.ic_progress) to { /* Current screen */ },
                    Pair("Pomodoro", R.drawable.ic_pomodoro) to {
                        context.startActivity(Intent(context, PomodoroActivity::class.java).putExtra("USERNAME", username))
                    },
                    Pair("Expense", R.drawable.ic_calendar) to {
                        context.startActivity(Intent(context, ExpenseActivity::class.java).putExtra("USERNAME", username))
                    }
                )

                navItems.forEach { (pair, action) ->
                    val (label, icon) = pair
                    val isHighlighted = highlightedNavItem == label || label == "Progress"
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
                            "Progress Tracker",
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
            ProgressTrackerContent(taskViewModel, username, paddingValues)
        }
    }
}

@Composable
fun ProgressTrackerContent(taskViewModel: TaskViewModel, username: String, paddingValues: PaddingValues) {
    val taskList: List<DatabaseHelper.Task> = taskViewModel.taskList
    var filterOption by remember { mutableStateOf(FilterOption.ALPHABETICAL) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Progress Tracker",
                style = androidx.compose.material3.Typography().titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color(0xFFFFFFFF),
                modifier = Modifier.padding(top = 16.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Filter Tasks",
                        style = androidx.compose.material3.Typography().bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color(0xFFFFFFFF)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterSection(
                        filterOption = filterOption,
                        onFilterSelected = { filterOption = it }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Text(
                text = "Progress List",
                style = androidx.compose.material3.Typography().bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = Color(0xFFFFFFFF)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        val filteredTasks = when (filterOption) {
            FilterOption.ALPHABETICAL -> taskList.sortedBy { it.subjectName }
            FilterOption.FARTHEST_DEADLINE -> taskList.sortedByDescending { it.dueTime }
            FilterOption.CLOSEST_DEADLINE -> taskList.sortedBy { it.dueTime }
        }

        if (filteredTasks.isNotEmpty()) {
            items(filteredTasks) { task ->
                ProgressCard(task = task, taskViewModel = taskViewModel)
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
                        text = "No tasks available",
                        style = androidx.compose.material3.Typography().bodyMedium,
                        color = Color(0xFF808080),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSection(
    filterOption: FilterOption,
    onFilterSelected: (FilterOption) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = filterOption == FilterOption.ALPHABETICAL,
            onClick = { onFilterSelected(FilterOption.ALPHABETICAL) },
            label = { Text("A-Z", color = if (filterOption == FilterOption.ALPHABETICAL) Color(0xFFFFA31A) else Color(0xFFFFFFFF)) },
            modifier = Modifier.weight(1f),
            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                containerColor = Color(0xFF808080),
                selectedContainerColor = Color(0xFF1B1B1B)
            )
        )
        FilterChip(
            selected = filterOption == FilterOption.FARTHEST_DEADLINE,
            onClick = { onFilterSelected(FilterOption.FARTHEST_DEADLINE) },
            label = { Text("Farthest", color = if (filterOption == FilterOption.FARTHEST_DEADLINE) Color(0xFFFFA31A) else Color(0xFFFFFFFF)) },
            modifier = Modifier.weight(1f),
            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                containerColor = Color(0xFF808080),
                selectedContainerColor = Color(0xFF1B1B1B)
            )
        )
        FilterChip(
            selected = filterOption == FilterOption.CLOSEST_DEADLINE,
            onClick = { onFilterSelected(FilterOption.CLOSEST_DEADLINE) },
            label = { Text("Closest", color = if (filterOption == FilterOption.CLOSEST_DEADLINE) Color(0xFFFFA31A) else Color(0xFFFFFFFF)) },
            modifier = Modifier.weight(1f),
            colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                containerColor = Color(0xFF808080),
                selectedContainerColor = Color(0xFF1B1B1B)
            )
        )
    }
}

enum class FilterOption {
    ALPHABETICAL,
    FARTHEST_DEADLINE,
    CLOSEST_DEADLINE
}

@Composable
fun ProgressCard(task: DatabaseHelper.Task, taskViewModel: TaskViewModel) {
    val progressStatus = when (task.completionPercentage) {
        in 0..24 -> "Not Started"
        in 25..49 -> "Started"
        in 50..74 -> "In Progress"
        in 75..99 -> "Almost Done"
        else -> "Completed"
    }

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
                text = "Task: ${task.subjectName}",
                style = androidx.compose.material3.Typography().bodyLarge.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFFFFFFFF)
            )
            Text(
                text = "Status: $progressStatus",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFF808080)
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

            LinearProgressIndicator(
                progress = task.completionPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFFFA31A),
                trackColor = Color(0xFF808080)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { taskViewModel.updateTaskCompletion(task.id, 25) },
                    enabled = task.completionPercentage < 25,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A),
                        contentColor = Color(0xFFFFFFFF),
                        disabledContainerColor = Color(0xFF808080).copy(alpha = 0.3f)
                    )
                ) {
                    Text("25%", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { taskViewModel.updateTaskCompletion(task.id, 50) },
                    enabled = task.completionPercentage < 50,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A),
                        contentColor = Color(0xFFFFFFFF),
                        disabledContainerColor = Color(0xFF808080).copy(alpha = 0.3f)
                    )
                ) {
                    Text("50%", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { taskViewModel.updateTaskCompletion(task.id, 75) },
                    enabled = task.completionPercentage < 75,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A),
                        contentColor = Color(0xFFFFFFFF),
                        disabledContainerColor = Color(0xFF808080).copy(alpha = 0.3f)
                    )
                ) {
                    Text("75%", fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { taskViewModel.updateTaskCompletion(task.id, 100) },
                    enabled = task.completionPercentage < 100,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A),
                        contentColor = Color(0xFFFFFFFF),
                        disabledContainerColor = Color(0xFF808080).copy(alpha = 0.3f)
                    )
                ) {
                    Text("100%", fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (task.completionPercentage == 100 && !task.isDone) {
                Button(
                    onClick = { taskViewModel.toggleTaskStatus(task.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A),
                        contentColor = Color(0xFFFFFFFF)
                    )
                ) {
                    Text("Mark as Done", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (task.isDone) {
                Text(
                    text = "Task Completed!",
                    style = androidx.compose.material3.Typography().bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFFA31A),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun ProgressTrackerScreenPreview() {
    MyAcademateTheme {
        val previewViewModel = TaskViewModel().apply {
            addTask(DatabaseHelper.Task(1, "Math Assignment", "MATH101", "2023-12-01"))
            addTask(DatabaseHelper.Task(2, "Science Project", "SCI202", "2023-11-15", completionPercentage = 50))
            addTask(DatabaseHelper.Task(3, "History Essay", "HIST303", "2023-10-30", completionPercentage = 100, isDone = true))
        }
    }
}
