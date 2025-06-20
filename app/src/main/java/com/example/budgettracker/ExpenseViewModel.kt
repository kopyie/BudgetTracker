package com.example.budgettracker

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import java.time.LocalDate


class ExpenseViewModel : ViewModel() {
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions

    val totalBalance: StateFlow<Double> = _transactions.map { txns ->
        txns.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    fun addTransaction(
        amount: Double,
        type: String,
        category: String,
        note: String,
        date: LocalDate
    ) {
        val newTxn = Transaction(
            id = _transactions.value.size + 1,
            amount = amount,
            type = type,
            category = category,
            note = note,
            date = date
        )
        _transactions.value += newTxn
    }
}

