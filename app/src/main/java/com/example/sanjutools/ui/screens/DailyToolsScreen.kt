package com.example.sanjutools.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanjutools.model.ToolItem
import com.example.sanjutools.ui.components.ToolScaffold
import com.example.sanjutools.viewmodel.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DailyToolsScreen(
    tool: ToolItem,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Digital Clock State
    var currentTimeString by remember { mutableStateOf("") }
    var currentDateString by remember { mutableStateOf("") }

    // Stopwatch State
    var isStopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchTimeMs by remember { mutableStateOf(0L) }
    val stopwatchLaps = remember { mutableStateListOf<String>() }

    // Pomodoro & Countdown State
    var pomodoroMinutes by remember { mutableStateOf(25) }
    var pomodoroSeconds by remember { mutableStateOf(0) }
    var isPomodoroRunning by remember { mutableStateOf(false) }
    var pomodoroSessionsCount by remember { mutableStateOf(0) }

    // Random Name Picker & Decision
    var namesInput by remember { mutableStateOf("Alice\nBob\nCharlie\nDiana\nEvan") }
    var pickedWinnerName by remember { mutableStateOf<String?>(null) }
    var decisionQuestion by remember { mutableStateOf("Should I start a new project?") }
    var decisionAnswer by remember { mutableStateOf<String?>(null) }

    // Tasks & Expenses from Room
    val tasks by viewModel.tasks.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    var newTaskTitle by remember { mutableStateOf("") }
    var newExpenseTitle by remember { mutableStateOf("") }
    var newExpenseAmount by remember { mutableStateOf("") }
    var newExpenseCategory by remember { mutableStateOf("Food") }

    // Live clock ticker
    LaunchedEffect(Unit) {
        val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val dateFmt = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        while (true) {
            val now = Date()
            currentTimeString = timeFmt.format(now)
            currentDateString = dateFmt.format(now)
            delay(500)
        }
    }

    // Stopwatch ticker
    LaunchedEffect(isStopwatchRunning) {
        var start = System.currentTimeMillis() - stopwatchTimeMs
        while (isStopwatchRunning) {
            stopwatchTimeMs = System.currentTimeMillis() - start
            delay(30)
        }
    }

    // Pomodoro ticker
    LaunchedEffect(isPomodoroRunning) {
        while (isPomodoroRunning) {
            delay(1000)
            if (pomodoroSeconds > 0) {
                pomodoroSeconds--
            } else if (pomodoroMinutes > 0) {
                pomodoroMinutes--
                pomodoroSeconds = 59
            } else {
                isPomodoroRunning = false
                pomodoroSessionsCount++
                Toast.makeText(context, "Pomodoro session complete! Take a break.", Toast.LENGTH_LONG).show()
                pomodoroMinutes = 25
                pomodoroSeconds = 0
            }
        }
    }

    ToolScaffold(
        title = tool.title,
        categoryName = tool.category.title,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when (tool.id) {
                "daily_digital_clock" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(36.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentDateString,
                                style = MaterialTheme.typography.titleSmall,
                                color = BrandCyan
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currentTimeString,
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Live System Clock",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray
                            )
                        }
                    }
                }

                "daily_stopwatch" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val totalSec = stopwatchTimeMs / 1000
                            val mins = totalSec / 60
                            val secs = totalSec % 60
                            val millis = (stopwatchTimeMs % 1000) / 10

                            Text(
                                text = String.format("%02d:%02d.%02d", mins, secs, millis),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { isStopwatchRunning = !isStopwatchRunning },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isStopwatchRunning) BrandRose else BrandEmerald
                                    )
                                ) {
                                    Icon(if (isStopwatchRunning) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isStopwatchRunning) "Pause" else "Start")
                                }

                                OutlinedButton(
                                    onClick = {
                                        if (isStopwatchRunning) {
                                            stopwatchLaps.add(0, String.format("%02d:%02d.%02d", mins, secs, millis))
                                        } else {
                                            stopwatchTimeMs = 0
                                            stopwatchLaps.clear()
                                        }
                                    }
                                ) {
                                    Text(if (isStopwatchRunning) "Lap" else "Reset")
                                }
                            }

                            if (stopwatchLaps.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Laps Recorded:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    stopwatchLaps.take(5).forEachIndexed { index, lap ->
                                        Text("Lap ${stopwatchLaps.size - index}: $lap", fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }

                "daily_pomodoro", "daily_countdown" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Focus Timer",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = String.format("%02d:%02d", pomodoroMinutes, pomodoroSeconds),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = BrandPurple
                            )
                            Text("Sessions Completed: $pomodoroSessionsCount", style = MaterialTheme.typography.bodySmall, color = BrandEmerald)

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { isPomodoroRunning = !isPomodoroRunning },
                                    colors = ButtonDefaults.buttonColors(containerColor = if (isPomodoroRunning) BrandRose else BrandPurple)
                                ) {
                                    Text(if (isPomodoroRunning) "Pause" else "Start Focus")
                                }
                                OutlinedButton(
                                    onClick = {
                                        isPomodoroRunning = false
                                        pomodoroMinutes = 25
                                        pomodoroSeconds = 0
                                    }
                                ) {
                                    Text("Reset 25m")
                                }
                                OutlinedButton(
                                    onClick = {
                                        isPomodoroRunning = false
                                        pomodoroMinutes = 5
                                        pomodoroSeconds = 0
                                    }
                                ) {
                                    Text("Break 5m")
                                }
                            }
                        }
                    }
                }

                "daily_task_planner" -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Daily Tasks & Action Items", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newTaskTitle,
                                    onValueChange = { newTaskTitle = it },
                                    placeholder = { Text("Add a new task...") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(onClick = {
                                    if (newTaskTitle.isNotBlank()) {
                                        viewModel.addTask(newTaskTitle)
                                        newTaskTitle = ""
                                    }
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (tasks.isEmpty()) {
                                Text("No tasks logged yet. Add your priorities above!", style = MaterialTheme.typography.bodySmall)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    tasks.forEach { task ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(
                                                        checked = task.isCompleted,
                                                        onCheckedChange = { viewModel.toggleTask(task) }
                                                    )
                                                    Text(
                                                        text = task.title,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = if (task.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "daily_expense_tracker" -> {
                    val total = expenses.sumOf { it.amount }
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Expense Log", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Total: $${String.format("%.2f", total)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = BrandEmerald)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = newExpenseTitle,
                                onValueChange = { newExpenseTitle = it },
                                label = { Text("Expense Title (e.g. Groceries)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = newExpenseAmount,
                                    onValueChange = { newExpenseAmount = it },
                                    label = { Text("Amount ($)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                Button(
                                    onClick = {
                                        val amt = newExpenseAmount.toDoubleOrNull() ?: 0.0
                                        if (newExpenseTitle.isNotBlank() && amt > 0) {
                                            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                            viewModel.addExpense(newExpenseTitle, amt, newExpenseCategory, dateStr)
                                            newExpenseTitle = ""
                                            newExpenseAmount = ""
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                ) {
                                    Text("Add")
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (expenses.isEmpty()) {
                                Text("No expenses recorded.", style = MaterialTheme.typography.bodySmall)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    expenses.forEach { exp ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(exp.title, fontWeight = FontWeight.SemiBold)
                                                    Text(exp.date, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text("-$${String.format("%.2f", exp.amount)}", fontWeight = FontWeight.Bold, color = BrandRose)
                                                    IconButton(onClick = { viewModel.deleteExpense(exp.id) }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(18.dp), tint = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "daily_random_name_picker" -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Random Name / Lucky Winner", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = namesInput,
                                onValueChange = { namesInput = it },
                                label = { Text("Names List (one per line)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 5
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val list = namesInput.lines().filter { it.isNotBlank() }
                                    if (list.isNotEmpty()) {
                                        pickedWinnerName = list.random()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandAmber)
                            ) {
                                Icon(Icons.Default.Casino, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Pick Random Winner")
                            }

                            pickedWinnerName?.let { winner ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = BrandAmber.copy(alpha = 0.2f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Selected Winner:", style = MaterialTheme.typography.labelMedium)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "🎉 $winner 🎉",
                                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                            color = BrandAmber
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "daily_decision_picker" -> {
                    val answers = listOf("Yes, absolutely!", "No, not right now.", "It is decidedly so.", "Better not tell you now.", "Ask again later.", "Definitely yes!", "Very doubtful.")
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Decision & Magic 8-Ball", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = decisionQuestion,
                                onValueChange = { decisionQuestion = it },
                                label = { Text("Your Question") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    decisionAnswer = answers.random()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Decide For Me")
                            }

                            decisionAnswer?.let { ans ->
                                Spacer(modifier = Modifier.height(16.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = ans,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(16.dp),
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                "daily_world_clock" -> {
                    val cities = listOf(
                        Pair("New York", "America/New_York"),
                        Pair("London", "Europe/London"),
                        Pair("Paris", "Europe/Paris"),
                        Pair("Dubai", "Asia/Dubai"),
                        Pair("New Delhi", "Asia/Kolkata"),
                        Pair("Tokyo", "Asia/Tokyo"),
                        Pair("Sydney", "Australia/Sydney")
                    )

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("World Time Zones", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

                            cities.forEach { (city, tz) ->
                                val cal = Calendar.getInstance(TimeZone.getTimeZone(tz))
                                val timeFormat = SimpleDateFormat("hh:mm a • EEE", Locale.getDefault()).apply {
                                    timeZone = TimeZone.getTimeZone(tz)
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(city, fontWeight = FontWeight.Bold)
                                            Text(tz, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                        Text(
                                            text = timeFormat.format(cal.time),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Default Daily Tool View
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(54.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(tool.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Text(tool.description, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Active and ready to use.", style = MaterialTheme.typography.labelMedium, color = BrandEmerald)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
