package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shop_info")
data class ShopInfo(
    @PrimaryKey
    val id: Int = 1,
    val shopName: String = "Kushwaha Hardware",
    val address: String = "Mahanwa, Bihar",
    val phone: String? = null,
    val email: String? = null,
    val gstNumber: String? = null,
    val logo: String? = null,
    val currency: String = "₹",
    val dateFormat: String = "dd/MM/yyyy",
    val lowStockAlertEnabled: Boolean = true,
    val lowStockThreshold: Double = 10.0,
    val biometricLockEnabled: Boolean = false,
    val ownerName: String = "Rajan Kumar",
    val alternativePhone: String? = "9771256815",
    val bankingName: String = "Kushwaha Hardware",
    val upiId: String = "9771256815@sbi",
    val updatedAt: Long = System.currentTimeMillis()
)
