package dev.vaibhav.posturepal.presentation

import android.content.Intent
import android.os.Build
import android.provider.Settings
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

// 1. Stateful Composable: Now accepts theme params
@Composable
fun PosturePalScreen(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val context = LocalContext.current

    // State
    var isEnabled by remember { mutableStateOf(false) }
    var intervalText by remember { mutableStateOf("30") }
    var hasExactAlarmPermission by remember { mutableStateOf(AlarmUtils.checkExactAlarmPermission(context)) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        hasExactAlarmPermission = AlarmUtils.checkExactAlarmPermission(context)
    }

    val onToggleAlarm = { active: Boolean ->
        isEnabled = active
        val mins = intervalText.toIntOrNull() ?: 30
        if (active) {
            AlarmUtils.startAlarm(context, mins)
        } else {
            AlarmUtils.cancelAlarm(context)
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
        intervalText = intervalText,
        hasPermission = hasExactAlarmPermission,
        isDarkTheme = isDarkTheme,       // Pass down
        onThemeToggle = onThemeToggle,   // Pass down
        onIntervalChange = { intervalText = it },
        onToggleAlarm = onToggleAlarm,
        onGrantPermission = onGrantPermission
    )
}

// 2. Stateless Composable: UI Layout
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
    // We use a Box so we can overlay the Icon at the TopRight
    Box(modifier = modifier.fillMaxSize()) {

        // --- 1. The Theme Toggle Icon (Top Right) ---
        IconButton(
            onClick = onThemeToggle,
            modifier = Modifier
                .align(Alignment.TopEnd) // Positions it at Top Right
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                contentDescription = "Toggle Theme"
            )
        }

        // --- 2. The Main Content (Centered) ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Add padding so it doesn't overlap the icon
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Posture Pal", fontSize = 32.sp, style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(32.dp))

            if (!hasPermission) {
                PermissionRequestCard(onGrantClick = onGrantPermission)
                Spacer(modifier = Modifier.height(24.dp))
            }

            IntervalTimePicker(
                hours = hoursText,
                minutes = minutesText,
                onHoursChange = onHoursChange,
                onMinutesChange = onMinutesChange,
                enabled = !isEnabled && hasPermission
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                val totalStr = if(h > 0) "$h hr $m min" else "$m min"

                Text("Alarm set for every $totalStr", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun ScreenLightPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        PosturePalContent(
            isEnabled = true,
            intervalText = "45",
            hasPermission = true,
            isDarkTheme = false,
            onThemeToggle = {},
            onIntervalChange = {},
            onToggleAlarm = {},
            onGrantPermission = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, name = "Dark Mode")
@Composable
fun ScreenDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        PosturePalContent(
            isEnabled = false,
            intervalText = "30",
            hasPermission = true,
            isDarkTheme = true,
            onThemeToggle = {},
            onIntervalChange = {},
            onToggleAlarm = {},
            onGrantPermission = {}
        )
    }
}