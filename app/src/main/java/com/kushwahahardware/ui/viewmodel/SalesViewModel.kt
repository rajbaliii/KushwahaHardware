package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.*
import com.kushwahahardware.data.repository.HardwareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val repository: HardwareRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()
    
    private val _saleItems = MutableStateFlow<List<SaleItemData>>(emptyList())
    val saleItems: StateFlow<List<SaleItemData>> = _saleItems.asStateFlow()
    
    init {
        loadSales()
        loadProducts()
        loadCustomers()
    }
    
    private fun loadSales() {
        viewModelScope.launch {
            repository.getAllSalesWithItems().collect { sales ->
                _uiState.update { it.copy(
                    sales = sales,
                    isLoading = false
                ) }
            }
        }
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            repository.getAllProducts().collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
    }
    
    private fun loadCustomers() {
        viewModelScope.launch {
            repository.getAllCustomers().collect { customers ->
                _uiState.update { it.copy(customers = customers) }
            }
        }
    }
    
    fun addSaleItem(product: Product, quantity: Int) {
        if (quantity > product.currentStock) {
            _uiState.update { it.copy(
                message = "Insufficient stock. Available: ${product.currentStock}",
                isError = true
            ) }
            return
        }
        
        val items = _saleItems.value.toMutableList()
        items.add(
            SaleItemData(
                product = product,
                quantity = quantity,
                sellingPrice = product.sellingPrice
            )
        )
        _saleItems.value = items
        updateTotals()
    }
    
    fun removeSaleItem(index: Int) {
        val items = _saleItems.value.toMutableList()
        if (index in items.indices) {
            items.removeAt(index)
            _saleItems.value = items
            updateTotals()
        }
    }
    
    fun clearSaleItems() {
        _saleItems.value = emptyList()
        updateTotals()
    }
    
    private fun updateTotals() {
        val total = _saleItems.value.sumOf { it.quantity * it.sellingPrice }
        _uiState.update { it.copy(
            currentTotal = total,
            currentPending = total - it.currentPaid
        ) }
    }
    
    fun setPaidAmount(amount: Double) {
        _uiState.update { it.copy(
            currentPaid = amount,
            currentPending = it.currentTotal - amount
        ) }
    }
    
    fun setPaymentType(paymentType: PaymentType) {
        _uiState.update { it.copy(currentPaymentType = paymentType) }
    }
    
    suspend fun saveSale(
        customerName: String,
        customerPhone: String,
        date: Long
    ): Sale? {
        val items = _saleItems.value
        if (items.isEmpty()) {
            _uiState.update { it.copy(
                message = "Please add at least one item",
                isError = true
            ) }
            return null
        }
        
        return try {
            val totalAmount = items.sumOf { it.quantity * it.sellingPrice }
            val paidAmount = if (_uiState.value.currentPaymentType == PaymentType.CASH) {
                totalAmount
            } else {
                _uiState.value.currentPaid
            }
            val pendingAmount = totalAmount - paidAmount
            
            // Get or create customer
            var customerId: Long? = null
            if (customerName.isNotEmpty()) {
                val existingCustomer = repository.getCustomerByPhone(customerPhone)
                customerId = existingCustomer?.id ?: repository.insertCustomer(
                    Customer(name = customerName, phone = customerPhone)
                )
            }
            
            // Get next invoice number
            val nextInvoiceNumber = repository.getNextInvoiceNumber() ?: 1
            val invoiceNumber = "INV${String.format("%04d", nextInvoiceNumber)}"
            
            val sale = Sale(
                customerId = customerId,
                customerName = customerName,
                customerPhone = customerPhone,
                date = date,
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                pendingAmount = pendingAmount,
                paymentType = _uiState.value.currentPaymentType,
                invoiceNumber = invoiceNumber
            )
            
            val saleId = repository.insertSale(sale)
            repository.incrementInvoiceNumber()
            
            // Insert sale items and update stock
            items.forEach { item ->
                repository.insertSaleItem(
                    SaleItem(
                        saleId = saleId,
                        productId = item.product.id,
                        productName = item.product.name,
                        quantity = item.quantity,
                        sellingPrice = item.sellingPrice,
                        totalAmount = item.quantity * item.sellingPrice
                    )
                )
                
                // Update product stock
                repository.decreaseStock(item.product.id, item.quantity)
                
                // Add stock history
                val product = repository.getProductById(item.product.id)
                product?.let {
                    repository.insertStockHistory(
                        StockHistory(
                            productId = item.product.id,
                            type = StockMovementType.OUT,
                            quantity = item.quantity,
                            previousStock = it.currentStock,
                            newStock = it.currentStock - item.quantity,
                            referenceId = saleId,
                            referenceType = "SALE",
                            notes = "Sale #$invoiceNumber"
                        )
                    )
                }
            }
            
            // Update customer due if pending
            if (pendingAmount > 0 && customerId != null) {
                repository.updateCustomerDue(customerId, pendingAmount)
            }
            
            clearSaleItems()
            _uiState.update { it.copy(
                message = "Sale saved successfully",
                isError = false,
                currentTotal = 0.0,
                currentPaid = 0.0,
                currentPending = 0.0,
                currentPaymentType = PaymentType.CASH
            ) }
            
            sale.copy(id = saleId)
        } catch (e: Exception) {
            _uiState.update { it.copy(
                message = "Error saving sale: ${e.message}",
                isError = true
            ) }
            null
        }
    }
    
    fun makePayment(sale: Sale, amount: Double) {
        viewModelScope.launch {
            try {
                val updatedSale = sale.copy(
                    paidAmount = sale.paidAmount + amount,
                    pendingAmount = (sale.pendingAmount - amount).coerceAtLeast(0.0)
                )
                repository.updateSale(updatedSale)
                
                // Update customer due
                sale.customerId?.let { customerId ->
                    repository.updateCustomerDue(customerId, -amount)
                }
                
                _uiState.update { it.copy(
                    message = "Payment recorded successfully",
                    isError = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Error recording payment: ${e.message}",
                    isError = true
                ) }
            }
        }
    }
    
    fun searchSales(query: String) {
        viewModelScope.launch {
            repository.searchSales(query).collect { sales ->
                _uiState.update { it.copy(sales = sales.map { 
                    SaleWithItems(sale = it, items = emptyList()) 
                }) }
            }
        }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class SalesUiState(
    val sales: List<SaleWithItems> = emptyList(),
    val products: List<Product> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val isLoading: Boolean = true,
    val currentTotal: Double = 0.0,
    val currentPaid: Double = 0.0,
    val currentPending: Double = 0.0,
    val currentPaymentType: PaymentType = PaymentType.CASH,
    val message: String? = null,
    val isError: Boolean = false
)

data class SaleItemData(
    val product: Product,
    val quantity: Int,
    val sellingPrice: Double
)