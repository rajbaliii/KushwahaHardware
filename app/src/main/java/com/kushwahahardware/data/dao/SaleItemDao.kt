package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.SaleItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleItemDao {
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getItemsForSale(saleId: Long): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getItemsForSaleSync(saleId: Long): List<SaleItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Delete
    suspend fun deleteSaleItem(item: SaleItem)
}
