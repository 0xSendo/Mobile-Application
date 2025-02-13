// TaskViewModel.kt

package com.example.myacademate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TaskViewModel : ViewModel() {
    val taskList = TaskRepository.taskList
    var editingTaskId by mutableStateOf<Int?>(null)
        private set

    fun addTask(task: DatabaseHelper.Task) {
        TaskRepository.addTask(task)
    }

    fun updateTask(updatedTask: DatabaseHelper.Task) {
        TaskRepository.updateTask(updatedTask)
    }

    fun deleteTask(taskId: Int) {
        TaskRepository.deleteTask(taskId)
    }

    fun toggleTaskStatus(taskId: Int) {
        TaskRepository.toggleTaskStatus(taskId)
    }

    fun updateTaskCompletion(taskId: Int, newCompletionPercentage: Int) {
        TaskRepository.updateTaskCompletion(taskId, newCompletionPercentage)
    }

    fun startEditingTask(taskId: Int) {
        editingTaskId = taskId
    }

    fun clearEditingTask() {
        editingTaskId = null
    }
}
