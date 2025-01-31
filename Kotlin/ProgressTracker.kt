package com.example.myacademate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myacademate.ui.theme.MyAcademateTheme

class ProgressTrackerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProgressTrackerScreen()
                }
            }
        }
    }
}

@Composable
fun ProgressTrackerScreen() {
    var progressList by remember { mutableStateOf(listOf<String>()) }
    var taskName by remember { mutableStateOf("") }
    var progressStatus by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Progress Tracker", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = taskName,
            onValueChange = { taskName = it },
            label = { Text("Task Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = progressStatus,
            onValueChange = { progressStatus = it },
            label = { Text("Progress Status") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (taskName.isNotEmpty() && progressStatus.isNotEmpty()) {
                    progressList = progressList + "$taskName - $progressStatus"
                    taskName = ""
                    progressStatus = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Progress")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(text = "Progress List", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(10.dp))

        if (progressList.isNotEmpty()) {
            progressList.forEach { progress ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = progress, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        } else {
            Text("No progress available", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressTrackerScreenPreview() {
    MyAcademateTheme {
        ProgressTrackerScreen()
    }
}
