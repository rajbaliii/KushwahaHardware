package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.StockHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface StockHistoryDao {
    @Query("SELECT * FROM stock_history ORDER BY createdAt DESC")
    fun getAllStockHistory(): Flow<List<StockHistory>>

    @Query("SELECT * FROM stock_history WHERE productId = :productId ORDER BY createdAt DESC")
    fun getStockHistoryByProduct(productId: Long): Flow<List<StockHistory>>

    @Query("SELECT * FROM stock_history WHERE transactionType = :transactionType ORDER BY createdAt DESC")
    fun getStockHistoryByType(transactionType: String): Flow<List<StockHistory>>

    @Query("SELECT * FROM stock_history WHERE createdAt >= :startDate AND createdAt <= :endDate ORDER BY createdAt DESC")
    fun getStockHistoryByDateRange(startDate: Long, endDate: Long): Flow<List<StockHistory>>

    @Query("SELECT * FROM stock_history WHERE referenceId = :referenceId AND referenceType = :referenceType")
    suspend fun getStockHistoryByReference(referenceId: Long, referenceType: String): List<StockHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockHistory(stockHistory: StockHistory): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockHistoryList(stockHistoryList: List<StockHistory>): List<Long>

    @Delete
    suspend fun deleteStockHistory(stockHistory: StockHistory)

    @Query("DELETE FROM stock_history WHERE id = :id")
    suspend fun deleteStockHistoryById(id: Long)

    @Query("DELETE FROM stock_history WHERE productId = :productId")
    suspend fun deleteStockHistoryByProduct(productId: Long)

    @Query("SELECT COUNT(*) FROM stock_history WHERE createdAt >= :startDate AND createdAt <= :endDate")
    suspend fun getStockHistoryCountByDateRange(startDate: Long, endDate: Long): Int
    @Query("SELECT * FROM stock_history ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<StockHistory>>

    @Upsert
    suspend fun insertHistory(stockHistory: StockHistory): Long
}
