package com.churchbooklibrary.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "general_settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        private val LIBRARY_NAME_KEY = stringPreferencesKey("library_name")
        private val HAS_CUSTOM_LOGO_KEY = booleanPreferencesKey("has_custom_logo")
        private val LOGO_LAST_MODIFIED_KEY = longPreferencesKey("logo_last_modified")
        private val IS_DEMO_MODE_KEY = booleanPreferencesKey("is_demo_mode")
        private val RIBBON_COLOR_KEY = longPreferencesKey("ribbon_color")
        private val HAS_SEEN_ABOUT_KEY = booleanPreferencesKey("has_seen_about")
        private val CATEGORIES_ENABLED_KEY = booleanPreferencesKey("categories_enabled")
        private val ENABLED_CATEGORIES_KEY = stringPreferencesKey("enabled_categories")
        private val CUSTOM_CATEGORIES_KEY = stringPreferencesKey("custom_categories")
        private val MAX_BORROW_LIMIT_KEY = intPreferencesKey("max_borrow_limit")
        private val BORROW_PERIODS_KEY = stringPreferencesKey("borrow_periods")
        private val LAST_DATABASE_UPDATE_KEY = longPreferencesKey("last_database_update")
        private val IMPORTED_CSV_HASHES_KEY = androidx.datastore.preferences.core.stringSetPreferencesKey("imported_csv_hashes")
        private val SYNC_ROLE_KEY = intPreferencesKey("sync_role")
        private val LAST_SUCCESSFUL_SYNC_DATE_KEY = stringPreferencesKey("last_successful_sync_date")
        private val SYNC_DAY_OF_WEEK_KEY = intPreferencesKey("sync_day_of_week")
        private val SYNC_START_HOUR_KEY = intPreferencesKey("sync_start_hour")
        private val SYNC_END_HOUR_KEY = intPreferencesKey("sync_end_hour")
        const val DEFAULT_LIBRARY_NAME = "Hope Church Library"
        const val DEFAULT_RIBBON_COLOR = 0xFF1E3A8AL // Blue800
        val DEFAULT_CATEGORIES = listOf(
            "Biographies",
            "Devotionals",
            "Kids",
            "Commentaries",
            "Theology",
            "Practical Christianity",
            "Bibles",
            "Bible Study Material",
            "Christian Living",
            "Church History",
            "Apologetics",
            "Other"
        )
    }

    val libraryName: Flow<String> = context.settingsDataStore.data
        .map { preferences -> preferences[LIBRARY_NAME_KEY] ?: DEFAULT_LIBRARY_NAME }

    val hasCustomLogo: Flow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[HAS_CUSTOM_LOGO_KEY] ?: false }

    val logoLastModified: Flow<Long> = context.settingsDataStore.data
        .map { preferences -> preferences[LOGO_LAST_MODIFIED_KEY] ?: 0L }

    val isDemoMode: Flow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[IS_DEMO_MODE_KEY] ?: true }

    val ribbonColor: Flow<Long> = context.settingsDataStore.data
        .map { preferences -> preferences[RIBBON_COLOR_KEY] ?: DEFAULT_RIBBON_COLOR }

    val hasSeenAboutDeveloper: Flow<Boolean> = context.settingsDataStore.data
        .map { preferences -> preferences[HAS_SEEN_ABOUT_KEY] ?: false }

    val categoriesEnabled: Flow<Boolean> = context.settingsDataStore.data
        .map { it[CATEGORIES_ENABLED_KEY] ?: false }

    val enabledCategories: Flow<Set<String>> = context.settingsDataStore.data
        .map { prefs ->
            val saved = prefs[ENABLED_CATEGORIES_KEY] ?: ""
            if (saved.isBlank()) DEFAULT_CATEGORIES.toSet()
            else saved.split("||").toSet()
        }

    val maxBorrowLimit: Flow<Int> = context.settingsDataStore.data
        .map { it[MAX_BORROW_LIMIT_KEY] ?: 4 }

    suspend fun setMaxBorrowLimit(limit: Int) {
        context.settingsDataStore.edit { it[MAX_BORROW_LIMIT_KEY] = limit }
    }

    val borrowPeriods: Flow<List<BorrowPeriod>> = context.settingsDataStore.data
        .map { BorrowPeriod.parseList(it[BORROW_PERIODS_KEY]) }

    suspend fun setBorrowPeriods(periods: List<BorrowPeriod>) {
        require(periods.size == BorrowPeriod.PERIOD_COUNT)
        context.settingsDataStore.edit { it[BORROW_PERIODS_KEY] = BorrowPeriod.serialize(periods) }
    }

    suspend fun setLibraryName(name: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[LIBRARY_NAME_KEY] = name
        }
    }

    suspend fun setHasCustomLogo(hasLogo: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[HAS_CUSTOM_LOGO_KEY] = hasLogo
            prefs[LOGO_LAST_MODIFIED_KEY] = System.currentTimeMillis()
        }
    }

    suspend fun setDemoMode(isDemo: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[IS_DEMO_MODE_KEY] = isDemo
        }
    }

    suspend fun setRibbonColor(color: Long) {
        context.settingsDataStore.edit { prefs ->
            prefs[RIBBON_COLOR_KEY] = color
        }
    }

    suspend fun setHasSeenAboutDeveloper(seen: Boolean) {
        context.settingsDataStore.edit { prefs ->
            prefs[HAS_SEEN_ABOUT_KEY] = seen
        }
    }

    val lastDatabaseUpdate: Flow<Long> = context.settingsDataStore.data
        .map { it[LAST_DATABASE_UPDATE_KEY] ?: 0L }

    val importedCsvHashes: Flow<Set<String>> = context.settingsDataStore.data
        .map { it[IMPORTED_CSV_HASHES_KEY] ?: emptySet() }

    suspend fun addImportedCsvHash(hash: String) {
        context.settingsDataStore.edit { prefs ->
            prefs[IMPORTED_CSV_HASHES_KEY] = (prefs[IMPORTED_CSV_HASHES_KEY] ?: emptySet()) + hash
        }
    }

    val syncRole: Flow<Int> = context.settingsDataStore.data
        .map { it[SYNC_ROLE_KEY] ?: 0 }

    val lastSuccessfulSyncDate: Flow<String> = context.settingsDataStore.data
        .map { it[LAST_SUCCESSFUL_SYNC_DATE_KEY] ?: "" }

    val syncDayOfWeek: Flow<Int> = context.settingsDataStore.data
        .map { it[SYNC_DAY_OF_WEEK_KEY] ?: java.util.Calendar.SUNDAY }

    val syncStartHour: Flow<Int> = context.settingsDataStore.data
        .map { it[SYNC_START_HOUR_KEY] ?: 8 }

    val syncEndHour: Flow<Int> = context.settingsDataStore.data
        .map { it[SYNC_END_HOUR_KEY] ?: 14 }

    suspend fun setSyncSchedule(day: Int, startHour: Int, endHour: Int) {
        context.settingsDataStore.edit { prefs ->
            prefs[SYNC_DAY_OF_WEEK_KEY] = day
            prefs[SYNC_START_HOUR_KEY] = startHour
            prefs[SYNC_END_HOUR_KEY] = endHour
        }
    }

    suspend fun setSyncRole(role: Int) {
        context.settingsDataStore.edit { it[SYNC_ROLE_KEY] = role }
    }

    suspend fun setLastSuccessfulSyncDate(date: String) {
        context.settingsDataStore.edit { it[LAST_SUCCESSFUL_SYNC_DATE_KEY] = date }
    }

    suspend fun touchDatabaseUpdateTimestamp() {
        context.settingsDataStore.edit { it[LAST_DATABASE_UPDATE_KEY] = System.currentTimeMillis() }
    }

    val customCategories: Flow<Set<String>> = context.settingsDataStore.data
        .map { prefs ->
            val saved = prefs[CUSTOM_CATEGORIES_KEY] ?: ""
            if (saved.isBlank()) emptySet()
            else saved.split("||").toSet()
        }

    suspend fun addCustomCategory(category: String) {
        context.settingsDataStore.edit { prefs ->
            val saved = prefs[CUSTOM_CATEGORIES_KEY] ?: ""
            val current = if (saved.isBlank()) emptySet() else saved.split("||").toSet()
            prefs[CUSTOM_CATEGORIES_KEY] = (current + category).joinToString("||")
        }
    }

    suspend fun removeCustomCategory(category: String) {
        context.settingsDataStore.edit { prefs ->
            val saved = prefs[CUSTOM_CATEGORIES_KEY] ?: ""
            val current = if (saved.isBlank()) emptySet() else saved.split("||").toSet()
            prefs[CUSTOM_CATEGORIES_KEY] = (current - category).joinToString("||")
        }
    }

    suspend fun setCategoriesEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { it[CATEGORIES_ENABLED_KEY] = enabled }
    }

    suspend fun setEnabledCategories(categories: Set<String>) {
        context.settingsDataStore.edit { it[ENABLED_CATEGORIES_KEY] = categories.joinToString("||") }
    }
}


