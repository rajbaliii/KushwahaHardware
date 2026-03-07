package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Product
import com.kushwahahardware.data.entity.Purchase
import com.kushwahahardware.data.entity.PurchaseItem
import com.kushwahahardware.data.entity.Supplier
import com.kushwahahardware.data.repository.CommonRepository
import com.kushwahahardware.data.repository.PurchaseRepository
import com.kushwahahardware.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import com.kushwahahardware.data.repository.RBACRepository
import com.kushwahahardware.security.PermissionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PurchaseCartItem(
    val product: Product,
    var quantity: Double,
    var rate: Double
) {
    val total: Double get() = quantity * rate
}

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val commonRepository: CommonRepository,
    private val productRepository: ProductRepository,
    private val rbacRepository: RBACRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<PurchaseCartItem>>(emptyList())
    val cartItems: StateFlow<List<PurchaseCartItem>> = _cartItems

    private val _selectedSupplier = MutableStateFlow<Supplier?>(null)
    val selectedSupplier: StateFlow<Supplier?> = _selectedSupplier

    private val _paidAmount = MutableStateFlow(0.0)
    val paidAmount: StateFlow<Double> = _paidAmount

    private val _invoiceNumber = MutableStateFlow("")
    val invoiceNumber: StateFlow<String> = _invoiceNumber

    val purchases: StateFlow<List<Purchase>> = purchaseRepository.getAllPurchases()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<Supplier>> = commonRepository.getAllSuppliers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = productRepository.getAllActiveProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAmount: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.total }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    fun addToCart(product: Product, quantity: Double, rate: Double) {
        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val existingItem = currentItems[index]
            currentItems[index] = existingItem.copy(
                quantity = existingItem.quantity + quantity,
                rate = rate
            )
        } else {
            currentItems.add(PurchaseCartItem(product, quantity, rate))
        }
        _cartItems.value = currentItems.toList()
    }

    fun removeFromCart(item: PurchaseCartItem) {
        _cartItems.value = _cartItems.value.filter { it.product.id != item.product.id }
    }

    fun selectSupplier(supplier: Supplier?) {
        _selectedSupplier.value = supplier
    }

    fun setPaidAmount(amount: Double) {
        _paidAmount.value = amount
    }

    fun setInvoiceNumber(number: String) {
        _invoiceNumber.value = number
    }

    fun savePurchase(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val total = totalAmount.value
            val paid = _paidAmount.value
            
            val purchase = Purchase(
                invoiceNumber = _invoiceNumber.value.ifBlank { "PUR-${System.currentTimeMillis() / 1000}" },
                supplierId = _selectedSupplier.value?.id,
                totalAmount = total,
                paidAmount = paid,
                pendingAmount = total - paid,
                purchaseDate = System.currentTimeMillis()
            )
            
            val purchaseItems = _cartItems.value.map { cartItem ->
                PurchaseItem(
                    purchaseId = 0,
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    unitPrice = cartItem.rate,
                    totalPrice = cartItem.total
                )
            }
            
            val id = purchaseRepository.insertPurchase(purchase, purchaseItems)
            
            val currentUser = permissionManager.currentUser.value
            rbacRepository.logAction(
                userId = currentUser?.id,
                userName = currentUser?.name ?: "System",
                module = com.kushwahahardware.security.AppModules.PURCHASE,
                action = com.kushwahahardware.security.AppActions.ADD,
                details = "Created purchase #$id, Amount: ₹$total"
            )
            
            onSuccess(id)
            
            // Clear cart after success
            _cartItems.value = emptyList()
            _selectedSupplier.value = null
            _paidAmount.value = 0.0
            _invoiceNumber.value = ""
        }
    }
}
