package com.churchbooklibrary.core.database

import androidx.room.Embedded

data class BookWithAvailability(
    @Embedded val book: BookEntity,
    val availableCopies: Int
)

