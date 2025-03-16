package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myacademate.ui.theme.MyAcademateTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        Log.d("SettingsActivity", "Username received: $username")

        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929)
                ) {
                    SettingsScreen(username)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(username: String) {
    val context = LocalContext.current
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var highlightedNavItem by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
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
                            context.startActivity(intent)
                        },
                        NavigationItem("Tasks", R.drawable.ic_tasks) {
                            val intent = Intent(context, TaskManagerActivity::class.java)
                            intent.putExtra("USERNAME", username)
                            context.startActivity(intent)
                        },
                        NavigationItem("Progress", R.drawable.ic_progress) {
                            val intent = Intent(context, ProgressTrackerActivity::class.java)
                            intent.putExtra("USERNAME", username)
                            context.startActivity(intent)
                        },
                        NavigationItem("Pomodoro", R.drawable.ic_pomodoro) {
                            val intent = Intent(context, PomodoroActivity::class.java)
                            intent.putExtra("USERNAME", username)
                            context.startActivity(intent)
                        },
                        NavigationItem("Expense", R.drawable.ic_calendar) {
                            val intent = Intent(context, ExpenseActivity::class.java)
                            intent.putExtra("USERNAME", username)
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
                SettingsCard(
                    title = "Notifications",
                    subtitle = "Enable or disable notifications",
                    trailingContent = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFFA31A),
                                checkedTrackColor = Color(0xFFFFA31A).copy(alpha = 0.5f),
                                uncheckedThumbColor = Color(0xFF808080),
                                uncheckedTrackColor = Color(0xFF808080).copy(alpha = 0.5f)
                            )
                        )
                    }
                )
            }

            item {
                SettingsCard(
                    title = "About Developer",
                    subtitle = "Learn more about the developer",
                    onClick = {
                        val intent = Intent(context, DeveloperActivity::class.java)
                        intent.putExtra("USERNAME", username)
                        context.startActivity(intent)
                    }
                )
            }

            item {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A),
                        contentColor = Color(0xFFFFFFFF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Log Out",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item {
                Text(
                    text = "Version 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF808080),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    "Log Out",
                    color = Color(0xFFFFFFFF),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    "Are you sure you want to log out?",
                    color = Color(0xFFFFFFFF),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        (context as ComponentActivity).finish()
                    }
                ) {
                    Text("Yes", color = Color(0xFFFFA31A), fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No", color = Color(0xFFFFA31A), fontSize = 16.sp)
                }
            },
            containerColor = Color(0xFF1B1B1B),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFFFFFFF),
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF808080)
                    )
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}
