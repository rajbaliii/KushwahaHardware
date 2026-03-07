package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.*

@Entity(
    tableName = "purchases",
    indices = [Index(value = ["supplierId"])],
    foreignKeys = [
        ForeignKey(
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class Purchase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val supplierId: Long? = null,
    val purchaseDate: Long = System.currentTimeMillis(),
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val pendingAmount: Double = 0.0,
    val discount: Double = 0.0,
    val tax: Double = 0.0,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
