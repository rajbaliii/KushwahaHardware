package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Sale
import com.kushwahahardware.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SalesViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {

    val sales: StateFlow<List<Sale>> = saleRepository.getAllSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
