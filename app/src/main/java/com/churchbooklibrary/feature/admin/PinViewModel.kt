package com.churchbooklibrary.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.churchbooklibrary.core.datastore.PinDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class PinViewModel(private val pinDataStore: PinDataStore) : ViewModel() {

    sealed class AuthState {
        object Idle : AuthState()
        object Authenticated : AuthState()
        data class Error(val shake: Boolean = true) : AuthState()
    }

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val isDefaultPin: StateFlow<Boolean> = pinDataStore.isDefaultPin
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun onDigit(digit: Char) {
        if (_enteredPin.value.length < 7) {
            _enteredPin.value += digit
            _authState.value = AuthState.Idle
            verifyPin()
        }
    }

    fun onBackspace() {
        if (_enteredPin.value.isNotEmpty()) {
            _enteredPin.value = _enteredPin.value.dropLast(1)
            // Reset error state when user starts correcting
            if (_authState.value is AuthState.Error) {
                _authState.value = AuthState.Idle
            }
        }
    }

    fun resetAuth() {
        _enteredPin.value = ""
        _authState.value = AuthState.Idle
    }

    fun clearAuthenticatedState() {
        _authState.value = AuthState.Idle
        _enteredPin.value = ""
    }

    private fun verifyPin() {
        val pin = _enteredPin.value
        viewModelScope.launch {
            val ok = pinDataStore.verifyPin(pin)
            if (ok) {
                _authState.value = AuthState.Authenticated
            } else {
                val storedLength = pinDataStore.storedPinLength.first()
                val isBackupPrefix = "7777777".startsWith(pin)

                // Fail and clear only if:
                // 1. Full 7 digits entered and incorrect, OR
                // 2. We reached/exceeded the configured PIN length AND the input cannot be a prefix of the emergency PIN 7777777
                if (pin.length == 7 || (pin.length >= storedLength && !isBackupPrefix)) {
                    _enteredPin.value = ""
                    _authState.value = AuthState.Error()
                }
            }
        }
    }

    class Factory(private val pinDataStore: PinDataStore) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PinViewModel(pinDataStore) as T
    }
}

