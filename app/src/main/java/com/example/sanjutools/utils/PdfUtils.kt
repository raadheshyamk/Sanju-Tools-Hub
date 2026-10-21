package com.example.sanjutools.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object PdfUtils {

    data class PdfMeta(
        val pageCount: Int,
        val widthPt: Int,
        val heightPt: Int,
        val sizeKb: Double
    )

    fun createPdfFromImages(
        context: Context,
        images: List<Bitmap>,
        addWatermarkText: String? = null,
        addPageNumbers: Boolean = true
    ): File {
        val document = PdfDocument()
        val pageWidth = 595 // Standard A4 width in PostScript points (72 DPI)
        val pageHeight = 842 // Standard A4 height in points

        images.forEachIndexed { index, bmp ->
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Background
            canvas.drawColor(Color.WHITE)

            // Scale and center bitmap within margins (margin = 36 pt / 0.5 inch)
            val margin = 36
            val availW = pageWidth - (margin * 2)
            val availH = pageHeight - (margin * 2) - 30 // reserve space for page number

            val scale = min(availW.toFloat() / bmp.width, availH.toFloat() / bmp.height)
            val targetW = (bmp.width * scale).toInt()
            val targetH = (bmp.height * scale).toInt()
            val left = margin + (availW - targetW) / 2
            val top = margin + (availH - targetH) / 2

            val srcRect = Rect(0, 0, bmp.width, bmp.height)
            val dstRect = Rect(left, top, left + targetW, top + targetH)
            canvas.drawBitmap(bmp, srcRect, dstRect, null)

            // Optional Watermark
            if (!addWatermarkText.isNullOrBlank()) {
                val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.GRAY
                    alpha = 60
                    textSize = 42f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                canvas.save()
                canvas.rotate(-45f, pageWidth / 2f, pageHeight / 2f)
                canvas.drawText(addWatermarkText, pageWidth / 2f, pageHeight / 2f, wmPaint)
                canvas.restore()
            }

            // Optional Page numbers
            if (addPageNumbers) {
                val numPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.DKGRAY
                    textSize = 11f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Page ${index + 1} of ${images.size}", pageWidth / 2f, pageHeight - 24f, numPaint)
            }

            document.finishPage(page)
        }

        val file = File(context.cacheDir, "sanju_doc_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }

    fun renderPdfToImages(context: Context, pdfFile: File, maxPages: Int = 10): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val count = min(renderer.pageCount, maxPages)

        for (i in 0 until count) {
            val page = renderer.openPage(i)
            // Render at 2x points for sharp display
            val bmp = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.WHITE)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmaps.add(bmp)
            page.close()
        }

        renderer.close()
        pfd.close()
        return bitmaps
    }

    fun getPdfInfo(pdfFile: File): PdfMeta {
        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val count = renderer.pageCount
        val firstPage = if (count > 0) renderer.openPage(0) else null
        val w = firstPage?.width ?: 595
        val h = firstPage?.height ?: 842
        firstPage?.close()
        renderer.close()
        pfd.close()

        val sizeKb = pdfFile.length().toDouble() / 1024.0
        return PdfMeta(
            pageCount = count,
            widthPt = w,
            heightPt = h,
            sizeKb = String.format("%.2f", sizeKb).toDoubleOrNull() ?: sizeKb
        )
    }

    fun createPdfFromText(
        context: Context,
        title: String,
        bodyText: String,
        author: String = "Sanju Tools Hub"
    ): File {
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 50

        val lines = bodyText.split("\n")
        var currentLineIndex = 0
        var pageNum = 1

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 10f
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 12f
        }

        while (currentLineIndex < lines.size || pageNum == 1) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNum).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            canvas.drawColor(Color.WHITE)

            var y = margin.toFloat()

            if (pageNum == 1) {
                // Draw decorative header bar
                val barPaint = Paint().apply {
                    color = Color.parseColor("#3B82F6")
                }
                canvas.drawRect(margin.toFloat(), y, (pageWidth - margin).toFloat(), y + 4f, barPaint)
                y += 28f

                // Draw title
                canvas.drawText(title.ifBlank { "Untitled Note" }, margin.toFloat(), y, titlePaint)
                y += 18f
                canvas.drawText("Generated by $author • ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}", margin.toFloat(), y, metaPaint)
                y += 26f
            } else {
                y += 20f
            }

            // Draw content lines
            val lineHeight = 18f
            while (currentLineIndex < lines.size) {
                val line = lines[currentLineIndex]
                // Wrap text if needed
                val maxChars = 75
                if (line.length > maxChars) {
                    val chunks = line.chunked(maxChars)
                    for (c in chunks) {
                        if (y > pageHeight - margin - 30) break
                        canvas.drawText(c, margin.toFloat(), y, bodyPaint)
                        y += lineHeight
                    }
                } else {
                    if (y > pageHeight - margin - 30) break
                    canvas.drawText(line, margin.toFloat(), y, bodyPaint)
                    y += lineHeight
                }
                currentLineIndex++
                if (y > pageHeight - margin - 30) break
            }

            // Page number footer
            val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.GRAY
                textSize = 10f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("- Page $pageNum -", pageWidth / 2f, pageHeight - 25f, footerPaint)

            document.finishPage(page)
            pageNum++

            if (currentLineIndex >= lines.size) break
        }

        val file = File(context.cacheDir, "sanju_note_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()
        return file
    }
}
