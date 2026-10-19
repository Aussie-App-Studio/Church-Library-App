package com.churchbooklibrary.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.churchbooklibrary.core.datastore.SettingsDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    settingsDataStore: SettingsDataStore
) : ViewModel() {

    val libraryName: StateFlow<String> = settingsDataStore.libraryName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsDataStore.DEFAULT_LIBRARY_NAME
        )

    val hasCustomLogo: StateFlow<Boolean> = settingsDataStore.hasCustomLogo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val logoLastModified: StateFlow<Long> = settingsDataStore.logoLastModified
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val isDemoMode: StateFlow<Boolean> = settingsDataStore.isDemoMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    class Factory(
        private val settingsDataStore: SettingsDataStore
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(settingsDataStore) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

