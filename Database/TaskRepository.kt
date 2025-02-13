// TaskRepository.kt
package com.example.myacademate

import androidx.compose.runtime.mutableStateListOf

data class Task(
    val id: Int,
    val subjectName: String,
    val courseCode: String,
    val dueTime: String,
    val isDone: Boolean = false
)

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

    fun getTasks(): List<DatabaseHelper.Task> {
        return taskList
    }
}
