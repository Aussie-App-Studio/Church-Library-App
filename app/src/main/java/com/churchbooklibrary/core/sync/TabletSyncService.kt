package com.churchbooklibrary.core.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.churchbooklibrary.LibraryApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class TabletSyncService : Service() {
    private val CHANNEL_ID = "sync_channel"
    private val NOTIFICATION_ID = 1001

    private lateinit var syncManager: OfflineSyncManager

    override fun onCreate() {
        super.onCreate()
        val app = application as LibraryApplication
        syncManager = app.syncManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Library Sync Active")
            .setContentText("Broadcasting to nearby librarian phones...")
            .setSmallIcon(android.R.drawable.stat_sys_upload) // default icon
            .setOngoing(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            syncManager.startAdvertising()
        } catch (e: Exception) {
            android.util.Log.e("TabletSyncService", "Failed to start foreground service: ${e.message}")
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        syncManager.stopAdvertising()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Offline Sync Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

