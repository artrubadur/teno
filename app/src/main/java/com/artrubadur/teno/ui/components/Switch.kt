package com.artrubadur.teno.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun Switch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            checkedBorderColor = Color.Transparent,

            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            uncheckedBorderColor = MaterialTheme.colorScheme.outline,

            disabledCheckedThumbColor =
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledCheckedTrackColor =
                MaterialTheme.colorScheme.primary.copy(alpha = 0.32f),

            disabledUncheckedThumbColor =
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            disabledUncheckedTrackColor =
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
            disabledUncheckedBorderColor =
                MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
        ),
    )
}