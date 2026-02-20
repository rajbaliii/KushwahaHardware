package com.kushwahahardware.data.dao

import androidx.room.*
import com.kushwahahardware.data.entity.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?
    
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?
    
    @Query("SELECT * FROM products WHERE categoryId = :categoryId ORDER BY name ASC")
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE brand = :brand ORDER BY name ASC")
    fun getProductsByBrand(brand: String): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE currentStock <= lowStockAlert ORDER BY name ASC")
    fun getLowStockProducts(): Flow<List<Product>>
    
    @Query("SELECT COUNT(*) FROM products")
    fun getTotalProductCount(): Flow<Int>
    
    @Query("SELECT SUM(currentStock * purchasePrice) FROM products")
    fun getTotalInventoryValue(): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long
    
    @Update
    suspend fun updateProduct(product: Product)
    
    @Delete
    suspend fun deleteProduct(product: Product)
    
    @Query("UPDATE products SET currentStock = currentStock + :quantity WHERE id = :productId")
    suspend fun increaseStock(productId: Long, quantity: Int)
    
    @Query("UPDATE products SET currentStock = currentStock - :quantity WHERE id = :productId")
    suspend fun decreaseStock(productId: Long, quantity: Int)
    
    @Query("SELECT DISTINCT brand FROM products WHERE brand != '' ORDER BY brand ASC")
    fun getAllBrands(): Flow<List<String>>
}