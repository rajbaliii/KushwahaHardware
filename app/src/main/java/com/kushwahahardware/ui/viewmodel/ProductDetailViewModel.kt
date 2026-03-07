package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Category
import com.kushwahahardware.data.entity.Product
import com.kushwahahardware.data.repository.CommonRepository
import com.kushwahahardware.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.kushwahahardware.data.repository.RBACRepository
import com.kushwahahardware.security.PermissionManager

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val commonRepository: CommonRepository,
    private val rbacRepository: RBACRepository,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    val categories: StateFlow<List<Category>> = commonRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadProduct(id: Long) {
        viewModelScope.launch {
            _product.value = productRepository.getProductById(id)
        }
    }

    suspend fun getProductBySku(sku: String): Product? {
        return productRepository.getProductBySku(sku)
    }

    fun saveProduct(
        id: Long = 0,
        name: String,
        sku: String,
        categoryId: Long?,
        purchasePrice: Double,
        sellingPrice: Double,
        currentStock: Double,
        minStock: Double,
        unit: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val productToSave = Product(
                id = id,
                name = name,
                sku = sku,
                categoryId = categoryId,
                purchasePrice = purchasePrice,
                sellingPrice = sellingPrice,
                currentStock = currentStock,
                minStockLevel = minStock,
                unit = unit,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                val newId = productRepository.insertProduct(productToSave)
                logProductAction(newId, name, "add", "Created new product: $name")
            } else {
                productRepository.updateProduct(productToSave)
                logProductAction(id, name, "edit", "Updated product: $name")
            }
            onSuccess()
        }
    }
    private fun logProductAction(productId: Long, productName: String, action: String, details: String) {
        val currentUser = permissionManager.currentUser.value
        viewModelScope.launch {
            rbacRepository.logAction(
                userId = currentUser?.id,
                userName = currentUser?.name ?: "System",
                module = com.kushwahahardware.security.AppModules.INVENTORY,
                action = action,
                details = details
            )
        }
    }
}
