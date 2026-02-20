package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.StockHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface StockHistoryDao {
    
    @Query("SELECT * FROM stock_history ORDER BY date DESC")
    fun getAllStockHistory(): Flow<List<StockHistory>>
    
    @Query("SELECT * FROM stock_history WHERE productId = :productId ORDER BY date DESC")
    fun getStockHistoryByProduct(productId: Long): Flow<List<StockHistory>>
    
    @Query("SELECT * FROM stock_history WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getStockHistoryByDateRange(startDate: Long, endDate: Long): Flow<List<StockHistory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockHistory(stockHistory: StockHistory): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockHistories(histories: List<StockHistory>)
    
    @Delete
    suspend fun deleteStockHistory(stockHistory: StockHistory)
    
    @Query("DELETE FROM stock_history WHERE productId = :productId")
    suspend fun deleteStockHistoryByProduct(productId: Long)
}