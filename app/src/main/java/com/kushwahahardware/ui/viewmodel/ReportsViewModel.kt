package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.*
import com.kushwahahardware.data.repository.HardwareRepository
import com.kushwahahardware.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val repository: HardwareRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()
    
    init {
        loadInitialData()
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            val startOfDay = DateUtils.getStartOfDay()
            val endOfDay = DateUtils.getEndOfDay()
            
            combine(
                repository.getTotalSalesAmount(startOfDay, endOfDay),
                repository.getTotalPurchaseAmount(startOfDay, endOfDay),
                repository.getLowStockProducts(),
                repository.getPendingPurchases(),
                repository.getPendingSales()
            ) { sales, purchases, lowStock, pendingPurchases, pendingSales ->
                ReportsUiState(
                    todaySales = sales ?: 0.0,
                    todayPurchases = purchases ?: 0.0,
                    lowStockProducts = lowStock,
                    pendingPurchases = pendingPurchases,
                    pendingSales = pendingSales,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun generateSalesReport(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            repository.getSalesByDateRange(startDate, endDate).collect { sales ->
                val totalSales = sales.sumOf { it.totalAmount }
                val totalProfit = calculateProfit(sales)
                
                _uiState.update { it.copy(
                    reportData = ReportData(
                        title = "Sales Report",
                        startDate = startDate,
                        endDate = endDate,
                        totalAmount = totalSales,
                        totalProfit = totalProfit,
                        items = sales.map { sale ->
                            ReportItem(
                                label = sale.getFormattedInvoiceNumber(),
                                value = CurrencyUtils.format(sale.totalAmount),
                                date = sale.date
                            )
                        }
                    )
                ) }
            }
        }
    }
    
    fun generateProfitReport(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            repository.getSalesByDateRange(startDate, endDate).collect { sales ->
                val totalProfit = calculateProfit(sales)
                
                _uiState.update { it.copy(
                    reportData = ReportData(
                        title = "Profit Report",
                        startDate = startDate,
                        endDate = endDate,
                        totalAmount = sales.sumOf { it.totalAmount },
                        totalProfit = totalProfit,
                        items = emptyList()
                    )
                ) }
            }
        }
    }
    
    fun generateStockReport() {
        viewModelScope.launch {
            repository.getAllProducts().collect { products ->
                val lowStock = products.filter { it.isLowStock() }
                val totalValue = products.sumOf { it.currentStock * it.purchasePrice }
                
                _uiState.update { it.copy(
                    reportData = ReportData(
                        title = "Stock Report",
                        totalAmount = totalValue,
                        items = lowStock.map { product ->
                            ReportItem(
                                label = product.name,
                                value = "Stock: ${product.currentStock} (Alert: ${product.lowStockAlert})"
                            )
                        }
                    )
                ) }
            }
        }
    }
    
    fun generateSupplierPendingReport() {
        viewModelScope.launch {
            repository.getPendingPurchases().collect { purchases ->
                val totalPending = purchases.sumOf { it.pendingAmount }
                
                _uiState.update { it.copy(
                    reportData = ReportData(
                        title = "Supplier Pending Payments",
                        totalAmount = totalPending,
                        items = purchases.map { purchase ->
                            ReportItem(
                                label = purchase.invoiceNumber,
                                value = CurrencyUtils.format(purchase.pendingAmount),
                                date = purchase.date
                            )
                        }
                    )
                ) }
            }
        }
    }
    
    fun generateCustomerPendingReport() {
        viewModelScope.launch {
            repository.getPendingSales().collect { sales ->
                val totalPending = sales.sumOf { it.pendingAmount }
                
                _uiState.update { it.copy(
                    reportData = ReportData(
                        title = "Customer Pending Payments",
                        totalAmount = totalPending,
                        items = sales.map { sale ->
                            ReportItem(
                                label = sale.getFormattedInvoiceNumber(),
                                value = CurrencyUtils.format(sale.pendingAmount),
                                date = sale.date
                            )
                        }
                    )
                ) }
            }
        }
    }
    
    private suspend fun calculateProfit(sales: List<Sale>): Double {
        var profit = 0.0
        sales.forEach { sale ->
            val saleWithItems = repository.getSaleWithItems(sale.id)
            saleWithItems?.items?.forEach { item ->
                item.productId?.let { productId ->
                    val product = repository.getProductById(productId)
                    product?.let {
                        profit += (item.sellingPrice - it.purchasePrice) * item.quantity
                    }
                }
            }
        }
        return profit
    }
    
    fun clearReportData() {
        _uiState.update { it.copy(reportData = null) }
    }
}

data class ReportsUiState(
    val todaySales: Double = 0.0,
    val todayPurchases: Double = 0.0,
    val lowStockProducts: List<Product> = emptyList(),
    val pendingPurchases: List<Purchase> = emptyList(),
    val pendingSales: List<Sale> = emptyList(),
    val reportData: ReportData? = null,
    val isLoading: Boolean = true
)

data class ReportData(
    val title: String,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val totalAmount: Double = 0.0,
    val totalProfit: Double = 0.0,
    val items: List<ReportItem> = emptyList()
)

data class ReportItem(
    val label: String,
    val value: String,
    val date: Long? = null
)