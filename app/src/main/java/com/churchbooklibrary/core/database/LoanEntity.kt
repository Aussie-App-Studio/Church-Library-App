package com.churchbooklibrary.core.database

import androidx.room.Entity
import kotlinx.serialization.Serializable
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Serializable
@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("bookId")]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val borrowerName: String,
    val borrowerEmail: String,
    val borrowerPhone: String,
    val bookId: Long,
    val borrowDate: Long,
    val dueDate: Long,
    val durationLabel: String,
    val status: String = "active", // "active" or "returned"
    val returnedDate: Long? = null,
    val quantity: Int = 1,
    @androidx.room.ColumnInfo(defaultValue = "0") val updatedAt: Long = 0L
)


