package com.churchbooklibrary.core.sync

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object SyncSchedulerManager {

    fun scheduleTabletAlarms(context: Context, dayOfWeek: Int, startHour: Int, endHour: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val startIntent = Intent(context, SyncAlarmReceiver::class.java).apply { action = "ACTION_START_TABLET_BROADCAST" }
        val startPending = PendingIntent.getBroadcast(context, 101, startIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        
        val stopIntent = Intent(context, SyncAlarmReceiver::class.java).apply { action = "ACTION_STOP_TABLET_BROADCAST" }
        val stopPending = PendingIntent.getBroadcast(context, 102, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val now = Calendar.getInstance()

        val startCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (startCal.before(now)) startCal.add(Calendar.WEEK_OF_YEAR, 1)

        val stopCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, endHour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        if (stopCal.before(now)) stopCal.add(Calendar.WEEK_OF_YEAR, 1)

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startCal.timeInMillis, AlarmManager.INTERVAL_DAY * 7, startPending)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, stopCal.timeInMillis, AlarmManager.INTERVAL_DAY * 7, stopPending)
    }

    fun schedulePhoneAlarms(context: Context, dayOfWeek: Int, startHour: Int, endHour: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val midHour = startHour + (endHour - startHour) / 2
        val times = listOf(startHour, midHour, endHour).distinct()
        
        times.forEachIndexed { index, hour ->
            val intent = Intent(context, SyncAlarmReceiver::class.java).apply { action = "ACTION_PHONE_PROMPT" }
            val pending = PendingIntent.getBroadcast(context, 201 + index, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            
            val now = Calendar.getInstance()
            val cal = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            if (cal.before(now)) cal.add(Calendar.WEEK_OF_YEAR, 1)

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.timeInMillis, AlarmManager.INTERVAL_DAY * 7, pending)
        }
    }
    
    fun cancelAllAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intents = listOf(
            Intent(context, SyncAlarmReceiver::class.java).apply { action = "ACTION_START_TABLET_BROADCAST" },
            Intent(context, SyncAlarmReceiver::class.java).apply { action = "ACTION_STOP_TABLET_BROADCAST" },
            Intent(context, SyncAlarmReceiver::class.java).apply { action = "ACTION_PHONE_PROMPT" }
        )
        // 101, 102, 201, 202, 203
        val ids = listOf(101, 102, 201, 202, 203)
        ids.forEach { id ->
            val intent = Intent(context, SyncAlarmReceiver::class.java) // action doesn't matter for cancellation if component matches
            val pending = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            alarmManager.cancel(pending)
        }
    }
}

