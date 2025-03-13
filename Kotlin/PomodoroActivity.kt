package com.example.myacademate

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PomodoroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        setContent {
            PomodoroScreen(username)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(username: String) {
    var timeLeft by remember { mutableLongStateOf(25 * 60 * 1000L) } // Pomodoro time
    var breakTimeLeft by remember { mutableLongStateOf(5 * 60 * 1000L) } // Break time
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
    var totalPomodoroDuration by remember { mutableLongStateOf(25 * 60 * 1000L) } // Track custom total
    var totalBreakDuration by remember { mutableLongStateOf(5 * 60 * 1000L) } // Track custom total
    val progress = remember { Animatable(1f) }
    val context = LocalContext.current

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
            delay(2000) // Show Pomodoro complete message for 2 seconds
            showPomodoroCompleteMessage = false
            isBreakRunning = true
            breakTimeLeft = totalBreakDuration // Use stored total break duration
        } else if (showBreakCompleteMessage) {
            completionScale.animateTo(1f, animationSpec = tween(600))
            delay(2000) // Show Break complete message for 2 seconds
            showBreakCompleteMessage = false
            isPomodoroRunning = true
            timeLeft = totalPomodoroDuration // Use stored total Pomodoro duration
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
            // Enhanced wave background
            Canvas(modifier = Modifier.fillMaxSize()) {
                val waveAmplitude = 20.dp.toPx()
                val waveFrequency = 0.02f
                for (i in 0..size.height.toInt() step 25) {
                    drawLine(
                        start = Offset(0f, i.toFloat() + waveAmplitude * kotlin.math.sin(waveOffset + i * waveFrequency)),
                        end = Offset(size.width, i.toFloat() + waveAmplitude * kotlin.math.sin(waveOffset + i * waveFrequency)),
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
                            },
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Custom time inputs
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
                                            totalPomodoroDuration = newDuration // Update total duration
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
                                            totalPomodoroDuration = newDuration // Update total duration
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
                                            totalBreakDuration = newDuration // Update total duration
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
                                            totalBreakDuration = newDuration // Update total duration
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

                        // Timer or Message Prompt
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

                // Control Buttons
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

        // Bottom Navigation with FilterChip
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1B1B)),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
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
                    NavigationItem("Pomodoro", R.drawable.ic_pomodoro) { /* Current screen */ },
                    NavigationItem("Expense", R.drawable.ic_calendar) {
                        val intent = Intent(context, ExpenseActivity::class.java)
                        intent.putExtra("USERNAME", username)
                        context.startActivity(intent)
                    }
                )

                navigationItems.forEach { item ->
                    FilterChip(
                        selected = false,
                        onClick = { item.action() },
                        label = {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.label,
                                modifier = Modifier.size(36.dp),
                                tint = Color(0xFFFFFFFF)
                            )
                        },
                        modifier = Modifier
                            .size(64.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF1B1B1B),
                            selectedContainerColor = Color(0xFF1B1B1B)
                        ),
                        shape = CircleShape,
                        border = null
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
