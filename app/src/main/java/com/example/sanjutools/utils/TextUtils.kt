package com.example.sanjutools.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder
import java.net.URLEncoder
import java.security.SecureRandom
import java.util.UUID

object TextUtils {

    data class TextStats(
        val words: Int,
        val charactersWithSpaces: Int,
        val charactersNoSpaces: Int,
        val lines: Int,
        val paragraphs: Int,
        val estimatedReadingTimeSeconds: Int
    )

    fun calculateStats(text: String): TextStats {
        if (text.isBlank()) {
            return TextStats(0, 0, 0, 0, 0, 0)
        }
        val words = text.trim().split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        val charsWithSpaces = text.length
        val charsNoSpaces = text.replace("\\s".toRegex(), "").length
        val lines = text.lines().size
        val paragraphs = text.split("\n\n").filter { it.isNotBlank() }.size
        val readingSeconds = (words * 60) / 200 // standard 200 wpm reading speed
        return TextStats(
            words = words,
            charactersWithSpaces = charsWithSpaces,
            charactersNoSpaces = charsNoSpaces,
            lines = lines,
            paragraphs = paragraphs,
            estimatedReadingTimeSeconds = readingSeconds.coerceAtLeast(if (words > 0) 1 else 0)
        )
    }

    enum class CaseType {
        UPPERCASE, LOWERCASE, TITLE_CASE, SENTENCE_CASE, CAMEL_CASE, SNAKE_CASE, KEBAB_CASE
    }

    fun convertCase(text: String, caseType: CaseType): String {
        return when (caseType) {
            CaseType.UPPERCASE -> text.uppercase()
            CaseType.LOWERCASE -> text.lowercase()
            CaseType.TITLE_CASE -> text.split(" ").joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            CaseType.SENTENCE_CASE -> {
                text.split(".").joinToString(". ") { s ->
                    val trimmed = s.trim()
                    trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
            CaseType.CAMEL_CASE -> {
                val words = text.split("[^a-zA-Z0-9]+".toRegex()).filter { it.isNotBlank() }
                if (words.isEmpty()) "" else words.first().lowercase() + words.drop(1).joinToString("") {
                    it.lowercase().replaceFirstChar { c -> c.titlecase() }
                }
            }
            CaseType.SNAKE_CASE -> {
                text.trim().split("[^a-zA-Z0-9]+".toRegex()).filter { it.isNotBlank() }
                    .joinToString("_") { it.lowercase() }
            }
            CaseType.KEBAB_CASE -> {
                text.trim().split("[^a-zA-Z0-9]+".toRegex()).filter { it.isNotBlank() }
                    .joinToString("-") { it.lowercase() }
            }
        }
    }

    fun removeExtraSpaces(text: String): String {
        return text.lines()
            .map { line -> line.trim().replace("\\s+".toRegex(), " ") }
            .joinToString("\n")
    }

    fun removeDuplicateLines(text: String): String {
        val seen = mutableSetOf<String>()
        return text.lines().filter { line ->
            seen.add(line)
        }.joinToString("\n")
    }

    fun sortLines(text: String, ascending: Boolean = true): String {
        val sorted = text.lines()
        return if (ascending) {
            sorted.sorted().joinToString("\n")
        } else {
            sorted.sortedDescending().joinToString("\n")
        }
    }

    fun encodeBase64(text: String): String =
        Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

    fun decodeBase64(text: String): String {
        return try {
            val bytes = Base64.decode(text.trim(), Base64.DEFAULT)
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "Error: Invalid Base64 string (${e.localizedMessage})"
        }
    }

    fun encodeUrl(text: String): String = URLEncoder.encode(text, "UTF-8")

    fun decodeUrl(text: String): String {
        return try {
            URLDecoder.decode(text, "UTF-8")
        } catch (e: Exception) {
            "Error: Invalid URL encoded string"
        }
    }

    fun encodeHtmlEntities(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;")
    }

    fun decodeHtmlEntities(text: String): String {
        return text.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
    }

    fun formatAndValidateJson(jsonString: String, indentSpaces: Int = 2): Pair<Boolean, String> {
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return Pair(false, "JSON input is empty")
        return try {
            if (trimmed.startsWith("{")) {
                val obj = JSONObject(trimmed)
                Pair(true, obj.toString(indentSpaces))
            } else if (trimmed.startsWith("[")) {
                val arr = JSONArray(trimmed)
                Pair(true, arr.toString(indentSpaces))
            } else {
                Pair(false, "Invalid JSON: Root must be an object '{' or array '['")
            }
        } catch (e: Exception) {
            Pair(false, "JSON Syntax Error: ${e.message}")
        }
    }

    fun generatePassword(
        length: Int = 16,
        includeUpper: Boolean = true,
        includeLower: Boolean = true,
        includeDigits: Boolean = true,
        includeSymbols: Boolean = true
    ): Pair<String, String> {
        val uppers = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowers = "abcdefghijklmnopqrstuvwxyz"
        val digits = "0123456789"
        val symbols = "!@#$%^&*()_+-=[]{}|;:,.<>?"

        val pool = buildString {
            if (includeUpper) append(uppers)
            if (includeLower) append(lowers)
            if (includeDigits) append(digits)
            if (includeSymbols) append(symbols)
        }

        if (pool.isEmpty()) return Pair("", "Weak")

        val random = SecureRandom()
        val len = length.coerceIn(6, 64)
        val password = (1..len).map { pool[random.nextInt(pool.length)] }.joinToString("")

        // Calculate entropy
        val poolSize = pool.length.toDouble()
        val entropy = len * (Math.log(poolSize) / Math.log(2.0))
        val strength = when {
            entropy < 40 -> "Weak ($entropy bits)"
            entropy < 65 -> "Moderate ($entropy bits)"
            entropy < 85 -> "Strong ($entropy bits)"
            else -> "Very Strong ($entropy bits)"
        }
        return Pair(password, strength)
    }

    fun generateUuid(count: Int = 1, upperCase: Boolean = false): List<String> {
        val n = count.coerceIn(1, 25)
        return (1..n).map {
            val u = UUID.randomUUID().toString()
            if (upperCase) u.uppercase() else u.lowercase()
        }
    }

    fun generateLoremIpsum(paragraphs: Int = 2): String {
        val sample = listOf(
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
            "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
            "Curabitur pretium tincidunt lacus. Nulla gravida orci a odio. Nullam varius, turpis et commodo pharetra, est eros bibendum elit, nec luctus magna felis sollicitudin mauris. Integer in mauris eu nibh euismod gravida.",
            "Fusce aliquet mollis malesuada. Morbi ac ex sit amet purus elementum porta. Cras vehicula leo at eros dapibus sagittis. Maecenas tristique orci ac sem rhoncus, sed tempor ligula euismod."
        )
        val count = paragraphs.coerceIn(1, 10)
        return (0 until count).joinToString("\n\n") { sample[it % sample.size] }
    }

    fun compareTexts(text1: String, text2: String): String {
        val lines1 = text1.lines()
        val lines2 = text2.lines()
        val diffBuilder = StringBuilder()

        val maxLines = maxOf(lines1.size, lines2.size)
        var hasDifferences = false

        for (i in 0 until maxLines) {
            val l1 = lines1.getOrNull(i)
            val l2 = lines2.getOrNull(i)

            if (l1 == l2) {
                diffBuilder.appendLine("  [Line ${i + 1}] $l1")
            } else {
                hasDifferences = true
                if (l1 != null) diffBuilder.appendLine("- [Line ${i + 1}] $l1")
                if (l2 != null) diffBuilder.appendLine("+ [Line ${i + 1}] $l2")
            }
        }

        return if (!hasDifferences) {
            "Both texts are 100% identical! No differences found."
        } else {
            diffBuilder.toString()
        }
    }

    fun copyToClipboard(context: Context, text: String, label: String = "Sanju Tools"): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            false
        }
    }
}
