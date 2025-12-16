package dev.vaibhav.posturepal.receivers

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    // Inside onReceive, before playing sound:
    private fun showNotification(context: Context) {
        val channelId = "posture_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Create Channel (Required for Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Posture Reminders",
                NotificationManager.IMPORTANCE_HIGH // High importance makes it pop up
            ).apply {
                description = "Reminds you to stand up"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Build Notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Use your own icon here
            .setContentTitle("Time to Stretch!")
            .setContentText("Take a break from the screen.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // 3. Show it
        notificationManager.notify(1, notification)
    }

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Acquire WakeLock to keep CPU running for the sound duration (5s)
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PosturePal::AlarmWakelock")

        // Safety timeout: release after 6 seconds if we forget
        wakeLock.acquire(6000L)

        try {
            // show notification
            showNotification(context)
            // 2. Play Sound
            playAlarmSound(context)

            // 3. Reschedule Next Alarm
            val intervalMinutes = intent.getIntExtra("INTERVAL", 30)
            scheduleNextAlarm(context, intervalMinutes)
        } finally {
            // WakeLock is usually released by timeout here since sound is async,
            // but relying on the 6s timeout is safe for this simple use case.
        }

    }

    private fun playAlarmSound(context: Context) {
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
//            val mp = MediaPlayer.create(context, notification)
//            mp.start()

            val mp = MediaPlayer().apply {
                setDataSource(context, notification)
                setAudioAttributes(
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM) // Critical for DND handling
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                prepare()
                start()
            }
            // Stop after 5 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    if (mp.isPlaying) {
                        mp.stop()
                        mp.release()
                        // Optional: Debug toast
                        // Toast.makeText(context, "Stretch time over!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 5000)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleNextAlarm(context: Context, intervalMinutes: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("INTERVAL", intervalMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = Calendar.getInstance().apply {
            add(Calendar.MINUTE, intervalMinutes)
        }

        // Logic for Android 12+ (S) vs older
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, pendingIntent)
            } else {
                // Permission revoked? Fallback or do nothing.
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime.timeInMillis, pendingIntent)
        }
    }
}