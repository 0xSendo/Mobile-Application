package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myacademate.ui.theme.MyAcademateTheme
import kotlinx.coroutines.launch

class DeveloperActivity : ComponentActivity() {
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        setContent {
            MyAcademateTheme {
                DeveloperScreen(username, this)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(username: String, context: ComponentActivity) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var highlightedNavItem by remember { mutableStateOf<String?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color(0xFF1B1B1B),
                drawerContentColor = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF1B1B1B), Color(0xFF2A2A2A))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFFA31A), Color(0xFFFFC107))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "MyAcademate",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    val navItems = listOf(
                        Pair("Home", R.drawable.ic_home) to {
                            context.startActivity(Intent(context, HomeActivity::class.java).putExtra("USERNAME", username))
                        },
                        Pair("Tasks", R.drawable.ic_tasks) to {
                            context.startActivity(Intent(context, TaskManagerActivity::class.java).putExtra("USERNAME", username))
                        },
                        Pair("Progress", R.drawable.ic_progress) to {
                            context.startActivity(Intent(context, ProgressTrackerActivity::class.java).putExtra("USERNAME", username))
                        },
                        Pair("Pomodoro", R.drawable.ic_pomodoro) to {
                            context.startActivity(Intent(context, PomodoroActivity::class.java).putExtra("USERNAME", username))
                        },
                        Pair("Expense", R.drawable.ic_calendar) to {
                            context.startActivity(Intent(context, ExpenseActivity::class.java).putExtra("USERNAME", username))
                        },
                        Pair("Profile", R.drawable.ic_profile) to {
                            context.startActivity(Intent(context, ProfileActivity::class.java).putExtra("USERNAME", username))
                        }
                    )

                    navItems.forEach { (pair, action) ->
                        val (label, icon) = pair
                        val isHighlighted = highlightedNavItem == label
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    action()
                                    scope.launch { drawerState.close() }
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isHighlighted) Color(0xFFFFA31A).copy(alpha = 0.1f) else Color.Transparent
                            ),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = icon),
                                    contentDescription = label,
                                    tint = if (isHighlighted) Color(0xFFFFA31A) else Color(0xFFFFFFFF),
                                    modifier = Modifier.size(28.dp)
                                )
                                Text(
                                    text = label,
                                    color = if (isHighlighted) Color(0xFFFFA31A) else Color(0xFFFFFFFF),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    ) {
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1B1B1B)
                    )
                )
            },
            containerColor = Color(0xFF292929)
        ) { paddingValues ->
            DeveloperContent(paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperContent(paddingValues: PaddingValues) {
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

@Preview(showBackground = true)
@Composable
fun DeveloperScreenPreview() {
    MyAcademateTheme {
        DeveloperScreen("previewUser", DeveloperActivity())
    }
}
