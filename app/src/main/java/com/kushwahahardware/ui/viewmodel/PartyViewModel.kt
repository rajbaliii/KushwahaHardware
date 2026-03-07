package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Customer
import com.kushwahahardware.data.entity.Supplier
import com.kushwahahardware.data.repository.CommonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PartyViewModel @Inject constructor(
    private val commonRepository: CommonRepository
) : ViewModel() {

    fun saveParty(
        type: String,
        name: String,
        phone: String,
        address: String,
        gstin: String,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val id = if (type == "CUSTOMER") {
                commonRepository.insertCustomer(
                    Customer(name = name, phone = phone, address = address, gstNumber = gstin)
                )
            } else {
                commonRepository.insertSupplier(
                    Supplier(name = name, phone = phone, fullAddress = address, gstNumber = gstin)
                )
            }

            onSuccess(id)
        }
    }
}
