package com.churchbooklibrary

import android.app.Application
import com.churchbooklibrary.core.database.AppDatabase
import com.churchbooklibrary.core.datastore.PinDataStore
import com.churchbooklibrary.core.datastore.SettingsDataStore
import com.churchbooklibrary.core.repository.LibraryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LibraryApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this) }
    val demoDatabase by lazy { AppDatabase.getInMemoryDatabase(this) }
    
    val pinDataStore by lazy { PinDataStore(this) }
    val settingsDataStore by lazy { SettingsDataStore(this) }
    
    val repository by lazy { 
        LibraryRepository(
            realBookDao = database.bookDao(), 
            realLoanDao = database.loanDao(),
            demoBookDao = demoDatabase.bookDao(),
            demoLoanDao = demoDatabase.loanDao(),
            settingsDataStore = settingsDataStore
        ) 
    }

    val syncManager by lazy {
        com.churchbooklibrary.core.sync.OfflineSyncManager(this, repository)
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            // Seed demo database
            repository.seedDemoDatabase()
            
            // Ensure PIN is set
            pinDataStore.ensureDefaultPin()
        }
    }
}

