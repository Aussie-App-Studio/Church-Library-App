package com.churchbooklibrary.core.backup

import com.churchbooklibrary.core.database.BookWithAvailability
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/** A column the admin can include when exporting the catalog for a website. */
enum class CatalogField(val key: String, val label: String, val hint: String) {
    TITLE("title", "Title", "Book title"),
    AUTHOR("author", "Author", "Author name"),
    ISBN("isbn", "ISBN", "Barcode number, if known"),
    CONSIGNOR("consignor", "Consignor / Owner", "Who donated or owns the book"),
    CATEGORY("category", "Category", "Shelf or category"),
    TOTAL_COPIES("total_copies", "Total copies", "Copies the library owns"),
    AVAILABLE_COPIES("available_copies", "Copies available now", "Copies not currently on loan"),
    COVER_URL("cover_url", "Cover image link", "Web address of the cover picture");

    companion object {
        val DEFAULT_SELECTION: Set<CatalogField> = setOf(TITLE, AUTHOR)
    }
}

enum class CatalogFormat(val label: String, val description: String, val extension: String, val mimeType: String) {
    CSV("CSV", "Spreadsheet-style table. Opens in Excel and imports into most website tools.", "csv", "text/csv"),
    JSON("JSON", "Structured data for web developers.", "json", "application/json"),
    HTML("HTML table", "A ready-made table that can be pasted straight into a web page.", "html", "text/html")
}

/** Builds website-friendly exports of the book catalog with only the fields the admin chose. */
object CatalogExporter {

    private val prettyJson = Json { prettyPrint = true }

    fun fileName(format: CatalogFormat, timestamp: Long = System.currentTimeMillis()): String =
        "church_library_catalog_$timestamp.${format.extension}"

    fun export(books: List<BookWithAvailability>, fields: Set<CatalogField>, format: CatalogFormat): String {
        // Keep columns in a stable, logical order regardless of tick order.
        val ordered = CatalogField.entries.filter { it in fields }
        require(ordered.isNotEmpty()) { "Select at least one field to export." }
        return when (format) {
            CatalogFormat.CSV -> toCsv(books, ordered)
            CatalogFormat.JSON -> toJson(books, ordered)
            CatalogFormat.HTML -> toHtml(books, ordered)
        }
    }

    private fun valueOf(item: BookWithAvailability, field: CatalogField): String? {
        val b = item.book
        return when (field) {
            CatalogField.TITLE -> b.title
            CatalogField.AUTHOR -> b.author
            CatalogField.ISBN -> b.isbn?.takeIf { it.isNotBlank() }
            CatalogField.CONSIGNOR -> b.owner?.takeIf { it.isNotBlank() }
            CatalogField.CATEGORY -> b.category?.takeIf { it.isNotBlank() }
            CatalogField.TOTAL_COPIES -> b.totalCopies.toString()
            CatalogField.AVAILABLE_COPIES -> item.availableCopies.coerceAtLeast(0).toString()
            CatalogField.COVER_URL -> b.coverUrl?.takeIf { it.isNotBlank() }
        }
    }

    private fun isNumeric(field: CatalogField) =
        field == CatalogField.TOTAL_COPIES || field == CatalogField.AVAILABLE_COPIES

    private fun toCsv(books: List<BookWithAvailability>, fields: List<CatalogField>): String {
        val sb = StringBuilder()
        sb.append(fields.joinToString(",") { csvCell(it.key) }).append("\r\n")
        books.forEach { item ->
            sb.append(fields.joinToString(",") { csvCell(valueOf(item, it) ?: "") }).append("\r\n")
        }
        return sb.toString()
    }

    private fun csvCell(value: String): String {
        val needsQuotes = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        return if (needsQuotes) "\"" + value.replace("\"", "\"\"") + "\"" else value
    }

    private fun toJson(books: List<BookWithAvailability>, fields: List<CatalogField>): String {
        val array = JsonArray(books.map { item ->
            JsonObject(fields.associate { field ->
                val v = valueOf(item, field)
                field.key to when {
                    v == null -> JsonNull
                    isNumeric(field) -> JsonPrimitive(v.toInt())
                    else -> JsonPrimitive(v)
                }
            })
        })
        return prettyJson.encodeToString(JsonArray.serializer(), array)
    }

    private fun toHtml(books: List<BookWithAvailability>, fields: List<CatalogField>): String {
        val sb = StringBuilder()
        sb.append("<table class=\"library-catalog\">\n  <thead>\n    <tr>")
        fields.forEach { sb.append("<th>").append(htmlEscape(it.label)).append("</th>") }
        sb.append("</tr>\n  </thead>\n  <tbody>\n")
        books.forEach { item ->
            sb.append("    <tr>")
            fields.forEach { field ->
                val v = valueOf(item, field) ?: ""
                sb.append("<td>")
                if (field == CatalogField.COVER_URL && v.isNotEmpty()) {
                    sb.append("<img src=\"").append(htmlEscape(v)).append("\" alt=\"\" height=\"80\">")
                } else {
                    sb.append(htmlEscape(v))
                }
                sb.append("</td>")
            }
            sb.append("</tr>\n")
        }
        sb.append("  </tbody>\n</table>\n")
        return sb.toString()
    }

    private fun htmlEscape(s: String): String =
        s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
}
