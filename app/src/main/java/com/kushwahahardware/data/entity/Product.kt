package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

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
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["name"]),
        Index(value = ["sku"])
    ]
)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val sku: String? = null,
    val categoryId: Long? = null,
    val brand: String? = null,
    val unit: String = "pcs",
    val purchasePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val currentStock: Double = 0.0,
    val minStockLevel: Double = 0.0,
    val maxStockLevel: Double = 1000.0,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
