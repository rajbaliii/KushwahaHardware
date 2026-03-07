package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Customer
import com.kushwahahardware.data.entity.Sale
import com.kushwahahardware.data.repository.CommonRepository
import com.kushwahahardware.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val commonRepository: CommonRepository,
    private val saleRepository: SaleRepository
) : ViewModel() {

    val customers: StateFlow<List<Customer>> = commonRepository.getAllCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer

    private val _customerSales = MutableStateFlow<List<Sale>>(emptyList())
    val customerSales: StateFlow<List<Sale>> = _customerSales

    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
        viewModelScope.launch {
            // Fetch sales for this customer
            saleRepository.getSalesByCustomer(customer.id).collect {
                _customerSales.value = it
            }
        }
    }

    fun saveCustomer(customer: Customer, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                commonRepository.insertCustomer(customer)
            } else {
                commonRepository.updateCustomer(customer)
            }
            onSuccess()
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            commonRepository.deleteCustomer(customer)
        }
    }
}
