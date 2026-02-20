package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.Purchase
import com.kushwahahardware.data.entity.PurchaseItem
import com.kushwahahardware.data.entity.PurchaseWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    
    @Query("SELECT * FROM purchases ORDER BY date DESC")
    fun getAllPurchases(): Flow<List<Purchase>>
    
    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseById(id: Long): Purchase?
    
    @Query("SELECT * FROM purchases WHERE supplierId = :supplierId ORDER BY date DESC")
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<Purchase>>
    
    @Query("SELECT * FROM purchases WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getPurchasesByDateRange(startDate: Long, endDate: Long): Flow<List<Purchase>>
    
    @Query("SELECT * FROM purchases WHERE pendingAmount > 0 ORDER BY date DESC")
    fun getPendingPurchases(): Flow<List<Purchase>>
    
    @Query("SELECT SUM(totalAmount) FROM purchases WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalPurchaseAmount(startDate: Long, endDate: Long): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long
    
    @Update
    suspend fun updatePurchase(purchase: Purchase)
    
    @Delete
    suspend fun deletePurchase(purchase: Purchase)
    
    @Transaction
    @Query("SELECT * FROM purchases WHERE id = :purchaseId")
    suspend fun getPurchaseWithItems(purchaseId: Long): PurchaseWithItems?
    
    @Transaction
    @Query("SELECT * FROM purchases ORDER BY date DESC")
    fun getAllPurchasesWithItems(): Flow<List<PurchaseWithItems>>
    
    // Purchase Items
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItem(purchaseItem: PurchaseItem): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItem>)
    
    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseItems(purchaseId: Long): List<PurchaseItem>
    
    @Delete
    suspend fun deletePurchaseItem(purchaseItem: PurchaseItem)
    
    @Query("DELETE FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun deletePurchaseItemsByPurchaseId(purchaseId: Long)
}