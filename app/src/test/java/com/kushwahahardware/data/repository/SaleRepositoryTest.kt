package com.kushwahahardware.data.repository

import com.kushwahahardware.data.dao.*
import com.kushwahahardware.data.entity.*
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class SaleRepositoryTest {

    private lateinit var saleDao: SaleDao
    private lateinit var saleItemDao: SaleItemDao
    private lateinit var productDao: ProductDao
    private lateinit var stockHistoryDao: StockHistoryDao
    private lateinit var paymentTransactionDao: PaymentTransactionDao
    private lateinit var repository: SaleRepository

    @Before
    fun setup() {
        saleDao = mockk(relaxed = true)
        saleItemDao = mockk(relaxed = true)
        productDao = mockk(relaxed = true)
        stockHistoryDao = mockk(relaxed = true)
        paymentTransactionDao = mockk(relaxed = true)
        repository = SaleRepository(
            saleDao,
            saleItemDao,
            productDao,
            stockHistoryDao,
            paymentTransactionDao
        )
    }

    @Test
    fun `insertSale should calculate profit correctly`() = runTest {
        val product = Product(
            id = 1L,
            name = "Test Product",
            purchasePrice = 100.0,
            sellingPrice = 150.0,
            currentStock = 10.0
        )
        val sale = Sale(invoiceNumber = "INV001", customerId = 1L, totalAmount = 300.0)
        val saleItem = SaleItem(
            saleId = 0L,    // will be updated by repo
            productId = 1L,
            quantity = 2.0,
            unitPrice = 150.0,
            totalPrice = 300.0
        )

        coEvery { productDao.getProductById(1L) } returns product
        coEvery { saleDao.insertSale(any()) } returns 1L

        val saleId = repository.insertSale(sale, listOf(saleItem))

        assertEquals(1L, saleId)

        // Verify profit: (150 - 100) * 2 = 100
        coVerify {
            saleDao.insertSale(match { it.totalProfit == 100.0 })
        }
    }

    @Test
    fun `insertSale should deduct stock after sale`() = runTest {
        val product = Product(
            id = 2L,
            name = "Product B",
            purchasePrice = 50.0,
            sellingPrice = 80.0,
            currentStock = 20.0
        )
        val sale = Sale(invoiceNumber = "INV002", totalAmount = 240.0)
        val saleItem = SaleItem(
            saleId = 0L,
            productId = 2L,
            quantity = 3.0,
            unitPrice = 80.0,
            totalPrice = 240.0
        )

        coEvery { productDao.getProductById(2L) } returns product
        coEvery { saleDao.insertSale(any()) } returns 2L

        repository.insertSale(sale, listOf(saleItem))

        // Verify stock update: 20 - 3 = 17
        coVerify { productDao.updateProductStock(2L, 17.0) }
    }

    @Test
    fun `insertSale should record stock history entry`() = runTest {
        val product = Product(
            id = 3L,
            name = "Product C",
            purchasePrice = 200.0,
            sellingPrice = 300.0,
            currentStock = 5.0
        )
        val sale = Sale(invoiceNumber = "INV003", totalAmount = 300.0)
        val saleItem = SaleItem(
            saleId = 0L,
            productId = 3L,
            quantity = 1.0,
            unitPrice = 300.0,
            totalPrice = 300.0
        )

        coEvery { productDao.getProductById(3L) } returns product
        coEvery { saleDao.insertSale(any()) } returns 3L

        repository.insertSale(sale, listOf(saleItem))

        // Verify stock history entry
        coVerify {
            stockHistoryDao.insertHistory(match {
                it.productId == 3L &&
                it.quantity == 1.0 &&
                it.previousStock == 5.0 &&
                it.newStock == 4.0 &&
                it.transactionType == "SALE"
            })
        }
    }
}
