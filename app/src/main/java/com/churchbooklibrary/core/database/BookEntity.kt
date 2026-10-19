package com.churchbooklibrary.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import kotlinx.serialization.Serializable
import androidx.room.PrimaryKey

@Serializable
@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val coverUrl: String? = null,
    val isbn: String? = null,
    @ColumnInfo(name = "owner") val owner: String? = null,
    val totalCopies: Int = 1,
    val category: String? = null,
    @ColumnInfo(defaultValue = "0") val updatedAt: Long = 0L
)


