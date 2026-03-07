package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "purchase_items",
    indices = [Index(value = ["purchaseId"]), Index(value = ["productId"])],
    foreignKeys = [
        ForeignKey(
            entity = Purchase::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class PurchaseItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseId: Long,
    val productId: Long,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double,
    val productName: String = "",
    val discount: Double = 0.0,
    val tax: Double = 0.0
)
