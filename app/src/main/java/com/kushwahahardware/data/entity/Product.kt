package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("categoryId"), Index("barcode")]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val categoryId: Long? = null,
    val brand: String = "",
    val size: String = "",
    val color: String = "",
    val unit: String = "pcs",
    val purchasePrice: Double,
    val sellingPrice: Double,
    val openingStock: Int = 0,
    val currentStock: Int = 0,
    val lowStockAlert: Int = 5,
    val barcode: String = ""
) {
    fun getProfitMargin(): Double {
        return if (purchasePrice > 0) {
            ((sellingPrice - purchasePrice) / purchasePrice) * 100
        } else 0.0
    }
    
    fun isLowStock(): Boolean {
        return currentStock <= lowStockAlert
    }
}