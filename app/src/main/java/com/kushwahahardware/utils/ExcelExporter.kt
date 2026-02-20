package com.kushwahahardware.utils

import android.content.Context
import android.os.Environment
import com.kushwahahardware.data.entity.*
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

object ExcelExporter {
    
    fun exportProducts(
        context: Context,
        products: List<Product>,
        categories: Map<Long, String>
    ): File? {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Products")
            
            // Header
            val headerRow = sheet.createRow(0)
            val headers = listOf("ID", "Name", "Category", "Brand", "Size", "Color", "Unit", 
                "Purchase Price", "Selling Price", "Opening Stock", "Current Stock", "Low Stock Alert")
            
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }
            
            // Data
            products.forEachIndexed { index, product ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(product.id.toDouble())
                row.createCell(1).setCellValue(product.name)
                row.createCell(2).setCellValue(categories[product.categoryId] ?: "")
                row.createCell(3).setCellValue(product.brand)
                row.createCell(4).setCellValue(product.size)
                row.createCell(5).setCellValue(product.color)
                row.createCell(6).setCellValue(product.unit)
                row.createCell(7).setCellValue(product.purchasePrice)
                row.createCell(8).setCellValue(product.sellingPrice)
                row.createCell(9).setCellValue(product.openingStock.toDouble())
                row.createCell(10).setCellValue(product.currentStock.toDouble())
                row.createCell(11).setCellValue(product.lowStockAlert.toDouble())
            }
            
            // Auto-size columns
            headers.indices.forEach { sheet.autoSizeColumn(it) }
            
            saveWorkbook(workbook, "Products_Export_${System.currentTimeMillis()}.xlsx")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun exportSales(
        context: Context,
        sales: List<SaleWithItems>
    ): File? {
        return try {
            val workbook = XSSFWorkbook()
            
            // Sales Sheet
            val salesSheet = workbook.createSheet("Sales")
            val salesHeader = salesSheet.createRow(0)
            val salesHeaders = listOf("Invoice No", "Date", "Customer", "Phone", "Total Amount", 
                "Paid Amount", "Pending Amount", "Payment Type")
            
            salesHeaders.forEachIndexed { index, header ->
                salesHeader.createCell(index).setCellValue(header)
            }
            
            sales.forEachIndexed { index, saleWithItems ->
                val row = salesSheet.createRow(index + 1)
                val sale = saleWithItems.sale
                row.createCell(0).setCellValue(sale.getFormattedInvoiceNumber())
                row.createCell(1).setCellValue(DateUtils.formatDate(sale.date))
                row.createCell(2).setCellValue(sale.customerName)
                row.createCell(3).setCellValue(sale.customerPhone)
                row.createCell(4).setCellValue(sale.totalAmount)
                row.createCell(5).setCellValue(sale.paidAmount)
                row.createCell(6).setCellValue(sale.pendingAmount)
                row.createCell(7).setCellValue(sale.paymentType.name)
            }
            
            salesHeaders.indices.forEach { salesSheet.autoSizeColumn(it) }
            
            // Sale Items Sheet
            val itemsSheet = workbook.createSheet("Sale Items")
            val itemsHeader = itemsSheet.createRow(0)
            val itemsHeaders = listOf("Invoice No", "Product", "Quantity", "Rate", "Amount")
            
            itemsHeaders.forEachIndexed { index, header ->
                itemsHeader.createCell(index).setCellValue(header)
            }
            
            var rowIndex = 1
            sales.forEach { saleWithItems ->
                saleWithItems.items.forEach { item ->
                    val row = itemsSheet.createRow(rowIndex++)
                    row.createCell(0).setCellValue(saleWithItems.sale.getFormattedInvoiceNumber())
                    row.createCell(1).setCellValue(item.productName)
                    row.createCell(2).setCellValue(item.quantity.toDouble())
                    row.createCell(3).setCellValue(item.sellingPrice)
                    row.createCell(4).setCellValue(item.totalAmount)
                }
            }
            
            itemsHeaders.indices.forEach { itemsSheet.autoSizeColumn(it) }
            
            saveWorkbook(workbook, "Sales_Export_${System.currentTimeMillis()}.xlsx")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun exportPurchases(
        context: Context,
        purchases: List<PurchaseWithItems>
    ): File? {
        return try {
            val workbook = XSSFWorkbook()
            
            // Purchases Sheet
            val purchasesSheet = workbook.createSheet("Purchases")
            val headerRow = purchasesSheet.createRow(0)
            val headers = listOf("ID", "Date", "Invoice No", "Supplier", "Total Amount", 
                "Paid Amount", "Pending Amount")
            
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }
            
            purchases.forEachIndexed { index, purchaseWithItems ->
                val row = purchasesSheet.createRow(index + 1)
                val purchase = purchaseWithItems.purchase
                row.createCell(0).setCellValue(purchase.id.toDouble())
                row.createCell(1).setCellValue(DateUtils.formatDate(purchase.date))
                row.createCell(2).setCellValue(purchase.invoiceNumber)
                row.createCell(3).setCellValue(purchase.supplierId?.toString() ?: "")
                row.createCell(4).setCellValue(purchase.totalAmount)
                row.createCell(5).setCellValue(purchase.paidAmount)
                row.createCell(6).setCellValue(purchase.pendingAmount)
            }
            
            headers.indices.forEach { purchasesSheet.autoSizeColumn(it) }
            
            saveWorkbook(workbook, "Purchases_Export_${System.currentTimeMillis()}.xlsx")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun exportStockReport(
        context: Context,
        products: List<Product>,
        categories: Map<Long, String>
    ): File? {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Stock Report")
            
            // Header
            val headerRow = sheet.createRow(0)
            val headers = listOf("Product", "Category", "Current Stock", "Low Stock Alert", "Status")
            
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).setCellValue(header)
            }
            
            // Data
            products.forEachIndexed { index, product ->
                val row = sheet.createRow(index + 1)
                row.createCell(0).setCellValue(product.name)
                row.createCell(1).setCellValue(categories[product.categoryId] ?: "")
                row.createCell(2).setCellValue(product.currentStock.toDouble())
                row.createCell(3).setCellValue(product.lowStockAlert.toDouble())
                row.createCell(4).setCellValue(if (product.isLowStock()) "LOW STOCK" else "OK")
            }
            
            headers.indices.forEach { sheet.autoSizeColumn(it) }
            
            saveWorkbook(workbook, "Stock_Report_${System.currentTimeMillis()}.xlsx")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun saveWorkbook(workbook: Workbook, fileName: String): File? {
        return try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )
            FileOutputStream(file).use { outputStream ->
                workbook.write(outputStream)
            }
            workbook.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}