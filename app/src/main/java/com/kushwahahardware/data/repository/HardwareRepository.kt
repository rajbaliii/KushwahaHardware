package com.kushwahahardware.data.repository

import com.kushwahahardware.data.dao.*
import com.kushwahahardware.data.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareRepository @Inject constructor(
    private val productDao: ProductDao,
    private val categoryDao: CategoryDao,
    private val supplierDao: SupplierDao,
    private val purchaseDao: PurchaseDao,
    private val saleDao: SaleDao,
    private val customerDao: CustomerDao,
    private val stockHistoryDao: StockHistoryDao,
    private val shopInfoDao: ShopInfoDao
) {
    
    // ==================== Products ====================
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    
    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)
    
    suspend fun getProductByBarcode(barcode: String): Product? = productDao.getProductByBarcode(barcode)
    
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>> = productDao.getProductsByCategory(categoryId)
    
    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)
    
    fun getLowStockProducts(): Flow<List<Product>> = productDao.getLowStockProducts()
    
    fun getTotalProductCount(): Flow<Int> = productDao.getTotalProductCount()
    
    fun getTotalInventoryValue(): Flow<Double?> = productDao.getTotalInventoryValue()
    
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    
    suspend fun increaseStock(productId: Long, quantity: Int) = productDao.increaseStock(productId, quantity)
    
    suspend fun decreaseStock(productId: Long, quantity: Int) = productDao.decreaseStock(productId, quantity)
    
    fun getAllBrands(): Flow<List<String>> = productDao.getAllBrands()
    
    // ==================== Categories ====================
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    
    suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)
    
    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
    
    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)
    
    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
    
    // ==================== Suppliers ====================
    fun getAllSuppliers(): Flow<List<Supplier>> = supplierDao.getAllSuppliers()
    
    suspend fun getSupplierById(id: Long): Supplier? = supplierDao.getSupplierById(id)
    
    fun searchSuppliers(query: String): Flow<List<Supplier>> = supplierDao.searchSuppliers(query)
    
    fun getTotalSupplierDue(): Flow<Double?> = supplierDao.getTotalSupplierDue()
    
    suspend fun insertSupplier(supplier: Supplier): Long = supplierDao.insertSupplier(supplier)
    
    suspend fun updateSupplier(supplier: Supplier) = supplierDao.updateSupplier(supplier)
    
    suspend fun deleteSupplier(supplier: Supplier) = supplierDao.deleteSupplier(supplier)
    
    suspend fun updateSupplierDue(supplierId: Long, amount: Double) = supplierDao.updateSupplierDue(supplierId, amount)
    
    // ==================== Purchases ====================
    fun getAllPurchases(): Flow<List<Purchase>> = purchaseDao.getAllPurchases()
    
    suspend fun getPurchaseById(id: Long): Purchase? = purchaseDao.getPurchaseById(id)
    
    fun getPurchasesBySupplier(supplierId: Long): Flow<List<Purchase>> = purchaseDao.getPurchasesBySupplier(supplierId)
    
    fun getPurchasesByDateRange(startDate: Long, endDate: Long): Flow<List<Purchase>> = 
        purchaseDao.getPurchasesByDateRange(startDate, endDate)
    
    fun getPendingPurchases(): Flow<List<Purchase>> = purchaseDao.getPendingPurchases()
    
    fun getTotalPurchaseAmount(startDate: Long, endDate: Long): Flow<Double?> = 
        purchaseDao.getTotalPurchaseAmount(startDate, endDate)
    
    suspend fun insertPurchase(purchase: Purchase): Long = purchaseDao.insertPurchase(purchase)
    
    suspend fun updatePurchase(purchase: Purchase) = purchaseDao.updatePurchase(purchase)
    
    suspend fun deletePurchase(purchase: Purchase) = purchaseDao.deletePurchase(purchase)
    
    suspend fun getPurchaseWithItems(purchaseId: Long): PurchaseWithItems? = purchaseDao.getPurchaseWithItems(purchaseId)
    
    fun getAllPurchasesWithItems(): Flow<List<PurchaseWithItems>> = purchaseDao.getAllPurchasesWithItems()
    
    // Purchase Items
    suspend fun insertPurchaseItem(purchaseItem: PurchaseItem): Long = purchaseDao.insertPurchaseItem(purchaseItem)
    
    suspend fun insertPurchaseItems(items: List<PurchaseItem>) = purchaseDao.insertPurchaseItems(items)
    
    suspend fun getPurchaseItems(purchaseId: Long): List<PurchaseItem> = purchaseDao.getPurchaseItems(purchaseId)
    
    // ==================== Sales ====================
    fun getAllSales(): Flow<List<Sale>> = saleDao.getAllSales()
    
    suspend fun getSaleById(id: Long): Sale? = saleDao.getSaleById(id)
    
    fun getSalesByCustomer(customerId: Long): Flow<List<Sale>> = saleDao.getSalesByCustomer(customerId)
    
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<Sale>> = 
        saleDao.getSalesByDateRange(startDate, endDate)
    
    fun getTodaySales(startOfDay: Long, endOfDay: Long): Flow<List<Sale>> = 
        saleDao.getTodaySales(startOfDay, endOfDay)
    
    fun getPendingSales(): Flow<List<Sale>> = saleDao.getPendingSales()
    
    fun getTotalSalesAmount(startDate: Long, endDate: Long): Flow<Double?> = 
        saleDao.getTotalSalesAmount(startDate, endDate)
    
    fun getTodayTotalSales(startOfDay: Long, endOfDay: Long): Flow<Double?> = 
        saleDao.getTodayTotalSales(startOfDay, endOfDay)
    
    fun getTotalCustomerPending(): Flow<Double?> = saleDao.getTotalCustomerPending()
    
    fun searchSales(query: String): Flow<List<Sale>> = saleDao.searchSales(query)
    
    suspend fun insertSale(sale: Sale): Long = saleDao.insertSale(sale)
    
    suspend fun updateSale(sale: Sale) = saleDao.updateSale(sale)
    
    suspend fun deleteSale(sale: Sale) = saleDao.deleteSale(sale)
    
    suspend fun getSaleWithItems(saleId: Long): SaleWithItems? = saleDao.getSaleWithItems(saleId)
    
    fun getAllSalesWithItems(): Flow<List<SaleWithItems>> = saleDao.getAllSalesWithItems()
    
    // Sale Items
    suspend fun insertSaleItem(saleItem: SaleItem): Long = saleDao.insertSaleItem(saleItem)
    
    suspend fun insertSaleItems(items: List<SaleItem>) = saleDao.insertSaleItems(items)
    
    suspend fun getSaleItems(saleId: Long): List<SaleItem> = saleDao.getSaleItems(saleId)
    
    // ==================== Customers ====================
    fun getAllCustomers(): Flow<List<Customer>> = customerDao.getAllCustomers()
    
    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)
    
    suspend fun getCustomerByPhone(phone: String): Customer? = customerDao.getCustomerByPhone(phone)
    
    fun searchCustomers(query: String): Flow<List<Customer>> = customerDao.searchCustomers(query)
    
    fun getTotalCustomerDue(): Flow<Double?> = customerDao.getTotalCustomerDue()
    
    suspend fun insertCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)
    
    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)
    
    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)
    
    suspend fun updateCustomerDue(customerId: Long, amount: Double) = customerDao.updateCustomerDue(customerId, amount)
    
    // ==================== Stock History ====================
    fun getAllStockHistory(): Flow<List<StockHistory>> = stockHistoryDao.getAllStockHistory()
    
    fun getStockHistoryByProduct(productId: Long): Flow<List<StockHistory>> = 
        stockHistoryDao.getStockHistoryByProduct(productId)
    
    suspend fun insertStockHistory(stockHistory: StockHistory): Long = stockHistoryDao.insertStockHistory(stockHistory)
    
    // ==================== Shop Info ====================
    fun getShopInfo(): Flow<ShopInfo?> = shopInfoDao.getShopInfo()
    
    suspend fun getShopInfoSync(): ShopInfo? = shopInfoDao.getShopInfoSync()
    
    suspend fun updateShopInfo(shopInfo: ShopInfo) = shopInfoDao.updateShopInfo(shopInfo)
    
    suspend fun incrementInvoiceNumber() = shopInfoDao.incrementInvoiceNumber()
    
    suspend fun getNextInvoiceNumber(): Int? = shopInfoDao.getNextInvoiceNumber()
    
    // ==================== Dashboard Summary ====================
    fun getDashboardSummary(startOfDay: Long, endOfDay: Long): Flow<DashboardSummary> {
        return combine(
            productDao.getTotalProductCount(),
            saleDao.getTodayTotalSales(startOfDay, endOfDay),
            combine(
                supplierDao.getTotalSupplierDue(),
                saleDao.getTotalCustomerPending()
            ) { supplierDue, customerPending ->
                (supplierDue ?: 0.0) + (customerPending ?: 0.0)
            },
            productDao.getLowStockProducts()
        ) { totalProducts, todaySales, totalPending, lowStockProducts ->
            DashboardSummary(
                totalProducts = totalProducts,
                todaySales = todaySales ?: 0.0,
                totalPending = totalPending,
                lowStockProducts = lowStockProducts
            )
        }
    }
}

data class DashboardSummary(
    val totalProducts: Int = 0,
    val todaySales: Double = 0.0,
    val totalPending: Double = 0.0,
    val lowStockProducts: List<Product> = emptyList()
)