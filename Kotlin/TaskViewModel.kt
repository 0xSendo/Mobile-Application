package com.example.myacademate

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

// Data class for representing a Task
data class Task(
    val id: Int,
    var subjectName: String,
    var courseCode: String,
    var dueTime: String,
    var isDone: Boolean = false
)

class TaskViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    // Mutable state to hold the list of tasks
    private val _taskList = mutableStateOf(savedStateHandle.get<List<Task>>("taskList") ?: emptyList())
    val taskList: List<Task>
        get() = _taskList.value

    // ID for the task being edited
    private val _editingTaskId = mutableStateOf<Int?>(null)
    val editingTaskId: Int?
        get() = _editingTaskId.value

    // Add a new task
    fun addTask(task: Task) {
        _taskList.value = _taskList.value + task
        savedStateHandle["taskList"] = _taskList.value
    }

    // Update an existing task
    fun updateTask(updatedTask: Task) {
        _taskList.value = _taskList.value.map {
            if (it.id == updatedTask.id) updatedTask else it
        }
        savedStateHandle["taskList"] = _taskList.value
    }

    // Delete a task by ID
    fun deleteTask(taskId: Int) {
        _taskList.value = _taskList.value.filter { it.id != taskId }
        savedStateHandle["taskList"] = _taskList.value
    }

    // Toggle task completion status
    fun toggleTaskStatus(taskId: Int) {
        _taskList.value = _taskList.value.map {
            if (it.id == taskId) it.copy(isDone = !it.isDone) else it
        }
        savedStateHandle["taskList"] = _taskList.value
    }

    // Start editing a task
    fun startEditingTask(taskId: Int) {
        _editingTaskId.value = taskId
    }

    // Clear editing state (when done editing or saving a task)
    fun clearEditingTask() {
        _editingTaskId.value = null
    }
}