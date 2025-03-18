package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
                    DeveloperScreen(username)
                }
            }
        }
    }

    override fun onBackPressed() {
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
fun DeveloperScreen(username: String) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "About the Developer",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color(0xFFFFFFFF),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFFFFA31A)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B1B1B)
                )
            )
        },
        bottomBar = {
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
                        NavigationItem("Tasks", R.drawable.ic_tasks) {
                            val intent = Intent(context, TaskManagerActivity::class.java)
                            intent.putExtra("USERNAME", username)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            context.startActivity(intent)
                        },
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
                        IconButton(
                            onClick = { item.action() },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.Transparent, shape = CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.label,
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFFFFFFFF)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Developed by Paul Abellana",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFFA31A),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "We are a passionate group of developers dedicated to creating tools that enhance productivity and learning experiences for students and professionals alike.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFFFFF)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Lead Developer",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFFFFFF),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Paul Abellana",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFA31A)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Contact",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFFFFFF),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "paulabellana4@gmail.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF808080)
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Our Mission",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFFFFA31A),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To empower users with intuitive, efficient, and visually appealing applications that streamline daily tasks and foster academic success.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFFFFF)
                        )
                    }
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
