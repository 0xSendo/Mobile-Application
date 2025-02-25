package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myacademate.ui.theme.MyAcademateTheme

class DeveloperActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: "" // Retrieve username from intent
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929) // Background: Dark Gray #292929
                ) {
                    DeveloperScreen(username)
                }
            }
        }
    }
}

@Composable
fun DeveloperScreen(username: String) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with Back Button and Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { (context as ComponentActivity).finish() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFFFFA31A) // Primary: Orange #FFA31A
                )
            }
            Text(
                text = "About the Developer",
                style = androidx.compose.material3.Typography().headlineLarge,
                color = Color(0xFFFFFFFF), // OnBackground: White #FFFFFF
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Developer Information
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = "Developed by Paul Abellana",
                style = androidx.compose.material3.Typography().bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFFA31A), // Primary: Orange #FFA31A
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "We are a passionate group of developers dedicated to creating tools that enhance productivity and learning experiences for students and professionals alike.",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFFFFFFFF), // OnBackground: White #FFFFFF
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Lead Developer: Paul Abellana",
                style = androidx.compose.material3.Typography().bodyLarge,
                color = Color(0xFFFFFFFF), // OnBackground: White #FFFFFF
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Contact: paulabellana4@gmail.com",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFF808080), // Secondary: Gray #808080
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Our Mission",
                style = androidx.compose.material3.Typography().bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFFA31A), // Primary: Orange #FFA31A
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "To empower users with intuitive, efficient, and visually appealing applications that streamline daily tasks and foster academic success.",
                style = androidx.compose.material3.Typography().bodyMedium,
                color = Color(0xFFFFFFFF), // OnBackground: White #FFFFFF
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f)) // Push navigation to the bottom

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
                    intent.putExtra("USERNAME", username) // Pass username
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                },
                NavigationItem("Tasks", R.drawable.ic_tasks) {
                    val intent = Intent(context, TaskManagerActivity::class.java)
                    intent.putExtra("USERNAME", username) // Pass username
                    context.startActivity(intent)
                },
                NavigationItem("Progress", R.drawable.ic_progress) {
                    val intent = Intent(context, ProgressTrackerActivity::class.java)
                    intent.putExtra("USERNAME", username) // Pass username
                    context.startActivity(intent)
                },
                NavigationItem("Pomodoro", R.drawable.ic_pomodoro) {
                    val intent = Intent(context, PomodoroActivity::class.java)
                    intent.putExtra("USERNAME", username) // Pass username
                    context.startActivity(intent)
                },
                NavigationItem("Expense", R.drawable.ic_calendar) {
                    val intent = Intent(context, ExpenseActivity::class.java)
                    intent.putExtra("USERNAME", username) // Pass username
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
                        tint = Color(0xFFFFFFFF) // OnBackground: White #FFFFFF
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DeveloperScreenPreview() {
    MyAcademateTheme {
        DeveloperScreen("previewUser")
    }
}
