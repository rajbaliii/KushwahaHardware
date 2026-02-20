package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_info")
data class ShopInfo(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "Kushwaha Hardware",
    val location: String = "Mahanwa, Bihar",
    val phone: String = "",
    val email: String = "",
    val gstNumber: String = "",
    val invoicePrefix: String = "INV",
    val nextInvoiceNumber: Int = 1,
    val biometricEnabled: Boolean = false
)