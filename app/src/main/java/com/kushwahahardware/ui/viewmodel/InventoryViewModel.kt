package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Category
import com.kushwahahardware.data.entity.Product
import com.kushwahahardware.data.entity.StockHistory
import com.kushwahahardware.data.entity.StockMovementType
import com.kushwahahardware.data.repository.HardwareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: HardwareRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<Long?>(null)
    val selectedCategory: StateFlow<Long?> = _selectedCategory.asStateFlow()
    
    init {
        loadProducts()
        loadCategories()
        loadBrands()
    }
    
    private fun loadProducts() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _selectedCategory
            ) { query, categoryId ->
                Pair(query, categoryId)
            }.flatMapLatest { (query, categoryId) ->
                when {
                    query.isNotEmpty() -> repository.searchProducts(query)
                    categoryId != null -> repository.getProductsByCategory(categoryId)
                    else -> repository.getAllProducts()
                }
            }.collect { products ->
                _uiState.update { it.copy(
                    products = products,
                    isLoading = false
                ) }
            }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            repository.getAllCategories().collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }
    
    private fun loadBrands() {
        viewModelScope.launch {
            repository.getAllBrands().collect { brands ->
                _uiState.update { it.copy(brands = brands) }
            }
        }
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSelectedCategory(categoryId: Long?) {
        _selectedCategory.value = categoryId
    }
    
    fun saveProduct(product: Product) {
        viewModelScope.launch {
            try {
                val isNewProduct = product.id == 0L
                val productId = repository.insertProduct(product)
                
                if (isNewProduct && product.openingStock > 0) {
                    // Add stock history for opening stock
                    repository.insertStockHistory(
                        StockHistory(
                            productId = productId,
                            type = StockMovementType.IN,
                            quantity = product.openingStock,
                            previousStock = 0,
                            newStock = product.openingStock,
                            referenceType = "OPENING_STOCK",
                            notes = "Opening stock"
                        )
                    )
                }
                
                _uiState.update { it.copy(
                    message = "Product saved successfully",
                    isError = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Error saving product: ${e.message}",
                    isError = true
                ) }
            }
        }
    }
    
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
                _uiState.update { it.copy(
                    message = "Product deleted successfully",
                    isError = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Error deleting product: ${e.message}",
                    isError = true
                ) }
            }
        }
    }
    
    fun loadStockHistory(productId: Long) {
        viewModelScope.launch {
            repository.getStockHistoryByProduct(productId).collect { history ->
                _uiState.update { it.copy(stockHistory = history) }
            }
        }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}

data class InventoryUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val brands: List<String> = emptyList(),
    val stockHistory: List<StockHistory> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val isError: Boolean = false
)