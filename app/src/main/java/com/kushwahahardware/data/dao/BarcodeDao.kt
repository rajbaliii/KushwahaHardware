package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.Barcode
import kotlinx.coroutines.flow.Flow

@Dao
interface BarcodeDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBarcode(barcode: Barcode): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBarcodes(barcodes: List<Barcode>)

    @Query("SELECT * FROM barcodes WHERE serialNumber = :serialNumber LIMIT 1")
    suspend fun getBarcodeBySerial(serialNumber: String): Barcode?

    @Query("SELECT * FROM barcodes ORDER BY createdAt DESC")
    fun getAllBarcodes(): Flow<List<Barcode>>

    @Query("SELECT COUNT(*) FROM barcodes")
    suspend fun getCount(): Int
}
