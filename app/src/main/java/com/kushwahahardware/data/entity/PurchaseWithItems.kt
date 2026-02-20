package com.kushwahahardware.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PurchaseWithItems(
    @Embedded
    val purchase: Purchase,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "purchaseId"
    )
    val items: List<PurchaseItem> = emptyList()
)