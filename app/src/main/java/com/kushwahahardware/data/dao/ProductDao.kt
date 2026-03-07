package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE sku = :sku")
    suspend fun getProductBySku(sku: String): Product?

    @Query("SELECT * FROM products WHERE name LIKE :search OR sku LIKE :search ORDER BY name ASC")
    fun searchProducts(search: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isActive = 1 ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE currentStock <= minStockLevel AND isActive = 1 ORDER BY name ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products WHERE currentStock <= minStockLevel AND isActive = 1")
    suspend fun getLowStockCount(): Int

    @Query("SELECT SUM(currentStock) FROM products WHERE isActive = 1")
    suspend fun getTotalStock(): Double?

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    suspend fun getActiveProductsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>): List<Long>

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET isActive = 0 WHERE id = :id")
    suspend fun deactivateProduct(id: Long)

    @Query("UPDATE products SET currentStock = :stock WHERE id = :id")
    suspend fun updateProductStock(id: Long, stock: Double)
    @Upsert
    suspend fun insertOrUpdate(product: Product): Long
}
