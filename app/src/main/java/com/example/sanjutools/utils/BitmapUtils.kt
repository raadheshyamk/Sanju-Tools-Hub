package com.example.sanjutools.utils

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min

object BitmapUtils {

    enum class ImageFormat(val extension: String, val mimeType: String, val compressFormat: Bitmap.CompressFormat) {
        PNG("png", "image/png", Bitmap.CompressFormat.PNG),
        JPEG("jpg", "image/jpeg", Bitmap.CompressFormat.JPEG),
        WEBP("webp", "image/webp", Bitmap.CompressFormat.WEBP)
    }

    data class ImageInfo(
        val width: Int,
        val height: Int,
        val aspectRatio: String,
        val sizeKb: Double,
        val config: String,
        val estimatedMegapixels: Double
    )

    fun getImageInfo(bitmap: Bitmap, byteSize: Long = 0): ImageInfo {
        val w = bitmap.width
        val h = bitmap.height
        val gcd = gcd(w, h)
        val ar = "${w / gcd}:${h / gcd}"
        val size = if (byteSize > 0) byteSize.toDouble() / 1024.0 else (bitmap.byteCount.toDouble() / 1024.0)
        val mp = (w.toLong() * h.toLong()).toDouble() / 1_000_000.0
        return ImageInfo(
            width = w,
            height = h,
            aspectRatio = ar,
            sizeKb = String.format("%.2f", size).toDoubleOrNull() ?: size,
            config = bitmap.config?.name ?: "ARGB_8888",
            estimatedMegapixels = String.format("%.2f", mp).toDoubleOrNull() ?: mp
        )
    }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    fun convertFormat(
        bitmap: Bitmap,
        targetFormat: ImageFormat,
        quality: Int = 90
    ): Pair<Bitmap, ByteArray> {
        val stream = ByteArrayOutputStream()
        bitmap.compress(targetFormat.compressFormat, quality.coerceIn(1, 100), stream)
        val bytes = stream.toByteArray()
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: bitmap.copy(Bitmap.Config.ARGB_8888, true)
        return Pair(decoded, bytes)
    }

    fun compressImage(
        bitmap: Bitmap,
        quality: Int,
        scalePercent: Int = 100
    ): Pair<Bitmap, Long> {
        val scaled = if (scalePercent < 100 && scalePercent > 10) {
            val nw = (bitmap.width * scalePercent) / 100
            val nh = (bitmap.height * scalePercent) / 100
            Bitmap.createScaledBitmap(bitmap, max(1, nw), max(1, nh), true)
        } else {
            bitmap
        }
        val stream = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(5, 100), stream)
        val bytes = stream.toByteArray()
        val compressed = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: scaled
        return Pair(compressed, bytes.size.toLong())
    }

    fun resize(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val w = max(1, targetWidth)
        val h = max(1, targetHeight)
        return Bitmap.createScaledBitmap(bitmap, w, h, true)
    }

    fun crop(bitmap: Bitmap, targetAspectRatio: Float): Bitmap {
        val currentAspect = bitmap.width.toFloat() / bitmap.height.toFloat()
        val cropW: Int
        val cropH: Int
        val startX: Int
        val startY: Int

        if (currentAspect > targetAspectRatio) {
            cropH = bitmap.height
            cropW = (cropH * targetAspectRatio).toInt()
            startX = (bitmap.width - cropW) / 2
            startY = 0
        } else {
            cropW = bitmap.width
            cropH = (cropW / targetAspectRatio).toInt()
            startX = 0
            startY = (bitmap.height - cropH) / 2
        }

        return Bitmap.createBitmap(
            bitmap,
            max(0, startX),
            max(0, startY),
            min(bitmap.width, cropW),
            min(bitmap.height, cropH)
        )
    }

    fun rotate(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun flip(bitmap: Bitmap, horizontal: Boolean, vertical: Boolean): Bitmap {
        val sx = if (horizontal) -1f else 1f
        val sy = if (vertical) -1f else 1f
        val matrix = Matrix().apply { postScale(sx, sy) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun applyGrayscale(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    fun applyBoxBlur(bitmap: Bitmap, radius: Int = 12): Bitmap {
        val r = radius.coerceIn(1, 25)
        val smallW = max(1, bitmap.width / r)
        val smallH = max(1, bitmap.height / r)
        val small = Bitmap.createScaledBitmap(bitmap, smallW, smallH, true)
        return Bitmap.createScaledBitmap(small, bitmap.width, bitmap.height, true)
    }

    fun removeBackgroundThreshold(
        bitmap: Bitmap,
        targetColorHex: Int = android.graphics.Color.WHITE,
        threshold: Int = 40
    ): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val width = result.width
        val height = result.height
        val pixels = IntArray(width * height)
        result.getPixels(pixels, 0, width, 0, 0, width, height)

        val targetR = android.graphics.Color.red(targetColorHex)
        val targetG = android.graphics.Color.green(targetColorHex)
        val targetB = android.graphics.Color.blue(targetColorHex)

        for (i in pixels.indices) {
            val c = pixels[i]
            val r = android.graphics.Color.red(c)
            val g = android.graphics.Color.green(c)
            val b = android.graphics.Color.blue(c)

            val diff = (Math.abs(r - targetR) + Math.abs(g - targetG) + Math.abs(b - targetB)) / 3
            if (diff <= threshold) {
                pixels[i] = 0 // Transparent
            }
        }
        result.setPixels(pixels, 0, width, 0, 0, width, height)
        return result
    }

    fun addSolidBackground(bitmap: Bitmap, colorHex: Int): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(colorHex)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return result
    }

    fun addTextToImage(
        bitmap: Bitmap,
        text: String,
        textSizeSp: Float = 36f,
        textColor: Int = android.graphics.Color.WHITE,
        position: String = "BOTTOM" // TOP, CENTER, BOTTOM
    ): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = textSizeSp * (bitmap.width / 400f).coerceAtLeast(1f)
            textAlign = Paint.Align.CENTER
            setShadowLayer(8f, 2f, 2f, android.graphics.Color.BLACK)
        }

        val x = canvas.width / 2f
        val y = when (position.uppercase()) {
            "TOP" -> paint.textSize * 1.5f
            "CENTER" -> (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f)
            else -> canvas.height - paint.textSize * 0.8f
        }

        canvas.drawText(text, x, y, paint)
        return result
    }

    fun addWatermark(
        bitmap: Bitmap,
        watermarkText: String,
        alpha: Int = 100,
        repeatDiagonal: Boolean = true
    ): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            this.alpha = alpha.coerceIn(20, 255)
            textSize = (bitmap.width / 15f).coerceAtLeast(24f)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        if (repeatDiagonal) {
            canvas.save()
            canvas.rotate(-30f, canvas.width / 2f, canvas.height / 2f)
            val stepY = paint.textSize * 3.5f
            val stepX = paint.measureText(watermarkText) * 1.8f
            var y = -canvas.height.toFloat()
            while (y < canvas.height * 2) {
                var x = -canvas.width.toFloat()
                while (x < canvas.width * 2) {
                    canvas.drawText(watermarkText, x, y, paint)
                    x += stepX
                }
                y += stepY
            }
            canvas.restore()
        } else {
            canvas.drawText(watermarkText, canvas.width / 2f, canvas.height - 40f, paint)
        }
        return result
    }

    fun mergeImages(bitmap1: Bitmap, bitmap2: Bitmap, isHorizontal: Boolean = true): Bitmap {
        val resultWidth = if (isHorizontal) bitmap1.width + bitmap2.width else max(bitmap1.width, bitmap2.width)
        val resultHeight = if (isHorizontal) max(bitmap1.height, bitmap2.height) else bitmap1.height + bitmap2.height
        val result = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(android.graphics.Color.WHITE)

        canvas.drawBitmap(bitmap1, 0f, 0f, null)
        if (isHorizontal) {
            canvas.drawBitmap(bitmap2, bitmap1.width.toFloat(), 0f, null)
        } else {
            canvas.drawBitmap(bitmap2, 0f, bitmap1.height.toFloat(), null)
        }
        return result
    }

    fun createCollage(images: List<Bitmap>): Bitmap {
        if (images.isEmpty()) return Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        if (images.size == 1) return images[0]

        val targetSize = 800
        val cellW = (targetSize - 30) / 2
        val cellH = (targetSize - 30) / 2
        val result = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawColor(android.graphics.Color.parseColor("#0F172A"))

        val positions = listOf(
            Pair(10, 10),
            Pair(targetSize / 2 + 5, 10),
            Pair(10, targetSize / 2 + 5),
            Pair(targetSize / 2 + 5, targetSize / 2 + 5)
        )

        for (i in 0 until min(4, images.size)) {
            val img = Bitmap.createScaledBitmap(images[i], cellW, cellH, true)
            val pos = positions[i]
            canvas.drawBitmap(img, pos.first.toFloat(), pos.second.toFloat(), null)
        }
        return result
    }

    fun createPassportPhoto(
        bitmap: Bitmap,
        backgroundHex: Int = android.graphics.Color.WHITE,
        multiCopiesSheet: Boolean = true
    ): Bitmap {
        // Standard passport aspect ratio is 35mm x 45mm (approx 7:9 or 0.777)
        val passportAspect = 35f / 45f
        val cropped = crop(bitmap, passportAspect)
        val singlePassport = Bitmap.createScaledBitmap(cropped, 413, 531, true) // 300 DPI approx 35x45mm

        if (!multiCopiesSheet) {
            val canvasBmp = Bitmap.createBitmap(singlePassport.width, singlePassport.height, Bitmap.Config.ARGB_8888)
            val c = Canvas(canvasBmp)
            c.drawColor(backgroundHex)
            c.drawBitmap(singlePassport, 0f, 0f, null)
            return canvasBmp
        }

        // 2x4 grid sheet on 4x6 inch canvas (1200 x 1800 px)
        val sheetW = 1200
        val sheetH = 1800
        val sheet = Bitmap.createBitmap(sheetW, sheetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)
        canvas.drawColor(android.graphics.Color.WHITE)

        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            color = android.graphics.Color.LTGRAY
            strokeWidth = 2f
        }

        val pw = 450
        val ph = (pw / passportAspect).toInt()
        val scaled = Bitmap.createScaledBitmap(singlePassport, pw, ph, true)

        val marginX = (sheetW - (pw * 2)) / 3
        val marginY = (sheetH - (ph * 2)) / 3

        for (row in 0..1) {
            for (col in 0..1) {
                val x = marginX + col * (pw + marginX)
                val y = marginY + row * (ph + marginY)
                canvas.drawBitmap(scaled, x.toFloat(), y.toFloat(), null)
                canvas.drawRect(
                    x.toFloat(),
                    y.toFloat(),
                    (x + pw).toFloat(),
                    (y + ph).toFloat(),
                    borderPaint
                )
            }
        }
        return sheet
    }

    fun saveBitmapToCache(
        context: Context,
        bitmap: Bitmap,
        format: ImageFormat = ImageFormat.PNG,
        quality: Int = 95
    ): File {
        val filename = "sanju_tool_${System.currentTimeMillis()}.${format.extension}"
        val file = File(context.cacheDir, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(format.compressFormat, quality, out)
        }
        return file
    }

    fun createShareIntent(context: Context, file: File, mimeType: String = "image/png"): Intent {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
