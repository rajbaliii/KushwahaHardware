package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.*

@Entity(
    tableName = "stock_history",
    indices = [Index(value = ["productId"])],
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StockHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val transactionType: String, // PURCHASE, SALE, ADJUSTMENT, RETURN
    val quantity: Double,
    val previousStock: Double,
    val newStock: Double,
    val referenceId: Long? = null, // Purchase ID or Sale ID
    val referenceType: String? = null, // PURCHASE, SALE
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
