package com.example.myacademate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Expense(
    val name: String,
    val amount: Double
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val _expenseList = MutableStateFlow<List<Expense>>(emptyList())
    val expenseList: StateFlow<List<Expense>> get() = _expenseList

    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            _expenseList.value = _expenseList.value + expense
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            _expenseList.value = _expenseList.value.filter { it != expense }
        }
    }
}