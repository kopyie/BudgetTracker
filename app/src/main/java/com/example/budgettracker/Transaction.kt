package com.example.budgettracker

import java.time.LocalDate

data class Transaction(
    val id: Int = 0,
    val amount: Double,
    val type: String, // "Income" or "Expense"
    val category: String,
    val note: String = "",
    val date: LocalDate
)
