package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.Purchase
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseById(id: Long): Purchase?

    @Query("SELECT * FROM purchases WHERE invoiceNumber = :invoiceNumber")
    suspend fun getPurchaseByInvoiceNumber(invoiceNumber: String): Purchase?

    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY purchaseDate DESC")
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE purchaseDate >= :startDate AND purchaseDate <= :endDate ORDER BY purchaseDate DESC")
    fun getPurchasesByDateRange(startDate: Long, endDate: Long): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE pendingAmount > 0 ORDER BY purchaseDate DESC")
    fun getPendingPayments(): Flow<List<Purchase>>

    @Query("SELECT SUM(totalAmount) FROM purchases WHERE purchaseDate >= :startDate AND purchaseDate <= :endDate")
    suspend fun getTotalPurchasesByDateRange(startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(pendingAmount) FROM purchases WHERE pendingAmount > 0")
    suspend fun getTotalPendingPayments(): Double?

    @Query("SELECT COUNT(*) FROM purchases WHERE purchaseDate >= :startDate AND purchaseDate <= :endDate")
    suspend fun getPurchasesCountByDateRange(startDate: Long, endDate: Long): Int

    @Query("SELECT COUNT(*) FROM purchases WHERE pendingAmount > 0")
    suspend fun getPendingPaymentsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(purchases: List<Purchase>): List<Long>

    @Update
    suspend fun updatePurchase(purchase: Purchase)

    @Delete
    suspend fun deletePurchase(purchase: Purchase)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deletePurchaseById(id: Long)
}
