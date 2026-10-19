package com.churchbooklibrary.core.backup

import android.content.Context
import android.net.Uri
import com.churchbooklibrary.core.database.BookEntity
import com.churchbooklibrary.core.database.LoanEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException

@Serializable
data class LibraryBackup(
    val books: List<BookBackupItem>,
    val loans: List<LoanBackupItem>,
    val exportedAt: Long = System.currentTimeMillis(),
    // v1 = books (id/title/author/isbn) + loans only. v2 = every column.
    val version: Int = 2
)

// Fields added in v2 all have defaults so v1 backup files still decode.
@Serializable
data class BookBackupItem(
    val id: Long,
    val title: String,
    val author: String,
    val isbn: String? = null,
    val owner: String? = null,
    val coverUrl: String? = null,
    val category: String? = null,
    val totalCopies: Int = 1,
    val updatedAt: Long = 0L
)

@Serializable
data class LoanBackupItem(
    val id: Long,
    val borrowerName: String,
    val borrowerEmail: String,
    val borrowerPhone: String,
    val bookId: Long,
    val borrowDate: Long,
    val dueDate: Long,
    val durationLabel: String,
    val status: String,
    val returnedDate: Long? = null,
    val quantity: Int = 1,
    val updatedAt: Long = 0L
)

object BackupManager {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true; encodeDefaults = true }

    fun exportToJson(books: List<BookEntity>, loans: List<LoanEntity>): String {
        val backup = LibraryBackup(
            books = books.map {
                BookBackupItem(
                    id = it.id,
                    title = it.title,
                    author = it.author,
                    isbn = it.isbn,
                    owner = it.owner,
                    coverUrl = it.coverUrl,
                    category = it.category,
                    totalCopies = it.totalCopies,
                    updatedAt = it.updatedAt
                )
            },
            loans = loans.map {
                LoanBackupItem(
                    id = it.id,
                    borrowerName = it.borrowerName,
                    borrowerEmail = it.borrowerEmail,
                    borrowerPhone = it.borrowerPhone,
                    bookId = it.bookId,
                    borrowDate = it.borrowDate,
                    dueDate = it.dueDate,
                    durationLabel = it.durationLabel,
                    status = it.status,
                    returnedDate = it.returnedDate,
                    quantity = it.quantity,
                    updatedAt = it.updatedAt
                )
            }
        )
        return json.encodeToString(backup)
    }

    fun BookBackupItem.toEntity() = BookEntity(
        id = id, title = title, author = author, isbn = isbn, owner = owner,
        coverUrl = coverUrl, category = category,
        totalCopies = totalCopies.coerceAtLeast(1), updatedAt = updatedAt
    )

    fun LoanBackupItem.toEntity() = LoanEntity(
        id = id, borrowerName = borrowerName, borrowerEmail = borrowerEmail,
        borrowerPhone = borrowerPhone, bookId = bookId, borrowDate = borrowDate,
        dueDate = dueDate, durationLabel = durationLabel, status = status,
        returnedDate = returnedDate, quantity = quantity.coerceAtLeast(1), updatedAt = updatedAt
    )

    fun writeToUri(context: Context, uri: Uri, jsonString: String) {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(jsonString.toByteArray(Charsets.UTF_8))
        } ?: throw IOException("Could not open output stream for URI: $uri")
    }

    fun readFromUri(context: Context, uri: Uri): LibraryBackup {
        val jsonString = context.contentResolver.openInputStream(uri)
            ?.bufferedReader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: throw IOException("Could not read from URI: $uri")
        return json.decodeFromString<LibraryBackup>(jsonString)
    }

    data class CsvBookRow(
        val title: String,
        val author: String,
        val isbn: String? = null,
        val owner: String? = null,
        val category: String? = null,
        val totalCopies: Int = 1
    )

    data class CsvParseResult(
        val rows: List<CsvBookRow>,
        val skippedMalformedCount: Int = 0
    )

    fun parseCsv(csvText: String): CsvParseResult {
        val sanitized = csvText.trimStart('\uFEFF')
        val firstLine = sanitized.lineSequence().firstOrNull() ?: ""
        val delimiter = if (firstLine.count { it == ';' } > firstLine.count { it == ',' }) ';' else ','

        val records = parseCsvRecords(sanitized, delimiter)
        val result = mutableListOf<CsvBookRow>()
        var skippedCount = 0
        var isFirstContentLine = true

        for (tokens in records) {
            // Ignore completely empty lines
            if (tokens.isEmpty() || (tokens.size == 1 && tokens[0].trim().isBlank())) {
                continue
            }

            val first = tokens.getOrNull(0)?.trim()?.removeSurrounding("\"")?.trim() ?: ""
            val second = tokens.getOrNull(1)?.trim()?.removeSurrounding("\"")?.trim() ?: ""

            // Header row detection on first non-empty line
            if (isFirstContentLine) {
                isFirstContentLine = false
                if (first.equals("title", ignoreCase = true) &&
                    (second.equals("author", ignoreCase = true) || second.equals("creator", ignoreCase = true))) {
                    continue
                }
            }

            if (first.isBlank() || second.isBlank() || tokens.size < 2) {
                skippedCount++
                continue
            }

            val title = first
            val author = second
            val isbn = tokens.getOrNull(2)?.trim()?.removeSurrounding("\"")?.trim()?.takeIf { it.isNotBlank() }
            val owner = tokens.getOrNull(3)?.trim()?.removeSurrounding("\"")?.trim()?.takeIf { it.isNotBlank() }
            val category = tokens.getOrNull(4)?.trim()?.removeSurrounding("\"")?.trim()?.takeIf { it.isNotBlank() }
            val copies = tokens.getOrNull(5)?.trim()?.removeSurrounding("\"")?.trim()?.toIntOrNull()?.coerceAtLeast(1) ?: 1

            result.add(CsvBookRow(title, author, isbn, owner, category, copies))
        }

        return CsvParseResult(result, skippedCount)
    }

    private fun parseCsvRecords(text: String, delimiter: Char): List<List<String>> {
        val records = mutableListOf<List<String>>()
        val currentRecord = mutableListOf<String>()
        val currentField = StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < text.length) {
            val c = text[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < text.length && text[i + 1] == '"') {
                        currentField.append('"')
                        i++ // Skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == delimiter && !inQuotes -> {
                    currentRecord.add(currentField.toString().trim().removeSurrounding("\"").trim())
                    currentField.setLength(0)
                }
                c == '\r' && !inQuotes -> {
                    if (i + 1 < text.length && text[i + 1] == '\n') {
                        i++ // Skip '\n' of Windows CRLF
                    }
                    currentRecord.add(currentField.toString().trim().removeSurrounding("\"").trim())
                    currentField.setLength(0)
                    records.add(ArrayList(currentRecord))
                    currentRecord.clear()
                }
                c == '\n' && !inQuotes -> {
                    currentRecord.add(currentField.toString().trim().removeSurrounding("\"").trim())
                    currentField.setLength(0)
                    records.add(ArrayList(currentRecord))
                    currentRecord.clear()
                }
                else -> {
                    currentField.append(c)
                }
            }
            i++
        }

        if (currentField.isNotEmpty() || currentRecord.isNotEmpty()) {
            currentRecord.add(currentField.toString().trim().removeSurrounding("\"").trim())
            records.add(ArrayList(currentRecord))
        }

        return records
    }
}
