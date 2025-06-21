package com.example.budgettracker

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
//import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ArrowDropDown
//import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.budgettracker.ui.theme.BudgetTrackerTheme
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.chart.line.LineChart as VicoLineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Locale

import androidx.compose.material.icons.filled.DateRange
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

// CHANGE: All imports from androidx.compose.material.* are now replaced with androidx.compose.material3.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.sp
import java.time.ZoneId
import androidx.compose.material3.MenuAnchorType

class MainActivity : ComponentActivity() {
    private val viewModel: ExpenseViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BudgetTrackerTheme {
                // CHANGE: Using M3 Surface. The 'color' is now set inside the Scaffold.
                Surface(modifier = Modifier.fillMaxSize()) {
                    BudgetTrackerApp(viewModel)
                }
            }
        }
    }
}

// CHANGE: This annotation allows us to use experimental M3 components like TopAppBar without warnings.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetTrackerApp(viewModel: ExpenseViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val balance by viewModel.totalBalance.collectAsState()

    // CHANGE: M3 uses a Scaffold to structure screens with top/bottom bars.
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(String.format(Locale.US,"Balance: $%.2f", balance)) },
                // CHANGE: Colors are handled differently in M3 TopAppBar
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            // CHANGE: BottomNavigation is now NavigationBar
            NavigationBar {
                // CHANGE: BottomNavigationItem is now NavigationBarItem
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Transactions") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Transactions") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Add") }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Report") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    label = { Text("Report") }
                )
            }
        }
    ) { innerPadding -> // Content of the screen goes here
        Box(
            modifier = Modifier
                .padding(innerPadding) // Apply padding from the Scaffold
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> TransactionList(viewModel)
                1 -> AddTransactionForm(viewModel) { selectedTab = 0 }
                2 -> ReportView(viewModel)
            }
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
                // CHANGE: Using M3 Card and its new elevation parameter
                Card(
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
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
                            // CHANGE: M2's 'h6' is now 'titleLarge' in M3
                            Text(text = txn.category, style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = String.format(Locale.US, "$%.2f", txn.amount),
                                fontWeight = FontWeight.Bold,
                                color = if (txn.type == "Expense") Color.Red else Color(0xFF008000) // Dark Green
                            )
                        }

                        if (txn.note.isNotBlank()) {
                            // CHANGE: M2's 'body2' is now 'bodyMedium' in M3
                            Text(text = txn.note, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                        // CHANGE: M2's 'caption' is now 'labelSmall' in M3
                        Text(text = txn.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

// CHANGE: This annotation allows us to use experimental M3 components like DropdownMenuBox without warnings.
@OptIn(ExperimentalMaterial3Api::class)
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
    calendar.time = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant())
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = LocalDate.of(year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
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

        // CHANGE: DropdownMenuBox is the new M3 component for this.
        ExposedDropdownMenuBox(
            expanded = type == "TypeExpanded",
            onExpandedChange = { expanded -> type = if (expanded) "TypeExpanded" else "" },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = type,
                onValueChange = {},
                readOnly = true,
                label = { Text("Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = type == "TypeExpanded") },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = type == "TypeExpanded",
                onDismissRequest = { type = if (type == "TypeExpanded") "" else type }
            ) {
                listOf("Expense", "Income").forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            type = selectionOption
                            category = "" // Reset category
                        }
                    )
                }
            }
        }

        var categoryExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = !categoryExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                (if (type == "Income") incomeCategories else expenseCategories).forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            category = selectionOption
                            categoryExpanded = false
                        }
                    )
                }
            }
        }


        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        // This interactionSource helps us detect taps reliably.
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed: Boolean by interactionSource.collectIsPressedAsState()

        // This effect will run whenever the field is pressed.
        LaunchedEffect(isPressed) {
            if (isPressed) {
                datePickerDialog.show()
            }
        }

        OutlinedTextField(
            value = date.format(formatter),
            onValueChange = {},
            readOnly = true, // Important: makes the field not editable by keyboard
            label = { Text("Date") },
            interactionSource = interactionSource, // We pass our interaction source here
            trailingIcon = {
                // We add a calendar icon for a better user experience
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date"
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val amt = amountText.toDoubleOrNull() ?: 0.0
                if (amt > 0 && category.isNotEmpty()) {
                    val finalAmount = if (type == "Expense") -amt else amt
                    viewModel.addTransaction(finalAmount, type, category, note, date)
                    onTransactionAdded()
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
fun BalanceLineChart(transactions: List<Transaction>) {
    val balanceData = remember(transactions) {
        transactions
            .sortedBy { it.date }
            .scan(0.0) { acc, transaction -> acc + transaction.amount }
            .drop(1)
            .map { it.toFloat() }
    }

    if (balanceData.size < 2) {
        Text("Not enough data for a line chart.")
        return
    }

    val chartModel: ChartEntryModel = entryModelOf(*balanceData.toTypedArray())

    Chart(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        chart = lineChart(
            lines = listOf(
                VicoLineChart.LineSpec(
                    // CHANGE: Using MaterialTheme.colorScheme for M3 colors
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
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // CHANGE: M2's 'h6' is now 'titleLarge' in M3
        Text("Balance Over Time", style = MaterialTheme.typography.titleLarge)
        BalanceLineChart(transactions = transactions)

        Text("Expenses by Category", style = MaterialTheme.typography.titleLarge)
        ExpensePieChart(transactions = transactions)
    }
}

@Composable
fun ExpensePieChart(transactions: List<Transaction>) {
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

    val colors = listOf(
        Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFFC107),
        Color(0xFFF44336), Color(0xFF9C27B0), Color(0xFFE91E63),
        Color(0xFFFF9800), Color(0xFF795548)
    )

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

    val pieChartConfig = PieChartConfig(
        isAnimationEnable = true,
        showSliceLabels = true,
        sliceLabelTextSize = 14.sp,
        labelVisible = true,
        // CHANGE: Using M3 color scheme
        labelColor = MaterialTheme.colorScheme.onSurface
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        PieChart(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            pieChartData = pieChartData,
            pieChartConfig = pieChartConfig
        )
    }
}