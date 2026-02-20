package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stock_history",
    foreignKeys = [
        ForeignKey(
            entity = Product::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productId"), Index("date")]
)
data class StockHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val date: Long = System.currentTimeMillis(),
    val type: StockMovementType,
    val quantity: Int,
    val previousStock: Int,
    val newStock: Int,
    val referenceId: Long? = null,
    val referenceType: String = "",
    val notes: String = ""
)

enum class StockMovementType {
    IN,
    OUT,
    ADJUSTMENT
}