package com.kushwahahardware.data.repository

import com.kushwahahardware.data.dao.ProductDao
import com.kushwahahardware.data.dao.SaleDao
import com.kushwahahardware.data.dao.SaleItemDao
import com.kushwahahardware.data.dao.StockHistoryDao
import com.kushwahahardware.data.entity.Product
import com.kushwahahardware.data.entity.Sale
import com.kushwahahardware.data.entity.SaleItem
import com.kushwahahardware.data.entity.StockHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleRepository @Inject constructor(
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val productDao: ProductDao,
    private val stockHistoryDao: StockHistoryDao,
    private val paymentTransactionDao: com.kushwahahardware.data.dao.PaymentTransactionDao
) {
    fun getAllSales(): Flow<List<Sale>> = saleDao.getAllSales()
    fun getSalesByDateRange(start: Long, end: Long): Flow<List<Sale>> = saleDao.getSalesByDateRange(start, end)
    
    suspend fun getSaleById(id: Long): Sale? = saleDao.getSaleById(id)
    fun getSaleItems(saleId: Long): Flow<List<SaleItem>> = saleItemDao.getItemsForSale(saleId)
    fun getSalesByCustomer(customerId: Long): Flow<List<Sale>> = saleDao.getSalesByCustomer(customerId)
    
    suspend fun insertSale(sale: Sale, items: List<SaleItem>): Long {
        var totalProfit = 0.0
        val processedItems = items.map { item ->
            val product = productDao.getProductById(item.productId)
            val purchasePrice = product?.purchasePrice ?: 0.0
            val profitPerUnit = item.unitPrice - purchasePrice
            val itemProfit = profitPerUnit * item.quantity
            totalProfit += itemProfit
            
            item.copy(purchasePriceAtSale = purchasePrice)
        }

        val saleWithProfit = sale.copy(totalProfit = totalProfit)
        val saleId = saleDao.insertSale(saleWithProfit)
        
        val itemsWithId = processedItems.map { item -> 
            // Also deduct stock and record history
            val product = productDao.getProductById(item.productId)
            if (product != null) {
                val newStock = product.currentStock - item.quantity
                productDao.updateProductStock(product.id, newStock)
                
                stockHistoryDao.insertHistory(
                    StockHistory(
                        productId = product.id,
                        transactionType = "SALE",
                        quantity = item.quantity,
                        previousStock = product.currentStock,
                        newStock = newStock,
                        referenceId = saleId,
                        referenceType = "SALE",
                        notes = "Sale: ${sale.invoiceNumber}"
                    )
                )
            }
            item.copy(saleId = saleId) 
        }
        
        saleItemDao.insertSaleItems(itemsWithId)
        return saleId
    }

    suspend fun getTodaySalesAmount(startOfDay: Long): Double = saleDao.getTodaySalesAmount(startOfDay) ?: 0.0
    suspend fun getTotalPendingAmount(): Double = saleDao.getTotalPendingAmount() ?: 0.0

    suspend fun getTotalProfit(startDate: Long, endDate: Long): Double = 
        saleDao.getTotalProfitByDateRange(startDate, endDate) ?: 0.0

    suspend fun getSalesDataByDate(timestamp: Long): Double {
        val nextDay = timestamp + 86400000L
        return saleDao.getTotalSalesByDateRange(timestamp, nextDay) ?: 0.0
    }

    /**
     * Reduce pending dues for a customer by [paymentAmount].
     * Works through the oldest sales first (FIFO) and reduces pendingAmount, 
     * increasing paidAmount accordingly.
     */
    /**
     * Reduce pending dues for a customer by [paymentAmount] (FIFO across oldest sales first).
     */
    suspend fun reduceCustomerPending(customerId: Long, paymentAmount: Double, transactionType: String = "GOT", notes: String = "Payment received") {
        var remaining = paymentAmount
        val sales = saleDao.getSalesByCustomer(customerId).first().sortedBy { it.saleDate }
        for (sale in sales) {
            if (remaining <= 0) break
            if (sale.pendingAmount <= 0) continue
            val reduce = minOf(remaining, sale.pendingAmount)
            saleDao.updateSale(
                sale.copy(
                    paidAmount = sale.paidAmount + reduce,
                    pendingAmount = sale.pendingAmount - reduce,
                    updatedAt = System.currentTimeMillis()
                )
            )
            remaining -= reduce
        }
        
        // Record payment transaction for ledger history
        paymentTransactionDao.insertTransaction(
            com.kushwahahardware.data.entity.PaymentTransaction(
                partyId = customerId,
                partyType = "CUSTOMER",
                amount = paymentAmount,
                transactionType = transactionType,
                notes = notes
            )
        )
    }

}

