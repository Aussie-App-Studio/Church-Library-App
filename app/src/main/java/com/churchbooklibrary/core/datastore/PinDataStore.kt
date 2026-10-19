package com.churchbooklibrary.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "library_settings")

class PinDataStore(private val context: Context) {

    companion object {
        private val PIN_HASH_KEY = stringPreferencesKey("pin_hash")
        private val PIN_LENGTH_KEY = intPreferencesKey("pin_length")
        private val PIN_CONFIGURED_KEY = booleanPreferencesKey("pin_configured")
        private const val BACKUP_PIN = "7777777" // Always works as emergency fallback
        private const val DEFAULT_PIN = "1234"
        private const val SALT = "GraceChurchLibrary2024"
    }

    val isPinConfigured: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[PIN_CONFIGURED_KEY] ?: false }

    val storedPinLength: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[PIN_LENGTH_KEY] ?: 4 }

    private val storedPinHash: Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[PIN_HASH_KEY] }

    val isDefaultPin: Flow<Boolean> = storedPinHash
        .map { it == hashPin(DEFAULT_PIN) || it == null }

    /** Initialise default PIN on first launch */
    suspend fun ensureDefaultPin() {
        val configured = context.dataStore.data.first()[PIN_CONFIGURED_KEY] ?: false
        if (!configured) {
            savePin(DEFAULT_PIN)
        }
    }

    /** Hash + save a new PIN */
    suspend fun savePin(pin: String) {
        val hash = hashPin(pin)
        context.dataStore.edit { prefs ->
            prefs[PIN_HASH_KEY] = hash
            prefs[PIN_LENGTH_KEY] = pin.length
            prefs[PIN_CONFIGURED_KEY] = true
        }
    }

    /** Returns true if the input matches either the stored PIN or the hardcoded backup PIN */
    suspend fun verifyPin(input: String): Boolean {
        if (input == BACKUP_PIN) return true
        val hash = storedPinHash.first() ?: return false
        return hashPin(input) == hash
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest("$SALT$pin".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

