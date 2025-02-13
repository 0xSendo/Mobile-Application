package com.example.myacademate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TaskViewModel : ViewModel() {
    // Live data for tasks
    val taskList = TaskRepository.taskList

    // Mutable state for editing task ID
    var editingTaskId by mutableStateOf<Int?>(null)
        private set

    // Add a new task
    fun addTask(task: DatabaseHelper.Task) {
        TaskRepository.addTask(task)
    }

    // Update an existing task
    fun updateTask(updatedTask: DatabaseHelper.Task) {
        TaskRepository.updateTask(updatedTask)
    }

    // Delete a task by ID
    fun deleteTask(taskId: Int) {
        TaskRepository.deleteTask(taskId)
    }

    // Toggle task completion status
    fun toggleTaskStatus(taskId: Int) {
        TaskRepository.toggleTaskStatus(taskId)
    }

    // Start editing a task
    fun startEditingTask(taskId: Int) {
        editingTaskId = taskId
    }

    // Clear editing state
    fun clearEditingTask() {
        editingTaskId = null
    }
}
