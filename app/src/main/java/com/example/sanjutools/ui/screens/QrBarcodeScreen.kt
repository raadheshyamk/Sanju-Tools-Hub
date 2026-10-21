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
import androidx.compose.foundation.text.selection.SelectionContainer
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
import com.example.sanjutools.utils.QrBarcodeUtils
import com.example.sanjutools.utils.TextUtils
import com.example.ui.theme.BrandEmerald
import com.example.ui.theme.BrandPurple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun QrBarcodeScreen(
    tool: ToolItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Form inputs
    var textInput by remember { mutableStateOf("https://google.com") }
    var wifiSsid by remember { mutableStateOf("Home_WiFi") }
    var wifiPassword by remember { mutableStateOf("SecretPassword123") }
    var wifiEnc by remember { mutableStateOf("WPA") }

    var contactName by remember { mutableStateOf("Sanju") }
    var contactPhone by remember { mutableStateOf("+1 234 567 8900") }
    var contactEmail by remember { mutableStateOf("sanju@example.com") }
    var contactCompany by remember { mutableStateOf("Sanju Hub") }

    var emailTo by remember { mutableStateOf("support@example.com") }
    var emailSubject by remember { mutableStateOf("Inquiry") }
    var emailBody by remember { mutableStateOf("Hello Sanju Tools Hub,") }

    var phoneInput by remember { mutableStateOf("+1234567890") }
    var whatsAppPhone by remember { mutableStateOf("+1234567890") }
    var whatsAppMsg by remember { mutableStateOf("Hi! Connecting from Sanju Tools Hub") }

    var upiId by remember { mutableStateOf("sanju@upi") }
    var upiName by remember { mutableStateOf("Sanju") }
    var upiAmount by remember { mutableStateOf("100") }

    var barcodeContent by remember { mutableStateOf("123456789012") }
    var selectedBarcodeFormat by remember { mutableStateOf("Code 128") } // Code 128, EAN-13, UPC-A

    // Color customization
    var qrFgColor by remember { mutableStateOf(android.graphics.Color.BLACK) }
    var qrBgColor by remember { mutableStateOf(android.graphics.Color.WHITE) }

    // Results
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var decodedResultText by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Image picker for scanning QR / Barcode
    val scanImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) {
                            val decoded = QrBarcodeUtils.decodeFromBitmap(bmp)
                            withContext(Dispatchers.Main) {
                                decodedResultText = decoded
                            }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        decodedResultText = "Failed to decode image: ${e.localizedMessage}"
                    }
                }
            }
        }
    }

    fun generate() {
        isProcessing = true
        coroutineScope.launch(Dispatchers.Default) {
            val contentToEncode: String = when (tool.id) {
                "qr_text", "qr_customize_colors", "qr_download", "qr_logo" -> textInput
                "qr_url" -> if (textInput.startsWith("http://") || textInput.startsWith("https://")) textInput else "https://$textInput"
                "qr_wifi" -> QrBarcodeUtils.formatWifiQr(wifiSsid, wifiPassword, wifiEnc)
                "qr_contact" -> QrBarcodeUtils.formatVCard(contactName, contactPhone, contactEmail, contactCompany)
                "qr_email" -> QrBarcodeUtils.formatEmailQr(emailTo, emailSubject, emailBody)
                "qr_phone" -> QrBarcodeUtils.formatPhoneQr(phoneInput)
                "qr_whatsapp" -> QrBarcodeUtils.formatWhatsAppQr(whatsAppPhone, whatsAppMsg)
                "qr_payment" -> QrBarcodeUtils.formatUpiPayment(upiId, upiName, upiAmount)
                "barcode_generate" -> barcodeContent
                else -> textInput
            }

            val bmp = if (tool.id == "barcode_generate") {
                val format = when (selectedBarcodeFormat) {
                    "EAN-13" -> com.google.zxing.BarcodeFormat.EAN_13
                    "UPC-A" -> com.google.zxing.BarcodeFormat.UPC_A
                    else -> com.google.zxing.BarcodeFormat.CODE_128
                }
                QrBarcodeUtils.generateBarcode(contentToEncode, format, 600, 200, qrFgColor, qrBgColor)
            } else {
                QrBarcodeUtils.generateQrBitmap(contentToEncode, 512, qrFgColor, qrBgColor)
            }

            withContext(Dispatchers.Main) {
                generatedBitmap = bmp
                isProcessing = false
            }
        }
    }

    // Auto-generate initial preview on launch
    LaunchedEffect(tool.id) {
        if (!tool.id.contains("read") && !tool.id.contains("decode")) {
            generate()
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
            // Scanner / Image Decoder Mode
            if (tool.id == "qr_read_image" || tool.id == "barcode_decode") {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (tool.id == "qr_read_image") "Upload image with QR code" else "Upload photo containing barcode",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Supports screenshots, downloaded images, and camera photos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { scanImagePicker.launch("image/*") },
                            modifier = Modifier.testTag("scan_upload_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.UploadFile, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Image to Scan")
                        }
                    }
                }

                if (decodedResultText != null) {
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
                                Text(
                                    text = "Decoded Data",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BrandEmerald
                                )
                                IconButton(
                                    onClick = {
                                        TextUtils.copyToClipboard(context, decodedResultText ?: "")
                                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            SelectionContainer {
                                Text(
                                    text = decodedResultText ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            } else {
                // Generator Mode Inputs
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Configure Details",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        when (tool.id) {
                            "qr_text", "qr_customize_colors", "qr_download", "qr_logo" -> {
                                OutlinedTextField(
                                    value = textInput,
                                    onValueChange = { textInput = it },
                                    label = { Text("Text or Message") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 4
                                )
                            }
                            "qr_url" -> {
                                OutlinedTextField(
                                    value = textInput,
                                    onValueChange = { textInput = it },
                                    label = { Text("Website URL (e.g. https://...)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            "qr_wifi" -> {
                                OutlinedTextField(
                                    value = wifiSsid,
                                    onValueChange = { wifiSsid = it },
                                    label = { Text("Wi-Fi SSID (Network Name)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = wifiPassword,
                                    onValueChange = { wifiPassword = it },
                                    label = { Text("Wi-Fi Password") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            "qr_contact" -> {
                                OutlinedTextField(
                                    value = contactName,
                                    onValueChange = { contactName = it },
                                    label = { Text("Full Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = contactPhone,
                                    onValueChange = { contactPhone = it },
                                    label = { Text("Phone Number") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = contactEmail,
                                    onValueChange = { contactEmail = it },
                                    label = { Text("Email Address") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            "qr_email" -> {
                                OutlinedTextField(
                                    value = emailTo,
                                    onValueChange = { emailTo = it },
                                    label = { Text("Recipient Email") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = emailSubject,
                                    onValueChange = { emailSubject = it },
                                    label = { Text("Subject") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            "qr_phone" -> {
                                OutlinedTextField(
                                    value = phoneInput,
                                    onValueChange = { phoneInput = it },
                                    label = { Text("Phone Number to Dial") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            "qr_whatsapp" -> {
                                OutlinedTextField(
                                    value = whatsAppPhone,
                                    onValueChange = { whatsAppPhone = it },
                                    label = { Text("WhatsApp Number (with country code)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = whatsAppMsg,
                                    onValueChange = { whatsAppMsg = it },
                                    label = { Text("Default Message") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            "qr_payment" -> {
                                OutlinedTextField(
                                    value = upiId,
                                    onValueChange = { upiId = it },
                                    label = { Text("UPI ID / Payee VPA") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = upiAmount,
                                    onValueChange = { upiAmount = it },
                                    label = { Text("Amount (INR)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            "barcode_generate" -> {
                                OutlinedTextField(
                                    value = barcodeContent,
                                    onValueChange = { barcodeContent = it },
                                    label = { Text("Barcode Number/Text") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Format:", style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("Code 128", "EAN-13", "UPC-A").forEach { fmt ->
                                        FilterChip(
                                            selected = selectedBarcodeFormat == fmt,
                                            onClick = { selectedBarcodeFormat = fmt },
                                            label = { Text(fmt) }
                                        )
                                    }
                                }
                            }
                        }

                        // Color Customization Chips
                        if (tool.id == "qr_customize_colors") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("QR Code Color Theme:", style = MaterialTheme.typography.bodySmall)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    Pair("Classic Dark", Pair(android.graphics.Color.BLACK, android.graphics.Color.WHITE)),
                                    Pair("Indigo Blue", Pair(android.graphics.Color.parseColor("#1E40AF"), android.graphics.Color.WHITE)),
                                    Pair("Royal Purple", Pair(android.graphics.Color.parseColor("#6B21A8"), android.graphics.Color.WHITE)),
                                    Pair("Emerald", Pair(android.graphics.Color.parseColor("#065F46"), android.graphics.Color.WHITE))
                                ).forEach { (name, colors) ->
                                    AssistChip(
                                        onClick = {
                                            qrFgColor = colors.first
                                            qrBgColor = colors.second
                                            generate()
                                        },
                                        label = { Text(name) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { generate() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("generate_qr_barcode_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate Code")
                        }
                    }
                }

                // Code Preview & Save/Share Card
                if (generatedBitmap != null) {
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
                                text = "Code Preview",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                generatedBitmap?.let { bmp ->
                                    Image(
                                        bitmap = bmp.asImageBitmap(),
                                        contentDescription = "Generated Code",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        generatedBitmap?.let { bmp ->
                                            val file = BitmapUtils.saveBitmapToCache(context, bmp, BitmapUtils.ImageFormat.PNG)
                                            Toast.makeText(context, "Saved to cache: ${file.name}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("download_qr_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandEmerald)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download PNG")
                                }

                                Button(
                                    onClick = {
                                        generatedBitmap?.let { bmp ->
                                            val file = BitmapUtils.saveBitmapToCache(context, bmp, BitmapUtils.ImageFormat.PNG)
                                            val shareIntent = BitmapUtils.createShareIntent(context, file, "image/png")
                                            context.startActivity(Intent.createChooser(shareIntent, "Share QR Code via"))
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("share_qr_button"),
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
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
