package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.Sale
import com.kushwahahardware.data.entity.SaleItem
import com.kushwahahardware.data.entity.SaleWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {
    
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSales(): Flow<List<Sale>>
    
    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: Long): Sale?
    
    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY date DESC")
    fun getSalesByCustomer(customerId: Long): Flow<List<Sale>>
    
    @Query("SELECT * FROM sales WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<Sale>>
    
    @Query("SELECT * FROM sales WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date DESC")
    fun getTodaySales(startOfDay: Long, endOfDay: Long): Flow<List<Sale>>
    
    @Query("SELECT * FROM sales WHERE pendingAmount > 0 ORDER BY date DESC")
    fun getPendingSales(): Flow<List<Sale>>
    
    @Query("SELECT SUM(totalAmount) FROM sales WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalSalesAmount(startDate: Long, endDate: Long): Flow<Double?>
    
    @Query("SELECT SUM(totalAmount) FROM sales WHERE date >= :startOfDay AND date < :endOfDay")
    fun getTodayTotalSales(startOfDay: Long, endOfDay: Long): Flow<Double?>
    
    @Query("SELECT SUM(pendingAmount) FROM sales")
    fun getTotalCustomerPending(): Flow<Double?>
    
    @Query("SELECT * FROM sales WHERE customerName LIKE '%' || :query || '%' OR invoiceNumber LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchSales(query: String): Flow<List<Sale>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale): Long
    
    @Update
    suspend fun updateSale(sale: Sale)
    
    @Delete
    suspend fun deleteSale(sale: Sale)
    
    @Transaction
    @Query("SELECT * FROM sales WHERE id = :saleId")
    suspend fun getSaleWithItems(saleId: Long): SaleWithItems?
    
    @Transaction
    @Query("SELECT * FROM sales ORDER BY date DESC")
    fun getAllSalesWithItems(): Flow<List<SaleWithItems>>
    
    // Sale Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItem(saleItem: SaleItem): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)
    
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: Long): List<SaleItem>
    
    @Delete
    suspend fun deleteSaleItem(saleItem: SaleItem)
    
    @Query("DELETE FROM sale_items WHERE saleId = :saleId")
    suspend fun deleteSaleItemsBySaleId(saleId: Long)
}