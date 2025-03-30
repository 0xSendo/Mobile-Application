package com.example.myacademate

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

class PomodoroActivity : ComponentActivity() {
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        setContent {
            PomodoroScreen(username, this)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(username: String, context: Context) {
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
                    Pair("Pomodoro", R.drawable.ic_pomodoro) to { /* Current screen */ },
                    Pair("Expense", R.drawable.ic_calendar) to {
                        context.startActivity(Intent(context, ExpenseActivity::class.java).putExtra("USERNAME", username))
                    }
                )

                navItems.forEach { (pair, action) ->
                    val (label, icon) = pair
                    val isHighlighted = highlightedNavItem == label || label == "Pomodoro"
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
                            "Pomodoro Timer",
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
            PomodoroContent(username, context, paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroContent(username: String, context: Context, paddingValues: PaddingValues) {
    var timeLeft by remember { mutableLongStateOf(25 * 60 * 1000L) }
    var breakTimeLeft by remember { mutableLongStateOf(5 * 60 * 1000L) }
    var customPomodoroMinutes by remember { mutableStateOf("25") }
    var customPomodoroSeconds by remember { mutableStateOf("0") }
    var customBreakMinutes by remember { mutableStateOf("5") }
    var customBreakSeconds by remember { mutableStateOf("0") }
    var isPomodoroRunning by remember { mutableStateOf(false) }
    var isBreakRunning by remember { mutableStateOf(false) }
    var showPomodoroCompleteMessage by remember { mutableStateOf(false) }
    var showBreakCompleteMessage by remember { mutableStateOf(false) }
    var timer: CountDownTimer? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()
    var totalPomodoroDuration by remember { mutableLongStateOf(25 * 60 * 1000L) }
    var totalBreakDuration by remember { mutableLongStateOf(5 * 60 * 1000L) }
    val progress = remember { Animatable(1f) }

    // Animations
    val pulseScale by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val waveOffset by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Completion animations
    val completionScale = remember { Animatable(0f) }
    LaunchedEffect(showPomodoroCompleteMessage, showBreakCompleteMessage, isBreakRunning) {
        if (showPomodoroCompleteMessage) {
            completionScale.animateTo(1f, animationSpec = tween(600))
            delay(2000)
            showPomodoroCompleteMessage = false
            isBreakRunning = true
            breakTimeLeft = totalBreakDuration
        } else if (showBreakCompleteMessage) {
            completionScale.animateTo(1f, animationSpec = tween(600))
            delay(2000)
            showBreakCompleteMessage = false
            isPomodoroRunning = true
            timeLeft = totalPomodoroDuration
        } else if (isBreakRunning) {
            completionScale.animateTo(1f, animationSpec = tween(600))
        }
    }

    LaunchedEffect(isPomodoroRunning, isBreakRunning) {
        if (isPomodoroRunning || isBreakRunning) {
            timer?.cancel()
            val currentTime = if (isPomodoroRunning) timeLeft else breakTimeLeft
            val totalDuration = if (isPomodoroRunning) totalPomodoroDuration else totalBreakDuration
            timer = object : CountDownTimer(currentTime, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    if (isPomodoroRunning) timeLeft = millisUntilFinished
                    else breakTimeLeft = millisUntilFinished
                    coroutineScope.launch {
                        progress.animateTo(millisUntilFinished.toFloat() / totalDuration)
                    }
                }

                override fun onFinish() {
                    if (isPomodoroRunning) {
                        isPomodoroRunning = false
                        showPomodoroCompleteMessage = true
                    } else if (isBreakRunning) {
                        isBreakRunning = false
                        showBreakCompleteMessage = true
                    }
                }
            }.start()
        } else {
            timer?.cancel()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A))
                )
            )
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            // Wave background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val waveAmplitude = 20.dp.toPx()
                val waveFrequency = 0.02f
                for (i in 0..size.height.toInt() step 25) {
                    drawLine(
                        start = Offset(0f, i.toFloat() + waveAmplitude * sin(waveOffset + i * waveFrequency)),
                        end = Offset(size.width, i.toFloat() + waveAmplitude * sin(waveOffset + i * waveFrequency)),
                        color = Color(0xFFFFA31A).copy(alpha = 0.15f),
                        strokeWidth = 4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B).copy(alpha = 0.97f)),
                    elevation = CardDefaults.cardElevation(12.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when {
                                showPomodoroCompleteMessage -> "Pomodoro Complete"
                                showBreakCompleteMessage -> "Break Complete"
                                isBreakRunning -> "Break Time"
                                else -> "Pomodoro Timer"
                            },
                            fontSize = 34.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isBreakRunning || showBreakCompleteMessage -> Color(0xFF4CAF50)
                                else -> Color(0xFFFFA31A)
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (!isPomodoroRunning && !isBreakRunning && !showPomodoroCompleteMessage && !showBreakCompleteMessage) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = customPomodoroMinutes,
                                        onValueChange = { newValue ->
                                            customPomodoroMinutes = newValue.filter { it.isDigit() }.take(3)
                                            val minutes = customPomodoroMinutes.toLongOrNull() ?: 0L
                                            val seconds = customPomodoroSeconds.toLongOrNull() ?: 0L
                                            val newDuration = (minutes * 60 + seconds) * 1000L
                                            timeLeft = newDuration
                                            totalPomodoroDuration = newDuration
                                        },
                                        label = { Text("Pomodoro Min", color = Color(0xFF808080)) },
                                        modifier = Modifier
                                            .width(110.dp)
                                            .padding(end = 8.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = Color(0xFFFFFFFF),
                                            fontSize = 16.sp
                                        ),
                                        singleLine = true,
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = Color(0xFFFFA31A),
                                            unfocusedBorderColor = Color(0xFF808080),
                                            cursorColor = Color(0xFFFFA31A)
                                        )
                                    )
                                    Text(
                                        text = "min",
                                        fontSize = 16.sp,
                                        color = Color(0xFFFFFFFF),
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    OutlinedTextField(
                                        value = customPomodoroSeconds,
                                        onValueChange = { newValue ->
                                            customPomodoroSeconds = newValue.filter { it.isDigit() }.take(2)
                                            val seconds = (customPomodoroSeconds.toLongOrNull() ?: 0L).coerceAtMost(59)
                                            val minutes = customPomodoroMinutes.toLongOrNull() ?: 0L
                                            val newDuration = (minutes * 60 + seconds) * 1000L
                                            timeLeft = newDuration
                                            totalPomodoroDuration = newDuration
                                        },
                                        label = { Text("Sec", color = Color(0xFF808080)) },
                                        modifier = Modifier.width(90.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = Color(0xFFFFFFFF),
                                            fontSize = 16.sp
                                        ),
                                        singleLine = true,
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = Color(0xFFFFA31A),
                                            unfocusedBorderColor = Color(0xFF808080),
                                            cursorColor = Color(0xFFFFA31A)
                                        )
                                    )
                                    Text(
                                        text = "sec",
                                        fontSize = 16.sp,
                                        color = Color(0xFFFFFFFF)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = customBreakMinutes,
                                        onValueChange = { newValue ->
                                            customBreakMinutes = newValue.filter { it.isDigit() }.take(3)
                                            val minutes = customBreakMinutes.toLongOrNull() ?: 0L
                                            val seconds = customBreakSeconds.toLongOrNull() ?: 0L
                                            val newDuration = (minutes * 60 + seconds) * 1000L
                                            breakTimeLeft = newDuration
                                            totalBreakDuration = newDuration
                                        },
                                        label = { Text("Break Min", color = Color(0xFF808080)) },
                                        modifier = Modifier
                                            .width(110.dp)
                                            .padding(end = 8.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = Color(0xFFFFFFFF),
                                            fontSize = 16.sp
                                        ),
                                        singleLine = true,
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = Color(0xFF4CAF50),
                                            unfocusedBorderColor = Color(0xFF808080),
                                            cursorColor = Color(0xFF4CAF50)
                                        )
                                    )
                                    Text(
                                        text = "min",
                                        fontSize = 16.sp,
                                        color = Color(0xFFFFFFFF),
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                    OutlinedTextField(
                                        value = customBreakSeconds,
                                        onValueChange = { newValue ->
                                            customBreakSeconds = newValue.filter { it.isDigit() }.take(2)
                                            val seconds = (customBreakSeconds.toLongOrNull() ?: 0L).coerceAtMost(59)
                                            val minutes = customBreakMinutes.toLongOrNull() ?: 0L
                                            val newDuration = (minutes * 60 + seconds) * 1000L
                                            breakTimeLeft = newDuration
                                            totalBreakDuration = newDuration
                                        },
                                        label = { Text("Sec", color = Color(0xFF808080)) },
                                        modifier = Modifier.width(90.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            color = Color(0xFFFFFFFF),
                                            fontSize = 16.sp
                                        ),
                                        singleLine = true,
                                        colors = TextFieldDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = Color(0xFF4CAF50),
                                            unfocusedBorderColor = Color(0xFF808080),
                                            cursorColor = Color(0xFF4CAF50)
                                        )
                                    )
                                    Text(
                                        text = "sec",
                                        fontSize = 16.sp,
                                        color = Color(0xFFFFFFFF)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        if (showPomodoroCompleteMessage) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.scale(completionScale.value)
                            ) {
                                Text(
                                    text = "Well Done!",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFA31A)
                                )
                                Text(
                                    text = "Your break starts soon...",
                                    fontSize = 22.sp,
                                    color = Color(0xFFFFFFFF)
                                )
                            }
                        } else if (showBreakCompleteMessage) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.scale(completionScale.value)
                            ) {
                                Text(
                                    text = "Break Over!",
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4CAF50)
                                )
                                Text(
                                    text = "Back to work soon...",
                                    fontSize = 22.sp,
                                    color = Color(0xFFFFFFFF)
                                )
                            }
                        } else if (!isPomodoroRunning && !isBreakRunning) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Ready to Focus?",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFFFFF)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = formatTime(timeLeft),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFFFFF)
                                )
                            }
                        } else {
                            Box(contentAlignment = Alignment.Center) {
                                Canvas(
                                    modifier = Modifier
                                        .size(280.dp)
                                        .scale(if (isPomodoroRunning || isBreakRunning) pulseScale else 1f)
                                ) {
                                    drawArc(
                                        color = Color(0xFF808080).copy(alpha = 0.25f),
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    drawArc(
                                        brush = Brush.linearGradient(
                                            colors = if (isBreakRunning) listOf(Color(0xFF4CAF50), Color(0xFF81C784))
                                            else listOf(Color(0xFFFFA31A), Color(0xFFFFC107)),
                                            start = Offset.Zero,
                                            end = Offset.Infinite
                                        ),
                                        startAngle = -90f,
                                        sweepAngle = progress.value * 360f,
                                        useCenter = false,
                                        style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                                Text(
                                    text = formatTime(if (isPomodoroRunning) timeLeft else breakTimeLeft),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFFFFF)
                                )
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
                ) {
                    AnimatedButton(
                        text = "Start",
                        onClick = {
                            if (!isPomodoroRunning && !isBreakRunning && !showPomodoroCompleteMessage && !showBreakCompleteMessage) {
                                isPomodoroRunning = true
                            }
                        },
                        enabled = !isPomodoroRunning && !isBreakRunning && !showPomodoroCompleteMessage && !showBreakCompleteMessage,
                        containerColor = if (isBreakRunning) Color(0xFF4CAF50) else Color(0xFFFFA31A),
                        contentColor = Color(0xFFFFFFFF),
                        coroutineScope = coroutineScope
                    )

                    AnimatedButton(
                        text = "Reset",
                        onClick = {
                            isPomodoroRunning = false
                            isBreakRunning = false
                            showPomodoroCompleteMessage = false
                            showBreakCompleteMessage = false
                            val pomodoroMinutes = customPomodoroMinutes.toLongOrNull() ?: 25L
                            val pomodoroSeconds = customPomodoroSeconds.toLongOrNull() ?: 0L
                            val breakMinutes = customBreakMinutes.toLongOrNull() ?: 5L
                            val breakSeconds = customBreakSeconds.toLongOrNull() ?: 0L
                            totalPomodoroDuration = (pomodoroMinutes * 60 + pomodoroSeconds) * 1000L
                            totalBreakDuration = (breakMinutes * 60 + breakSeconds) * 1000L
                            timeLeft = totalPomodoroDuration
                            breakTimeLeft = totalBreakDuration
                            timer?.cancel()
                            coroutineScope.launch {
                                progress.snapTo(1f)
                            }
                        },
                        enabled = true,
                        containerColor = Color(0xFF808080),
                        contentColor = Color(0xFFFFFFFF),
                        coroutineScope = coroutineScope
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
    coroutineScope: CoroutineScope
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
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .scale(scale.value)
            .height(56.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Custom click handling in onClick */ }
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

fun formatTime(millis: Long): String {
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    return "%02d:%02d".format(minutes, seconds)
}
