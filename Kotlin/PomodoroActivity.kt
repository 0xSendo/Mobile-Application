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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PomodoroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PomodoroScreen()
        }
    }
}

@Composable
fun PomodoroScreen() {
    var timeLeft by remember { mutableStateOf(25 * 60 * 1000L) } // 25 minutes
    var isRunning by remember { mutableStateOf(false) }
    var timer: CountDownTimer? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()
    val totalDuration = 25 * 60 * 1000L
    val progress = remember { Animatable(1f) }
    val context = LocalContext.current

    // Pulsing animation for the timer circle
    val pulseScale by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Wave animation for background
    val waveOffset by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    LaunchedEffect(isRunning) {
        if (isRunning) {
            timer = object : CountDownTimer(timeLeft, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeft = millisUntilFinished
                    coroutineScope.launch {
                        progress.animateTo(millisUntilFinished.toFloat() / totalDuration)
                    }
                }

                override fun onFinish() {
                    isRunning = false
                }
            }.start()
        } else {
            timer?.cancel()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF292929)) // background: Dark Gray #292929
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            // Subtle wave background animation
            Canvas(modifier = Modifier.fillMaxSize()) {
                val waveAmplitude = 10.dp.toPx()
                val waveFrequency = 0.01f
                for (i in 0..size.height.toInt() step 20) {
                    drawLine(
                        start = Offset(0f, i.toFloat() + waveAmplitude * kotlin.math.sin(waveOffset + i * waveFrequency)),
                        end = Offset(size.width, i.toFloat() + waveAmplitude * kotlin.math.sin(waveOffset + i * waveFrequency)),
                        color = Color(0xFF1B1B1B).copy(alpha = 0.2f), // surface: Darker Gray #1b1b1b with transparency
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pomodoro Timer",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFA31A) // primary: Orange #ffa31a
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Circular Countdown Timer with pulsing effect
                Box(contentAlignment = Alignment.Center) {
                    Canvas(
                        modifier = Modifier
                            .size(220.dp)
                            .padding(16.dp)
                            .scale(if (isRunning) pulseScale else 1f)
                    ) {
                        drawArc(
                            color = Color(0xFFFFA31A), // primary: Orange #ffa31a
                            startAngle = -90f,
                            sweepAngle = progress.value * 360f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // Background arc for contrast
                        drawArc(
                            color = Color(0xFF808080), // secondary: Gray #808080
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = formatTime(timeLeft),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFFFFF) // onSurface: White #ffffff
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Control Buttons with scale animation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    AnimatedButton(
                        text = "Start",
                        onClick = { isRunning = true },
                        enabled = !isRunning,
                        containerColor = Color(0xFFFFA31A), // primary: Orange #ffa31a
                        contentColor = Color(0xFFFFFFFF), // onPrimary: White #ffffff
                        coroutineScope = coroutineScope
                    )

                    AnimatedButton(
                        text = "Reset",
                        onClick = {
                            isRunning = false
                            timeLeft = totalDuration
                            timer?.cancel()
                            coroutineScope.launch {
                                progress.snapTo(1f)
                            }
                        },
                        enabled = true,
                        containerColor = Color(0xFF808080), // secondary: Gray #808080
                        contentColor = Color(0xFFFFFFFF), // onSecondary: White #ffffff
                        coroutineScope = coroutineScope
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
                    context.startActivity(intent)
                },
                NavigationItem("Tasks", R.drawable.ic_tasks) {
                    val intent = Intent(context, TaskManagerActivity::class.java)
                    context.startActivity(intent)
                },
                NavigationItem("Progress", R.drawable.ic_progress) {
                    val intent = Intent(context, ProgressTrackerActivity::class.java)
                    context.startActivity(intent)
                },
                NavigationItem("Pomodoro", R.drawable.ic_pomodoro) { /* Current screen, no action */ },
                NavigationItem("Expense", R.drawable.ic_calendar) {
                    val intent = Intent(context, ExpenseActivity::class.java)
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
                scale.animateTo(0.9f, animationSpec = tween(100))
                scale.animateTo(1f, animationSpec = tween(100))
            }
        },
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        modifier = Modifier
            .scale(scale.value)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Custom click handling already in onClick */ }
    ) {
        Text(text)
    }
}



fun formatTime(millis: Long): String {
    val minutes = (millis / 1000) / 60
    val seconds = (millis / 1000) % 60
    return "%02d:%02d".format(minutes, seconds)
}
