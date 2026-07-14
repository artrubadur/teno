package com.artrubadur.teno.ui.screens.connections.components.dialog

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.artrubadur.teno.R
import com.artrubadur.teno.ui.components.buttons.PlainLeadingIconButton
import com.artrubadur.teno.ui.components.buttons.PrimaryLeadingIconButton
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun LocalConnectionDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        DialogContent(
            initialName = initialName,
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun DialogContent(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var attemptedConfirm by remember { mutableStateOf(false) }
    val error = when {
        name.isBlank() -> "Name cannot be empty"
        else -> null
    }
    val confirmEnabled = !attemptedConfirm || error == null

    Surface(
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                label = { Text("Name") },
                isError = attemptedConfirm && error != null,
                singleLine = true
            )

            if (attemptedConfirm && error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrimaryLeadingIconButton(
                    iconRes = R.drawable.ic_confirm,
                    text = "Confirm",
                    onClick = {
                        if (error != null) {
                            attemptedConfirm = true
                            return@PrimaryLeadingIconButton
                        }
                        onConfirm(name.trim())
                    },
                    modifier = Modifier.weight(1f),
                    enabled = confirmEnabled,
                    shape = RoundedCornerShape(8.dp)
                )

                PlainLeadingIconButton(
                    iconRes = R.drawable.ic_close,
                    text = "Cancel",
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun DialogPreview() {
    AppTheme {
        DialogContent(
            initialName = "Local",
            onDismiss = {},
            onConfirm = {},
        )
    }
}
