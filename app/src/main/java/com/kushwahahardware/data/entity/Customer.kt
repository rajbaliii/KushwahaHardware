package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String, // Mandatory
    val email: String? = null,
    val address: String? = null,
    val photoUri: String? = null, // New field for photo upload
    val gstNumber: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
