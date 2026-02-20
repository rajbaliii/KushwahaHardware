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
class PurchaseViewModel @Inject constructor(
    private val repository: HardwareRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()
    
    private val _purchaseItems = MutableStateFlow<List<PurchaseItemData>>(emptyList())
    val purchaseItems: StateFlow<List<PurchaseItemData>> = _purchaseItems.asStateFlow()
    
    init {
        loadSuppliers()
        loadPurchases()
        loadProducts()
    }
    
    private fun loadSuppliers() {
        viewModelScope.launch {
            repository.getAllSuppliers().collect { suppliers ->
                _uiState.update { it.copy(suppliers = suppliers) }
            }
        }
    }
    
    private fun loadPurchases() {
        viewModelScope.launch {
            repository.getAllPurchasesWithItems().collect { purchases ->
                _uiState.update { it.copy(
                    purchases = purchases,
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
    
    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            try {
                repository.insertSupplier(supplier)
                _uiState.update { it.copy(
                    message = "Supplier saved successfully",
                    isError = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Error saving supplier: ${e.message}",
                    isError = true
                ) }
            }
        }
    }
    
    fun addPurchaseItem(product: Product, quantity: Int, price: Double) {
        val items = _purchaseItems.value.toMutableList()
        items.add(
            PurchaseItemData(
                product = product,
                quantity = quantity,
                purchasePrice = price
            )
        )
        _purchaseItems.value = items
        updateTotals()
    }
    
    fun removePurchaseItem(index: Int) {
        val items = _purchaseItems.value.toMutableList()
        if (index in items.indices) {
            items.removeAt(index)
            _purchaseItems.value = items
            updateTotals()
        }
    }
    
    fun clearPurchaseItems() {
        _purchaseItems.value = emptyList()
        updateTotals()
    }
    
    private fun updateTotals() {
        val total = _purchaseItems.value.sumOf { it.quantity * it.purchasePrice }
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
    
    fun savePurchase(supplierId: Long?, date: Long, invoiceNumber: String) {
        viewModelScope.launch {
            try {
                val items = _purchaseItems.value
                if (items.isEmpty()) {
                    _uiState.update { it.copy(
                        message = "Please add at least one item",
                        isError = true
                    ) }
                    return@launch
                }
                
                val totalAmount = items.sumOf { it.quantity * it.purchasePrice }
                val paidAmount = _uiState.value.currentPaid
                val pendingAmount = totalAmount - paidAmount
                
                val purchase = Purchase(
                    supplierId = supplierId,
                    date = date,
                    invoiceNumber = invoiceNumber,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    pendingAmount = pendingAmount
                )
                
                val purchaseId = repository.insertPurchase(purchase)
                
                // Insert purchase items and update stock
                items.forEach { item ->
                    repository.insertPurchaseItem(
                        PurchaseItem(
                            purchaseId = purchaseId,
                            productId = item.product.id,
                            quantity = item.quantity,
                            purchasePrice = item.purchasePrice,
                            totalAmount = item.quantity * item.purchasePrice
                        )
                    )
                    
                    // Update product stock
                    repository.increaseStock(item.product.id, item.quantity)
                    
                    // Add stock history
                    val product = repository.getProductById(item.product.id)
                    product?.let {
                        repository.insertStockHistory(
                            StockHistory(
                                productId = item.product.id,
                                type = StockMovementType.IN,
                                quantity = item.quantity,
                                previousStock = it.currentStock,
                                newStock = it.currentStock + item.quantity,
                                referenceId = purchaseId,
                                referenceType = "PURCHASE",
                                notes = "Purchase #${invoiceNumber}"
                            )
                        )
                    }
                }
                
                // Update supplier due if pending
                if (pendingAmount > 0 && supplierId != null) {
                    repository.updateSupplierDue(supplierId, pendingAmount)
                }
                
                clearPurchaseItems()
                _uiState.update { it.copy(
                    message = "Purchase saved successfully",
                    isError = false,
                    currentTotal = 0.0,
                    currentPaid = 0.0,
                    currentPending = 0.0
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Error saving purchase: ${e.message}",
                    isError = true
                ) }
            }
        }
    }
    
    fun makePayment(purchase: Purchase, amount: Double) {
        viewModelScope.launch {
            try {
                val updatedPurchase = purchase.copy(
                    paidAmount = purchase.paidAmount + amount,
                    pendingAmount = (purchase.pendingAmount - amount).coerceAtLeast(0.0)
                )
                repository.updatePurchase(updatedPurchase)
                
                // Update supplier due
                purchase.supplierId?.let { supplierId ->
                    repository.updateSupplierDue(supplierId, -amount)
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
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class PurchaseUiState(
    val suppliers: List<Supplier> = emptyList(),
    val purchases: List<PurchaseWithItems> = emptyList(),
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = true,
    val currentTotal: Double = 0.0,
    val currentPaid: Double = 0.0,
    val currentPending: Double = 0.0,
    val message: String? = null,
    val isError: Boolean = false
)

data class PurchaseItemData(
    val product: Product,
    val quantity: Int,
    val purchasePrice: Double
)