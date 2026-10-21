package com.example.sanjutools.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.google.zxing.*
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.oned.Code128Writer
import com.google.zxing.oned.EAN13Writer
import com.google.zxing.oned.UPCAWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.net.URLEncoder

object QrBarcodeUtils {

    fun generateQrBitmap(
        content: String,
        size: Int = 512,
        foregroundColor: Int = android.graphics.Color.BLACK,
        backgroundColor: Int = android.graphics.Color.WHITE,
        logoBitmap: Bitmap? = null
    ): Bitmap {
        val hints = mutableMapOf<EncodeHintType, Any>().apply {
            put(EncodeHintType.CHARACTER_SET, "UTF-8")
            put(EncodeHintType.MARGIN, 1)
            put(EncodeHintType.ERROR_CORRECTION, if (logoBitmap != null) ErrorCorrectionLevel.H else ErrorCorrectionLevel.M)
        }

        val writer = QRCodeWriter()
        val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
            }
        }

        val qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        qrBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        if (logoBitmap != null) {
            val combined = qrBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(combined)
            val logoSize = (width * 0.22f).toInt()
            val scaledLogo = Bitmap.createScaledBitmap(logoBitmap, logoSize, logoSize, true)

            val left = (width - logoSize) / 2f
            val top = (height - logoSize) / 2f

            val bgPaint = Paint().apply {
                color = backgroundColor
                style = Paint.Style.FILL
            }
            // Draw a protective background badge behind the logo
            canvas.drawRoundRect(left - 6f, top - 6f, left + logoSize + 6f, top + logoSize + 6f, 12f, 12f, bgPaint)
            canvas.drawBitmap(scaledLogo, left, top, null)
            return combined
        }

        return qrBitmap
    }

    fun generateBarcode(
        content: String,
        format: BarcodeFormat = BarcodeFormat.CODE_128,
        width: Int = 600,
        height: Int = 200,
        foregroundColor: Int = android.graphics.Color.BLACK,
        backgroundColor: Int = android.graphics.Color.WHITE
    ): Bitmap {
        val cleanContent = when (format) {
            BarcodeFormat.EAN_13 -> {
                val digits = content.filter { it.isDigit() }
                if (digits.length >= 13) digits.take(13) else digits.padEnd(12, '0') + calculateEan13CheckDigit(digits.padEnd(12, '0'))
            }
            BarcodeFormat.UPC_A -> {
                val digits = content.filter { it.isDigit() }
                if (digits.length >= 12) digits.take(12) else digits.padEnd(11, '0') + calculateUpcCheckDigit(digits.padEnd(11, '0'))
            }
            else -> content
        }

        val writer: Writer = when (format) {
            BarcodeFormat.EAN_13 -> EAN13Writer()
            BarcodeFormat.UPC_A -> UPCAWriter()
            else -> Code128Writer()
        }

        val bitMatrix = writer.encode(cleanContent, format, width, height)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)

        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
            }
        }

        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, w, 0, 0, w, h)
        return bmp
    }

    private fun calculateEan13CheckDigit(twelveDigits: String): String {
        var sum = 0
        for (i in 0 until 12) {
            val d = twelveDigits[i].digitToInt()
            sum += if (i % 2 == 0) d else d * 3
        }
        val check = (10 - (sum % 10)) % 10
        return check.toString()
    }

    private fun calculateUpcCheckDigit(elevenDigits: String): String {
        var sum = 0
        for (i in 0 until 11) {
            val d = elevenDigits[i].digitToInt()
            sum += if (i % 2 == 0) d * 3 else d
        }
        val check = (10 - (sum % 10)) % 10
        return check.toString()
    }

    fun decodeFromBitmap(bitmap: Bitmap): String {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()

        return try {
            val result = reader.decode(binaryBitmap)
            result.text ?: "No content found in barcode."
        } catch (e: Exception) {
            // Try inverted if first pass fails
            try {
                val invertedSource = source.invert()
                val invertedBmp = BinaryBitmap(HybridBinarizer(invertedSource))
                val result = reader.decode(invertedBmp)
                result.text ?: "Could not detect valid barcode/QR code."
            } catch (e2: Exception) {
                "Could not detect valid barcode or QR code in this image. Please ensure good lighting and clear contrast."
            }
        }
    }

    // Specific QR Format Helpers
    fun formatWifiQr(ssid: String, password: String, encryption: String = "WPA"): String {
        val cleanEnc = when (encryption.uppercase()) {
            "WEP" -> "WEP"
            "OPEN", "NONE" -> "nopass"
            else -> "WPA"
        }
        return "WIFI:S:$ssid;T:$cleanEnc;P:$password;;"
    }

    fun formatVCard(
        name: String,
        phone: String,
        email: String,
        company: String = "",
        title: String = ""
    ): String {
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            appendLine("FN:$name")
            if (company.isNotBlank()) appendLine("ORG:$company")
            if (title.isNotBlank()) appendLine("TITLE:$title")
            if (phone.isNotBlank()) appendLine("TEL;TYPE=CELL:$phone")
            if (email.isNotBlank()) appendLine("EMAIL:$email")
            append("END:VCARD")
        }
    }

    fun formatEmailQr(email: String, subject: String = "", body: String = ""): String {
        val encodedSubject = URLEncoder.encode(subject, "UTF-8").replace("+", "%20")
        val encodedBody = URLEncoder.encode(body, "UTF-8").replace("+", "%20")
        return "mailto:$email?subject=$encodedSubject&body=$encodedBody"
    }

    fun formatPhoneQr(phone: String): String = "tel:${phone.replace(" ", "")}"

    fun formatWhatsAppQr(phone: String, message: String = ""): String {
        val cleanPhone = phone.filter { it.isDigit() || it == '+' }
        val encodedMsg = URLEncoder.encode(message, "UTF-8")
        return "https://wa.me/$cleanPhone?text=$encodedMsg"
    }

    fun formatUpiPayment(pa: String, pn: String, am: String, cu: String = "INR"): String {
        return "upi://pay?pa=$pa&pn=${URLEncoder.encode(pn, "UTF-8")}&am=$am&cu=$cu"
    }
}
