package com.kushwahahardware.data.repository

import com.kushwahahardware.data.dao.BarcodeDao
import com.kushwahahardware.data.entity.Barcode
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BarcodeRepository @Inject constructor(
    private val barcodeDao: BarcodeDao
) {
    fun getAllBarcodes(): Flow<List<Barcode>> = barcodeDao.getAllBarcodes()

    suspend fun generateUniqueBarcodes(count: Int): List<String> {
        val generatedSerials = mutableListOf<String>()
        val datePrefix = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
        
        var i = 0
        while (i < count) {
            // Format: BC-YYYYMMDD-XXXXXX (6 random alphanumeric)
            val randomPart = UUID.randomUUID().toString().substring(0, 6).uppercase()
            val serial = "BC-$datePrefix-$randomPart"
            
            // Check uniqueness in DB
            if (barcodeDao.getBarcodeBySerial(serial) == null && !generatedSerials.contains(serial)) {
                barcodeDao.insertBarcode(Barcode(serialNumber = serial))
                generatedSerials.add(serial)
                i++
            }
        }
        return generatedSerials
    }
}
