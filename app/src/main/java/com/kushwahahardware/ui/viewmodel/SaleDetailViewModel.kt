package com.kushwahahardware.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kushwahahardware.data.entity.*
import com.kushwahahardware.data.repository.CommonRepository
import com.kushwahahardware.data.repository.SaleRepository
import com.kushwahahardware.utils.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SaleDetailViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val commonRepository: CommonRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _sale = MutableStateFlow<Sale?>(null)
    val sale: StateFlow<Sale?> = _sale

    private val _items = MutableStateFlow<List<SaleItem>>(emptyList())
    val items: StateFlow<List<SaleItem>> = _items

    private val _customer = MutableStateFlow<Customer?>(null)
    val customer: StateFlow<Customer?> = _customer

    private val _shopInfo = MutableStateFlow<ShopInfo?>(null)
    val shopInfo: StateFlow<ShopInfo?> = _shopInfo

    fun loadSale(saleId: Long) {
        viewModelScope.launch {
            val saleObj = saleRepository.getSaleById(saleId)
            _sale.value = saleObj
            val itemsList = saleRepository.getSaleItems(saleId).first()
            _items.value = itemsList
            _shopInfo.value = commonRepository.getShopInfo().first()
            
            saleObj?.customerId?.let {
                _customer.value = commonRepository.getCustomerById(it)
            }
        }
    }

    fun shareInvoice() {
        viewModelScope.launch {
            val s = _sale.value ?: return@launch
            val i = _items.value
            val info = _shopInfo.value
            val c = _customer.value
            
            val file = PdfGenerator.generateInvoice(context, s, i, info, c)
            
            if (file != null) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "com.kushwahahardware.fileprovider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = Intent.createChooser(intent, "Share Invoice")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        }
    }
}
