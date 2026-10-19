package com.churchbooklibrary.core.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.churchbooklibrary.LibraryApplication
import com.churchbooklibrary.MainActivity
import com.churchbooklibrary.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Calendar

class SyncAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        Log.d("SyncAlarmReceiver", "Received action: $action")
        
        when (action) {
            "ACTION_START_TABLET_BROADCAST" -> {
                // Double check if we already synced today
                val app = context.applicationContext as LibraryApplication
                runBlocking {
                    val lastSync = app.settingsDataStore.lastSuccessfulSyncDate.first()
                    val today = getTodayDateString()
                    if (lastSync != today) {
                        val serviceIntent = Intent(context, TabletSyncService::class.java)
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                context.startForegroundService(serviceIntent)
                            } else {
                                context.startService(serviceIntent)
                            }
                        } catch (e: Exception) {
                            Log.w("SyncAlarmReceiver", "Background start of TabletSyncService prevented by OS: ${e.message}")
                        }
                    }
                }
            }
            "ACTION_STOP_TABLET_BROADCAST" -> {
                context.stopService(Intent(context, TabletSyncService::class.java))
            }
            "ACTION_PHONE_PROMPT" -> {
                val app = context.applicationContext as LibraryApplication
                runBlocking {
                    val lastSync = app.settingsDataStore.lastSuccessfulSyncDate.first()
                    val today = getTodayDateString()
                    if (lastSync != today) {
                        showPhoneNotification(context)
                    }
                }
            }
        }
    }

    private fun showPhoneNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("sync_prompts", "Sync Reminders", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // We could add an extra to deep-link directly to the sync UI
            putExtra("open_sync", true) 
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, "sync_prompts")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("Library Backup Scheduled")
            .setContentText("Tap to connect to the Kiosk and sync the database.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        manager.notify(2002, notification)
    }
    
    private fun getTodayDateString(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule alarms based on role
            val app = context.applicationContext as LibraryApplication
            runBlocking {
                val role = app.settingsDataStore.syncRole.first()
                val day = app.settingsDataStore.syncDayOfWeek.first()
                val start = app.settingsDataStore.syncStartHour.first()
                val end = app.settingsDataStore.syncEndHour.first()
                if (role == 1) {
                    SyncSchedulerManager.scheduleTabletAlarms(context, day, start, end)
                } else if (role == 2) {
                    SyncSchedulerManager.schedulePhoneAlarms(context, day, start, end)
                }
            }
        }
    }
}

