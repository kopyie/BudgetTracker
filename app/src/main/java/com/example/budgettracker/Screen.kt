package com.example.budgettracker

sealed class Screen(val route: String) {
    object Transactions : Screen("transactions")
    object Add : Screen("add")
    object Report : Screen("report")
}
