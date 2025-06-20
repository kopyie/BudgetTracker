package com.example.budgettracker

import ads_mobile_sdk.h6
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import com.example.budgettracker.ui.theme.BudgetTrackerTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

import androidx.compose.ui.viewinterop.AndroidView

import java.util.Random

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.fromComponent
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.chart.line.LineChart as VicoLineChart // Alias to avoid name clash
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.ChartEntryModel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgettracker.Transaction // Your transaction data class
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData


class MainActivity : ComponentActivity() {
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BudgetTrackerApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun BudgetTrackerApp(viewModel: ExpenseViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val balance by viewModel.totalBalance.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(String.format("Balance: $%.2f", balance)) },
            backgroundColor = colors.primary
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> TransactionList(viewModel)
                1 -> AddTransactionForm(viewModel) { selectedTab = 0 } // Navigate back on success
                2 -> ReportView(viewModel)
            }
        }

        BottomNavigation {
            BottomNavigationItem(
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Transactions") },
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                label = { Text("Transactions") }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                label = { Text("Add") }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Default.PieChart, contentDescription = "Report") },
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                label = { Text("Report") }
            )
        }
    }
}


@Composable
fun TransactionList(viewModel: ExpenseViewModel) {
    val transactions by viewModel.transactions.collectAsState()

    if (transactions.isEmpty()){
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Text("No transactions yet. Add one!")
        }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(transactions) { txn ->
                Card(
                    elevation = 4.dp,
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = txn.category, style = MaterialTheme.typography.h6)
                            Text(
                                text = String.format("$%.2f", txn.amount),
                                fontWeight = FontWeight.Bold,
                                color = if (txn.type == "Expense") Color.Red else Color(0xFF008000) // Dark Green
                            )
                        }

                        if (txn.note.isNotBlank()) {
                            Text(text = txn.note, style = MaterialTheme.typography.body2, color = Color.Gray)
                        }

                        Text(text = txn.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
    }
}



@Composable
fun AddTransactionForm(viewModel: ExpenseViewModel, onTransactionAdded: () -> Unit) {
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") }
    var category by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }

    val context = LocalContext.current
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val expenseCategories = listOf("Food", "Transportation", "Bill", "Medical", "Education", "Other")
    val incomeCategories = listOf("Salary", "Gift", "Other Income")

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = LocalDate.of(year, month + 1, dayOfMonth)
        },
        date.year,
        date.monthValue - 1,
        date.dayOfMonth
    )

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it.filter { char -> char.isDigit() || char == '.' } },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth()
        )

        ClassicDropdownMenuBox(
            label = "Type",
            options = listOf("Expense", "Income"),
            selectedOption = type,
            onOptionSelected = {
                type = it
                category = "" // Reset category when type changes
            }
        )

        ClassicDropdownMenuBox(
            label = "Category",
            options = if (type == "Income") incomeCategories else expenseCategories,
            selectedOption = category,
            onOptionSelected = { category = it }
        )


        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = date.format(formatter),
            onValueChange = {},
            label = { Text("Date") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { datePickerDialog.show() }
        )

        Button(
            onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (amt > 0 && category.isNotEmpty()) {
                    // Store expense amounts as negative for easy calculation, but pass positive to ViewModel
                    val finalAmount = if (type == "Expense") -amt else amt
                    viewModel.addTransaction(finalAmount, type, category, note, date)
                    onTransactionAdded() // Callback to navigate
                }
            },
            modifier = Modifier.align(Alignment.End),
            enabled = amountText.isNotBlank() && category.isNotBlank()
        ) {
            Text("Add Transaction")
        }
    }
}
@Composable
fun ClassicDropdownMenuBox(
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Use a Box to allow the dropdown to overlap other elements
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onOptionSelected(option)
                    expanded = false
                }) {
                    Text(text = option)
                }
            }
        }
    }
}

@Composable
fun BalanceLineChart(transactions: List<Transaction>) {
    // 1. Prepare the data for the chart (your logic was perfect)
    val balanceData = remember(transactions) {
        transactions
            .sortedBy { it.date }
            // Calculate running total
            .scan(0.0) { acc, transaction -> acc + transaction.amount }
            .drop(1) // Drop the initial 0.0
            .map { it.toFloat() } // We just need the numbers
    }

    if (balanceData.size < 2) {
        Text("Not enough data for a line chart.")
        return
    }

    // 2. Create the ChartEntryModel for Vico
    val chartModel: ChartEntryModel = entryModelOf(*balanceData.toTypedArray())

    // 3. Display the chart using Vico's native composable
    Chart(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        chart = lineChart(
            // Set the line color using the theme
            lines = listOf(
                VicoLineChart.LineSpec(
                    lineColor = MaterialTheme.colorScheme.primary.hashCode()
                )
            ),
        ),
        model = chartModel,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(),
    )
}

@Composable
fun ReportView(viewModel: ExpenseViewModel) {
    val transactions by viewModel.transactions.collectAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Balance Over Time", style = MaterialTheme.typography.h6)
        BalanceLineChart(transactions = transactions)

        Text("Expenses by Category", style = MaterialTheme.typography.h6)
        // This now calls the new MPAndroidChart Pie Chart
        ExpensePieChart(transactions = transactions)
    }
}

@Composable
fun ExpensePieChart(transactions: List<Transaction>) {
    // 1. Prepare the data (your logic is perfect)
    val expenseData = remember(transactions) {
        transactions
            .filter { it.type == "Expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { kotlin.math.abs(it.amount) } }
    }

    if (expenseData.isEmpty()) {
        Text("No expense data for pie chart.")
        return
    }

    // A list of nice colors for the chart slices
    val colors = listOf(
        Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFFC107),
        Color(0xFFF44336), Color(0xFF9C27B0), Color(0xFFE91E63),
        Color(0xFFFF9800), Color(0xFF795548)
    )

    // 2. Create the data model required by YCharts
    val pieChartData = PieChartData(
        slices = expenseData.entries.mapIndexed { index, entry ->
            PieChartData.Slice(
                label = entry.key,
                value = entry.value.toFloat(),
                color = colors.getOrElse(index) { Color.Gray }
            )
        },
        plotType = PlotType.Pie
    )

    // 3. Configure the chart's appearance
    val pieChartConfig = PieChartConfig(
        isAnimationEnable = true,
        showSliceLabels = true,
        sliceLabelTextSize = 14f,
        labelVisible = true,
        labelColor = Color.Black
    )

    // 4. Display the YCharts PieChart
    Column(modifier = Modifier.fillMaxWidth()) {
        PieChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            pieChartData = pieChartData,
            pieChartConfig = pieChartConfig
        )
    }
}

@Composable
fun ChartLegend(data: List<PieChartData>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        data.forEach { item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .padding(end = 4.dp)
                        .background(item.color) // Use background for Spacer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = item.label, fontSize = 14.sp)
            }
        }
    }
}

// Custom function to ensure 'abs' works correctly
private fun abs(d: Double): Double = if (d < 0) -d else d