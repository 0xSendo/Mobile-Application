package com.example.myacademate

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val databaseHelper = DatabaseHelper(application)

    // Mutable state to hold the list of tasks
    private val _taskList = mutableStateOf<List<DatabaseHelper.Task>>(emptyList())
    val taskList: List<DatabaseHelper.Task>
        get() = _taskList.value

    // ID for the task being edited
    private val _editingTaskId = mutableStateOf<Int?>(null)
    val editingTaskId: Int?
        get() = _editingTaskId.value

    // Load tasks from the database
    fun loadTasks(username: String) {
        viewModelScope.launch {
            _taskList.value = databaseHelper.getUserTasks(username)
        }
    }

    // Add a new task
    fun addTask(username: String, task: DatabaseHelper.Task) {
        viewModelScope.launch {
            databaseHelper.addTask(username, task.subjectName, task.courseCode, task.dueTime)
            loadTasks(username) // Refresh the task list
        }
    }

    // Update an existing task
    fun updateTask(username: String, updatedTask: DatabaseHelper.Task) {
        viewModelScope.launch {
            databaseHelper.updateTask(updatedTask)
            loadTasks(username) // Refresh the task list
        }
    }

    // Delete a task by ID
    fun deleteTask(username: String, taskId: Int) {
        viewModelScope.launch {
            databaseHelper.deleteTask(taskId)
            loadTasks(username) // Refresh the task list
        }
    }

    // Toggle task completion status
    fun toggleTaskStatus(username: String, taskId: Int, isDone: Boolean) {
        viewModelScope.launch {
            databaseHelper.toggleTaskStatus(taskId, isDone)
            loadTasks(username) // Refresh the task list
        }
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
