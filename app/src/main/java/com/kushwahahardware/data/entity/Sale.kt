package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("customerId"), Index("date")]
)
data class Sale(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val date: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val paymentType: PaymentType = PaymentType.CASH,
    val invoiceNumber: String = ""
) {
    fun calculatePending(): Double {
        return totalAmount - paidAmount
    }
    
    fun getFormattedInvoiceNumber(): String {
        return invoiceNumber.ifEmpty { "INV${String.format("%04d", id)}" }
    }
}

enum class PaymentType {
    CASH,
    CREDIT
}