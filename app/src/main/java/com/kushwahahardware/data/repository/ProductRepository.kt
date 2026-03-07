package com.kushwahahardware.data.repository

import com.kushwahahardware.data.dao.ProductDao
import com.kushwahahardware.data.entity.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    fun getAllActiveProducts(): Flow<List<Product>> = productDao.getAllActiveProducts()
    fun getLowStockProducts(): Flow<List<Product>> = productDao.getLowStockProducts()
    
    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)
    suspend fun getActiveProductsCount(): Int = productDao.getActiveProductsCount()
    suspend fun getLowStockCount(): Int = productDao.getLowStockCount()
    
    suspend fun insertProduct(product: Product) = productDao.insertProduct(product)
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    suspend fun updateProductStock(id: Long, stock: Double) = productDao.updateProductStock(id, stock)

    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts("%$query%")
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>> = productDao.getProductsByCategory(categoryId)
    suspend fun getProductBySku(sku: String): Product? = productDao.getProductBySku(sku)
}
