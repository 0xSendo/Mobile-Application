package com.example.myacademate

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myacademate.ui.theme.MyAcademateTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
                    color = Color(0xFF292929) // Dark Gray #292929
                ) {
                    ExpenseTrackerScreen(expenseViewModel, username)
                }
            }
        }
    }
}

enum class ExpenseFilterOption {
    ALPHABETICAL,
    HIGHEST_AMOUNT,
    LOWEST_AMOUNT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(expenseViewModel: ExpenseViewModel, username: String) {
    val expenseList by expenseViewModel.expenseList.collectAsState()
    var expenseName by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var filterOption by remember { mutableStateOf(ExpenseFilterOption.ALPHABETICAL) }
    var showFilterDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val totalExpenses = expenseList.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A))
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Expense Tracker",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFA31A),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Total Spending: $${String.format("%.2f", totalExpenses)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF808080),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B).copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = expenseName,
                            onValueChange = { expenseName = it },
                            label = { Text("Expense Name", color = Color(0xFF808080)) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFFFFFFFF),
                                fontSize = 18.sp
                            ),
                            singleLine = true,
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFFFA31A),
                                unfocusedBorderColor = Color(0xFF808080),
                                cursorColor = Color(0xFFFFA31A)
                            )
                        )

                        OutlinedTextField(
                            value = expenseAmount,
                            onValueChange = { expenseAmount = it },
                            label = { Text("Amount", color = Color(0xFF808080)) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFFFFFFFF),
                                fontSize = 18.sp
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFFFA31A),
                                unfocusedBorderColor = Color(0xFF808080),
                                cursorColor = Color(0xFFFFA31A)
                            )
                        )

                        AnimatedButton(
                            text = "Add Expense",
                            onClick = {
                                if (expenseName.isNotEmpty() && expenseAmount.isNotEmpty()) {
                                    val amount = expenseAmount.toDoubleOrNull() ?: 0.0
                                    expenseViewModel.addExpense(Expense(expenseName, amount))
                                    expenseName = ""
                                    expenseAmount = ""
                                }
                            },
                            enabled = expenseName.isNotEmpty() && expenseAmount.isNotEmpty(),
                            containerColor = Color(0xFFFFA31A),
                            contentColor = Color(0xFFFFFFFF),
                            coroutineScope = coroutineScope
                        )
                    }
                }
            }

            item {
                AnimatedButton(
                    text = "Sort By: ${filterOption.name.lowercase().replace("_", " ").capitalize()}",
                    onClick = { showFilterDialog = true },
                    enabled = expenseList.isNotEmpty(),
                    containerColor = Color(0xFF808080),
                    contentColor = Color(0xFFFFFFFF),
                    coroutineScope = coroutineScope,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    text = "Your Expenses",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            val filteredExpenses = when (filterOption) {
                ExpenseFilterOption.ALPHABETICAL -> expenseList.sortedBy { it.name }
                ExpenseFilterOption.HIGHEST_AMOUNT -> expenseList.sortedByDescending { it.amount }
                ExpenseFilterOption.LOWEST_AMOUNT -> expenseList.sortedBy { it.amount }
            }

            if (filteredExpenses.isNotEmpty()) {
                items(filteredExpenses) { expense ->
                    ExpenseItem(expense, expenseViewModel, coroutineScope)
                }
            } else {
                item {
                    Text(
                        text = "No expenses added yet",
                        fontSize = 18.sp,
                        color = Color(0xFF808080),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
            elevation = CardDefaults.cardElevation(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
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
                    NavigationItem("Expense", R.drawable.ic_calendar) { /* Current screen */ }
                )

                navigationItems.forEach { item ->
                    FilterChip(
                        selected = item.label == "Expense",
                        onClick = { item.action() },
                        label = {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.label,
                                modifier = Modifier.size(34.dp),
                                tint = if (item.label == "Expense") Color(0xFFFFA31A) else Color(0xFFFFFFFF)
                            )
                        },
                        modifier = Modifier.size(60.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF1B1B1B),
                            selectedContainerColor = Color(0xFF1B1B1B).copy(alpha = 0.9f)
                        ),
                        shape = CircleShape,
                        border = null
                    )
                }
            }
        }
    }

    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = {
                Text(
                    "Sort Expenses",
                    color = Color(0xFFFFFFFF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${expenseList.size} expenses",
                        color = Color(0xFF808080),
                        fontSize = 16.sp
                    )
                    listOf(
                        ExpenseFilterOption.ALPHABETICAL to "A-Z",
                        ExpenseFilterOption.HIGHEST_AMOUNT to "High to Low",
                        ExpenseFilterOption.LOWEST_AMOUNT to "Low to High"
                    ).forEach { (option, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    filterOption = option
                                    showFilterDialog = false
                                }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = if (filterOption == option) Color(0xFFFFA31A) else Color(0xFFFFFFFF),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = when (option) {
                                    ExpenseFilterOption.ALPHABETICAL -> "Sort by name"
                                    ExpenseFilterOption.HIGHEST_AMOUNT -> "Highest amounts first"
                                    ExpenseFilterOption.LOWEST_AMOUNT -> "Lowest amounts first"
                                },
                                color = Color(0xFF808080),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showFilterDialog = false }) {
                    Text("Close", color = Color(0xFFFFA31A), fontSize = 18.sp)
                }
            },
            containerColor = Color(0xFF1B1B1B),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseItem(expense: Expense, expenseViewModel: ExpenseViewModel, coroutineScope: CoroutineScope) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPaidDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(expense.name) }
    var editedAmount by remember { mutableStateOf(expense.amount.toString()) }
    var editedProgress by remember { mutableStateOf(expense.completionPercentage.toString()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B).copy(alpha = 0.95f)),
        elevation = CardDefaults.cardElevation(8.dp)
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
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFFFFF)
                    )
                    Text(
                        text = "$${String.format("%.2f", expense.amount)}",
                        fontSize = 16.sp,
                        color = Color(0xFF808080)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit), // Replace with your edit icon
                            contentDescription = "Edit Expense",
                            tint = Color(0xFFFFA31A),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = { showPaidDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_markdone),
                            contentDescription = "Mark as Paid",
                            tint = Color(0xFFFFA31A),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete Expense",
                            tint = Color(0xFFFFA31A),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = expense.completionPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Color(0xFFFFA31A),
                trackColor = Color(0xFF404040)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AnimatedButton(
                    text = "Mark Progress",
                    onClick = {
                        val newPercentage = when {
                            expense.completionPercentage < 25 -> 25
                            expense.completionPercentage < 50 -> 50
                            expense.completionPercentage < 75 -> 75
                            expense.completionPercentage < 100 -> 100
                            else -> 100
                        }
                        expenseViewModel.updateExpenseCompletion(expense, newPercentage)
                    },
                    enabled = expense.completionPercentage < 100,
                    containerColor = if (expense.completionPercentage == 100) Color(0xFF808080) else Color(0xFFFFA31A),
                    contentColor = Color(0xFFFFFFFF),
                    coroutineScope = coroutineScope,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${expense.completionPercentage}% Paid",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (expense.completionPercentage == 100) Color(0xFF4CAF50) else Color(0xFFFFFFFF),
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    "Remove Expense",
                    color = Color(0xFFFFFFFF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Remove '${expense.name}' from your list?",
                        color = Color(0xFFFFFFFF),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Amount: $${String.format("%.2f", expense.amount)}",
                        color = Color(0xFF808080),
                        fontSize = 16.sp
                    )
                    if (expense.completionPercentage > 0) {
                        Text(
                            text = "Progress: ${expense.completionPercentage}% paid",
                            color = Color(0xFF808080),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    expenseViewModel.deleteExpense(expense)
                    showDeleteDialog = false
                }) {
                    Text("Remove", color = Color(0xFFFFA31A), fontSize = 18.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Keep", color = Color(0xFFFFA31A), fontSize = 18.sp)
                }
            },
            containerColor = Color(0xFF1B1B1B),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showPaidDialog) {
        AlertDialog(
            onDismissRequest = { showPaidDialog = false },
            title = {
                Text(
                    "Mark as Paid",
                    color = Color(0xFFFFFFFF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Mark '${expense.name}' as fully paid?",
                        color = Color(0xFFFFFFFF),
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Amount: $${String.format("%.2f", expense.amount)}",
                        color = Color(0xFF808080),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "This will remove it from your list.",
                        color = Color(0xFF808080),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    expenseViewModel.deleteExpense(expense)
                    showPaidDialog = false
                }) {
                    Text("Mark Paid", color = Color(0xFFFFA31A), fontSize = 18.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaidDialog = false }) {
                    Text("Cancel", color = Color(0xFFFFA31A), fontSize = 18.sp)
                }
            },
            containerColor = Color(0xFF1B1B1B),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    "Edit Expense",
                    color = Color(0xFFFFFFFF),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Name", color = Color(0xFF808080)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFFFFFFFF),
                            fontSize = 18.sp
                        ),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFA31A),
                            unfocusedBorderColor = Color(0xFF808080),
                            cursorColor = Color(0xFFFFA31A)
                        )
                    )
                    OutlinedTextField(
                        value = editedAmount,
                        onValueChange = { editedAmount = it },
                        label = { Text("Amount", color = Color(0xFF808080)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFFFFFFFF),
                            fontSize = 18.sp
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFA31A),
                            unfocusedBorderColor = Color(0xFF808080),
                            cursorColor = Color(0xFFFFA31A)
                        )
                    )
                    OutlinedTextField(
                        value = editedProgress,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() }.take(3)
                            editedProgress = if (filtered.toIntOrNull() ?: 0 <= 100) filtered else "100"
                        },
                        label = { Text("Progress (%)", color = Color(0xFF808080)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFFFFFFFF),
                            fontSize = 18.sp
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFFFFA31A),
                            unfocusedBorderColor = Color(0xFF808080),
                            cursorColor = Color(0xFFFFA31A)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val newAmount = editedAmount.toDoubleOrNull() ?: expense.amount
                    val newProgress = editedProgress.toIntOrNull()?.coerceIn(0, 100) ?: expense.completionPercentage
                    expenseViewModel.updateExpense(expense, editedName, newAmount, newProgress)
                    showEditDialog = false
                }) {
                    Text("Save", color = Color(0xFFFFA31A), fontSize = 18.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = Color(0xFFFFA31A), fontSize = 18.sp)
                }
            },
            containerColor = Color(0xFF1B1B1B),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    coroutineScope: CoroutineScope,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val interactionSource = remember { MutableInteractionSource() }

    Button(
        onClick = {
            onClick()
            coroutineScope.launch {
                scale.animateTo(0.92f, animationSpec = tween(120))
                scale.animateTo(1f, animationSpec = tween(120))
            }
        },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
        modifier = modifier
            .scale(scale.value)
            .height(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Custom click handling in onClick */ }
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
