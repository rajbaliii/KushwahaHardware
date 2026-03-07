package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.PurchaseItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseItemDao {
    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    fun getItemsForPurchase(purchaseId: Long): Flow<List<PurchaseItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: PurchaseItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PurchaseItem>): List<Long>

    @Update
    suspend fun updateItem(item: PurchaseItem)

    @Delete
    suspend fun deleteItem(item: PurchaseItem)

    @Query("DELETE FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun deleteItemsForPurchase(purchaseId: Long)
}
