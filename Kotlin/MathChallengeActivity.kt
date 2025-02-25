package com.example.myacademate

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myacademate.ui.theme.MyAcademateTheme

class MathChallengeActivity : ComponentActivity() {
    private val gameViewModel: MathGameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929)
                ) {
                    MathChallengeScreen(gameViewModel, username) { finish() }
                }
            }
        }
    }
}

@Composable
fun MathChallengeScreen(viewModel: MathGameViewModel, username: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Math Challenge",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFFFFFFFF),
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Score: ${viewModel.score.value}",
                color = Color(0xFFFFA31A),
                fontSize = 20.sp
            )
            Text(
                text = "Time: ${viewModel.timeLeft.value}s",
                color = Color(0xFFFFFFFF),
                fontSize = 20.sp
            )
        }
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = viewModel.problem.value.question,
            color = Color(0xFFFFFFFF),
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        viewModel.problem.value.options.forEach { option ->
            Button(
                onClick = { viewModel.checkAnswer(option) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF808080),
                    contentColor = Color(0xFFFFFFFF)
                ),
                enabled = viewModel.isGameRunning.value
            ) {
                Text(text = option.toString(), fontSize = 18.sp)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (viewModel.isGameRunning.value) viewModel.resetGame()
                else viewModel.startGame()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFA31A),
                contentColor = Color(0xFFFFFFFF)
            )
        ) {
            Text(text = if (viewModel.isGameRunning.value) "Reset" else "Start")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF808080),
                contentColor = Color(0xFFFFFFFF)
            )
        ) {
            Text(text = "Back")
        }
    }
}
