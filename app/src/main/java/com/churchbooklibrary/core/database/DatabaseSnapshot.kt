package com.churchbooklibrary.core.database

import kotlinx.serialization.Serializable

@Serializable
data class DatabaseSnapshot(
    val timestamp: Long,
    val books: List<BookEntity>,
    val loans: List<LoanEntity>
)

