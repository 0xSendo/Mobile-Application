package com.example.myacademate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Task(
    val id: Int,
    val subjectName: String,
    val courseCode: String,
    val dueTime: String,
    isDone: Boolean = false,
    completionPercentage: Int = 0
) {
    var isDone by mutableStateOf(isDone)
    var completionPercentage by mutableStateOf(completionPercentage)
}


object TaskRepository {
    val taskList = mutableStateListOf<DatabaseHelper.Task>()

    fun addTask(task: DatabaseHelper.Task) {
        taskList.add(task)
    }


    fun updateTask(updatedTask: DatabaseHelper.Task) {
        val index = taskList.indexOfFirst { it.id == updatedTask.id }
        if (index != -1) {
            taskList[index] = updatedTask
        }
    }

    fun deleteTask(taskId: Int) {
        taskList.removeAll { it.id == taskId }
    }

    fun toggleTaskStatus(taskId: Int) {
        val index = taskList.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val task = taskList[index]
            taskList[index] = task.copy(isDone = !task.isDone)
        }
    }

    fun updateTaskCompletion(taskId: Int, newCompletionPercentage: Int) {
        val index = taskList.indexOfFirst { it.id == taskId }
        if (index != null) {
            val task = taskList[index]
            taskList[index] = task.copy(completionPercentage = newCompletionPercentage)
        }
    }

    fun getTasks(): List<DatabaseHelper.Task> {
        return taskList
    }
}
