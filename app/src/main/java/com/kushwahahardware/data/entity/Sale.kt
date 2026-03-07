package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.*

@Entity(
    tableName = "sales",
    indices = [Index(value = ["customerId"])],
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val saleDate: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val totalProfit: Double = 0.0,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val paymentType: String = "CASH", // CASH, CREDIT
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
