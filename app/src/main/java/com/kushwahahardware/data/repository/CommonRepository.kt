package com.kushwahahardware.data.repository

import com.kushwahahardware.data.dao.CustomerDao
import com.kushwahahardware.data.dao.SupplierDao
import com.kushwahahardware.data.dao.CategoryDao
import com.kushwahahardware.data.dao.ShopInfoDao
import com.kushwahahardware.data.dao.StockHistoryDao
import com.kushwahahardware.data.entity.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommonRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val supplierDao: SupplierDao,
    private val categoryDao: CategoryDao,
    private val shopInfoDao: ShopInfoDao,
    private val stockHistoryDao: StockHistoryDao
) {
    // Customers
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()
    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)
    suspend fun insertCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    // Suppliers
    fun getAllSuppliers(): Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    suspend fun getSupplierById(id: Long): Supplier? = supplierDao.getSupplierById(id)
    suspend fun insertSupplier(supplier: Supplier): Long = supplierDao.insertSupplier(supplier)
    suspend fun updateSupplier(supplier: Supplier) = supplierDao.updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = supplierDao.deleteSupplier(supplier)

    // Categories
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    suspend fun insertCategory(category: Category) = categoryDao.insertCategory(category)
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)

    // Shop Info
    fun getShopInfo(): Flow<ShopInfo?> = shopInfoDao.getShopInfo()
    suspend fun updateShopInfo(shopInfo: ShopInfo) = shopInfoDao.insertOrUpdate(shopInfo)

    // Stock History
    fun getAllStockHistory(): Flow<List<StockHistory>> = stockHistoryDao.getAllHistory()
    suspend fun insertStockHistory(history: StockHistory) = stockHistoryDao.insertHistory(history)
}
