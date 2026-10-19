package com.churchbooklibrary

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.churchbooklibrary.theme.ChurchLibraryTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private val openSyncFlow = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)

        enableEdgeToEdge(
            statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )

        val dataStore = com.churchbooklibrary.core.datastore.SettingsDataStore(this)

        setContent {
            val ribbonColorLong by dataStore.ribbonColor.collectAsStateWithLifecycle(
                initialValue = com.churchbooklibrary.core.datastore.SettingsDataStore.DEFAULT_RIBBON_COLOR
            )
            val openSync by openSyncFlow.collectAsStateWithLifecycle()

            ChurchLibraryTheme(primaryColor = androidx.compose.ui.graphics.Color(ribbonColorLong)) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(
                        openSync = openSync,
                        onClearOpenSync = { openSyncFlow.value = false }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("open_sync", false) == true) {
            openSyncFlow.value = true
        }
    }

    override fun onStop() {
        super.onStop()
    }
}


