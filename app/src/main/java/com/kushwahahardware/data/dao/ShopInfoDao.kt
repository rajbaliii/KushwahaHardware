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

    @Upsert
    suspend fun insertOrUpdate(shopInfo: ShopInfo)

    @Update
    suspend fun updateShopInfo(shopInfo: ShopInfo)

    @Query("DELETE FROM shop_info WHERE id = 1")
    suspend fun deleteShopInfo()
}
