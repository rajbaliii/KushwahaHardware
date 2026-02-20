package com.kushwahahardware.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
) {
    companion object {
        fun getDefaultCategories(): List<Category> {
            return listOf(
                Category(name = "Paint"),
                Category(name = "Plumbing"),
                Category(name = "Steel"),
                Category(name = "Iron"),
                Category(name = "Tools"),
                Category(name = "Others")
            )
        }
    }
}