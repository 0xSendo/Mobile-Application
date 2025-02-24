package com.example.myacademate

import android.app.Application
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.myacademate.ui.theme.MyAcademateTheme

class ExpenseActivity : ComponentActivity() {
    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory((applicationContext as Application))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpenseTrackerScreen(expenseViewModel)
                }
            }
        }
    }
}

@Composable
fun ExpenseTrackerScreen(expenseViewModel: ExpenseViewModel) {
    val expenseList by expenseViewModel.expenseList.collectAsState()
    var expenseName by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Input Fields for Adding Expenses
        OutlinedTextField(
            value = expenseName,
            onValueChange = { expenseName = it },
            label = { Text("Expense Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = expenseAmount,
            onValueChange = { expenseAmount = it },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Expense")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // List of Expenses
        Text(
            text = "Expenses",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (expenseList.isNotEmpty()) {
            LazyColumn {
                items(expenseList) { expense ->
                    ExpenseItem(expense, expenseViewModel)
                }
            }
        } else {
            Text(
                text = "No expenses added yet",
                style = MaterialTheme.typography.bodyMedium
            )
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
        elevation = CardDefaults.cardElevation(4.dp)
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
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "$${expense.amount}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showPaidDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_markdone),
                            contentDescription = "Mark as Paid"
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete Expense"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = expense.completionPercentage / 100f,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { expenseViewModel.updateExpenseCompletion(expense, 25) },
                    enabled = expense.completionPercentage < 25
                ) {
                    Text("25%")
                }
                Button(
                    onClick = { expenseViewModel.updateExpenseCompletion(expense, 50) },
                    enabled = expense.completionPercentage < 50
                ) {
                    Text("50%")
                }
                Button(
                    onClick = { expenseViewModel.updateExpenseCompletion(expense, 75) },
                    enabled = expense.completionPercentage < 75
                ) {
                    Text("75%")
                }
                Button(
                    onClick = { expenseViewModel.updateExpenseCompletion(expense, 100) },
                    enabled = expense.completionPercentage < 100
                ) {
                    Text("100%")
                }
            }

            // Delete Confirmation Dialog
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Expense") },
                    text = { Text("Delete this expense?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                expenseViewModel.deleteExpense(expense)
                                showDeleteDialog = false
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteDialog = false }
                        ) {
                            Text("No")
                        }
                    }
                )
            }

            // Mark as Paid Confirmation Dialog
            if (showPaidDialog) {
                AlertDialog(
                    onDismissRequest = { showPaidDialog = false },
                    title = { Text("Mark Expense as Paid") },
                    text = { Text("Mark this expense as paid? This will delete it.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                expenseViewModel.deleteExpense(expense)
                                showPaidDialog = false
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showPaidDialog = false }
                        ) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}
