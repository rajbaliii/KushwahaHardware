package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.Customer
import com.kushwahahardware.data.entity.Supplier
import com.kushwahahardware.data.repository.CommonRepository
import com.kushwahahardware.data.repository.PurchaseRepository
import com.kushwahahardware.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PendingDue(
    val id: Long,
    val name: String,
    val phone: String,
    val amount: Double,
    val type: String // "CUSTOMER" or "SUPPLIER"
)

@HiltViewModel
class PendingDuesViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val purchaseRepository: PurchaseRepository,
    private val commonRepository: CommonRepository,
    private val transactionDao: com.kushwahahardware.data.dao.PaymentTransactionDao
) : ViewModel() {

    private val _pendingDues = MutableStateFlow<List<PendingDue>>(emptyList())
    val pendingDues: StateFlow<List<PendingDue>> = _pendingDues

    init {
        loadPendingDues()
    }

    /**
     * Record a payment against a party's pending dues.
     * [due] – the party (customer or supplier) receiving/making payment
     * [amount] – the amount paid/received
     * [type] – "GOT" means we received money, "GAVE" means we paid
     */
    fun recordPayment(due: PendingDue, amount: Double, type: String) {
        viewModelScope.launch {
            val notes = if (type == "GOT") "Payment received" else "Payment made"
            if (due.type == "CUSTOMER") {
                saleRepository.reduceCustomerPending(due.id, amount, type, notes)
            } else {
                purchaseRepository.reduceSupplierPending(due.id, amount, type, notes)
            }
        }
    }

    private fun loadPendingDues() {
        viewModelScope.launch {
            combine(
                saleRepository.getAllSales(),
                purchaseRepository.getAllPurchases(),
                commonRepository.getAllCustomers(),
                commonRepository.getAllSuppliers(),
                transactionDao.getAllTransactions()
            ) { sales, purchases, customers, suppliers, transactions ->
                val customerDues = customers.map { customer ->
                    val totalSales = sales.filter { it.customerId == customer.id }.sumOf { it.totalAmount }
                    val customerTransactions = transactions.filter { it.partyId == customer.id && it.partyType == "CUSTOMER" }
                    
                    val gave = customerTransactions.filter { it.transactionType == "GAVE" }.sumOf { it.amount }
                    val got = customerTransactions.filter { it.transactionType == "GOT" }.sumOf { it.amount }
                    
                    // Balance = (Sales + YOU GAVE) - YOU GOT
                    val balance = (totalSales + gave) - got
                    PendingDue(customer.id, customer.name, customer.phone, balance, "CUSTOMER")
                }
                
                val supplierDues = suppliers.map { supplier ->
                    val totalPurchases = purchases.filter { it.supplierId == supplier.id }.sumOf { it.totalAmount }
                    val supplierTransactions = transactions.filter { it.partyId == supplier.id && it.partyType == "SUPPLIER" }
                    
                    val gave = supplierTransactions.filter { it.transactionType == "GAVE" }.sumOf { it.amount }
                    val got = supplierTransactions.filter { it.transactionType == "GOT" }.sumOf { it.amount }
                    
                    // Balance = (Purchases + YOU GOT) - YOU GAVE
                    val balance = (totalPurchases + got) - gave
                    PendingDue(supplier.id, supplier.name, supplier.phone, balance, "SUPPLIER")
                }
                
                customerDues + supplierDues
            }.collect {
                _pendingDues.value = it
            }
        }
    }

}
