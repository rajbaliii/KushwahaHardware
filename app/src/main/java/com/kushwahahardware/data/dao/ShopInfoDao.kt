package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.ShopInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ShopInfoDao {
    
    @Query("SELECT * FROM shop_info WHERE id = 1")
    fun getShopInfo(): Flow<ShopInfo?>
    
    @Query("SELECT * FROM shop_info WHERE id = 1")
    suspend fun getShopInfoSync(): ShopInfo?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopInfo(shopInfo: ShopInfo)
    
    @Update
    suspend fun updateShopInfo(shopInfo: ShopInfo)
    
    @Query("UPDATE shop_info SET nextInvoiceNumber = nextInvoiceNumber + 1 WHERE id = 1")
    suspend fun incrementInvoiceNumber()
    
    @Query("SELECT nextInvoiceNumber FROM shop_info WHERE id = 1")
    suspend fun getNextInvoiceNumber(): Int?
}