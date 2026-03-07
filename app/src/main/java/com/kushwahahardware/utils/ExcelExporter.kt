package com.kushwahahardware.utils

import android.content.Context
import android.os.Environment
import com.kushwahahardware.data.entity.Product
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class ExcelExporter(private val context: Context) {

    fun exportProducts(products: List<Product>): File? {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Products")
        
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("Name")
        headerRow.createCell(1).setCellValue("SKU")
        headerRow.createCell(2).setCellValue("Purchase Price")
        headerRow.createCell(3).setCellValue("Selling Price")
        headerRow.createCell(4).setCellValue("Stock")
        
        products.forEachIndexed { index, product ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(product.name)
            row.createCell(1).setCellValue(product.sku ?: "")
            row.createCell(2).setCellValue(product.purchasePrice)
            row.createCell(3).setCellValue(product.sellingPrice)
            row.createCell(4).setCellValue(product.currentStock)
        }
        
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "Products_Export.xlsx")
        try {
            val fos = FileOutputStream(file)
            workbook.write(fos)
            fos.close()
            workbook.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
