package com.churchbooklibrary.core.database

import androidx.room.Embedded
import androidx.room.Relation

data class LoanWithBook(
    @Embedded val loan: LoanEntity,
    @Relation(
        parentColumn = "bookId",
        entityColumn = "id"
    )
    val book: BookEntity
)

