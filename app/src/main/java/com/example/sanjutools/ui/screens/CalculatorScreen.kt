package com.example.sanjutools.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanjutools.model.ToolItem
import com.example.sanjutools.ui.components.ToolScaffold
import com.example.sanjutools.utils.MathUtils
import com.example.sanjutools.utils.TextUtils
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    tool: ToolItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // Standard & Scientific Calculator state
    var calcExpression by remember { mutableStateOf("") }
    var calcResult by remember { mutableStateOf("0") }
    var isDegreeMode by remember { mutableStateOf(true) }

    // Percentage Calculator state
    var percentValue1 by remember { mutableStateOf("15") }
    var percentValue2 by remember { mutableStateOf("200") }
    var percentResult by remember { mutableStateOf("") }

    // Age Calculator state
    var birthYear by remember { mutableStateOf("2000") }
    var birthMonth by remember { mutableStateOf("1") }
    var birthDay by remember { mutableStateOf("1") }
    var ageResult by remember { mutableStateOf<MathUtils.AgeResult?>(null) }

    // Unit Converter state
    var unitInputValue by remember { mutableStateOf("100") }
    var unitOutputResult by remember { mutableStateOf("") }

    val categoryFactors = when (tool.id) {
        "conv_length" -> MathUtils.lengthFactors
        "conv_weight" -> MathUtils.weightFactors
        "conv_speed" -> MathUtils.speedFactors
        "conv_area" -> MathUtils.areaFactors
        "conv_volume" -> MathUtils.volumeFactors
        "conv_data_storage" -> MathUtils.dataFactors
        else -> MathUtils.lengthFactors
    }

    val unitOptions = remember(tool.id) { categoryFactors.keys.toList() }
    var fromUnit by remember(tool.id) { mutableStateOf(unitOptions.firstOrNull() ?: "") }
    var toUnit by remember(tool.id) { mutableStateOf(unitOptions.getOrNull(1) ?: unitOptions.firstOrNull() ?: "") }

    // Temperature Converter state
    var tempInputValue by remember { mutableStateOf("25") }
    var tempFromUnit by remember { mutableStateOf("Celsius (°C)") }
    var tempToUnit by remember { mutableStateOf("Fahrenheit (°F)") }
    var tempResult by remember { mutableStateOf("") }

    // Number System Converter state
    var numberSystemInput by remember { mutableStateOf("255") }
    var fromRadix by remember { mutableStateOf(10) }
    var toRadix by remember { mutableStateOf(16) }
    var numberSystemResult by remember { mutableStateOf("") }

    // Currency Converter state
    var currencyAmount by remember { mutableStateOf("100") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("INR") }
    var currencyResult by remember { mutableStateOf("") }

    fun evaluateCalc() {
        if (calcExpression.isNotBlank()) {
            calcResult = MathUtils.evaluateExpression(calcExpression, isDegreeMode)
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
                "calc_standard", "calc_scientific" -> {
                    // Calculator Display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (tool.id == "calc_scientific") {
                                    AssistChip(
                                        onClick = { isDegreeMode = !isDegreeMode },
                                        label = { Text(if (isDegreeMode) "DEG" else "RAD", fontWeight = FontWeight.Bold) }
                                    )
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                IconButton(
                                    onClick = {
                                        TextUtils.copyToClipboard(context, calcResult)
                                        Toast.makeText(context, "Copied result: $calcResult", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                            }

                            Text(
                                text = calcExpression.ifBlank { "0" },
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "= $calcResult",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Keypad
                    val isSci = tool.id == "calc_scientific"
                    val keypadRows = if (isSci) {
                        listOf(
                            listOf("sin", "cos", "tan", "sqrt", "C"),
                            listOf("ln", "log", "^", "!", "÷"),
                            listOf("7", "8", "9", "(", "×"),
                            listOf("4", "5", "6", ")", "−"),
                            listOf("1", "2", "3", "pi", "+"),
                            listOf("0", ".", "e", "⌫", "=")
                        )
                    } else {
                        listOf(
                            listOf("C", "(", ")", "÷"),
                            listOf("7", "8", "9", "×"),
                            listOf("4", "5", "6", "−"),
                            listOf("1", "2", "3", "+"),
                            listOf("0", ".", "⌫", "=")
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        keypadRows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { key ->
                                    val isOp = key in listOf("+", "−", "×", "÷", "^", "=")
                                    val isSpecial = key in listOf("C", "⌫")
                                    val isFn = key in listOf("sin", "cos", "tan", "sqrt", "ln", "log", "pi", "e", "!")

                                    Button(
                                        onClick = {
                                            when (key) {
                                                "C" -> {
                                                    calcExpression = ""
                                                    calcResult = "0"
                                                }
                                                "⌫" -> {
                                                    if (calcExpression.isNotEmpty()) {
                                                        calcExpression = calcExpression.dropLast(1)
                                                    }
                                                }
                                                "=" -> evaluateCalc()
                                                "sin", "cos", "tan", "sqrt", "ln", "log" -> {
                                                    calcExpression += "$key("
                                                }
                                                else -> {
                                                    calcExpression += key
                                                    evaluateCalc()
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(if (isSci) 48.dp else 56.dp)
                                            .testTag("calc_btn_$key"),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = when {
                                                key == "=" -> BrandEmerald
                                                isOp -> MaterialTheme.colorScheme.primary
                                                isSpecial -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                                isFn -> BrandPurple
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            contentColor = if (isOp || isSpecial || key == "=" || isFn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text(
                                            text = key,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = if (isSci) 14.sp else 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "calc_percentage" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("What is X% of Y?", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = percentValue1,
                                    onValueChange = { percentValue1 = it },
                                    label = { Text("Percentage (%)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = percentValue2,
                                    onValueChange = { percentValue2 = it },
                                    label = { Text("Total Amount") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val p = percentValue1.toDoubleOrNull() ?: 0.0
                                    val total = percentValue2.toDoubleOrNull() ?: 0.0
                                    val ans = MathUtils.calculatePercentageOf(p, total)
                                    percentResult = "$p% of $total = $ans"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Calculate")
                            }

                            if (percentResult.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = percentResult,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(14.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                "calc_age" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Enter Date of Birth", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = birthDay,
                                    onValueChange = { birthDay = it },
                                    label = { Text("Day") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = birthMonth,
                                    onValueChange = { birthMonth = it },
                                    label = { Text("Month") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = birthYear,
                                    onValueChange = { birthYear = it },
                                    label = { Text("Year") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    val y = birthYear.toIntOrNull() ?: 2000
                                    val m = birthMonth.toIntOrNull() ?: 1
                                    val d = birthDay.toIntOrNull() ?: 1
                                    ageResult = MathUtils.calculateAge(y, m, d)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Calculate Age")
                            }

                            ageResult?.let { res ->
                                Spacer(modifier = Modifier.height(14.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = "${res.years} Years, ${res.months} Months, ${res.days} Days",
                                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text("• Total Days Lived: ${res.totalDays} days")
                                        Text("• Total Hours Lived: ${res.totalHours} hours")
                                        Text("• Next Birthday: in ${res.daysToNextBirthday} days!")
                                    }
                                }
                            }
                        }
                    }
                }

                "conv_temperature" -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = tempInputValue,
                                onValueChange = { tempInputValue = it },
                                label = { Text("Temperature Value") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)").forEach { u ->
                                    FilterChip(
                                        selected = tempFromUnit == u,
                                        onClick = { tempFromUnit = u },
                                        label = { Text(u) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Convert To:", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Celsius (°C)", "Fahrenheit (°F)", "Kelvin (K)").forEach { u ->
                                    FilterChip(
                                        selected = tempToUnit == u,
                                        onClick = { tempToUnit = u },
                                        label = { Text(u) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val v = tempInputValue.toDoubleOrNull() ?: 0.0
                                    val converted = MathUtils.convertTemperature(v, tempFromUnit, tempToUnit)
                                    tempResult = "$v $tempFromUnit = ${String.format("%.2f", converted)} $tempToUnit"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Convert")
                            }
                            if (tempResult.isNotBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = tempResult,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                "conv_currency" -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = currencyAmount,
                                onValueChange = { currencyAmount = it },
                                label = { Text("Amount") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("From Currency: $fromCurrency", style = MaterialTheme.typography.bodySmall)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MathUtils.currencyRatesToUsd.keys.forEach { curr ->
                                    FilterChip(
                                        selected = fromCurrency == curr,
                                        onClick = { fromCurrency = curr },
                                        label = { Text(curr) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("To Currency: $toCurrency", style = MaterialTheme.typography.bodySmall)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                MathUtils.currencyRatesToUsd.keys.forEach { curr ->
                                    FilterChip(
                                        selected = toCurrency == curr,
                                        onClick = { toCurrency = curr },
                                        label = { Text(curr) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val amt = currencyAmount.toDoubleOrNull() ?: 0.0
                                    val res = MathUtils.convertCurrency(amt, fromCurrency, toCurrency)
                                    currencyResult = "$amt $fromCurrency = ${String.format("%.2f", res)} $toCurrency"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Convert Currency")
                            }
                            if (currencyResult.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = currencyResult,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(14.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Universal Unit Converters (Length, Weight, Speed, Area, Volume, Data Storage)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = unitInputValue,
                                onValueChange = { unitInputValue = it },
                                label = { Text("Input Value") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("From:", style = MaterialTheme.typography.bodySmall)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                unitOptions.forEach { opt ->
                                    FilterChip(
                                        selected = fromUnit == opt,
                                        onClick = { fromUnit = opt },
                                        label = { Text(opt) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("To:", style = MaterialTheme.typography.bodySmall)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                unitOptions.forEach { opt ->
                                    FilterChip(
                                        selected = toUnit == opt,
                                        onClick = { toUnit = opt },
                                        label = { Text(opt) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val v = unitInputValue.toDoubleOrNull() ?: 0.0
                                    val out = MathUtils.convertUnits(v, fromUnit, toUnit, categoryFactors)
                                    unitOutputResult = "$v $fromUnit = ${String.format("%.4f", out)} $toUnit"
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Calculate Conversion")
                            }
                            if (unitOutputResult.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = unitOutputResult,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(14.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
