package dev.vaibhav.posturepal.receivers

import android.app.AlarmManager
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
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Acquire WakeLock to keep CPU running for the sound duration (5s)
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PosturePal::AlarmWakelock")

        // Safety timeout: release after 6 seconds if we forget
        wakeLock.acquire(6000L)

        try {
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
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val mp = MediaPlayer.create(context, notification)
            mp.start()

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