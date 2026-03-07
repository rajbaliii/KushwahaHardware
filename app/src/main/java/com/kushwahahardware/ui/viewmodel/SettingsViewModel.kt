package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.ShopInfo
import com.kushwahahardware.data.repository.CommonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val commonRepository: CommonRepository
) : ViewModel() {

    val shopInfo: StateFlow<ShopInfo?> = commonRepository.getShopInfo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateShopInfo(
        name: String, 
        address: String, 
        phone: String,
        ownerName: String,
        altPhone: String,
        bankingName: String,
        upiId: String,
        gstNumber: String
    ) {
        viewModelScope.launch {
            val current = shopInfo.value ?: ShopInfo()
            commonRepository.updateShopInfo(current.copy(
                shopName = name,
                address = address,
                phone = phone,
                ownerName = ownerName,
                alternativePhone = altPhone,
                bankingName = bankingName,
                upiId = upiId,
                gstNumber = gstNumber,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }
}
