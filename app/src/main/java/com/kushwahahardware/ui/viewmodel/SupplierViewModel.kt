package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Supplier
import com.kushwahahardware.data.entity.Purchase
import com.kushwahahardware.data.repository.CommonRepository
import com.kushwahahardware.data.repository.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierViewModel @Inject constructor(
    private val commonRepository: CommonRepository,
    private val purchaseRepository: PurchaseRepository
) : ViewModel() {

    val suppliers: StateFlow<List<Supplier>> = commonRepository.getAllSuppliers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedSupplier = MutableStateFlow<Supplier?>(null)
    val selectedSupplier: StateFlow<Supplier?> = _selectedSupplier

    private val _supplierPurchases = MutableStateFlow<List<Purchase>>(emptyList())
    val supplierPurchases: StateFlow<List<Purchase>> = _supplierPurchases

    fun selectSupplier(supplier: Supplier) {
        _selectedSupplier.value = supplier
        viewModelScope.launch {
            purchaseRepository.getPurchasesBySupplier(supplier.id).collect {
                _supplierPurchases.value = it
            }
        }
    }

    fun saveSupplier(supplier: Supplier, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (supplier.id == 0L) {
                commonRepository.insertSupplier(supplier)
            } else {
                commonRepository.updateSupplier(supplier)
            }
            onSuccess()
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            commonRepository.deleteSupplier(supplier)
        }
    }
}
