package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "sale_items",
    indices = [Index(value = ["saleId"]), Index(value = ["productId"])],
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
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
data class SaleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val quantity: Double,
    val unitPrice: Double,
    val purchasePriceAtSale: Double = 0.0,
    val totalPrice: Double,
    val productName: String = "",
    val discount: Double = 0.0,
    val tax: Double = 0.0
)
