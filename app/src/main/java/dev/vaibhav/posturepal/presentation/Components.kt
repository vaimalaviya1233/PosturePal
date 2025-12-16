package dev.vaibhav.posturepal.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

/**
 * A custom Time Picker style input for Intervals (Hours : Minutes).
 * Mimics the visual style of Android's TimePicker but for duration.
 */
@Composable
fun IntervalTimePicker(
    hours: String,
    minutes: String,
    onHoursChange: (String) -> Unit,
    onMinutesChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TimeInputBlock(
            value = hours,
            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) onHoursChange(it) },
            label = "HR",
            enabled = enabled
        )

        Text(
            text = ":",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        TimeInputBlock(
            value = minutes,
            onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) onMinutesChange(it) },
            label = "MIN",
            enabled = enabled
        )
    }
}

@Composable
private fun TimeInputBlock(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean
) {
    var isFocused by remember { mutableStateOf(false) }

    val backgroundColor = when {
        !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        isFocused -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp, 80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(backgroundColor)
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = value, // FIX: Removed .padStart(2, '0')
                onValueChange = onValueChange,
                enabled = enabled,
                textStyle = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                cursorBrush = SolidColor(textColor),
                singleLine = true,
                modifier = Modifier
                    .onFocusChanged { isFocused = it.isFocused }
                    .fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.Center) {
                        // Show "00" hint if empty
                        if (value.isEmpty()) {
                            Text(
                                text = "00",
                                style = TextStyle(
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor.copy(alpha = 0.3f),
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
// --- Updated Previews ---

@Preview(showBackground = true)
@Composable
fun IntervalPickerPreview() {
    MaterialTheme {
        IntervalTimePicker(
            hours = "01",
            minutes = "30",
            onHoursChange = {},
            onMinutesChange = {},
            enabled = true,
            modifier = Modifier.padding(24.dp)
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