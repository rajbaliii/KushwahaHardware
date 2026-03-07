package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Category
import com.kushwahahardware.data.entity.Product
import com.kushwahahardware.data.repository.BarcodeRepository
import com.kushwahahardware.data.repository.CommonRepository
import com.kushwahahardware.data.repository.ProductRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val commonRepository: CommonRepository,
    private val barcodeRepository: BarcodeRepository
) : ViewModel() {


    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId

    private val _showLowStockOnly = MutableStateFlow(false)
    val showLowStockOnly: StateFlow<Boolean> = _showLowStockOnly


    val categories: StateFlow<List<Category>> = commonRepository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = productRepository.getAllProducts()
        .combine(_searchQuery) { list, query ->
            if (query.isEmpty()) list else list.filter { 
                it.name.contains(query, ignoreCase = true) || it.sku?.contains(query, ignoreCase = true) == true 
            }
        }
        .combine(_selectedCategoryId) { list, catId ->
            if (catId == null) list else list.filter { it.categoryId == catId }
        }
        .combine(_showLowStockOnly) { list, lowStockOnly ->
            if (!lowStockOnly) list else list.filter { it.currentStock <= it.minStockLevel }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())




    init {
        checkAndPopulateCategories()
    }

    private fun checkAndPopulateCategories() {
        viewModelScope.launch {
            commonRepository.getAllCategories().first().let { 
                if (it.isEmpty()) {
                    val defaultCategories = listOf(
                        Category(name = "🎨 Paint"),
                        Category(name = "🔧 Plumbing"),
                        Category(name = "🏗️ Steel"),
                        Category(name = "🔩 Iron"),
                        Category(name = "🛠️ Tools"),
                        Category(name = "📦 Others")
                    )
                    defaultCategories.forEach { cat -> commonRepository.insertCategory(cat) }
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun setShowLowStockOnly(show: Boolean) {
        _showLowStockOnly.value = show
    }


    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            productRepository.deleteProduct(product)
        }
    }

    private val _generatedBarcodes = MutableStateFlow<List<String>>(emptyList())
    val generatedBarcodes: StateFlow<List<String>> = _generatedBarcodes

    fun generateBarcodes(count: Int, onComplete: () -> Unit) {
        viewModelScope.launch {
            val serials = barcodeRepository.generateUniqueBarcodes(count)
            _generatedBarcodes.value = serials
            onComplete()
        }
    }
}

