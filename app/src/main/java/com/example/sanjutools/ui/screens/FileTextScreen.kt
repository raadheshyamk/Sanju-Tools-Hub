package com.example.sanjutools.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanjutools.model.ToolItem
import com.example.sanjutools.ui.components.ToolScaffold
import com.example.sanjutools.utils.BitmapUtils
import com.example.sanjutools.utils.TextUtils
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPurple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun FileTextScreen(
    tool: ToolItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var textInput by remember { mutableStateOf("") }
    var textInput2 by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Password generator states
    var passLength by remember { mutableStateOf(16f) }
    var incUpper by remember { mutableStateOf(true) }
    var incLower by remember { mutableStateOf(true) }
    var incDigits by remember { mutableStateOf(true) }
    var incSymbols by remember { mutableStateOf(true) }
    var passwordStrength by remember { mutableStateOf("") }

    // Color converter states
    var redVal by remember { mutableStateOf(59f) }
    var greenVal by remember { mutableStateOf(130f) }
    var blueVal by remember { mutableStateOf(246f) }

    // File info state
    var pickedFileName by remember { mutableStateOf("") }
    var pickedFileSize by remember { mutableStateOf("") }
    var pickedFileMime by remember { mutableStateOf("") }
    var pickedFileSha256 by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val pfd = context.contentResolver.openFileDescriptor(uri, "r")
                    val sizeBytes = pfd?.statSize ?: 0L
                    pfd?.close()

                    val name = uri.lastPathSegment ?: "document"
                    val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"

                    // Calculate SHA-256
                    val digest = MessageDigest.getInstance("SHA-256")
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val buffer = ByteArray(8192)
                        var read = stream.read(buffer)
                        while (read != -1) {
                            digest.update(buffer, 0, read)
                            read = stream.read(buffer)
                        }
                    }
                    val hash = digest.digest().joinToString("") { "%02x".format(it) }

                    withContext(Dispatchers.Main) {
                        pickedFileName = name
                        pickedFileSize = "${String.format("%.2f", sizeBytes.toDouble() / 1024.0)} KB ($sizeBytes Bytes)"
                        pickedFileMime = mime
                        pickedFileSha256 = hash
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        statusMessage = "Error reading file: ${e.localizedMessage}"
                    }
                }
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
                "file_info_viewer", "file_size_checker", "file_extension_checker" -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FindInPage, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Select any file to inspect properties & checksums", style = MaterialTheme.typography.bodyMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { filePicker.launch("*/*") }) {
                                Icon(Icons.Default.AttachFile, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose Any File")
                            }

                            if (pickedFileName.isNotBlank()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("• File Name: $pickedFileName", fontWeight = FontWeight.Bold)
                                    Text("• File Size: $pickedFileSize")
                                    Text("• MIME Type: $pickedFileMime")
                                    Text("• SHA-256 Checksum: $pickedFileSha256", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }

                "file_zip_creator" -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Create Compressed ZIP Archive", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = { Text("Content for document inside ZIP") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 4
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    try {
                                        val zipFile = File(context.cacheDir, "SanjuArchive_${System.currentTimeMillis()}.zip")
                                        ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                                            val entry = ZipEntry("Document.txt")
                                            zos.putNextEntry(entry)
                                            zos.write(textInput.toByteArray(Charsets.UTF_8))
                                            zos.closeEntry()
                                        }
                                        val shareIntent = BitmapUtils.createShareIntent(context, zipFile, "application/zip")
                                        context.startActivity(Intent.createChooser(shareIntent, "Share ZIP via"))
                                        statusMessage = "Created ZIP: ${zipFile.name}"
                                    } catch (e: Exception) {
                                        statusMessage = "Error creating ZIP: ${e.message}"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                            ) {
                                Icon(Icons.Default.FolderZip, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate & Download ZIP")
                            }
                        }
                    }
                }

                "tool_password_gen" -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Password Options", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Length: ${passLength.toInt()} characters", style = MaterialTheme.typography.bodySmall)
                            Slider(value = passLength, onValueChange = { passLength = it }, valueRange = 8f..32f)

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Uppercase Letters (A-Z)")
                                Switch(checked = incUpper, onCheckedChange = { incUpper = it })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Lowercase Letters (a-z)")
                                Switch(checked = incLower, onCheckedChange = { incLower = it })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Numbers (0-9)")
                                Switch(checked = incDigits, onCheckedChange = { incDigits = it })
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Symbols (!@#$%^&*)")
                                Switch(checked = incSymbols, onCheckedChange = { incSymbols = it })
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val (pass, strength) = TextUtils.generatePassword(passLength.toInt(), incUpper, incLower, incDigits, incSymbols)
                                    outputText = pass
                                    passwordStrength = strength
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.VpnKey, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Password")
                            }

                            if (outputText.isNotBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = outputText,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            IconButton(onClick = {
                                                TextUtils.copyToClipboard(context, outputText)
                                                Toast.makeText(context, "Password copied!", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                            }
                                        }
                                        Text("Strength: $passwordStrength", style = MaterialTheme.typography.labelSmall, color = BrandEmerald)
                                    }
                                }
                            }
                        }
                    }
                }

                "text_json_formatter" -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = { Text("Paste JSON to format & validate") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 5
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        val (valid, formatted) = TextUtils.formatAndValidateJson(textInput, 2)
                                        outputText = formatted
                                        statusMessage = if (valid) "Valid JSON format!" else "Invalid JSON"
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Format JSON")
                                }
                                OutlinedButton(
                                    onClick = {
                                        val (valid, formatted) = TextUtils.formatAndValidateJson(textInput, 0)
                                        outputText = formatted.replace("\n", "")
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Minify")
                                }
                            }
                        }
                    }
                }

                "text_comparison" -> {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Text Diff Comparison", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = { Text("Original Text (Left / A)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = textInput2,
                                onValueChange = { textInput2 = it },
                                label = { Text("Modified Text (Right / B)") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    outputText = TextUtils.compareTexts(textInput, textInput2)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Compare Differences")
                            }
                        }
                    }
                }

                "color_picker_palette", "color_converter_hex_rgb" -> {
                    val currColor = Color(redVal.toInt(), greenVal.toInt(), blueVal.toInt())
                    val hexCode = String.format("#%02X%02X%02X", redVal.toInt(), greenVal.toInt(), blueVal.toInt())

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(currColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = hexCode,
                                    color = if (redVal + greenVal + blueVal > 380) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Red: ${redVal.toInt()}")
                            Slider(value = redVal, onValueChange = { redVal = it }, valueRange = 0f..255f)
                            Text("Green: ${greenVal.toInt()}")
                            Slider(value = greenVal, onValueChange = { greenVal = it }, valueRange = 0f..255f)
                            Text("Blue: ${blueVal.toInt()}")
                            Slider(value = blueVal, onValueChange = { blueVal = it }, valueRange = 0f..255f)

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("HEX: $hexCode\nRGB(${redVal.toInt()}, ${greenVal.toInt()}, ${blueVal.toInt()})", fontWeight = FontWeight.SemiBold)
                                Button(onClick = {
                                    TextUtils.copyToClipboard(context, hexCode)
                                    Toast.makeText(context, "Copied $hexCode", Toast.LENGTH_SHORT).show()
                                }) {
                                    Text("Copy HEX")
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Generic Text processing tools (Base64, URL, HTML, UUID, Lorem Ipsum)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                label = { Text("Enter Input Text") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                when (tool.id) {
                                    "text_base64" -> {
                                        Button(onClick = { outputText = TextUtils.encodeBase64(textInput) }, modifier = Modifier.weight(1f)) {
                                            Text("Encode")
                                        }
                                        OutlinedButton(onClick = { outputText = TextUtils.decodeBase64(textInput) }, modifier = Modifier.weight(1f)) {
                                            Text("Decode")
                                        }
                                    }
                                    "text_url_encode" -> {
                                        Button(onClick = { outputText = TextUtils.encodeUrl(textInput) }, modifier = Modifier.weight(1f)) {
                                            Text("Encode URL")
                                        }
                                        OutlinedButton(onClick = { outputText = TextUtils.decodeUrl(textInput) }, modifier = Modifier.weight(1f)) {
                                            Text("Decode URL")
                                        }
                                    }
                                    "text_html_entity" -> {
                                        Button(onClick = { outputText = TextUtils.encodeHtmlEntities(textInput) }, modifier = Modifier.weight(1f)) {
                                            Text("Escape HTML")
                                        }
                                        OutlinedButton(onClick = { outputText = TextUtils.decodeHtmlEntities(textInput) }, modifier = Modifier.weight(1f)) {
                                            Text("Unescape")
                                        }
                                    }
                                    "tool_uuid_gen" -> {
                                        Button(onClick = { outputText = TextUtils.generateUuid(5).joinToString("\n") }, modifier = Modifier.weight(1f)) {
                                            Text("Generate 5 UUIDs")
                                        }
                                    }
                                    "tool_lorem_ipsum" -> {
                                        Button(onClick = { outputText = TextUtils.generateLoremIpsum(3) }, modifier = Modifier.weight(1f)) {
                                            Text("Generate 3 Paragraphs")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Results Display Card
            if (outputText.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Result Output", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            IconButton(onClick = {
                                TextUtils.copyToClipboard(context, outputText)
                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionContainer {
                            Text(
                                text = outputText,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
