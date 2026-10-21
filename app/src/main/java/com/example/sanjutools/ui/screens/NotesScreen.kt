package com.example.sanjutools.ui.screens

import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanjutools.data.NoteEntity
import com.example.sanjutools.model.ToolItem
import com.example.sanjutools.ui.components.ToolScaffold
import com.example.sanjutools.utils.BitmapUtils
import com.example.sanjutools.utils.PdfUtils
import com.example.sanjutools.utils.TextUtils
import com.example.sanjutools.viewmodel.MainViewModel
import com.example.ui.theme.BrandAmber
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPurple
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    tool: ToolItem,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val notes by viewModel.notes.collectAsState()

    var activeNoteId by remember { mutableStateOf<Long?>(null) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var selectedFolder by remember { mutableStateOf("General") }
    var noteTags by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

    // TTS Engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        tts.language = Locale.getDefault()
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // Auto-save logic whenever title or content changes
    LaunchedEffect(noteTitle, noteContent) {
        if (noteTitle.isNotBlank() || noteContent.isNotBlank()) {
            val entity = NoteEntity(
                id = activeNoteId ?: 0L,
                title = noteTitle.ifBlank { "Untitled Note" },
                content = noteContent,
                folder = selectedFolder,
                tags = noteTags,
                updatedAt = System.currentTimeMillis()
            )
            viewModel.saveNote(entity)
        }
    }

    val stats = remember(noteContent) { TextUtils.calculateStats(noteContent) }

    ToolScaffold(
        title = tool.title,
        categoryName = tool.category.title,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Note Editor Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = noteTitle,
                            onValueChange = { noteTitle = it },
                            placeholder = { Text("Note Title...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("note_title_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // TTS Speak Button
                            IconButton(
                                onClick = {
                                    if (noteContent.isNotBlank()) {
                                        ttsEngine?.speak(noteContent, TextToSpeech.QUEUE_FLUSH, null, "tts_id")
                                    } else {
                                        Toast.makeText(context, "Nothing to read aloud", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Read aloud", tint = BrandPurple)
                            }

                            // Copy Button
                            IconButton(
                                onClick = {
                                    val full = "$noteTitle\n\n$noteContent"
                                    TextUtils.copyToClipboard(context, full)
                                    Toast.makeText(context, "Copied note to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                            }

                            // Clear Editor
                            IconButton(
                                onClick = {
                                    activeNoteId = null
                                    noteTitle = ""
                                    noteContent = ""
                                    noteTags = ""
                                }
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "New note")
                            }
                        }
                    }

                    // Folder Selector & Tags Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("General", "Work", "Personal", "Ideas", "Tasks").forEach { folder ->
                            FilterChip(
                                selected = selectedFolder == folder,
                                onClick = { selectedFolder = folder },
                                label = { Text(folder) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        placeholder = { Text("Write your thoughts, checklist items, or paste text to clean, convert or analyze...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("note_content_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )

                    // Real-Time Writing Stats & Actions Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${stats.words} words • ${stats.charactersNoSpaces} chars • ~${stats.estimatedReadingTimeSeconds}s read",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Quick Format Operations Dropdown / Buttons
                            Row {
                                AssistChip(
                                    onClick = {
                                        noteContent = TextUtils.convertCase(noteContent, TextUtils.CaseType.TITLE_CASE)
                                    },
                                    label = { Text("Title Case", fontSize = 11.sp) }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AssistChip(
                                    onClick = {
                                        noteContent = TextUtils.removeExtraSpaces(noteContent)
                                        Toast.makeText(context, "Cleaned extra spaces", Toast.LENGTH_SHORT).show()
                                    },
                                    label = { Text("Trim", fontSize = 11.sp) }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                AssistChip(
                                    onClick = {
                                        noteContent = TextUtils.sortLines(noteContent, true)
                                    },
                                    label = { Text("Sort A-Z", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Export Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                try {
                                    val txtFile = File(context.cacheDir, "Note_${System.currentTimeMillis()}.txt")
                                    FileOutputStream(txtFile).use { out ->
                                        out.write("$noteTitle\n\n$noteContent".toByteArray())
                                    }
                                    val shareIntent = BitmapUtils.createShareIntent(context, txtFile, "text/plain")
                                    context.startActivity(Intent.createChooser(shareIntent, "Export TXT via"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error saving TXT: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export TXT")
                        }

                        Button(
                            onClick = {
                                try {
                                    val pdfFile = PdfUtils.createPdfFromText(context, noteTitle, noteContent)
                                    val shareIntent = BitmapUtils.createShareIntent(context, pdfFile, "application/pdf")
                                    context.startActivity(Intent.createChooser(shareIntent, "Export Note as PDF"))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error exporting PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export PDF")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Saved Notes List Drawer / Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Saved Notes (${notes.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (notes.isEmpty()) {
                        Text(
                            text = "No saved notes yet. Start writing above!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(notes) { n ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeNoteId = n.id
                                            noteTitle = n.title
                                            noteContent = n.content
                                            selectedFolder = n.folder
                                            noteTags = n.tags
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = n.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${n.folder} • ${n.content.take(30)}...",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteNote(n.id) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                modifier = Modifier.size(16.dp),
                                                tint = Color.Gray
                                            )
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
}
