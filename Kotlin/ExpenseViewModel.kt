package com.example.myacademate

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class Expense(
    val name: String,
    val amount: Double,
    val completionPercentage: Int = 0
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

    fun updateExpenseCompletion(expense: Expense, newCompletionPercentage: Int) {
        val index = _expenseList.value.indexOfFirst { it.name == expense.name && it.amount == expense.amount }
        if (index != -1) {
            val updatedExpense = expense.copy(completionPercentage = newCompletionPercentage)
            _expenseList.value[index] = updatedExpense
        }
    }

    // New method to update an entire expense
    fun updateExpense(oldExpense: Expense, newName: String, newAmount: Double, newCompletionPercentage: Int) {
        val index = _expenseList.value.indexOfFirst { it.name == oldExpense.name && it.amount == oldExpense.amount }
        if (index != -1) {
            val updatedExpense = Expense(newName, newAmount, newCompletionPercentage)
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

    class ExpenseViewModelFactory(private val application: Application) : androidx.lifecycle.ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ExpenseViewModel.getInstance(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

