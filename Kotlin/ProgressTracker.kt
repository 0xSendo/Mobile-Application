package com.example.myacademate

// Added imports for UI enhancements and fragment-like navigation
import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

class ProgressTrackerActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929)
                ) {
                    ProgressTrackerScreen(taskViewModel, username)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressTrackerScreen(taskViewModel: TaskViewModel, username: String) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf("Progress") } // State to track current screen

    Scaffold(
        bottomBar = {
            NavigationBar(username, currentScreen) { screen ->
                when (screen) {
                    "Home", "Tasks", "Pomodoro", "Expense" -> {
                        val intent = when (screen) {
                            "Home" -> Intent(context, HomeActivity::class.java)
                            "Tasks" -> Intent(context, TaskManagerActivity::class.java)
                            "Pomodoro" -> Intent(context, PomodoroActivity::class.java)
                            "Expense" -> Intent(context, ExpenseActivity::class.java)
                            else -> null
                        }
                        intent?.putExtra("USERNAME", username)
                        intent?.let { context.startActivity(it) }
                    }
                    "Progress" -> currentScreen = "Progress" // Stay on current screen
                }
            }
        },
        containerColor = Color(0xFF292929)
    ) { paddingValues ->
        when (currentScreen) {
            "Progress" -> ProgressTrackerContent(taskViewModel, username, paddingValues)
        }
    }
}

@Composable
fun ProgressTrackerContent(taskViewModel: TaskViewModel, username: String, paddingValues: PaddingValues) {
    val taskList: List<DatabaseHelper.Task> = taskViewModel.taskList
    var filterOption by remember { mutableStateOf(FilterOption.ALPHABETICAL) }
    val context = LocalContext.current

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationBar(username: String, currentScreen: String, onScreenChange: (String) -> Unit) {
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
                NavigationItem("Home", R.drawable.ic_home) { onScreenChange("Home") },
                NavigationItem("Tasks", R.drawable.ic_tasks) { onScreenChange("Tasks") },
                NavigationItem("Progress", R.drawable.ic_progress) { onScreenChange("Progress") },
                NavigationItem("Pomodoro", R.drawable.ic_pomodoro) { onScreenChange("Pomodoro") },
                NavigationItem("Expense", R.drawable.ic_calendar) { onScreenChange("Expense") }
            )

            navigationItems.forEach { item ->
                FilterChip(
                    selected = false, // Always false to prevent persistent highlighting
                    onClick = { item.action() },
                    label = {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.label,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFFFFFFFF) // Always white, no highlight
                        )
                    },
                    modifier = Modifier
                        .size(56.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF1B1B1B),
                        selectedContainerColor = Color(0xFF1B1B1B) // Same color, no change
                    ),
                    shape = CircleShape,
                    border = null // No border to avoid any highlight effect
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
        ProgressTrackerScreen(previewViewModel, "previewUser")
    }
}
