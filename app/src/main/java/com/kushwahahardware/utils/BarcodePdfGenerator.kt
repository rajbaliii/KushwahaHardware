package com.kushwahahardware.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Environment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.properties.UnitValue

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object BarcodePdfGenerator {

    fun generateBarcodePdf(context: Context, serials: List<String>): String? {
        val fileName = "Barcodes_${System.currentTimeMillis()}.pdf"
        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return null

        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                val writer = PdfWriter(outputStream)
                val pdf = PdfDocument(writer)
                val document = Document(pdf, PageSize.A4)
                document.setMargins(20f, 20f, 20f, 20f)

                val title = Paragraph("Kushwaha Hardware - Generated Barcodes")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18f)
                    .setBold()
                document.add(title)
                document.add(Paragraph("\n"))

                val table = Table(UnitValue.createPointArray(floatArrayOf(180f, 180f, 180f)))
                table.setHorizontalAlignment(HorizontalAlignment.CENTER)

                for (serial in serials) {
                    val barcodeBitmap = createBarcodeBitmap(serial, 300, 100)
                    if (barcodeBitmap != null) {
                        val stream = ByteArrayOutputStream()
                        barcodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                        val imageData = ImageDataFactory.create(stream.toByteArray())
                        val image = Image(imageData).scaleToFit(150f, 50f)
                        
                        val cell = Cell()
                            .add(image.setHorizontalAlignment(HorizontalAlignment.CENTER))
                            .add(Paragraph(serial).setTextAlignment(TextAlignment.CENTER).setFontSize(8f))
                            .setPadding(10f)
                            .setKeepTogether(true)
                        
                        table.addCell(cell)
                    }
                }

                document.add(table)
                document.close()
            }
            return uri.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }


    private fun createBarcodeBitmap(data: String, width: Int, height: Int): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                data,
                BarcodeFormat.CODE_128,
                width,
                height
            )
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
