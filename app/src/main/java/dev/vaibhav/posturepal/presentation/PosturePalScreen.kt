package dev.vaibhav.posturepal.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import dev.vaibhav.posturepal.utils.AlarmUtils

@Composable
fun PosturePalScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val context = LocalContext.current

    // --- State ---
    var isEnabled by remember { mutableStateOf(false) }
    var hoursText by remember { mutableStateOf("00") }
    var minutesText by remember { mutableStateOf("30") }

    // Track Exact Alarm Permission (Android 12+)
    var hasExactAlarmPermission by remember { mutableStateOf(AlarmUtils.checkExactAlarmPermission(context)) }

    // Track Notification Permission (Android 13+)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    // --- Launchers ---

    // Launcher for Exact Alarm Settings
    val alarmPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        hasExactAlarmPermission = AlarmUtils.checkExactAlarmPermission(context)
    }

    // Launcher for Notification Permission Dialog
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasNotificationPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Notifications needed for visual reminders", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Logic ---

    val onToggleAlarm = { active: Boolean ->
        // 1. Check Notification Permission first (Android 13+)
        if (active && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            // We don't enable the switch yet; user must click again after granting
        }
        // 2. If we have permissions (or don't need them), proceed
        else {
            val h = hoursText.toIntOrNull() ?: 0
            val m = minutesText.toIntOrNull() ?: 30
            val totalMinutes = (h * 60) + m

            if (totalMinutes <= 0) {
                Toast.makeText(context, "Please set a valid time > 0 mins", Toast.LENGTH_SHORT).show()
            } else {
                isEnabled = active
                if (active) {
                    AlarmUtils.startAlarm(context, totalMinutes)
                } else {
                    AlarmUtils.cancelAlarm(context)
                }
            }
        }
    }

    val onGrantExactPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            alarmPermissionLauncher.launch(intent)
        }
    }

    // --- UI ---
    PosturePalContent(
        modifier = modifier,
        isEnabled = isEnabled,
        hoursText = hoursText,
        minutesText = minutesText,
        hasExactPermission = hasExactAlarmPermission, // Only block UI for Exact Alarm
        isDarkTheme = isDarkTheme,
        onThemeToggle = onThemeToggle,
        onHoursChange = { hoursText = it },
        onMinutesChange = { minutesText = it },
        onToggleAlarm = onToggleAlarm,
        onGrantExactPermission = onGrantExactPermission
    )
}

@Composable
fun PosturePalContent(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    hoursText: String,
    minutesText: String,
    hasExactPermission: Boolean,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit,
    onToggleAlarm: (Boolean) -> Unit,
    onGrantExactPermission: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {

        IconButton(
            onClick = onThemeToggle,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = "Toggle Theme"
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Posture Pal", fontSize = 32.sp, style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(8.dp))
            Text("Set your break interval", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Spacer(modifier = Modifier.height(32.dp))

            // Only show the card for the "Hard" permission (Exact Alarm)
            // Notification permission is handled dynamically when they click the toggle
            if (!hasExactPermission) {
                PermissionRequestCard(onGrantClick = onGrantExactPermission)
                Spacer(modifier = Modifier.height(24.dp))
            }

            IntervalTimePicker(
                hours = hoursText,
                minutes = minutesText,
                onHoursChange = onHoursChange,
                onMinutesChange = onMinutesChange,
                enabled = !isEnabled && hasExactPermission
            )

            Spacer(modifier = Modifier.height(48.dp))

            AlarmStatusToggle(
                isActive = isEnabled,
                isEnabled = hasExactPermission,
                onToggle = onToggleAlarm
            )

            if (isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                val h = hoursText.toIntOrNull() ?: 0
                val m = minutesText.toIntOrNull() ?: 0
                val totalStr = if(h > 0) "$h hr $m min" else "$m min"
                Text("Alarm set for every $totalStr", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}