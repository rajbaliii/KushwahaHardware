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
class ReportsViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _totalSalesAmount = MutableStateFlow(0.0)
    val totalSalesAmount: StateFlow<Double> = _totalSalesAmount

    private val _totalProfit = MutableStateFlow(0.0)
    val totalProfit: StateFlow<Double> = _totalProfit

    private val _lowStockItems = MutableStateFlow(0)
    val lowStockItems: StateFlow<Int> = _lowStockItems

    private val _weeklySales = MutableStateFlow<List<Pair<String, Float>>>(emptyList())
    val weeklySales: StateFlow<List<Pair<String, Float>>> = _weeklySales

    init {
        loadReportData()
    }

    private fun loadReportData() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val todayStart = calendar.timeInMillis
            val endOfDay = todayStart + 86400000L - 1
            
            _totalSalesAmount.value = saleRepository.getTodaySalesAmount(todayStart)
            _totalProfit.value = saleRepository.getTotalProfit(todayStart, endOfDay)
            _lowStockItems.value = productRepository.getLowStockCount()

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
