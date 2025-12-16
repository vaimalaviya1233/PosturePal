package dev.vaibhav.posturepal.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A simple card to warn users about missing permissions.
 */
@Composable
fun PermissionRequestCard(
    onGrantClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Permission Required", style = MaterialTheme.typography.titleMedium)
            Text("Android 12+ requires permission to schedule precise posture reminders.")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onGrantClick) {
                Text("Grant Permission")
            }
        }
    }
}

/**
 * Reusable input field for numeric intervals.
 */
@Composable
fun IntervalInputField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            if (input.all { it.isDigit() }) onValueChange(input)
        },
        label = { Text("Interval (Minutes)") },
        enabled = enabled,
        singleLine = true,
        modifier = modifier
    )
}

/**
 * The main toggle row with status text.
 */
@Composable
fun AlarmStatusToggle(
    isActive: Boolean,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = if (isActive) "Active" else "Inactive",
            fontSize = 20.sp
        )
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = isActive,
            enabled = isEnabled,
            onCheckedChange = onToggle
        )
    }
}

// --- Previews ---

@Preview(showBackground = true)
@Composable
fun PermissionCardPreview() {
    MaterialTheme {
        PermissionRequestCard(onGrantClick = {}, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun IntervalInputPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            IntervalInputField(value = "30", onValueChange = {}, enabled = true)
            Spacer(modifier = Modifier.height(8.dp))
            IntervalInputField(value = "30", onValueChange = {}, enabled = false)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TogglePreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AlarmStatusToggle(isActive = true, isEnabled = true, onToggle = {})
            AlarmStatusToggle(isActive = false, isEnabled = true, onToggle = {})
            AlarmStatusToggle(isActive = false, isEnabled = false, onToggle = {})
        }
    }
}