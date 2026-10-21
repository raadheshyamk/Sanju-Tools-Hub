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
import com.example.ui.theme.BrandBlue
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPurple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PhotoToolsScreen(
    tool: ToolItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var outputBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var outputSizeBytes by remember { mutableStateOf<Long?>(null) }

    // Tool-specific parameter states
    var qualitySlider by remember { mutableStateOf(80f) }
    var resizeScaleSlider by remember { mutableStateOf(50f) }
    var blurRadiusSlider by remember { mutableStateOf(10f) }
    var watermarkText by remember { mutableStateOf("Sanju Tools") }
    var customText by remember { mutableStateOf("Hello World") }
    var selectedCropRatio by remember { mutableStateOf("1:1") } // 1:1, 4:3, 16:9, Passport
    var selectedSolidColor by remember { mutableStateOf(android.graphics.Color.WHITE) }
    var passportCopiesSheet by remember { mutableStateOf(true) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        withContext(Dispatchers.Main) {
                            inputBitmap = bmp
                            outputBitmap = null
                            statusMessage = "Image loaded successfully (${bmp.width}x${bmp.height} px)"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        statusMessage = "Failed to load image: ${e.localizedMessage}"
                    }
                }
            }
        }
    }

    fun processTool() {
        val bmp = inputBitmap ?: run {
            statusMessage = "Please select or upload an image first."
            return
        }

        isProcessing = true
        statusMessage = "Processing image..."

        coroutineScope.launch(Dispatchers.Default) {
            try {
                when (tool.id) {
                    "photo_jpg_to_png" -> {
                        val (resultBmp, bytes) = BitmapUtils.convertFormat(bmp, BitmapUtils.ImageFormat.PNG, 100)
                        outputBitmap = resultBmp
                        outputSizeBytes = bytes.size.toLong()
                    }
                    "photo_png_to_jpg" -> {
                        val (resultBmp, bytes) = BitmapUtils.convertFormat(bmp, BitmapUtils.ImageFormat.JPEG, qualitySlider.toInt())
                        outputBitmap = resultBmp
                        outputSizeBytes = bytes.size.toLong()
                    }
                    "photo_webp_to_png" -> {
                        val (resultBmp, bytes) = BitmapUtils.convertFormat(bmp, BitmapUtils.ImageFormat.PNG, 100)
                        outputBitmap = resultBmp
                        outputSizeBytes = bytes.size.toLong()
                    }
                    "photo_png_to_webp" -> {
                        val (resultBmp, bytes) = BitmapUtils.convertFormat(bmp, BitmapUtils.ImageFormat.WEBP, qualitySlider.toInt())
                        outputBitmap = resultBmp
                        outputSizeBytes = bytes.size.toLong()
                    }
                    "photo_compress" -> {
                        val (resultBmp, size) = BitmapUtils.compressImage(bmp, qualitySlider.toInt(), resizeScaleSlider.toInt())
                        outputBitmap = resultBmp
                        outputSizeBytes = size
                    }
                    "photo_resize" -> {
                        val targetW = (bmp.width * (resizeScaleSlider / 100f)).toInt().coerceAtLeast(10)
                        val targetH = (bmp.height * (resizeScaleSlider / 100f)).toInt().coerceAtLeast(10)
                        outputBitmap = BitmapUtils.resize(bmp, targetW, targetH)
                    }
                    "photo_crop" -> {
                        val ratio = when (selectedCropRatio) {
                            "1:1" -> 1f
                            "4:3" -> 4f / 3f
                            "16:9" -> 16f / 9f
                            "Passport" -> 35f / 45f
                            else -> 1f
                        }
                        outputBitmap = BitmapUtils.crop(bmp, ratio)
                    }
                    "photo_rotate" -> {
                        outputBitmap = BitmapUtils.rotate(bmp, 90f)
                    }
                    "photo_flip" -> {
                        outputBitmap = BitmapUtils.flip(bmp, horizontal = true, vertical = false)
                    }
                    "photo_remove_bg", "photo_transparent_bg" -> {
                        // Chroma key / color threshold background remover
                        outputBitmap = BitmapUtils.removeBackgroundThreshold(bmp, android.graphics.Color.WHITE, 45)
                    }
                    "photo_solid_bg" -> {
                        outputBitmap = BitmapUtils.addSolidBackground(bmp, selectedSolidColor)
                    }
                    "photo_black_white" -> {
                        outputBitmap = BitmapUtils.applyGrayscale(bmp)
                    }
                    "photo_blur" -> {
                        outputBitmap = BitmapUtils.applyBoxBlur(bmp, blurRadiusSlider.toInt())
                    }
                    "photo_add_text" -> {
                        outputBitmap = BitmapUtils.addTextToImage(bmp, customText, textSizeSp = 36f, position = "BOTTOM")
                    }
                    "photo_watermark" -> {
                        outputBitmap = BitmapUtils.addWatermark(bmp, watermarkText, alpha = 90, repeatDiagonal = true)
                    }
                    "photo_merge" -> {
                        outputBitmap = BitmapUtils.mergeImages(bmp, bmp, isHorizontal = true)
                    }
                    "photo_collage" -> {
                        outputBitmap = BitmapUtils.createCollage(listOf(bmp, bmp, bmp, bmp))
                    }
                    "photo_passport" -> {
                        outputBitmap = BitmapUtils.createPassportPhoto(bmp, selectedSolidColor, multiCopiesSheet = passportCopiesSheet)
                    }
                    "photo_to_pdf" -> {
                        val pdfFile = PdfUtils.createPdfFromImages(context, listOf(bmp))
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "PDF generated successfully at: ${pdfFile.name}", Toast.LENGTH_LONG).show()
                            val shareIntent = BitmapUtils.createShareIntent(context, pdfFile, "application/pdf")
                            context.startActivity(shareIntent)
                        }
                    }
                    "photo_info", "photo_quality_settings", "photo_format_converter" -> {
                        outputBitmap = bmp
                    }
                    else -> {
                        outputBitmap = bmp
                    }
                }

                withContext(Dispatchers.Main) {
                    isProcessing = false
                    statusMessage = "Processing complete!"
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isProcessing = false
                    statusMessage = "Error: ${e.localizedMessage}"
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
            // Upload / Pick Image Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (inputBitmap == null) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Upload Photo",
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Select an image from your device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { imagePicker.launch("image/*") },
                            modifier = Modifier.testTag("upload_image_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose Image")
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Selected Photo",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${inputBitmap?.width} x ${inputBitmap?.height} px",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            OutlinedButton(
                                onClick = { imagePicker.launch("image/*") }
                            ) {
                                Text("Change")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tool Parameters Controls
            if (inputBitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Tool Options",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        when (tool.id) {
                            "photo_compress", "photo_png_to_jpg", "photo_png_to_webp", "photo_quality_settings" -> {
                                Text(
                                    text = "Compression Quality: ${qualitySlider.toInt()}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Slider(
                                    value = qualitySlider,
                                    onValueChange = { qualitySlider = it },
                                    valueRange = 10f..100f,
                                    modifier = Modifier.testTag("quality_slider")
                                )
                            }
                            "photo_resize" -> {
                                Text(
                                    text = "Scale: ${resizeScaleSlider.toInt()}% (${((inputBitmap?.width ?: 100) * resizeScaleSlider / 100).toInt()} x ${((inputBitmap?.height ?: 100) * resizeScaleSlider / 100).toInt()} px)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Slider(
                                    value = resizeScaleSlider,
                                    onValueChange = { resizeScaleSlider = it },
                                    valueRange = 10f..200f
                                )
                            }
                            "photo_crop" -> {
                                Text("Select Aspect Ratio:", style = MaterialTheme.typography.bodySmall)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("1:1", "4:3", "16:9", "Passport").forEach { ratio ->
                                        FilterChip(
                                            selected = selectedCropRatio == ratio,
                                            onClick = { selectedCropRatio = ratio },
                                            label = { Text(ratio) }
                                        )
                                    }
                                }
                            }
                            "photo_blur" -> {
                                Text("Blur Intensity: ${blurRadiusSlider.toInt()}", style = MaterialTheme.typography.bodySmall)
                                Slider(
                                    value = blurRadiusSlider,
                                    onValueChange = { blurRadiusSlider = it },
                                    valueRange = 2f..25f
                                )
                            }
                            "photo_watermark" -> {
                                OutlinedTextField(
                                    value = watermarkText,
                                    onValueChange = { watermarkText = it },
                                    label = { Text("Watermark Text") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            "photo_add_text" -> {
                                OutlinedTextField(
                                    value = customText,
                                    onValueChange = { customText = it },
                                    label = { Text("Text to Overlay") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            "photo_solid_bg" -> {
                                Text("Choose Background Color:", style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(
                                        Pair("White", android.graphics.Color.WHITE),
                                        Pair("Blue", android.graphics.Color.parseColor("#3B82F6")),
                                        Pair("Purple", android.graphics.Color.parseColor("#8B5CF6")),
                                        Pair("Dark", android.graphics.Color.parseColor("#0F172A")),
                                        Pair("Red", android.graphics.Color.parseColor("#EF4444"))
                                    ).forEach { (name, colorInt) ->
                                        AssistChip(
                                            onClick = { selectedSolidColor = colorInt },
                                            label = { Text(name) },
                                            leadingIcon = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Color(colorInt))
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                            "photo_passport" -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Sheet of 4 copies for printing", style = MaterialTheme.typography.bodySmall)
                                    Switch(
                                        checked = passportCopiesSheet,
                                        onCheckedChange = { passportCopiesSheet = it }
                                    )
                                }
                            }
                            "photo_info" -> {
                                val info = inputBitmap?.let { BitmapUtils.getImageInfo(it) }
                                if (info != null) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text("• Dimensions: ${info.width} x ${info.height} px")
                                        Text("• Aspect Ratio: ${info.aspectRatio}")
                                        Text("• Megapixels: ${info.estimatedMegapixels} MP")
                                        Text("• Color Profile: ${info.config}")
                                        Text("• Memory Footprint: ${info.sizeKb} KB")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { processTool() },
                            enabled = !isProcessing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("process_tool_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Processing...")
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Apply & Process")
                            }
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
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Output Preview & Download / Share Card
            if (outputBitmap != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Output Preview",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (outputSizeBytes != null) {
                            Text(
                                text = "File Size: ${String.format("%.2f", outputSizeBytes!!.toDouble() / 1024.0)} KB",
                                style = MaterialTheme.typography.bodySmall,
                                color = BrandEmerald
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 320.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            outputBitmap?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Processed Image Preview",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    outputBitmap?.let { bmp ->
                                        val file = BitmapUtils.saveBitmapToCache(context, bmp, BitmapUtils.ImageFormat.PNG)
                                        Toast.makeText(context, "Saved image to cache: ${file.name}", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_image_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Image")
                            }

                            Button(
                                onClick = {
                                    outputBitmap?.let { bmp ->
                                        val file = BitmapUtils.saveBitmapToCache(context, bmp, BitmapUtils.ImageFormat.PNG)
                                        val shareIntent = BitmapUtils.createShareIntent(context, file, "image/png")
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Image via"))
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_image_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
