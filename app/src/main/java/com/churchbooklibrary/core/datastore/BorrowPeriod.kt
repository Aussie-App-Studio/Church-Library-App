package com.churchbooklibrary.core.datastore

import java.util.Calendar

/** Unit of a borrow period. [code] is the single-letter persistence code. */
enum class BorrowPeriodUnit(val code: Char, val calendarField: Int, val calendarMultiplier: Int) {
    DAYS('d', Calendar.DAY_OF_YEAR, 1),
    WEEKS('w', Calendar.DAY_OF_YEAR, 7),
    MONTHS('m', Calendar.MONTH, 1);

    companion object {
        fun fromCode(c: Char): BorrowPeriodUnit? = entries.firstOrNull { it.code == c.lowercaseChar() }
    }
}

/**
 * A single admin-configurable borrow period (e.g. 6 months).
 * [value] is the compact persisted form such as "2w" or "6m"; this is also the format that
 * older builds passed to the due-date calculation ("1w", "2w", "1m", "2m").
 */
data class BorrowPeriod(val count: Int, val unit: BorrowPeriodUnit) {
    val value: String get() = "$count${unit.code}"

    /** Adds this period to [cal]. */
    fun addTo(cal: Calendar) {
        cal.add(unit.calendarField, count * unit.calendarMultiplier)
    }

    companion object {
        const val MIN_COUNT = 1
        const val MAX_COUNT = 99
        const val PERIOD_COUNT = 4

        val DEFAULTS = listOf(
            BorrowPeriod(1, BorrowPeriodUnit.WEEKS),
            BorrowPeriod(2, BorrowPeriodUnit.WEEKS),
            BorrowPeriod(1, BorrowPeriodUnit.MONTHS),
            BorrowPeriod(2, BorrowPeriodUnit.MONTHS)
        )

        fun parse(value: String): BorrowPeriod? {
            val trimmed = value.trim()
            if (trimmed.length < 2) return null
            val unit = BorrowPeriodUnit.fromCode(trimmed.last()) ?: return null
            val count = trimmed.dropLast(1).toIntOrNull() ?: return null
            if (count !in MIN_COUNT..MAX_COUNT) return null
            return BorrowPeriod(count, unit)
        }

        fun serialize(periods: List<BorrowPeriod>): String = periods.joinToString(",") { it.value }

        /** Parses a stored list, falling back to [DEFAULTS] if it is missing or malformed. */
        fun parseList(stored: String?): List<BorrowPeriod> {
            if (stored.isNullOrBlank()) return DEFAULTS
            val parsed = stored.split(",").mapNotNull { parse(it) }
            return if (parsed.size == PERIOD_COUNT) parsed else DEFAULTS
        }
    }
}
