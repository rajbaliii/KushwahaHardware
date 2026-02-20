package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Product
import com.kushwahahardware.data.repository.DashboardSummary
import com.kushwahahardware.data.repository.HardwareRepository
import com.kushwahahardware.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: HardwareRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    private fun loadDashboardData() {
        viewModelScope.launch {
            val startOfDay = DateUtils.getStartOfDay()
            val endOfDay = DateUtils.getEndOfDay()
            
            repository.getDashboardSummary(startOfDay, endOfDay)
                .collect { summary ->
                    _uiState.update { it.copy(
                        summary = summary,
                        isLoading = false
                    ) }
                }
        }
    }
    
    fun refreshData() {
        _uiState.update { it.copy(isLoading = true) }
        loadDashboardData()
    }
}

data class DashboardUiState(
    val summary: DashboardSummary = DashboardSummary(),
    val isLoading: Boolean = true
)