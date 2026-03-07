package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.*
import com.kushwahahardware.data.repository.CommonRepository
import com.kushwahahardware.data.repository.ProductRepository
import com.kushwahahardware.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.kushwahahardware.data.repository.RBACRepository
import com.kushwahahardware.security.PermissionManager

data class CartItem(
    val product: Product,
    var quantity: Double,
    var rate: Double
) {
    val total: Double get() = quantity * rate
}

@HiltViewModel
class NewSaleViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val commonRepository: CommonRepository,
    private val rbacRepository: com.kushwahahardware.data.repository.RBACRepository,
    private val permissionManager: com.kushwahahardware.security.PermissionManager
) : ViewModel() {

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer

    private val _paymentType = MutableStateFlow("CASH")
    val paymentType: StateFlow<String> = _paymentType

    private val _paidAmount = MutableStateFlow(0.0)
    val paidAmount: StateFlow<Double> = _paidAmount

    val products: StateFlow<List<Product>> = productRepository.getAllActiveProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = commonRepository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAmount: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.total }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    fun addToCart(product: Product, quantity: Double) {
        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.product.id == product.id }
        if (index != -1) {
            val existingItem = currentItems[index]
            currentItems[index] = existingItem.copy(quantity = existingItem.quantity + quantity)
        } else {
            currentItems.add(CartItem(product, quantity, product.sellingPrice))
        }
        _cartItems.value = currentItems.toList()
    }

    fun removeFromCart(cartItem: CartItem) {
        _cartItems.value = _cartItems.value.filter { it.product.id != cartItem.product.id }
    }

    fun updateQuantity(cartItem: CartItem, quantity: Double) {
        val currentItems = _cartItems.value.toMutableList()
        val index = currentItems.indexOfFirst { it.product.id == cartItem.product.id }
        if (index != -1) {
            currentItems[index] = currentItems[index].copy(quantity = quantity)
            _cartItems.value = currentItems.toList()
        }
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun setPaymentType(type: String) {
        _paymentType.value = type
    }

    fun setPaidAmount(amount: Double) {
        _paidAmount.value = amount
    }

    private val _autoIncrement = MutableStateFlow(true)
    val autoIncrement: StateFlow<Boolean> = _autoIncrement

    fun setAutoIncrement(enabled: Boolean) {
        _autoIncrement.value = enabled
    }

    fun onBarcodeScanned(barcode: String, products: List<Product>) {
        val trimmedBarcode = barcode.trim()
        val product = products.find { it.sku?.trim() == trimmedBarcode || it.name.trim() == trimmedBarcode }

        if (product != null) {
            if (com.kushwahahardware.utils.BarcodeParser.isWeightedBarcode(barcode)) {
                val weight = com.kushwahahardware.utils.BarcodeParser.extractQuantity(barcode, product)
                if (weight != null) {
                    addToCart(product, weight)
                }
            } else {
                // For non-weighted barcodes, always add at least 1 unit if scanned
                val quantity = if (product.unit == "pcs" && _autoIncrement.value) 1.0 else 1.0 
                addToCart(product, quantity)
            }
        }
    }


    fun saveSale(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            val total = totalAmount.value
            val paid = if (_paymentType.value == "CASH") total else _paidAmount.value
            
            val sale = Sale(
                invoiceNumber = "INV-${System.currentTimeMillis() / 1000}",
                customerId = _selectedCustomer.value?.id,
                totalAmount = total,
                paidAmount = paid,
                pendingAmount = total - paid,
                paymentType = _paymentType.value
            )
            
            val saleItems = _cartItems.value.map { cartItem ->
                SaleItem(
                    saleId = 0,
                    productId = cartItem.product.id,
                    productName = cartItem.product.name,
                    quantity = cartItem.quantity,
                    unitPrice = cartItem.rate,
                    totalPrice = cartItem.total
                )
            }
            
            val saleId = saleRepository.insertSale(sale, saleItems)
            
            val currentUser = permissionManager.currentUser.value
            rbacRepository.logAction(
                userId = currentUser?.id,
                userName = currentUser?.name ?: "System",
                module = com.kushwahahardware.security.AppModules.SALES,
                action = com.kushwahahardware.security.AppActions.ADD,
                details = "Created sale #$saleId, Amount: ₹$total"
            )
            
            onSuccess(saleId)
        }
    }
}
