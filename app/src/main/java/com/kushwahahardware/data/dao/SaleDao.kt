package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.Sale
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY saleDate DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): Sale?

    @Query("SELECT * FROM sales WHERE invoiceNumber = :invoiceNumber")
    suspend fun getSaleByInvoiceNumber(invoiceNumber: String): Sale?

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY saleDate DESC")
    fun getSalesByCustomer(customerId: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE saleDate >= :startDate AND saleDate <= :endDate ORDER BY saleDate DESC")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE pendingAmount > 0 ORDER BY saleDate DESC")
    fun getPendingPayments(): Flow<List<Sale>>

    @Query("SELECT SUM(totalAmount) FROM sales WHERE saleDate >= :startDate AND saleDate <= :endDate")
    suspend fun getTotalSalesByDateRange(startDate: Long, endDate: Long): Double?

    @Query("SELECT SUM(pendingAmount) FROM sales WHERE pendingAmount > 0")
    suspend fun getTotalPendingPayments(): Double?

    @Query("SELECT COUNT(*) FROM sales WHERE saleDate >= :startDate AND saleDate <= :endDate")
    suspend fun getSalesCountByDateRange(startDate: Long, endDate: Long): Int

    @Query("SELECT COUNT(*) FROM sales WHERE pendingAmount > 0")
    suspend fun getPendingPaymentsCount(): Int

    @Query("SELECT SUM(totalAmount) FROM sales WHERE saleDate >= :startOfDay")
    suspend fun getTodaySalesAmount(startOfDay: Long): Double?

    @Query("SELECT SUM(pendingAmount) FROM sales")
    suspend fun getTotalPendingAmount(): Double?

    @Query("SELECT SUM(totalProfit) FROM sales WHERE saleDate >= :startDate AND saleDate <= :endDate")
    suspend fun getTotalProfitByDateRange(startDate: Long, endDate: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<Sale>): List<Long>

    @Update
    suspend fun updateSale(sale: Sale)

    @Delete
    suspend fun deleteSale(sale: Sale)

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteSaleById(id: Long)
}
