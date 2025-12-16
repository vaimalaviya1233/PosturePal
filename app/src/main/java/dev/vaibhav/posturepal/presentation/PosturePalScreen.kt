package dev.vaibhav.posturepal.presentation

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    var intervalText by remember { mutableStateOf("30") }
    var hasExactAlarmPermission by remember {
        mutableStateOf(
            AlarmUtils.checkExactAlarmPermission(
                context
            )
        )
    }

    // Launcher for permission settings
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            hasExactAlarmPermission = AlarmUtils.checkExactAlarmPermission(context)
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Posture Pal", fontSize = 32.sp, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        // 1. Permission Component
        if (!hasExactAlarmPermission) {
            PermissionRequestCard(
                onGrantClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                        launcher.launch(intent)
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 2. Input Component
        IntervalInputField(
            value = intervalText,
            onValueChange = { intervalText = it },
            enabled = !isEnabled && hasExactAlarmPermission
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Toggle Component
        AlarmStatusToggle(
            isActive = isEnabled,
            isEnabled = hasExactAlarmPermission,
            onToggle = { active ->
                isEnabled = active
                val mins = intervalText.toIntOrNull() ?: 30
                if (active) {
                    AlarmUtils.startAlarm(context, mins)
                } else {
                    AlarmUtils.cancelAlarm(context)
                }
            }
        )

        if (isEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Looping every $intervalText mins.")
        }
    }
}

// 2. Stateless Composable (Pure UI - Best for Previews)
@Composable
fun PosturePalContent(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    intervalText: String,
    hasPermission: Boolean,
    onIntervalChange: (String) -> Unit,
    onToggle: (Boolean) -> Unit,
    onGrantPermission: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Posture Pal", fontSize = 32.sp, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(32.dp))

        if (!hasPermission) {
            PermissionRequestCard(onGrantClick = onGrantPermission)
            Spacer(modifier = Modifier.height(24.dp))
        }

        IntervalInputField(
            value = intervalText,
            onValueChange = onIntervalChange,
            enabled = !isEnabled && hasPermission
        )

        Spacer(modifier = Modifier.height(32.dp))

        AlarmStatusToggle(
            isActive = isEnabled,
            isEnabled = hasPermission,
            onToggle = onToggle
        )

        if (isEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Looping every $intervalText mins.")
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, name = "Active State")
@Composable
fun ScreenActivePreview() {
    MaterialTheme {
        PosturePalContent(
            isEnabled = true,
            intervalText = "45",
            hasPermission = true,
            onIntervalChange = {},
            onToggle = {},
            onGrantPermission = {}
        )
    }
}

@Preview(showBackground = true, name = "Missing Permission")
@Composable
fun ScreenPermissionPreview() {
    MaterialTheme {
        PosturePalContent(
            isEnabled = false,
            intervalText = "30",
            hasPermission = false, // Force permission card to show
            onIntervalChange = {},
            onToggle = {},
            onGrantPermission = {}
        )
    }
}