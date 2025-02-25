package com.example.myacademate

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MathGameViewModel : ViewModel() {
    var score = mutableStateOf(0)
    var timeLeft = mutableStateOf(60)
    var problem = mutableStateOf(generateProblem())
    var isGameRunning = mutableStateOf(false)
    private var timerJob: Job? = null

    fun startGame() {
        if (!isGameRunning.value) {
            score.value = 0
            timeLeft.value = 60
            isGameRunning.value = true
            startTimer()
            problem.value = generateProblem()
        }
    }

    fun resetGame() {
        timerJob?.cancel()
        score.value = 0
        timeLeft.value = 60
        isGameRunning.value = false
        problem.value = generateProblem()
    }

    fun checkAnswer(selectedAnswer: Int) {
        if (isGameRunning.value && selectedAnswer == problem.value.correctAnswer) {
            score.value += 10
            problem.value = generateProblem()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (timeLeft.value > 0) {
                delay(1000)
                timeLeft.value--
            }
            isGameRunning.value = false
        }
    }

    private fun generateProblem(): MathProblem {
        val num1 = Random.nextInt(1, 20)
        val num2 = Random.nextInt(1, 20)
        val operation = Random.nextInt(0, 3) // 0: +, 1: -, 2: *
        val correctAnswer = when (operation) {
            0 -> num1 + num2
            1 -> num1 - num2
            else -> num1 * num2
        }
        val options = mutableListOf(correctAnswer)
        while (options.size < 4) {
            val wrongAnswer = correctAnswer + Random.nextInt(-10, 11)
            if (wrongAnswer != correctAnswer && wrongAnswer >= 0 && !options.contains(wrongAnswer)) {
                options.add(wrongAnswer)
            }
        }
        options.shuffle()
        return MathProblem(
            question = when (operation) {
                0 -> "$num1 + $num2 = ?"
                1 -> "$num1 - $num2 = ?"
                else -> "$num1 * $num2 = ?"
            },
            correctAnswer = correctAnswer,
            options = options
        )
    }
}

data class MathProblem(
    val question: String,
    val correctAnswer: Int,
    val options: List<Int>
)
