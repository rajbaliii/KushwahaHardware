package com.kushwahahardware.data.repository

import com.kushwahahardware.data.dao.PurchaseDao
import com.kushwahahardware.data.dao.PurchaseItemDao
import com.kushwahahardware.data.dao.ProductDao
import com.kushwahahardware.data.dao.StockHistoryDao
import com.kushwahahardware.data.entity.Purchase
import com.kushwahahardware.data.entity.PurchaseItem
import com.kushwahahardware.data.entity.StockHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseRepository @Inject constructor(
    private val purchaseDao: PurchaseDao,
    private val purchaseItemDao: PurchaseItemDao,
    private val productDao: ProductDao,
    private val stockHistoryDao: StockHistoryDao,
    private val paymentTransactionDao: com.kushwahahardware.data.dao.PaymentTransactionDao
) {
    fun getAllPurchases(): Flow<List<Purchase>> = purchaseDao.getAllPurchases()
    
    suspend fun getPurchaseById(id: Long): Purchase? = purchaseDao.getPurchaseById(id)
    fun getPurchaseItems(purchaseId: Long): Flow<List<PurchaseItem>> = purchaseItemDao.getItemsForPurchase(purchaseId)
    
    suspend fun insertPurchase(purchase: Purchase, items: List<PurchaseItem>): Long {
        val purchaseId = purchaseDao.insertPurchase(purchase)
        val itemsWithId = items.map { it.copy(purchaseId = purchaseId) }
        purchaseItemDao.insertItems(itemsWithId)
        
        // Update stock for each product
        itemsWithId.forEach { item ->
            val product = productDao.getProductById(item.productId)
            if (product != null) {
                val newStock = product.currentStock + item.quantity
                productDao.updateProductStock(item.productId, newStock)
                
                // Record stock history
                stockHistoryDao.insertHistory(
                    StockHistory(
                        productId = item.productId,
                        transactionType = "PURCHASE",
                        quantity = item.quantity,
                        previousStock = product.currentStock,
                        newStock = newStock,
                        referenceId = purchaseId,
                        referenceType = "PURCHASE",
                        notes = "Purchase Invoice: ${purchase.invoiceNumber}"
                    )
                )
            }
        }
        
        return purchaseId
    }

    fun getPurchasesBySupplier(supplierId: Long): Flow<List<Purchase>> = purchaseDao.getPurchasesBySupplier(supplierId)

    /**
     * Reduce pending dues owed to a supplier by [paymentAmount] (FIFO across oldest purchases first).
     */
    suspend fun reduceSupplierPending(supplierId: Long, paymentAmount: Double, transactionType: String = "GAVE", notes: String = "Payment made") {
        var remaining = paymentAmount
        val purchases = purchaseDao.getPurchasesBySupplier(supplierId).first().sortedBy { it.purchaseDate }
        for (purchase in purchases) {
            if (remaining <= 0) break
            if (purchase.pendingAmount <= 0) continue
            val reduce = minOf(remaining, purchase.pendingAmount)
            purchaseDao.updatePurchase(
                purchase.copy(
                    paidAmount = purchase.paidAmount + reduce,
                    pendingAmount = purchase.pendingAmount - reduce
                )
            )
            remaining -= reduce
        }

        // Record payment transaction for ledger history
        paymentTransactionDao.insertTransaction(
            com.kushwahahardware.data.entity.PaymentTransaction(
                partyId = supplierId,
                partyType = "SUPPLIER",
                amount = paymentAmount,
                transactionType = transactionType,
                notes = notes
            )
        )
    }

}

