package com.example.myacademate

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Expense(
    val name: String,
    val amount: Double,
    val completionPercentage: Int = 0 // New field for progress
)



class ExpenseViewModel private constructor() : ViewModel() {
    private val _expenseList = MutableStateFlow(mutableStateListOf<Expense>())
    val expenseList: StateFlow<List<Expense>> = _expenseList

    fun addExpense(expense: Expense) {
        _expenseList.value.add(expense)
    }

    fun deleteExpense(expense: Expense) {
        _expenseList.value.remove(expense)
    }

    // New method to update completion percentage
    fun updateExpenseCompletion(expense: Expense, newCompletionPercentage: Int) {
        val index = _expenseList.value.indexOfFirst { it.name == expense.name && it.amount == expense.amount }
        if (index != -1) {
            val updatedExpense = expense.copy(completionPercentage = newCompletionPercentage)
            _expenseList.value[index] = updatedExpense
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ExpenseViewModel? = null

        fun getInstance(application: Application): ExpenseViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: ExpenseViewModel().also { INSTANCE = it }
            }
        }
    }
}
