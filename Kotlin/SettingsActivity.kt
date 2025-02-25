package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myacademate.ui.theme.MyAcademateTheme

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        Log.d("SettingsActivity", "Username received: $username") // Debug log to verify username
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929) // Background: Dark Gray #292929
                ) {
                    SettingsScreen(username)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(username: String) {
    val context = LocalContext.current
    var theme by remember { mutableStateOf("Light") }  // Example user preference
    var notificationsEnabled by remember { mutableStateOf(true) }  // Example user preference
    var showLogoutDialog by remember { mutableStateOf(false) } // State for logout confirmation

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back Button and Title
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
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFFFFFFFF), // OnBackground: White #FFFFFF
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Theme Selection
        SettingsItem(
            title = "Theme",
            subtitle = "Change app theme",
            onClick = { theme = if (theme == "Light") "Dark" else "Light" },
            trailingContent = {
                Text(
                    text = theme,
                    color = Color(0xFFFFA31A), // Primary: Orange #FFA31A
                    fontSize = 14.sp
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications Toggle
        SettingsItem(
            title = "Notifications",
            subtitle = "Enable or disable notifications",
            trailingContent = {
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFFFFA31A), // Primary: Orange #FFA31A
                        uncheckedThumbColor = Color(0xFF808080) // Secondary: Gray #808080
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // About Developer
        SettingsItem(
            title = "About Developer",
            subtitle = "Learn more about the developer",
            onClick = {
                val intent = Intent(context, DeveloperActivity::class.java)
                intent.putExtra("USERNAME", username) // Pass username
                context.startActivity(intent)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button with Confirmation Dialog
        Button(
            onClick = { showLogoutDialog = true }, // Show confirmation dialog
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFA31A), // Primary: Orange #FFA31A
                contentColor = Color(0xFFFFFFFF) // OnPrimary: White #FFFFFF
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "Log Out")
        }

        // Version Info
        Text(
            text = "Version 1.0.0",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF808080), // Secondary: Gray #808080
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 24.dp)
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", color = Color(0xFFFFFFFF)) }, // OnBackground: White #FFFFFF
            text = { Text("Are you sure you want to log out?", color = Color(0xFFFFFFFF)) }, // OnBackground: White #FFFFFF
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                        (context as ComponentActivity).finish() // Finish SettingsActivity
                    }
                ) {
                    Text("Yes", color = Color(0xFFFFA31A)) // Primary: Orange #FFA31A
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No", color = Color(0xFFFFA31A)) // Primary: Orange #FFA31A
                }
            },
            containerColor = Color(0xFF1B1B1B) // Surface: Darker Gray #1B1B1B
        )
    }

    // Bottom Navigation
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .align(Alignment.BottomCenter), // Align Row at the bottom
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val navigationItems = listOf(
                NavigationItem("Home", R.drawable.ic_home) {
                    val intent = Intent(context, HomeActivity::class.java)
                    intent.putExtra("USERNAME", username) // Pass username
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP // Avoid recreating HomeActivity
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

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() },
        color = Color(0xFF1B1B1B), // Surface: Darker Gray #1B1B1B
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFFFFFFF) // OnBackground: White #FFFFFF
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF808080) // Secondary: Gray #808080
                    )
                }
            }
            if (trailingContent != null) {
                trailingContent()
            }
        }
    }
}
