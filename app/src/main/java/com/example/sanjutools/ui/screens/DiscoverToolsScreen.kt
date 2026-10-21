package com.example.sanjutools.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanjutools.model.ToolItem
import com.example.sanjutools.model.ToolRegistry
import com.example.sanjutools.ui.components.ToolScaffold
import com.example.sanjutools.viewmodel.MainViewModel
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverToolsScreen(
    tool: ToolItem?,
    viewModel: MainViewModel,
    onOpenTool: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val suggestions by viewModel.suggestions.collectAsState()

    var userIntentQuery by remember { mutableStateOf("") }
    var matchedTools by remember { mutableStateOf<List<ToolItem>>(emptyList()) }

    var suggestTitle by remember { mutableStateOf("") }
    var suggestCategory by remember { mutableStateOf("Photo Tools") }
    var suggestDescription by remember { mutableStateOf("") }

    fun findMatchingTools(query: String) {
        val q = query.lowercase().trim()
        if (q.isEmpty()) {
            matchedTools = emptyList()
            return
        }

        matchedTools = ToolRegistry.tools.filter { t ->
            t.title.lowercase().contains(q) ||
            t.description.lowercase().contains(q) ||
            (q.contains("visa") || q.contains("passport")) && t.id.contains("passport") ||
            (q.contains("wifi") || q.contains("internet")) && t.id.contains("wifi") ||
            (q.contains("compress") || q.contains("reduce size")) && (t.id.contains("compress") || t.id.contains("zip")) ||
            (q.contains("print") || q.contains("doc")) && t.id.contains("pdf") ||
            (q.contains("secret") || q.contains("secure")) && t.id.contains("password") ||
            (q.contains("focus") || q.contains("study")) && t.id.contains("pomodoro") ||
            (q.contains("money") || q.contains("budget")) && (t.id.contains("expense") || t.id.contains("currency")) ||
            (q.contains("calc") || q.contains("math")) && t.id.contains("calc")
        }.take(6)
    }

    ToolScaffold(
        title = tool?.title ?: "Discover More Tools",
        categoryName = "Sanju Tools Hub Ecosystem",
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Smart Intent Finder Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = BrandPurple)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Smart Tool Matcher",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Describe what you want to achieve (e.g. 'I want to share home wifi', 'I need passport photos for visa', 'compress photos to email', 'calculate discount')",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = userIntentQuery,
                        onValueChange = {
                            userIntentQuery = it
                            findMatchingTools(it)
                        },
                        placeholder = { Text("What task do you want to accomplish?") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("intent_search_input"),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (matchedTools.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Recommended Tools for You:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = BrandEmerald
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            matchedTools.forEach { matched ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenTool(matched.id) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(matched.title, fontWeight = FontWeight.SemiBold)
                                            Text(matched.category.title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                        }
                                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Suggest a Tool Submission Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Suggest a New Tool",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Have an idea for a missing tool? Submit it to the Sanju Tools Hub development roadmap.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = suggestTitle,
                        onValueChange = { suggestTitle = it },
                        label = { Text("Tool Name / Concept") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("suggest_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = suggestDescription,
                        onValueChange = { suggestDescription = it },
                        label = { Text("Describe how it should work & inputs/outputs") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("suggest_desc_input"),
                        minLines = 3
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (suggestTitle.isNotBlank()) {
                                viewModel.submitToolSuggestion(suggestTitle, suggestDescription, suggestCategory)
                                Toast.makeText(context, "Thank you! Tool suggestion submitted.", Toast.LENGTH_SHORT).show()
                                suggestTitle = ""
                                suggestDescription = ""
                            } else {
                                Toast.makeText(context, "Please enter a tool name", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_suggestion_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Feature Request")
                    }
                }
            }

            // Submitted Suggestions List
            if (suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Your Submitted Suggestions (${suggestions.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    suggestions.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.toolName, fontWeight = FontWeight.Bold)
                                    if (item.description.isNotBlank()) {
                                        Text(item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BrandAmber.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = item.status,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = BrandAmber
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Future Roadmap Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Official Roadmap & Upcoming Utilities",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(
                        Pair("OCR Optical Character Recognition", "Extract text directly from scanned documents and receipts"),
                        Pair("Audio & Voice Memo Recorder", "High quality voice recording with waveform visualization"),
                        Pair("Video GIF Maker", "Convert short clips into optimized animated GIFs"),
                        Pair("Batch Image Watermarker", "Apply logos to 50+ images at once")
                    ).forEach { (name, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
