package com.example.myacademate

import android.app.Application
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.myacademate.ui.theme.MyAcademateTheme

class ExpenseActivity : ComponentActivity() {
    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((applicationContext as Application))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929) // background: Dark Gray #292929
                ) {
                    ExpenseTrackerScreen(expenseViewModel, username)
                }
            }
        }
    }
}

@Composable
fun ExpenseTrackerScreen(expenseViewModel: ExpenseViewModel, username: String) {
    val expenseList by expenseViewModel.expenseList.collectAsState()
    var expenseName by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                // Input Fields for Adding Expenses
                OutlinedTextField(
                    value = expenseName,
                    onValueChange = { expenseName = it },
                    label = { Text("Expense Name", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFFFFF)) // onSurface: White #ffffff
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                OutlinedTextField(
                    value = expenseAmount,
                    onValueChange = { expenseAmount = it },
                    label = { Text("Amount", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFFFFF)) // onSurface: White #ffffff
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                // Add Expense Button
                Button(
                    onClick = {
                        if (expenseName.isNotEmpty() && expenseAmount.isNotEmpty()) {
                            val amount = expenseAmount.toDoubleOrNull() ?: 0.0
                            expenseViewModel.addExpense(Expense(expenseName, amount))
                            expenseName = ""
                            expenseAmount = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("Add Expense")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                // List of Expenses
                Text(
                    text = "Expenses",
                    style = androidx.compose.material3.Typography().titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = Color(0xFFFFFFFF) // onBackground: White #ffffff
                )
            }

            if (expenseList.isNotEmpty()) {
                items(expenseList) { expense ->
                    ExpenseItem(expense, expenseViewModel)
                }
            } else {
                item {
                    Text(
                        text = "No expenses added yet",
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
                    intent.putExtra("USERNAME", username) // Pass username
                    context.startActivity(intent)
                },
                NavigationItem("Tasks", R.drawable.ic_tasks) { /* Current screen, no action */ },
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
                        tint = Color(0xFFFFFFFF) // onBackground: White #ffffff
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: Expense, expenseViewModel: ExpenseViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPaidDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)) // surface: Darker Gray #1b1b1b
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = expense.name,
                        style = androidx.compose.material3.Typography().bodyLarge,
                        color = Color(0xFFFFFFFF) // onSurface: White #ffffff
                    )
                    Text(
                        text = "$${expense.amount}",
                        style = androidx.compose.material3.Typography().bodyMedium,
                        color = Color(0xFFFFFFFF) // onSurface: White #ffffff
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showPaidDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_markdone),
                            contentDescription = "Mark as Paid",
                            tint = Color(0xFFFFA31A) // primary: Orange #ffa31a
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete Expense",
                            tint = Color(0xFFFFA31A) // primary: Orange #ffa31a
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = expense.completionPercentage / 100f,
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
                    onClick = { expenseViewModel.updateExpenseCompletion(expense, 25) },
                    enabled = expense.completionPercentage < 25,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("25%")
                }
                Button(
                    onClick = { expenseViewModel.updateExpenseCompletion(expense, 50) },
                    enabled = expense.completionPercentage < 50,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("50%")
                }
                Button(
                    onClick = { expenseViewModel.updateExpenseCompletion(expense, 75) },
                    enabled = expense.completionPercentage < 75,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("75%")
                }
                Button(
                    onClick = { expenseViewModel.updateExpenseCompletion(expense, 100) },
                    enabled = expense.completionPercentage < 100,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF) // onPrimary: White #ffffff
                    )
                ) {
                    Text("100%")
                }
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Expense", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
                    text = { Text("Delete this expense?", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
                    confirmButton = {
                        TextButton(
                            onClick = {
                                expenseViewModel.deleteExpense(expense)
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Yes", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("No", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                        }
                    },
                    containerColor = Color(0xFF1B1B1B) // surface: Darker Gray #1b1b1b
                )
            }

            if (showPaidDialog) {
                AlertDialog(
                    onDismissRequest = { showPaidDialog = false },
                    title = { Text("Mark Expense as Paid", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
                    text = { Text("Mark this expense as paid? This will delete it.", color = Color(0xFFFFFFFF)) }, // onSurface: White #ffffff
                    confirmButton = {
                        TextButton(
                            onClick = {
                                expenseViewModel.deleteExpense(expense)
                                showPaidDialog = false
                            }
                        ) {
                            Text("Yes", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showPaidDialog = false }
                        ) {
                            Text("No", color = Color(0xFFFFA31A)) // primary: Orange #ffa31a
                        }
                    },
                    containerColor = Color(0xFF1B1B1B) // surface: Darker Gray #1b1b1b
                )
            }
        }
    }
}
