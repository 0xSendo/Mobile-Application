package com.example.myacademate

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class TaskViewModel : ViewModel() {
    // Wrap TaskRepository.taskList in a reactive MutableState
    private val _taskList = mutableStateOf(TaskRepository.taskList)
    val taskList: List<DatabaseHelper.Task> by _taskList // Expose as reactive state

    var editingTaskId by mutableStateOf<Int?>(null)
        private set

    fun addTask(task: DatabaseHelper.Task) {
        TaskRepository.addTask(task)
        _taskList.value = TaskRepository.taskList
    }

    fun updateTask(updatedTask: DatabaseHelper.Task) {
        TaskRepository.updateTask(updatedTask)
        _taskList.value = TaskRepository.taskList // Update reactive state
    }

    fun deleteTask(taskId: Int) {
        TaskRepository.deleteTask(taskId)
        _taskList.value = TaskRepository.taskList // Update reactive state
    }

    fun toggleTaskStatus(taskId: Int) {
        TaskRepository.toggleTaskStatus(taskId)
        _taskList.value = TaskRepository.taskList // Update reactive state
    }

    fun updateTaskCompletion(taskId: Int, newCompletionPercentage: Int) {
        TaskRepository.updateTaskCompletion(taskId, newCompletionPercentage)
        _taskList.value = TaskRepository.taskList // Update reactive state
    }

    fun startEditingTask(taskId: Int) {
        editingTaskId = taskId
    }

    fun clearEditingTask() {
        editingTaskId = null
    }
}
