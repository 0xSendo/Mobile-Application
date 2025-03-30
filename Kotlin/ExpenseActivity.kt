package com.example.myacademate

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.myacademate.ui.theme.MyAcademateTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ExpenseActivity : ComponentActivity() {
    private val expenseViewModel: ExpenseViewModel by viewModels {
        ExpenseViewModelFactory(applicationContext as Application)
    }
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        setContent {
            MyAcademateTheme {
                ExpenseTrackerScreen(expenseViewModel, username, this)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
            finishAffinity()
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}

enum class ExpenseFilterOption {
    ALPHABETICAL,
    HIGHEST_AMOUNT,
    LOWEST_AMOUNT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(expenseViewModel: ExpenseViewModel, username: String, context: Context) {
    var highlightedNavItem by remember { mutableStateOf<String?>(null) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(250.dp),
                drawerContainerColor = Color(0xFF1B1B1B)
            ) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "MyAcademate",
                    color = Color(0xFFFFA31A),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Spacer(Modifier.height(16.dp))

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
                    Pair("Expense", R.drawable.ic_calendar) to { /* Current screen */ }
                )

                navItems.forEach { (pair, action) ->
                    val (label, icon) = pair
                    val isHighlighted = highlightedNavItem == label || label == "Expense"
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = label,
                                color = if (isHighlighted) Color(0xFFFFA31A) else Color(0xFFFFFFFF)
                            )
                        },
                        selected = isHighlighted,
                        onClick = {
                            action()
                            scope.launch { drawerState.close() }
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = label,
                                tint = if (isHighlighted) Color(0xFFFFA31A) else Color(0xFFFFFFFF)
                            )
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Expense Tracker",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B1B1B))
                )
            },
            containerColor = Color(0xFF292929)
        ) { paddingValues ->
            ExpenseContent(expenseViewModel, username, paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseContent(expenseViewModel: ExpenseViewModel, username: String, paddingValues: PaddingValues) {
    val expenseList by expenseViewModel.expenseList.collectAsState()
    val paidExpenses by expenseViewModel.paidExpenses.collectAsState()
    var expenseName by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var filterOption by remember { mutableStateOf(ExpenseFilterOption.ALPHABETICAL) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showPaidExpensesDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val totalExpenses = expenseList.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(Brush.verticalGradient(colors = listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A))))
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Expense Tracker",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFA31A),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        IconButton(onClick = { showPaidExpensesDialog = true }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_history),
                                contentDescription = "View Paid Expenses",
                                tint = Color(0xFFFFA31A),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Text(
                        text = "Total: $${String.format("%.2f", totalExpenses)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF808080),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B).copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = expenseName,
                            onValueChange = { expenseName = it },
                            label = { Text("Expense Name", color = Color(0xFF808080)) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFFFFF), fontSize = 16.sp),
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
                            textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFFFFF), fontSize = 16.sp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color(0xFFFFA31A),
                                unfocusedBorderColor = Color(0xFF808080),
                                cursorColor = Color(0xFFFFA31A)
                            )
                        )
                        AnimatedButton(
                            text = "Add",
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
                    text = "Sort: ${filterOption.name.lowercase().replace("_", " ").capitalize()}",
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
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFFFFF),
                    modifier = Modifier.padding(vertical = 8.dp)
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
                        fontSize = 16.sp,
                        color = Color(0xFF808080),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        if (showFilterDialog) {
            FilterDialog(
                expenseListSize = expenseList.size,
                currentFilter = filterOption,
                onFilterSelected = { filterOption = it; showFilterDialog = false },
                onDismiss = { showFilterDialog = false }
            )
        }

        if (showPaidExpensesDialog) {
            PaidExpensesDialog(
                paidExpenses = paidExpenses,
                expenseViewModel = expenseViewModel,
                onDismiss = { showPaidExpensesDialog = false }
            )
        }
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
    var showProgressHint by remember { mutableStateOf(false) }

    val nextProgress = when {
        expense.completionPercentage < 25 -> 25
        expense.completionPercentage < 50 -> 50
        expense.completionPercentage < 75 -> 75
        expense.completionPercentage < 100 -> 100
        else -> 100
    }
    val progressButtonText = if (expense.completionPercentage < 100) "+$nextProgress%" else "Done"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B).copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = expense.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFFFFF)
                    )
                    Text(
                        text = "$${String.format("%.2f", expense.amount)}",
                        fontSize = 14.sp,
                        color = Color(0xFF808080)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Edit",
                            tint = Color(0xFFFFA31A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { showPaidDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_markdone),
                            contentDescription = "Mark Paid",
                            tint = Color(0xFFFFA31A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete",
                            tint = Color(0xFFFFA31A),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = expense.completionPercentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFFFFA31A),
                trackColor = Color(0xFF404040)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    AnimatedButton(
                        text = progressButtonText,
                        onClick = {
                            if (expense.completionPercentage < 100) {
                                expenseViewModel.updateExpenseCompletion(expense, nextProgress)
                            }
                        },
                        enabled = expense.completionPercentage < 100,
                        containerColor = if (expense.completionPercentage == 100) Color(0xFF808080) else Color(0xFFFFA31A),
                        contentColor = Color(0xFFFFFFFF),
                        coroutineScope = coroutineScope,
                        modifier = Modifier
                            .height(36.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { /* Handled by AnimatedButton */ }
                            )
                            .pointerInput(Unit) {
                                detectTapGestures(onLongPress = { showProgressHint = true })
                            }
                    )
                    if (showProgressHint) {
                        Popup(
                            onDismissRequest = { showProgressHint = false },
                            alignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = "Next: $nextProgress%",
                                fontSize = 12.sp,
                                color = Color(0xFFFFFFFF),
                                modifier = Modifier
                                    .background(Color(0xFF404040), RoundedCornerShape(4.dp))
                                    .padding(4.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${expense.completionPercentage}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (expense.completionPercentage == 100) Color(0xFF4CAF50) else Color(0xFFFFFFFF)
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteDialog(expense, expenseViewModel, onDismiss = { showDeleteDialog = false })
    }

    if (showPaidDialog) {
        PaidDialog(expense, expenseViewModel, onDismiss = { showPaidDialog = false })
    }

    if (showEditDialog) {
        EditDialog(
            expense = expense,
            editedName = editedName,
            editedAmount = editedAmount,
            editedProgress = editedProgress,
            onNameChange = { editedName = it },
            onAmountChange = { editedAmount = it },
            onProgressChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() }.take(3)
                editedProgress = if (filtered.toIntOrNull() ?: 0 <= 100) filtered else "100"
            },
            onSave = {
                val newAmount = editedAmount.toDoubleOrNull() ?: expense.amount
                val newProgress = editedProgress.toIntOrNull()?.coerceIn(0, 100) ?: expense.completionPercentage
                expenseViewModel.updateExpense(expense, editedName, newAmount, newProgress)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
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
                scale.animateTo(0.95f, animationSpec = tween(100))
                scale.animateTo(1f, animationSpec = tween(100))
            }
        },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        modifier = modifier
            .scale(scale.value)
            .clickable(interactionSource = interactionSource, indication = null) {}
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    expenseListSize: Int,
    currentFilter: ExpenseFilterOption,
    onFilterSelected: (ExpenseFilterOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sort Expenses", color = Color(0xFFFFFFFF), fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "$expenseListSize expenses", color = Color(0xFF808080), fontSize = 14.sp)
                listOf(
                    ExpenseFilterOption.ALPHABETICAL to "A-Z",
                    ExpenseFilterOption.HIGHEST_AMOUNT to "High to Low",
                    ExpenseFilterOption.LOWEST_AMOUNT to "Low to High"
                ).forEach { (option, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFilterSelected(option) }
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            color = if (currentFilter == option) Color(0xFFFFA31A) else Color(0xFFFFFFFF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = when (option) {
                                ExpenseFilterOption.ALPHABETICAL -> "By name"
                                ExpenseFilterOption.HIGHEST_AMOUNT -> "Highest first"
                                ExpenseFilterOption.LOWEST_AMOUNT -> "Lowest first"
                            },
                            color = Color(0xFF808080),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFFFA31A), fontSize = 16.sp)
            }
        },
        containerColor = Color(0xFF1B1B1B),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun DeleteDialog(expense: Expense, expenseViewModel: ExpenseViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Remove Expense", color = Color(0xFFFFFFFF), fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Remove '${expense.name}'?", color = Color(0xFFFFFFFF), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Amount: $${String.format("%.2f", expense.amount)}", color = Color(0xFF808080), fontSize = 14.sp)
                if (expense.completionPercentage > 0) {
                    Text(
                        text = "Progress: ${expense.completionPercentage}%",
                        color = Color(0xFF808080),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                expenseViewModel.deleteExpense(expense)
                onDismiss()
            }) {
                Text("Remove", color = Color(0xFFFFA31A), fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Keep", color = Color(0xFFFFA31A), fontSize = 16.sp)
            }
        },
        containerColor = Color(0xFF1B1B1B),
        shape = RoundedCornerShape(12.dp)
    )
}

@Composable
fun PaidDialog(expense: Expense, expenseViewModel: ExpenseViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mark as Paid", color = Color(0xFFFFFFFF), fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Mark '${expense.name}' as paid?", color = Color(0xFFFFFFFF), fontSize = 16.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Amount: $${String.format("%.2f", expense.amount)}", color = Color(0xFF808080), fontSize = 14.sp)
                Text("This will move it to paid expenses.", color = Color(0xFF808080), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                expenseViewModel.deleteExpense(expense) // Moves to paidExpenses in ViewModel
                onDismiss()
            }) {
                Text("Mark Paid", color = Color(0xFFFFA31A), fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFFFA31A), fontSize = 16.sp)
            }
        },
        containerColor = Color(0xFF1B1B1B),
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDialog(
    expense: Expense,
    editedName: String,
    editedAmount: String,
    editedProgress: String,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onProgressChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Expense", color = Color(0xFFFFFFFF), fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = onNameChange,
                    label = { Text("Name", color = Color(0xFF808080)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFFFFF), fontSize = 16.sp),
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color(0xFFFFA31A),
                        unfocusedBorderColor = Color(0xFF808080),
                        cursorColor = Color(0xFFFFA31A)
                    )
                )
                OutlinedTextField(
                    value = editedAmount,
                    onValueChange = onAmountChange,
                    label = { Text("Amount", color = Color(0xFF808080)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFFFFF), fontSize = 16.sp),
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
                    onValueChange = onProgressChange,
                    label = { Text("Progress (%)", color = Color(0xFF808080)) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFFFFFFFF), fontSize = 16.sp),
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
            TextButton(onClick = onSave) {
                Text("Save", color = Color(0xFFFFA31A), fontSize = 16.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFFFFA31A), fontSize = 16.sp)
            }
        },
        containerColor = Color(0xFF1B1B1B),
        shape = RoundedCornerShape(12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaidExpensesDialog(
    paidExpenses: List<Expense>,
    expenseViewModel: ExpenseViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Paid Expenses", color = Color(0xFFFFFFFF), fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        text = {
            if (paidExpenses.isEmpty()) {
                Text(
                    text = "No paid expenses yet.",
                    color = Color(0xFF808080),
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 300.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(paidExpenses) { expense ->
                        Card(
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = expense.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFFFFFFF)
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", expense.amount)}",
                                        fontSize = 14.sp,
                                        color = Color(0xFF808080)
                                    )
                                }
                                TextButton(onClick = {
                                    expenseViewModel.retrieveExpense(expense)
                                }) {
                                    Text("Retrieve", color = Color(0xFFFFA31A), fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFFFFA31A), fontSize = 16.sp)
            }
        },
        containerColor = Color(0xFF1B1B1B),
        shape = RoundedCornerShape(12.dp)
    )
}
