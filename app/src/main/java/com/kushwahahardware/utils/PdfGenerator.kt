package com.kushwahahardware.utils

import android.content.Context
import android.os.Environment
import com.itextpdf.text.*
import com.itextpdf.text.pdf.*
import com.kushwahahardware.data.entity.*
import java.io.File
import java.io.FileOutputStream
import java.util.*

object PdfGenerator {
    
    private const val INVOICE_WIDTH = 226f // 80mm in points
    
    fun generateInvoice(
        context: Context,
        sale: Sale,
        items: List<SaleItem>,
        shopInfo: ShopInfo
    ): File? {
        return try {
            val fileName = "Invoice_${sale.getFormattedInvoiceNumber()}_${System.currentTimeMillis()}.pdf"
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            
            val document = Document(PageSize(width = INVOICE_WIDTH, height = 800f), 10f, 10f, 10f, 10f)
            val writer = PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()
            
            // Shop Header
            val shopNameFont = Font(Font.FontFamily.HELVETICA, 14f, Font.BOLD)
            val shopInfoFont = Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL)
            val headerFont = Font(Font.FontFamily.HELVETICA, 10f, Font.BOLD)
            val normalFont = Font(Font.FontFamily.HELVETICA, 9f, Font.NORMAL)
            val smallFont = Font(Font.FontFamily.HELVETICA, 8f, Font.NORMAL)
            
            // Shop Name
            val shopNamePara = Paragraph(shopInfo.name.uppercase(), shopNameFont)
            shopNamePara.alignment = Element.ALIGN_CENTER
            document.add(shopNamePara)
            
            // Shop Location
            val locationPara = Paragraph(shopInfo.location, shopInfoFont)
            locationPara.alignment = Element.ALIGN_CENTER
            document.add(locationPara)
            
            // Divider
            document.add(Paragraph("-".repeat(32), normalFont))
            
            // Invoice Info
            val infoTable = PdfPTable(2)
            infoTable.widthPercentage = 100f
            infoTable.setWidths(floatArrayOf(1f, 1f))
            
            infoTable.addCell(createCell("Date: ${DateUtils.formatDate(sale.date)}", normalFont, Element.ALIGN_LEFT))
            infoTable.addCell(createCell("Inv: ${sale.getFormattedInvoiceNumber()}", normalFont, Element.ALIGN_RIGHT))
            
            document.add(infoTable)
            
            // Customer Info
            if (sale.customerName.isNotEmpty()) {
                document.add(Paragraph("Customer: ${sale.customerName}", normalFont))
            }
            if (sale.customerPhone.isNotEmpty()) {
                document.add(Paragraph("Phone: ${sale.customerPhone}", normalFont))
            }
            
            // Divider
            document.add(Paragraph("-".repeat(32), normalFont))
            
            // Items Header
            val itemsTable = PdfPTable(4)
            itemsTable.widthPercentage = 100f
            itemsTable.setWidths(floatArrayOf(2f, 0.8f, 1f, 1.2f))
            
            itemsTable.addCell(createCell("Item", headerFont, Element.ALIGN_LEFT))
            itemsTable.addCell(createCell("Qty", headerFont, Element.ALIGN_CENTER))
            itemsTable.addCell(createCell("Rate", headerFont, Element.ALIGN_RIGHT))
            itemsTable.addCell(createCell("Amt", headerFont, Element.ALIGN_RIGHT))
            
            // Items
            items.forEach { item ->
                itemsTable.addCell(createCell(item.productName.take(15), smallFont, Element.ALIGN_LEFT))
                itemsTable.addCell(createCell(item.quantity.toString(), smallFont, Element.ALIGN_CENTER))
                itemsTable.addCell(createCell(CurrencyUtils.formatWithoutSymbol(item.sellingPrice), smallFont, Element.ALIGN_RIGHT))
                itemsTable.addCell(createCell(CurrencyUtils.formatWithoutSymbol(item.totalAmount), smallFont, Element.ALIGN_RIGHT))
            }
            
            document.add(itemsTable)
            
            // Divider
            document.add(Paragraph("-".repeat(32), normalFont))
            
            // Totals
            val totalsTable = PdfPTable(2)
            totalsTable.widthPercentage = 100f
            totalsTable.setWidths(floatArrayOf(1f, 1f))
            
            totalsTable.addCell(createCell("Total:", headerFont, Element.ALIGN_LEFT, false))
            totalsTable.addCell(createCell(CurrencyUtils.formatWithSymbol(sale.totalAmount), headerFont, Element.ALIGN_RIGHT, false))
            
            totalsTable.addCell(createCell("Paid:", normalFont, Element.ALIGN_LEFT, false))
            totalsTable.addCell(createCell(CurrencyUtils.formatWithSymbol(sale.paidAmount), normalFont, Element.ALIGN_RIGHT, false))
            
            if (sale.pendingAmount > 0) {
                totalsTable.addCell(createCell("Pending:", normalFont, Element.ALIGN_LEFT, false))
                totalsTable.addCell(createCell(CurrencyUtils.formatWithSymbol(sale.pendingAmount), normalFont, Element.ALIGN_RIGHT, false))
            }
            
            totalsTable.addCell(createCell("Payment:", normalFont, Element.ALIGN_LEFT, false))
            totalsTable.addCell(createCell(sale.paymentType.name, normalFont, Element.ALIGN_RIGHT, false))
            
            document.add(totalsTable)
            
            // Divider
            document.add(Paragraph("-".repeat(32), normalFont))
            
            // Thank You
            val thankYouPara = Paragraph("Thank you for your purchase!", shopInfoFont)
            thankYouPara.alignment = Element.ALIGN_CENTER
            document.add(thankYouPara)
            
            document.close()
            writer.close()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun generateReport(
        context: Context,
        title: String,
        data: List<Pair<String, String>>,
        shopInfo: ShopInfo
    ): File? {
        return try {
            val fileName = "${title.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            
            val document = Document(PageSize.A4, 36f, 36f, 36f, 36f)
            val writer = PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()
            
            val titleFont = Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
            val headerFont = Font(Font.FontFamily.HELVETICA, 12f, Font.BOLD)
            val normalFont = Font(Font.FontFamily.HELVETICA, 10f, Font.NORMAL)
            
            // Shop Header
            val shopNamePara = Paragraph(shopInfo.name, titleFont)
            shopNamePara.alignment = Element.ALIGN_CENTER
            document.add(shopNamePara)
            
            val locationPara = Paragraph(shopInfo.location, normalFont)
            locationPara.alignment = Element.ALIGN_CENTER
            document.add(locationPara)
            
            document.add(Paragraph(" "))
            
            // Report Title
            val reportTitle = Paragraph(title, headerFont)
            reportTitle.alignment = Element.ALIGN_CENTER
            document.add(reportTitle)
            
            document.add(Paragraph("Date: ${DateUtils.formatDate(System.currentTimeMillis())}", normalFont))
            document.add(Paragraph(" "))
            
            // Data Table
            val table = PdfPTable(2)
            table.widthPercentage = 100f
            
            data.forEach { (key, value) ->
                table.addCell(createCell(key, normalFont, Element.ALIGN_LEFT))
                table.addCell(createCell(value, normalFont, Element.ALIGN_RIGHT))
            }
            
            document.add(table)
            document.close()
            writer.close()
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun createCell(text: String, font: Font, alignment: Int, withBorder: Boolean = true): PdfPCell {
        val cell = PdfPCell(Phrase(text, font))
        cell.horizontalAlignment = alignment
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        cell.border = if (withBorder) Rectangle.BOTTOM else Rectangle.NO_BORDER
        cell.paddingTop = 2f
        cell.paddingBottom = 2f
        return cell
    }
}