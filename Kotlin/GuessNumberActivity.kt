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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import com.example.myacademate.ui.theme.MyAcademateTheme
import kotlin.random.Random

class GuessNumberActivity : ComponentActivity() {
    private val guessViewModel: GuessNumberViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("USERNAME") ?: ""
        setContent {
            MyAcademateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF292929)
                ) {
                    GuessNumberScreen(guessViewModel, username) { finish() }
                }
            }
        }
    }
}

class GuessNumberViewModel : ViewModel() {
    private val targetNumber = Random.nextInt(1, 101) // Random number between 1 and 100
    var guess = mutableStateOf("")
    var hint = mutableStateOf("Guess a number between 1 and 100!")
    var attempts = mutableStateOf(0)
    var gameWon = mutableStateOf(false)

    fun checkGuess() {
        val guessNumber = guess.value.toIntOrNull()
        if (guessNumber != null && !gameWon.value) {
            attempts.value++
            when {
                guessNumber < targetNumber -> hint.value = "Too low! Try again."
                guessNumber > targetNumber -> hint.value = "Too high! Try again."
                else -> {
                    hint.value = "Correct! You won in ${attempts.value} attempts!"
                    gameWon.value = true
                }
            }
            guess.value = "" // Clear input after each guess
        } else if (gameWon.value) {
            hint.value = "Game over! Start a new game."
        } else {
            hint.value = "Please enter a valid number!"
        }
    }

    fun resetGame() {
        guess.value = ""
        hint.value = "Guess a number between 1 and 100!"
        attempts.value = 0
        gameWon.value = false
        // Note: targetNumber remains constant unless we create a new instance of ViewModel
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuessNumberScreen(viewModel: GuessNumberViewModel, username: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Guess the Number",
            style = androidx.compose.material3.Typography().titleLarge,
            color = Color(0xFFFFFFFF),
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = viewModel.hint.value,
            style = androidx.compose.material3.Typography().bodyLarge,
            color = if (viewModel.gameWon.value) Color(0xFFFFA31A) else Color(0xFFFFFFFF),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.guess.value,
            onValueChange = { viewModel.guess.value = it },
            label = { Text("Enter your guess", color = Color(0xFFFFFFFF)) },
            modifier = Modifier.fillMaxWidth(0.8f),
            singleLine = true,
            enabled = !viewModel.gameWon.value,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF808080),
                unfocusedBorderColor = Color(0xFFFFFFFF),
                cursorColor = Color(0xFFFFFFFF),
                focusedLabelColor = Color(0xFF808080),
                unfocusedLabelColor = Color(0xFFFFFFFF),
                disabledBorderColor = Color(0xFF808080),
                disabledTextColor = Color(0xFFFFFFFF),
                disabledLabelColor = Color(0xFFFFFFFF)
            ),
            textStyle = androidx.compose.material3.Typography().bodyLarge.copy(color = Color(0xFFFFFFFF))
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { viewModel.checkGuess() },
                enabled = !viewModel.gameWon.value,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA31A), contentColor = Color(0xFFFFFFFF))
            ) {
                Text("Guess")
            }

            Button(
                onClick = { viewModel.resetGame() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF808080), contentColor = Color(0xFFFFFFFF))
            ) {
                Text("Reset")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF808080), contentColor = Color(0xFFFFFFFF))
        ) {
            Text("Back")
        }
    }
}
