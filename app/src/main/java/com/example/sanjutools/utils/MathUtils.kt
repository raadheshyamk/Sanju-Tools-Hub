package com.example.sanjutools.utils

import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

object MathUtils {

    fun evaluateExpression(expr: String, isDegree: Boolean = true): String {
        return try {
            val clean = expr.replace("×", "*").replace("÷", "/").replace("−", "-")
            val result = Parser(clean, isDegree).parse()
            if (result.isNaN()) "Error"
            else if (result % 1.0 == 0.0) result.toLong().toString()
            else String.format(Locale.US, "%.8f", result).trimEnd('0').trimEnd('.')
        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: "Invalid expression"}"
        }
    }

    private class Parser(val str: String, val isDegree: Boolean) {
        var pos = -1
        var ch = 0

        fun nextChar() {
            ch = if (++pos < str.length) str[pos].code else -1
        }

        fun eat(charToEat: Int): Boolean {
            while (ch == ' '.code) nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
            return x
        }

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                when {
                    eat('+'.code) -> x += parseTerm()
                    eat('-'.code) -> x -= parseTerm()
                    else -> return x
                }
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                when {
                    eat('*'.code) -> x *= parseFactor()
                    eat('/'.code) -> x /= parseFactor()
                    eat('%'.code) -> x %= parseFactor()
                    else -> return x
                }
            }
        }

        fun parseFactor(): Double {
            if (eat('+'.code)) return +parseFactor()
            if (eat('-'.code)) return -parseFactor()

            var x: Double
            val startPos = pos
            if (eat('('.code)) {
                x = parseExpression()
                eat(')'.code)
            } else if ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) {
                while ((ch >= '0'.code && ch <= '9'.code) || ch == '.'.code) nextChar()
                x = str.substring(startPos, pos).toDouble()
            } else if (ch >= 'a'.code && ch <= 'z'.code) {
                while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
                val func = str.substring(startPos, pos)
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else {
                    x = parseFactor()
                }
                val angle = if (isDegree) Math.toRadians(x) else x
                x = when (func) {
                    "sqrt" -> sqrt(x)
                    "sin" -> sin(angle)
                    "cos" -> cos(angle)
                    "tan" -> tan(angle)
                    "ln" -> ln(x)
                    "log" -> log10(x)
                    "pi" -> Math.PI
                    "e" -> Math.E
                    else -> throw RuntimeException("Unknown func: $func")
                }
            } else {
                throw RuntimeException("Unexpected: " + ch.toChar())
            }

            if (eat('^'.code)) x = x.pow(parseFactor())
            if (eat('!'.code)) x = factorial(x.toInt()).toDouble()

            return x
        }

        fun factorial(n: Int): Long {
            if (n < 0) return 0
            var res = 1L
            for (i in 2..min(20, n)) res *= i
            return res
        }
    }

    // Percentage Calculations
    fun calculatePercentageOf(percentage: Double, total: Double): Double = (percentage / 100.0) * total

    fun calculateWhatPercentIs(part: Double, total: Double): Double =
        if (total != 0.0) (part / total) * 100.0 else 0.0

    fun calculatePercentageChange(original: Double, newAmount: Double): Double =
        if (original != 0.0) ((newAmount - original) / original) * 100.0 else 0.0

    // Age & Date math
    data class AgeResult(
        val years: Int,
        val months: Int,
        val days: Int,
        val totalDays: Long,
        val totalHours: Long,
        val daysToNextBirthday: Int
    )

    fun calculateAge(birthYear: Int, birthMonth: Int, birthDay: Int): AgeResult {
        val birthCal = Calendar.getInstance().apply {
            set(birthYear, birthMonth - 1, birthDay, 0, 0, 0)
        }
        val today = Calendar.getInstance()

        var years = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR)
        var months = today.get(Calendar.MONTH) - birthCal.get(Calendar.MONTH)
        var days = today.get(Calendar.DAY_OF_MONTH) - birthCal.get(Calendar.DAY_OF_MONTH)

        if (days < 0) {
            months--
            val prevMonth = Calendar.getInstance().apply {
                time = today.time
                add(Calendar.MONTH, -1)
            }
            days += prevMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        if (months < 0) {
            years--
            months += 12
        }

        val diffMs = today.timeInMillis - birthCal.timeInMillis
        val totalDays = diffMs / (1000 * 60 * 60 * 24)
        val totalHours = totalDays * 24

        // Next birthday
        val nextBirthday = Calendar.getInstance().apply {
            set(Calendar.MONTH, birthMonth - 1)
            set(Calendar.DAY_OF_MONTH, birthDay)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (nextBirthday.before(today)) {
            nextBirthday.add(Calendar.YEAR, 1)
        }
        val daysToBirthday = ((nextBirthday.timeInMillis - today.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        return AgeResult(
            years = max(0, years),
            months = max(0, months),
            days = max(0, days),
            totalDays = max(0, totalDays),
            totalHours = max(0, totalHours),
            daysToNextBirthday = daysToBirthday
        )
    }

    fun calculateDateDiff(date1Millis: Long, date2Millis: Long): Triple<Long, Long, Long> {
        val diff = Math.abs(date2Millis - date1Millis)
        val days = diff / (1000 * 60 * 60 * 24)
        val weeks = days / 7
        val months = days / 30
        return Triple(days, weeks, months)
    }

    // Units Conversion Factors to Base Units
    // Length (Base: Meter)
    val lengthFactors = mapOf(
        "Meter (m)" to 1.0,
        "Kilometer (km)" to 1000.0,
        "Centimeter (cm)" to 0.01,
        "Millimeter (mm)" to 0.001,
        "Foot (ft)" to 0.3048,
        "Inch (in)" to 0.0254,
        "Yard (yd)" to 0.9144,
        "Mile (mi)" to 1609.344,
        "Nautical Mile" to 1852.0
    )

    // Weight (Base: Kilogram)
    val weightFactors = mapOf(
        "Kilogram (kg)" to 1.0,
        "Gram (g)" to 0.001,
        "Milligram (mg)" to 0.000001,
        "Pound (lb)" to 0.45359237,
        "Ounce (oz)" to 0.0283495,
        "Metric Ton (t)" to 1000.0,
        "Stone (st)" to 6.35029
    )

    // Speed (Base: m/s)
    val speedFactors = mapOf(
        "Meters/sec (m/s)" to 1.0,
        "Kilometers/hour (km/h)" to 0.277778,
        "Miles/hour (mph)" to 0.44704,
        "Knots (kn)" to 0.514444,
        "Feet/sec (ft/s)" to 0.3048
    )

    // Area (Base: Square Meter)
    val areaFactors = mapOf(
        "Square Meter (m²)" to 1.0,
        "Square Kilometer (km²)" to 1_000_000.0,
        "Square Foot (ft²)" to 0.092903,
        "Square Yard (yd²)" to 0.836127,
        "Acre (ac)" to 4046.86,
        "Hectare (ha)" to 10_000.0,
        "Square Mile (mi²)" to 2_589_988.0
    )

    // Volume (Base: Liter)
    val volumeFactors = mapOf(
        "Liter (L)" to 1.0,
        "Milliliter (mL)" to 0.001,
        "Gallon US (gal)" to 3.78541,
        "Quart US (qt)" to 0.946353,
        "Pint US (pt)" to 0.473176,
        "Cup US (cup)" to 0.24,
        "Cubic Meter (m³)" to 1000.0
    )

    // Data Storage (Base: Bytes)
    val dataFactors = mapOf(
        "Bytes (B)" to 1.0,
        "Kilobytes (KB)" to 1024.0,
        "Megabytes (MB)" to 1048576.0,
        "Gigabytes (GB)" to 1073741824.0,
        "Terabytes (TB)" to 1099511627776.0,
        "Bits (b)" to 0.125
    )

    fun convertUnits(value: Double, fromUnit: String, toUnit: String, factors: Map<String, Double>): Double {
        val fromFactor = factors[fromUnit] ?: 1.0
        val toFactor = factors[toUnit] ?: 1.0
        val baseValue = value * fromFactor
        return baseValue / toFactor
    }

    fun convertTemperature(value: Double, fromUnit: String, toUnit: String): Double {
        if (fromUnit == toUnit) return value
        // Convert fromUnit to Celsius first
        val c = when (fromUnit) {
            "Celsius (°C)" -> value
            "Fahrenheit (°F)" -> (value - 32.0) * (5.0 / 9.0)
            "Kelvin (K)" -> value - 273.15
            "Rankine (°R)" -> (value - 491.67) * (5.0 / 9.0)
            else -> value
        }
        // Convert Celsius to toUnit
        return when (toUnit) {
            "Celsius (°C)" -> c
            "Fahrenheit (°F)" -> (c * (9.0 / 5.0)) + 32.0
            "Kelvin (K)" -> c + 273.15
            "Rankine (°R)" -> (c + 273.15) * (9.0 / 5.0)
            else -> c
        }
    }

    fun convertNumberSystem(input: String, fromRadix: Int, toRadix: Int): String {
        return try {
            val num = input.trim().toLong(fromRadix)
            num.toString(toRadix).uppercase()
        } catch (e: Exception) {
            "Invalid input for selected base"
        }
    }

    // Currency rates against USD (reference matrix)
    val currencyRatesToUsd = mapOf(
        "USD" to 1.0,
        "EUR" to 0.92,
        "GBP" to 0.79,
        "INR" to 83.50,
        "JPY" to 154.20,
        "CAD" to 1.37,
        "AUD" to 1.52,
        "CHF" to 0.91,
        "CNY" to 7.24,
        "AED" to 3.67,
        "SGD" to 1.35
    )

    fun convertCurrency(amount: Double, fromCurrency: String, toCurrency: String): Double {
        val fromRate = currencyRatesToUsd[fromCurrency] ?: 1.0
        val toRate = currencyRatesToUsd[toCurrency] ?: 1.0
        // Convert to USD first, then to target
        val amountInUsd = amount / fromRate
        return amountInUsd * toRate
    }
}
