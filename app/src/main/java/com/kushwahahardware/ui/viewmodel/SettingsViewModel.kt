package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Category
import com.kushwahahardware.data.entity.ShopInfo
import com.kushwahahardware.data.repository.HardwareRepository
import com.kushwahahardware.utils.BiometricHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: HardwareRepository,
    private val biometricHelper: BiometricHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadShopInfo()
        loadCategories()
        checkBiometricAvailability()
    }
    
    private fun loadShopInfo() {
        viewModelScope.launch {
            repository.getShopInfo().collect { shopInfo ->
                _uiState.update { it.copy(
                    shopInfo = shopInfo ?: ShopInfo(),
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
    
    private fun checkBiometricAvailability() {
        val canAuthenticate = biometricHelper.canAuthenticate()
        _uiState.update { it.copy(
            biometricAvailable = canAuthenticate,
            biometricStatus = biometricHelper.getBiometricStatus()
        ) }
    }
    
    fun updateShopInfo(shopInfo: ShopInfo) {
        viewModelScope.launch {
            try {
                repository.updateShopInfo(shopInfo)
                _uiState.update { it.copy(
                    message = "Shop information updated",
                    isError = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Error updating shop info: ${e.message}",
                    isError = true
                ) }
            }
        }
    }
    
    fun toggleBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            val shopInfo = _uiState.value.shopInfo
            updateShopInfo(shopInfo.copy(biometricEnabled = enabled))
        }
    }
    
    fun addCategory(name: String) {
        viewModelScope.launch {
            try {
                repository.insertCategory(Category(name = name))
                _uiState.update { it.copy(
                    message = "Category added successfully",
                    isError = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Error adding category: ${e.message}",
                    isError = true
                ) }
            }
        }
    }
    
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(category)
                _uiState.update { it.copy(
                    message = "Category deleted successfully",
                    isError = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    message = "Error deleting category: ${e.message}",
                    isError = true
                ) }
            }
        }
    }
    
    fun exportData(exportType: ExportType) {
        viewModelScope.launch {
            _uiState.update { it.copy(exportInProgress = true) }
            
            try {
                val result = when (exportType) {
                    ExportType.PRODUCTS -> exportProducts()
                    ExportType.SALES -> exportSales()
                    ExportType.PURCHASES -> exportPurchases()
                    ExportType.STOCK -> exportStockReport()
                }
                
                _uiState.update { it.copy(
                    exportInProgress = false,
                    exportResult = result,
                    message = if (result != null) "Export successful" else "Export failed",
                    isError = result == null
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    exportInProgress = false,
                    message = "Export error: ${e.message}",
                    isError = true
                ) }
            }
        }
    }
    
    private suspend fun exportProducts(): java.io.File? {
        val products = repository.getAllProducts().first()
        val categories = repository.getAllCategories().first()
        val categoryMap = categories.associateBy({ it.id }, { it.name })
        return com.kushwahahardware.utils.ExcelExporter.exportProducts(
            KushwahaHardwareApp.instance,
            products,
            categoryMap
        )
    }
    
    private suspend fun exportSales(): java.io.File? {
        val sales = repository.getAllSalesWithItems().first()
        return com.kushwahahardware.utils.ExcelExporter.exportSales(
            KushwahaHardwareApp.instance,
            sales
        )
    }
    
    private suspend fun exportPurchases(): java.io.File? {
        val purchases = repository.getAllPurchasesWithItems().first()
        return com.kushwahahardware.utils.ExcelExporter.exportPurchases(
            KushwahaHardwareApp.instance,
            purchases
        )
    }
    
    private suspend fun exportStockReport(): java.io.File? {
        val products = repository.getAllProducts().first()
        val categories = repository.getAllCategories().first()
        val categoryMap = categories.associateBy({ it.id }, { it.name })
        return com.kushwahahardware.utils.ExcelExporter.exportStockReport(
            KushwahaHardwareApp.instance,
            products,
            categoryMap
        )
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
    
    fun clearExportResult() {
        _uiState.update { it.copy(exportResult = null) }
    }
}

data class SettingsUiState(
    val shopInfo: ShopInfo = ShopInfo(),
    val categories: List<Category> = emptyList(),
    val biometricAvailable: Boolean = false,
    val biometricStatus: BiometricHelper.BiometricStatus = BiometricHelper.BiometricStatus.UNKNOWN,
    val exportInProgress: Boolean = false,
    val exportResult: java.io.File? = null,
    val isLoading: Boolean = true,
    val message: String? = null,
    val isError: Boolean = false
)

enum class ExportType {
    PRODUCTS,
    SALES,
    PURCHASES,
    STOCK
}