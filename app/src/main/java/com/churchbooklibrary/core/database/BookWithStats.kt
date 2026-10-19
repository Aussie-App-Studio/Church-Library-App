package com.churchbooklibrary.core.database

import androidx.room.Embedded

data class BookWithStats(
    @Embedded val book: BookEntity,
    val borrowCount: Int,
    val lastBorrowedDate: Long?
)

