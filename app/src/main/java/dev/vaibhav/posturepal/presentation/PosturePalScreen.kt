package dev.vaibhav.posturepal.presentation

import android.content.Intent
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.vaibhav.posturepal.utils.AlarmUtils

@Composable
fun PosturePalScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val context = LocalContext.current

    // State
    var isEnabled by remember { mutableStateOf(false) }

    // Split interval into Hours and Minutes
    var hoursText by remember { mutableStateOf("00") }
    var minutesText by remember { mutableStateOf("30") }

    var hasExactAlarmPermission by remember {
        mutableStateOf(
            AlarmUtils.checkExactAlarmPermission(
                context
            )
        )
    }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            hasExactAlarmPermission = AlarmUtils.checkExactAlarmPermission(context)
        }

    val onToggleAlarm = { active: Boolean ->
        val h = hoursText.toIntOrNull() ?: 0
        val m = minutesText.toIntOrNull() ?: 30
        val totalMinutes = (h * 60) + m

        if (totalMinutes <= 0) {
            Toast.makeText(context, "Please set a valid time > 0 mins", Toast.LENGTH_SHORT).show()
            // Don't enable the switch
        } else {
            isEnabled = active
            if (active) {
                AlarmUtils.startAlarm(context, totalMinutes)
            } else {
                AlarmUtils.cancelAlarm(context)
            }
        }
    }

    val onGrantPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            launcher.launch(intent)
        }
    }

    PosturePalContent(
        modifier = modifier,
        isEnabled = isEnabled,
        hoursText = hoursText,
        minutesText = minutesText,
        hasPermission = hasExactAlarmPermission,
        isDarkTheme = isDarkTheme,
        onThemeToggle = onThemeToggle,
        onHoursChange = { hoursText = it },
        onMinutesChange = { minutesText = it },
        onToggleAlarm = onToggleAlarm,
        onGrantPermission = onGrantPermission
    )
}

@Composable
fun PosturePalContent(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    hoursText: String,
    minutesText: String,
    hasPermission: Boolean,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit,
    onToggleAlarm: (Boolean) -> Unit,
    onGrantPermission: () -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {

        // Theme Toggle Icon
        IconButton(
            onClick = onThemeToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = "Toggle Theme"
            )
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Posture Pal", fontSize = 32.sp, style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Set your break interval",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (!hasPermission) {
                PermissionRequestCard(onGrantClick = onGrantPermission)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // --- New Custom Interval Picker ---
            IntervalTimePicker(
                hours = hoursText,
                minutes = minutesText,
                onHoursChange = onHoursChange,
                onMinutesChange = onMinutesChange,
                enabled = !isEnabled && hasPermission
            )
            // ----------------------------------

            Spacer(modifier = Modifier.height(48.dp))

            AlarmStatusToggle(
                isActive = isEnabled,
                isEnabled = hasPermission,
                onToggle = onToggleAlarm
            )

            if (isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                // Calculate display string for feedback
                val h = hoursText.toIntOrNull() ?: 0
                val m = minutesText.toIntOrNull() ?: 0
                val totalStr = if (h > 0) "$h hr $m min" else "$m min"

                Text("Alarm set for every $totalStr", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}