package com.kushwahahardware.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.kushwahahardware.data.entity.Customer
import com.kushwahahardware.data.entity.Sale
import com.kushwahahardware.data.entity.SaleItem
import com.kushwahahardware.data.entity.ShopInfo
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateInvoice(
        context: Context,
        sale: Sale,
        items: List<SaleItem>,
        shopInfo: ShopInfo?,
        customer: Customer?
    ): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint()

        var currentY = 50f

        // Header - Shop Name
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 24f
        titlePaint.color = Color.BLACK
        canvas.drawText(shopInfo?.shopName ?: "KUSHWAHA HARDWARE", 50f, currentY, titlePaint)
        currentY += 30f

        // Shop Address, GST & Contact
        paint.textSize = 11f
        canvas.drawText(shopInfo?.address ?: "Mahanwa, Motihari, Bihar", 50f, currentY, paint)
        currentY += 18f
        val businessGst = "10MBIPK4582N1Z4"
        canvas.drawText("GSTIN: $businessGst", 50f, currentY, paint)
        canvas.drawText("Contact: +91 8235719538", 400f, currentY, paint)
        currentY += 18f
        canvas.drawText("Email: kushwahahardware@gmail.com", 50f, currentY, paint)
        currentY += 40f

        // Invoice Info
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("TAX INVOICE", 50f, currentY, paint)
        
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date(sale.saleDate))
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        canvas.drawText("Date: $dateStr", 400f, currentY, paint)
        currentY += 20f
        canvas.drawText("Inv No: ${sale.invoiceNumber}", 50f, currentY, paint)
        currentY += 40f

        // Customer Info
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BILL TO:", 50f, currentY, paint)
        currentY += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(customer?.name ?: "Walking Customer", 50f, currentY, paint)
        customer?.phone?.let {
            currentY += 15f
            canvas.drawText("Mobile: $it", 50f, currentY, paint)
        }
        customer?.address?.let {
            currentY += 15f
            canvas.drawText("Address: $it", 50f, currentY, paint)
        }
        currentY += 40f

        // Table Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Item Details", 50f, currentY, paint)
        canvas.drawText("Qty", 320f, currentY, paint)
        canvas.drawText("Rate", 420f, currentY, paint)
        canvas.drawText("Amount", 500f, currentY, paint)
        currentY += 10f
        canvas.drawLine(50f, currentY, 550f, currentY, paint)
        currentY += 25f

        // Table Rows
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        items.forEach { item ->
            canvas.drawText(item.productName, 50f, currentY, paint)
            canvas.drawText("${item.quantity}", 320f, currentY, paint)
            canvas.drawText("%.2f".format(item.unitPrice), 420f, currentY, paint)
            canvas.drawText("%.2f".format(item.totalPrice), 500f, currentY, paint)
            currentY += 20f
        }

        currentY += 10f
        canvas.drawLine(50f, currentY, 550f, currentY, paint)
        currentY += 30f

        // Summary Calculations (GST calculation)
        val subtotal = sale.totalAmount / 1.18 // Assuming 18% GST inclusive
        val gstAmount = sale.totalAmount - subtotal
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Subtotal:", 350f, currentY, paint)
        canvas.drawText("₹%.2f".format(subtotal), 500f, currentY, paint)
        currentY += 20f
        canvas.drawText("GST (18%):", 350f, currentY, paint)
        canvas.drawText("₹%.2f".format(gstAmount), 500f, currentY, paint)
        currentY += 25f
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 14f
        canvas.drawText("Grand Total:", 350f, currentY, paint)
        canvas.drawText("₹%.2f".format(sale.totalAmount), 500f, currentY, paint)
        currentY += 25f
        
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Paid Amount:", 350f, currentY, paint)
        canvas.drawText("₹%.2f".format(sale.paidAmount), 500f, currentY, paint)
        currentY += 20f
        if (sale.pendingAmount > 0) {
            paint.color = Color.RED
            canvas.drawText("Pending Amount:", 350f, currentY, paint)
            canvas.drawText("₹%.2f".format(sale.pendingAmount), 500f, currentY, paint)
            paint.color = Color.BLACK
        }

        currentY += 40f
        
        // QR Code Section
        val upiId = "BHARATPE.8F0V1P1U0V14772@fbpe"
        val shopName = "Mr Rajbali Kumar"


        val upiUrl = "upi://pay?pa=$upiId&pn=${shopName.replace(" ", "%20")}&am=${sale.totalAmount}&cu=INR"
        
        try {
            val qrBitmap = generateQRCode(upiUrl, 100)
            if (qrBitmap != null) {
                canvas.drawBitmap(qrBitmap, 50f, currentY - 20f, paint)
                val qrTextPaint = Paint().apply {
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("Scan to Pay", 50f, currentY + 90f, qrTextPaint)
            }
        } catch (e: Exception) {}

        // Signature & Query Footer
        currentY += 120f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Authorized Signatory", 400f, currentY, paint)
        
        currentY += 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        paint.textSize = 10f
        canvas.drawText("For any queries, call on +91 8235719538", 50f, currentY, paint)
        
        currentY += 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Thank you for your business! Visit again.", 50f, currentY, paint)

        pdfDocument.finishPage(page)

        val file = File(context.cacheDir, "Invoice_${sale.invoiceNumber}.pdf")
        return try {
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun generateQRCode(text: String, size: Int): Bitmap? {
        val writer = MultiFormatWriter()
        return try {
            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
