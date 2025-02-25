package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myacademate.ui.theme.MyAcademateTheme

class ProgressTrackerActivity : ComponentActivity() {
    private val taskViewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929) // background: Dark Gray #292929
                ) {
                    ProgressTrackerScreen(taskViewModel)
                }
            }
        }
    }
}

@Composable
fun ProgressTrackerScreen(taskViewModel: TaskViewModel) {
    val taskList: List<DatabaseHelper.Task> = taskViewModel.taskList
    var filterOption by remember { mutableStateOf(FilterOption.ALPHABETICAL) }
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Text(
                    text = "Progress Tracker",
                    style = androidx.compose.material3.Typography().titleLarge,
                    color = Color(0xFFFFFFFF) // onBackground: White #ffffff
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            item {
                FilterSection(
                    filterOption = filterOption,
                    onFilterSelected = { filterOption = it }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                Text(
                    text = "Progress List",
                    style = androidx.compose.material3.Typography().bodyMedium,
                    color = Color(0xFFFFFFFF) // onBackground: White #ffffff
                )
                Spacer(modifier = Modifier.height(10.dp))
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
                    Text(
                        text = "No tasks available",
                        style = androidx.compose.material3.Typography().bodyMedium,
                        color = Color(0xFFFFFFFF) // onBackground: White #ffffff
                    )
                }
            }
        }

        // Bottom Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navigationItems = listOf(
                NavigationItem("Home", R.drawable.ic_home) {
                    val intent = Intent(context, HomeActivity::class.java)
                    context.startActivity(intent)
                },
                NavigationItem("Tasks", R.drawable.ic_tasks) {
                    val intent = Intent(context, TaskManagerActivity::class.java)
                    context.startActivity(intent)
                },
                NavigationItem("Progress", R.drawable.ic_progress) { /* Current screen, no action */ },
                NavigationItem("Pomodoro", R.drawable.ic_pomodoro) {
                    val intent = Intent(context, PomodoroActivity::class.java)
                    context.startActivity(intent)
                },
                NavigationItem("Expense", R.drawable.ic_calendar) {
                    val intent = Intent(context, ExpenseActivity::class.java)
                    context.startActivity(intent)
                }
            )
            navigationItems.forEach { item ->
                IconButton(
                    onClick = { item.action() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.label,
                        modifier = Modifier.size(32.dp),
                        tint = Color(0xFFFFFFFF) // onBackground: White #ffffff
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
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        FilterChip(
            selected = filterOption == FilterOption.ALPHABETICAL,
            onClick = { onFilterSelected(FilterOption.ALPHABETICAL) },
            label = { Text("A-Z", color = Color(0xFFFFFFFF)) } // onSecondary: White #ffffff
        )
        FilterChip(
            selected = filterOption == FilterOption.FARTHEST_DEADLINE,
            onClick = { onFilterSelected(FilterOption.FARTHEST_DEADLINE) },
            label = { Text("Farthest Deadline", color = Color(0xFFFFFFFF)) } // onSecondary: White #ffffff
        )
        FilterChip(
            selected = filterOption == FilterOption.CLOSEST_DEADLINE,
            onClick = { onFilterSelected(FilterOption.CLOSEST_DEADLINE) },
            label = { Text("Closest Deadline", color = Color(0xFFFFFFFF)) } // onSecondary: White #ffffff
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
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)) // surface: Darker Gray #1b1b1b
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Task: ${task.subjectName}",
                style = androidx.compose.material3.Typography().bodyLarge,
                color = Color(0xFFFFFFFF) // onSurface: White #ffffff
            )
            Text(
                text = "Status: $progressStatus",
                style = androidx.compose.material3.Typography().bodyMedium,
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

            LinearProgressIndicator(
                progress = task.completionPercentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFA31A), // primary: Orange #ffa31a
                trackColor = Color(0xFF808080) // secondary: Gray #808080
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { taskViewModel.updateTaskCompletion(task.id, 25) },
                    enabled = task.completionPercentage < 25,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("25%")
                }
                Button(
                    onClick = { taskViewModel.updateTaskCompletion(task.id, 50) },
                    enabled = task.completionPercentage < 50,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("50%")
                }
                Button(
                    onClick = { taskViewModel.updateTaskCompletion(task.id, 75) },
                    enabled = task.completionPercentage < 75,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("75%")
                }
                Button(
                    onClick = { taskViewModel.updateTaskCompletion(task.id, 100) },
                    enabled = task.completionPercentage < 100,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("100%")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (task.completionPercentage == 100 && !task.isDone) {
                Button(
                    onClick = { taskViewModel.toggleTaskStatus(task.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("Mark as Done")
                }
            }

            if (task.isDone) {
                Text(
                    text = "Task Completed!",
                    style = androidx.compose.material3.Typography().bodyMedium,
                    color = Color(0xFFFFA31A), // primary: Orange #ffa31a
                    modifier = Modifier.align(Alignment.CenterHorizontally)
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
        ProgressTrackerScreen(previewViewModel)
    }
}
