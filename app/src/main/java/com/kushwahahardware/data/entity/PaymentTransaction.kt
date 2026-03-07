package com.kushwahahardware.data.entity

import androidx.room.*

@Entity(
    tableName = "payment_transactions",
    indices = [
        Index(value = ["partyId", "partyType"])
    ]
)
data class PaymentTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val partyId: Long,
    val partyType: String, // "CUSTOMER" or "SUPPLIER"
    val amount: Double,
    val transactionType: String, // "GOT" or "GAVE"
    val notes: String? = null,
    val transactionDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
