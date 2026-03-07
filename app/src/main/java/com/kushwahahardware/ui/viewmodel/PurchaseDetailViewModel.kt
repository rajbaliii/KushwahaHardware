package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Purchase
import com.kushwahahardware.data.entity.PurchaseItem
import com.kushwahahardware.data.repository.PurchaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseDetailViewModel @Inject constructor(
    private val purchaseRepository: PurchaseRepository,
    private val commonRepository: com.kushwahahardware.data.repository.CommonRepository
) : ViewModel() {

    private val _purchase = MutableStateFlow<Purchase?>(null)
    val purchase: StateFlow<Purchase?> = _purchase.asStateFlow()

    private val _items = MutableStateFlow<List<PurchaseItem>>(emptyList())
    val items: StateFlow<List<PurchaseItem>> = _items.asStateFlow()

    private val _supplier = MutableStateFlow<com.kushwahahardware.data.entity.Supplier?>(null)
    val supplier: StateFlow<com.kushwahahardware.data.entity.Supplier?> = _supplier.asStateFlow()

    private val _shopInfo = MutableStateFlow<com.kushwahahardware.data.entity.ShopInfo?>(null)
    val shopInfo: StateFlow<com.kushwahahardware.data.entity.ShopInfo?> = _shopInfo.asStateFlow()

    fun loadPurchase(purchaseId: Long) {
        viewModelScope.launch {
            val p = purchaseRepository.getPurchaseById(purchaseId)
            _purchase.value = p
            
            p?.supplierId?.let { id ->
                _supplier.value = commonRepository.getSupplierById(id)
            }

            commonRepository.getShopInfo().collect { _shopInfo.value = it }
        }
        viewModelScope.launch {
            purchaseRepository.getPurchaseItems(purchaseId).collect {
                _items.value = it
            }
        }
    }
}
