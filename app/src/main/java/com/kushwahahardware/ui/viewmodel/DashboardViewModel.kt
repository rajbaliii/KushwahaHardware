package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.repository.ProductRepository
import com.kushwahahardware.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {

    private val _totalProducts = MutableStateFlow(0)
    val totalProducts: StateFlow<Int> = _totalProducts

    private val _todaySales = MutableStateFlow(0.0)
    val todaySales: StateFlow<Double> = _todaySales

    private val _todayProfit = MutableStateFlow(0.0)
    val todayProfit: StateFlow<Double> = _todayProfit

    private val _grossMargin = MutableStateFlow(0.0)
    val grossMargin: StateFlow<Double> = _grossMargin

    private val _pendingAmount = MutableStateFlow(0.0)
    val pendingAmount: StateFlow<Double> = _pendingAmount

    private val _lowStockCount = MutableStateFlow(0)
    val lowStockCount: StateFlow<Int> = _lowStockCount

    private val _weeklySales = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val weeklySales: StateFlow<List<Pair<String, Float>>> = _weeklySales

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _totalProducts.value = productRepository.getActiveProductsCount()
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val todayStart = calendar.timeInMillis
            val salesAmount = saleRepository.getTodaySalesAmount(todayStart)
            val profitAmount = saleRepository.getTotalProfit(todayStart, System.currentTimeMillis())
            
            _todaySales.value = salesAmount
            _todayProfit.value = profitAmount
            _grossMargin.value = if (salesAmount > 0) (profitAmount / salesAmount) * 100 else 0.0
            
            _pendingAmount.value = saleRepository.getTotalPendingAmount()
            _lowStockCount.value = productRepository.getLowStockCount()

            // Load last 7 days sales
            val last7DaysSales = mutableListOf<Pair<String, Float>>()
            val sdf = java.text.SimpleDateFormat("dd/MM", Locale.getDefault())
            for (i in 6 downTo 0) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = todayStart
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val dayStart = cal.timeInMillis
                val amount = saleRepository.getSalesDataByDate(dayStart)
                last7DaysSales.add(sdf.format(cal.time) to amount.toFloat())
            }
            _weeklySales.value = last7DaysSales
        }
    }
}
