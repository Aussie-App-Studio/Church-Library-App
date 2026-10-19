package com.churchbooklibrary.core.sync

import android.content.Context
import android.util.Log
import com.churchbooklibrary.LibraryApplication
import com.churchbooklibrary.core.repository.LibraryRepository
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets

class OfflineSyncManager(private val context: Context, private val repository: LibraryRepository) {
    private val connectionsClient: ConnectionsClient = Nearby.getConnectionsClient(context)
    private val SERVICE_ID = "com.churchbooklibrary.SYNC_SERVICE"
    private val STRATEGY = Strategy.P2P_POINT_TO_POINT

    private val _syncStatus = MutableStateFlow("Idle")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    private var connectedEndpointId: String? = null
    private val syncScope = CoroutineScope(Dispatchers.IO)
    
    // Store incoming streams
    private val incomingPayloads = mutableMapOf<Long, Payload>()

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            if (payload.type == Payload.Type.BYTES) {
                val data = String(payload.asBytes()!!, StandardCharsets.UTF_8)
                if (data.startsWith("SYNC_REQ")) {
                    _syncStatus.value = "Sending Database..."
                    syncScope.launch {
                        sendDatabase(endpointId)
                    }
                } else if (data.startsWith("TS:")) {
                    val remoteTs = data.removePrefix("TS:").toLongOrNull() ?: 0L
                    syncScope.launch {
                        val app = context.applicationContext as LibraryApplication
                        val localTs = app.settingsDataStore.lastDatabaseUpdate.first()
                        if (remoteTs == localTs && localTs > 0L) {
                            _syncStatus.value = "Up to date (No changes)"
                        } else {
                            _syncStatus.value = "Requesting library data..."
                            sendSyncRequest(endpointId)
                        }
                    }
                }
            } else if (payload.type == Payload.Type.STREAM) {
                incomingPayloads[payload.id] = payload
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            if (update.status == PayloadTransferUpdate.Status.SUCCESS) {
                val payload = incomingPayloads.remove(update.payloadId)
                if (payload != null && payload.type == Payload.Type.STREAM) {
                    _syncStatus.value = "Database Received. Importing..."
                    syncScope.launch {
                        try {
                            val inputStream = payload.asStream()!!.asInputStream()
                            val jsonString = inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                            repository.mergeDatabaseFromJson(jsonString)
                            val app = context.applicationContext as LibraryApplication
                            val cal = java.util.Calendar.getInstance()
                            val today = "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.MONTH)}-${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
                            app.settingsDataStore.setLastSuccessfulSyncDate(today)
                            _syncStatus.value = "Sync Complete!"
                        } catch (e: Exception) {
                            _syncStatus.value = "Sync Failed: ${e.message}"
                        }
                    }
                } else {
                    if (_syncStatus.value.contains("Sending Database")) {
                        _syncStatus.value = "Sync Sent Successfully."
                        syncScope.launch {
                            val app = context.applicationContext as LibraryApplication
                            val cal = java.util.Calendar.getInstance()
                            val today = "${cal.get(java.util.Calendar.YEAR)}-${cal.get(java.util.Calendar.MONTH)}-${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
                            app.settingsDataStore.setLastSuccessfulSyncDate(today)
                            // Allow ongoing incoming transfer to finish before stopping service
                            kotlinx.coroutines.delay(3000)
                            context.stopService(android.content.Intent(context, TabletSyncService::class.java))
                        }
                    }
                }
            } else if (update.status == PayloadTransferUpdate.Status.FAILURE) {
                _syncStatus.value = "Transfer Failed"
            } else if (update.status == PayloadTransferUpdate.Status.IN_PROGRESS) {
                // Ignore progress updates to avoid spamming the UI flow too much
            }
        }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
            _syncStatus.value = "Connecting to ${connectionInfo.endpointName}..."
            connectionsClient.acceptConnection(endpointId, payloadCallback)
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            if (result.status.isSuccess) {
                connectedEndpointId = endpointId
                
                syncScope.launch {
                    val ts = (context.applicationContext as LibraryApplication).settingsDataStore.lastDatabaseUpdate.first()
                    sendTimestamp(ts)
                }
            } else {
                _syncStatus.value = "Connection Failed"
            }
        }

        override fun onDisconnected(endpointId: String) {
            connectedEndpointId = null
            if (!_syncStatus.value.contains("Complete") && !_syncStatus.value.contains("Up to date") && !_syncStatus.value.contains("Successfully")) {
                _syncStatus.value = "Disconnected"
            }
        }
    }

    fun startAdvertising() {
        if (!SyncPermissionHelper.hasAllRequiredPermissions(context)) {
            _syncStatus.value = "Permissions Required: Nearby Device & Bluetooth permissions must be granted."
            return
        }
        val options = AdvertisingOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startAdvertising("Library Kiosk", SERVICE_ID, connectionLifecycleCallback, options)
            .addOnSuccessListener { _syncStatus.value = "Broadcasting (Ready for Phone)" }
            .addOnFailureListener { error ->
                val msg = if (error.message?.contains("8034") == true) {
                    "Permissions Required: Grant Nearby Device permissions in Settings."
                } else {
                    "Broadcasting Failed: ${error.message}"
                }
                _syncStatus.value = msg
            }
    }

    fun stopAdvertising(disconnectEndpoints: Boolean = false) {
        connectionsClient.stopAdvertising()
        if (disconnectEndpoints) {
            connectionsClient.stopAllEndpoints()
            connectedEndpointId = null
            _syncStatus.value = "Idle"
        } else if (connectedEndpointId == null) {
            _syncStatus.value = "Idle"
        }
    }

    fun startDiscovery() {
        if (!SyncPermissionHelper.hasAllRequiredPermissions(context)) {
            _syncStatus.value = "Permissions Required: Nearby Device & Bluetooth permissions must be granted."
            return
        }
        val options = DiscoveryOptions.Builder().setStrategy(STRATEGY).build()
        connectionsClient.startDiscovery(
            SERVICE_ID,
            object : EndpointDiscoveryCallback() {
                override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                    _syncStatus.value = "Found Kiosk! Connecting..."
                    connectionsClient.requestConnection("Librarian Phone", endpointId, connectionLifecycleCallback)
                }

                override fun onEndpointLost(endpointId: String) {
                    _syncStatus.value = "Lost Kiosk Signal"
                }
            },
            options
        )
            .addOnSuccessListener { _syncStatus.value = "Scanning for Kiosk..." }
            .addOnFailureListener { error ->
                val msg = if (error.message?.contains("8034") == true) {
                    "Permissions Required: Grant Nearby Device permissions in Settings."
                } else {
                    "Scanner Failed: ${error.message}"
                }
                _syncStatus.value = msg
            }
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
        connectionsClient.stopAllEndpoints()
        _syncStatus.value = "Idle"
    }
    
    private fun sendTimestamp(timestamp: Long) {
        connectedEndpointId?.let {
            val payload = Payload.fromBytes("TS:$timestamp".toByteArray(StandardCharsets.UTF_8))
            connectionsClient.sendPayload(it, payload)
        }
    }

    private fun sendSyncRequest(endpointId: String) {
        try {
            val payload = Payload.fromBytes("SYNC_REQ".toByteArray(StandardCharsets.UTF_8))
            connectionsClient.sendPayload(endpointId, payload)
        } catch (e: Exception) {
            Log.e("OfflineSyncManager", "Failed to send SYNC_REQ: ${e.message}")
        }
    }

    fun triggerManualSync() {
        connectedEndpointId?.let { sendSyncRequest(it) }
    }

    private suspend fun sendDatabase(endpointId: String) {
        try {
            val jsonString = repository.exportDatabaseToJson()
            val bytes = jsonString.toByteArray(StandardCharsets.UTF_8)
            val streamPayload = Payload.fromStream(ByteArrayInputStream(bytes))
            connectionsClient.sendPayload(endpointId, streamPayload)
        } catch (e: Exception) {
            _syncStatus.value = "Failed to export: ${e.message}"
        }
    }
}


