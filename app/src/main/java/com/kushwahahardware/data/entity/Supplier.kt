package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Dealer Name (Mandatory)
    val contactPerson: String? = null,
    val phone: String, // Mobile Number (Mandatory)
    val alternatePhone: String? = null,
    val email: String? = null,
    
    // Business Details
    val businessName: String? = null,
    val gstNumber: String? = null,
    val panNumber: String? = null,
    
    // Address Details
    val fullAddress: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pincode: String? = null,
    
    // Banking Details
    val bankName: String? = null,
    val accountNumber: String? = null,
    val ifscCode: String? = null,
    val upiId: String? = null,
    
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
