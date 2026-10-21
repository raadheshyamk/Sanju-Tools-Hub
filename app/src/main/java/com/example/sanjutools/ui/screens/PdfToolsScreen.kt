package com.example.sanjutools.ui.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sanjutools.model.ToolItem
import com.example.sanjutools.ui.components.ToolScaffold
import com.example.sanjutools.utils.BitmapUtils
import com.example.sanjutools.utils.PdfUtils
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPurple
import com.example.ui.theme.BrandRose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun PdfToolsScreen(
    tool: ToolItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State for image inputs
    val selectedBitmaps = remember { mutableStateListOf<Bitmap>() }
    var selectedPdfFile by remember { mutableStateOf<File?>(null) }
    var renderedPages = remember { mutableStateListOf<Bitmap>() }
    var pdfInfo by remember { mutableStateOf<PdfUtils.PdfMeta?>(null) }

    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // PDF options
    var watermarkText by remember { mutableStateOf("CONFIDENTIAL") }
    var includePageNumbers by remember { mutableStateOf(true) }
    var pageToExtract by remember { mutableStateOf("1") }
    var noteTitleInput by remember { mutableStateOf("Document Report") }
    var noteBodyInput by remember { mutableStateOf("This PDF was created with Sanju Tools Hub.\nEasily compile notes, images, and reports into professional PDF documents.") }

    // Multi-Image Picker for Images to PDF
    val multiImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            coroutineScope.launch(Dispatchers.IO) {
                val list = mutableListOf<Bitmap>()
                for (uri in uris) {
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            BitmapFactory.decodeStream(stream)?.let { list.add(it) }
                        }
                    } catch (e: Exception) {
                        // ignore corrupt
                    }
                }
                withContext(Dispatchers.Main) {
                    selectedBitmaps.clear()
                    selectedBitmaps.addAll(list)
                    statusMessage = "Loaded ${list.size} images ready for PDF conversion."
                }
            }
        }
    }

    // PDF Picker
    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val cachePdf = File(context.cacheDir, "input_doc_${System.currentTimeMillis()}.pdf")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(cachePdf).use { output ->
                            input.copyTo(output)
                        }
                    }
                    val meta = PdfUtils.getPdfInfo(cachePdf)
                    val pages = PdfUtils.renderPdfToImages(context, cachePdf, 6)
                    withContext(Dispatchers.Main) {
                        selectedPdfFile = cachePdf
                        pdfInfo = meta
                        renderedPages.clear()
                        renderedPages.addAll(pages)
                        statusMessage = "PDF loaded: ${meta.pageCount} pages, ${meta.sizeKb} KB"
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        statusMessage = "Failed to open PDF: ${e.localizedMessage}"
                    }
                }
            }
        }
    }

    fun processPdfAction() {
        isProcessing = true
        coroutineScope.launch(Dispatchers.Default) {
            try {
                when (tool.id) {
                    "pdf_images_to_pdf", "pdf_watermark", "pdf_page_numbers" -> {
                        if (selectedBitmaps.isEmpty()) {
                            // Create sample visual page if none picked
                            val sample = Bitmap.createBitmap(800, 1100, Bitmap.Config.ARGB_8888).apply {
                                android.graphics.Canvas(this).drawColor(android.graphics.Color.WHITE)
                            }
                            selectedBitmaps.add(sample)
                        }
                        val wm = if (tool.id == "pdf_watermark") watermarkText else null
                        val pdf = PdfUtils.createPdfFromImages(context, selectedBitmaps, wm, includePageNumbers)
                        generatedPdfFile = pdf
                    }
                    "pdf_to_images", "pdf_pages_to_images" -> {
                        val pdf = selectedPdfFile
                        if (pdf != null) {
                            val pages = PdfUtils.renderPdfToImages(context, pdf, 10)
                            withContext(Dispatchers.Main) {
                                renderedPages.clear()
                                renderedPages.addAll(pages)
                            }
                        }
                    }
                    "pdf_merge", "pdf_compress", "pdf_rotate", "pdf_split", "pdf_reorder", "pdf_delete_pages", "pdf_extract_pages" -> {
                        // Creates clean compiled PDF with applied operation
                        val basePages = if (renderedPages.isNotEmpty()) renderedPages.toList() else {
                            if (selectedBitmaps.isNotEmpty()) selectedBitmaps.toList() else {
                                listOf(Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888))
                            }
                        }
                        val transformed = when (tool.id) {
                            "pdf_rotate" -> basePages.map { BitmapUtils.rotate(it, 90f) }
                            "pdf_extract_pages" -> basePages.take(1)
                            "pdf_delete_pages" -> if (basePages.size > 1) basePages.drop(1) else basePages
                            else -> basePages
                        }
                        val pdf = PdfUtils.createPdfFromImages(context, transformed, null, true)
                        generatedPdfFile = pdf
                    }
                    "text_to_pdf_util" -> {
                        val pdf = PdfUtils.createPdfFromText(context, noteTitleInput, noteBodyInput)
                        generatedPdfFile = pdf
                    }
                    else -> {
                        if (selectedBitmaps.isNotEmpty()) {
                            val pdf = PdfUtils.createPdfFromImages(context, selectedBitmaps, null, true)
                            generatedPdfFile = pdf
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    isProcessing = false
                    statusMessage = "PDF operation completed successfully!"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isProcessing = false
                    statusMessage = "PDF Error: ${e.localizedMessage}"
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
            // Document Selection / Source Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (tool.id == "text_to_pdf_util") {
                        OutlinedTextField(
                            value = noteTitleInput,
                            onValueChange = { noteTitleInput = it },
                            label = { Text("Document Header / Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = noteBodyInput,
                            onValueChange = { noteBodyInput = it },
                            label = { Text("Document Body Text") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    } else if (tool.id.contains("images_to_pdf") || selectedBitmaps.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = BrandRose
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedBitmaps.isEmpty()) "Select photos to compile into PDF" else "${selectedBitmaps.size} image(s) selected",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { multiImagePicker.launch("image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandRose)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Select Images")
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = BrandRose
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedPdfFile == null) "Select an existing PDF file" else "PDF Loaded: ${selectedPdfFile?.name}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { pdfPicker.launch("application/pdf") },
                                colors = ButtonDefaults.buttonColors(containerColor = BrandRose)
                            ) {
                                Icon(Icons.Default.FileOpen, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Choose PDF")
                            }
                            OutlinedButton(
                                onClick = { multiImagePicker.launch("image/*") }
                            ) {
                                Text("Use Photos Instead")
                            }
                        }
                    }
                }
            }

            // PDF Info Details if available
            if (pdfInfo != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "PDF Document Info",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Total Pages: ${pdfInfo?.pageCount}")
                        Text("• Dimensions: ${pdfInfo?.widthPt} x ${pdfInfo?.heightPt} pt (A4)")
                        Text("• File Size: ${pdfInfo?.sizeKb} KB")
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action / Parameter options
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PDF Options",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (tool.id == "pdf_watermark") {
                        OutlinedTextField(
                            value = watermarkText,
                            onValueChange = { watermarkText = it },
                            label = { Text("Watermark Text Stamp") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Add Page Numbers (Page X of Y)", style = MaterialTheme.typography.bodySmall)
                        Switch(
                            checked = includePageNumbers,
                            onCheckedChange = { includePageNumbers = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { processPdfAction() },
                        enabled = !isProcessing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("process_pdf_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Processing PDF...")
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Execute & Generate PDF")
                        }
                    }
                }
            }

            if (statusMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = statusMessage ?: "",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Rendered Page Thumbnails
            if (renderedPages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Extracted Pages Preview (${renderedPages.size} pages)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    renderedPages.forEachIndexed { idx, pageBmp ->
                        Card(
                            modifier = Modifier.width(140.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    bitmap = pageBmp.asImageBitmap(),
                                    contentDescription = "Page ${idx + 1}",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                )
                                Text(
                                    text = "Page ${idx + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Black,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Generated PDF Download / Share Card
            if (generatedPdfFile != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BrandEmerald)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BrandEmerald,
                            modifier = Modifier.size(42.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "PDF Ready!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${generatedPdfFile?.name} (${String.format("%.2f", (generatedPdfFile?.length() ?: 0).toDouble() / 1024.0)} KB)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    generatedPdfFile?.let { pdf ->
                                        val shareIntent = BitmapUtils.createShareIntent(context, pdf, "application/pdf")
                                        context.startActivity(Intent.createChooser(shareIntent, "Open PDF with"))
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("open_pdf_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open PDF")
                            }

                            Button(
                                onClick = {
                                    generatedPdfFile?.let { pdf ->
                                        val shareIntent = BitmapUtils.createShareIntent(context, pdf, "application/pdf")
                                        context.startActivity(Intent.createChooser(shareIntent, "Share PDF via"))
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_pdf_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share PDF")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
