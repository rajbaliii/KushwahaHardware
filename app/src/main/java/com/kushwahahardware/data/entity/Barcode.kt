package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "barcodes",
    indices = [
        Index(value = ["serialNumber"], unique = true)
    ]
)
data class Barcode(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serialNumber: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "AVAILABLE" // "AVAILABLE", "ASSIGNED", "USED"
)
