package com.kushwahahardware.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.dao.PaymentTransactionDao
import com.kushwahahardware.data.entity.PaymentTransaction
import com.kushwahahardware.data.repository.CommonRepository
import com.kushwahahardware.data.repository.PurchaseRepository
import com.kushwahahardware.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LedgerEntry {
    data class Transaction(val transaction: PaymentTransaction) : LedgerEntry()
    data class SaleEntry(val date: Long, val invoiceNo: String, val amount: Double) : LedgerEntry()
    data class PurchaseEntry(val date: Long, val invoiceNo: String, val amount: Double) : LedgerEntry()
}

@HiltViewModel
class PartyLedgerViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val purchaseRepository: PurchaseRepository,
    private val transactionDao: PaymentTransactionDao,
    private val commonRepository: CommonRepository
) : ViewModel() {

    private val _entries = MutableStateFlow<List<LedgerEntry>>(emptyList())
    val entries: StateFlow<List<LedgerEntry>> = _entries

    private val _netBalance = MutableStateFlow(0.0)
    val netBalance: StateFlow<Double> = _netBalance

    private val _totalDues = MutableStateFlow(0.0)
    val totalDues: StateFlow<Double> = _totalDues

    private val _totalPaid = MutableStateFlow(0.0)
    val totalPaid: StateFlow<Double> = _totalPaid

    private val _partyName = MutableStateFlow("")
    val partyName: StateFlow<String> = _partyName

    private val _partyPhone = MutableStateFlow("")
    val partyPhone: StateFlow<String> = _partyPhone

    fun loadLedger(partyId: Long, partyType: String) {
        viewModelScope.launch {
            // Load party info
            if (partyType == "CUSTOMER") {
                commonRepository.getCustomerById(partyId)?.let {
                    _partyName.value = it.name
                    _partyPhone.value = it.phone
                }
            } else {
                commonRepository.getSupplierById(partyId)?.let {
                    _partyName.value = it.name
                    _partyPhone.value = it.phone
                }
            }

            // Combine history
            val transactionsFlow = transactionDao.getTransactionsByParty(partyId, partyType)
            val salesFlow = if (partyType == "CUSTOMER") saleRepository.getSalesByCustomer(partyId) else flowOf(emptyList())
            val purchasesFlow = if (partyType == "SUPPLIER") purchaseRepository.getPurchasesBySupplier(partyId) else flowOf(emptyList())

            combine(transactionsFlow, salesFlow, purchasesFlow) { transactions, sales, purchases ->
                val allEntries = mutableListOf<LedgerEntry>()
                
                transactions.forEach { allEntries.add(LedgerEntry.Transaction(it)) }
                sales.forEach { allEntries.add(LedgerEntry.SaleEntry(it.saleDate, it.invoiceNumber, it.totalAmount)) }
                purchases.forEach { allEntries.add(LedgerEntry.PurchaseEntry(it.purchaseDate, it.invoiceNumber, it.totalAmount)) }

                allEntries.sortedByDescending { 
                    when (it) {
                        is LedgerEntry.Transaction -> it.transaction.transactionDate
                        is LedgerEntry.SaleEntry -> it.date
                        is LedgerEntry.PurchaseEntry -> it.date
                    }
                }
            }.collect { sortedEntries ->
                _entries.value = sortedEntries
                
                // Calculate summaries
                var dues = 0.0
                var paid = 0.0
                
                if (partyType == "CUSTOMER") {
                    dues = sortedEntries.filterIsInstance<LedgerEntry.SaleEntry>().sumOf { it.amount }
                    // Also include GAVE (if we gave money back or correction) as it increases what they owe? 
                    // No, usually what they owe is Sales + GAVE (if correction)
                    dues += sortedEntries.filterIsInstance<LedgerEntry.Transaction>()
                        .filter { it.transaction.transactionType == "GAVE" }.sumOf { it.transaction.amount }
                    
                    paid = sortedEntries.filterIsInstance<LedgerEntry.Transaction>()
                        .filter { it.transaction.transactionType == "GOT" }.sumOf { it.transaction.amount }
                } else {
                    dues = sortedEntries.filterIsInstance<LedgerEntry.PurchaseEntry>().sumOf { it.amount }
                    dues += sortedEntries.filterIsInstance<LedgerEntry.Transaction>()
                        .filter { it.transaction.transactionType == "GOT" }.sumOf { it.transaction.amount }
                    
                    paid = sortedEntries.filterIsInstance<LedgerEntry.Transaction>()
                        .filter { it.transaction.transactionType == "GAVE" }.sumOf { it.transaction.amount }
                }
                
                _totalDues.value = dues
                _totalPaid.value = paid
                _netBalance.value = dues - paid
            }
        }
    }

    fun recordPayment(partyId: Long, partyType: String, amount: Double, type: String) {
        viewModelScope.launch {
            val notes = if (type == "GOT") "Payment received" else "Payment made"
            if (partyType == "CUSTOMER") {
                saleRepository.reduceCustomerPending(partyId, amount, type, notes)
            } else {
                purchaseRepository.reduceSupplierPending(partyId, amount, type, notes)
            }
        }
    }
}
