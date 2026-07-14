package com.artrubadur.teno.ui.screens.connections.components.dialog

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.artrubadur.teno.R
import com.artrubadur.teno.connection.RemoteConnectionConfig
import com.artrubadur.teno.ui.components.buttons.PlainLeadingIconButton
import com.artrubadur.teno.ui.components.buttons.PrimaryLeadingIconButton
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun RemoteConnectionDialog(
    initialName: String,
    initialConfig: RemoteConnectionConfig?,
    onDismiss: () -> Unit,
    onConfirm: (String, RemoteConnectionConfig) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        DialogContent(
            initialName = initialName,
            initialConfig = initialConfig ?: RemoteConnectionConfig("", "", ""),
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
    }
}

@Composable
private fun DialogContent(
    initialName: String,
    initialConfig: RemoteConnectionConfig,
    onDismiss: () -> Unit,
    onConfirm: (String, RemoteConnectionConfig) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var apiKeyVisible by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(initialName) }
    var baseUrl by remember { mutableStateOf(initialConfig.baseUrl) }
    var model by remember { mutableStateOf(initialConfig.model) }
    var apiKey by remember { mutableStateOf(initialConfig.apiKey) }
    var attemptedConfirm by remember { mutableStateOf(false) }
    val error = when {
        name.isBlank() -> "Name cannot be empty"
        baseUrl.isBlank() -> "Base URL cannot be empty"
        model.isBlank() -> "Model cannot be empty"
        apiKey.isBlank() -> "API Key cannot be empty"
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                label = { Text("Name") },
                isError = attemptedConfirm && name.isBlank(),
                singleLine = true
            )

            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                label = { Text("Base URL") },
                isError = attemptedConfirm && baseUrl.isBlank(),
                singleLine = true
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                label = { Text("Model") },
                isError = attemptedConfirm && model.isBlank(),
                singleLine = true
            )

            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                label = { Text("API Key") },
                isError = attemptedConfirm && apiKey.isBlank(),
                singleLine = true,
                interactionSource = interactionSource,
                visualTransformation = if (apiKeyVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                        Icon(
                            painter = if (apiKeyVisible) {
                                painterResource(R.drawable.ic_hide)
                            } else {
                                painterResource(R.drawable.ic_show)
                            },
                            contentDescription = null,
                            tint = if (attemptedConfirm && apiKey.isBlank()) {
                                MaterialTheme.colorScheme.error
                            } else if (isFocused) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

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
            ) {
                PrimaryLeadingIconButton(
                    iconRes = R.drawable.ic_confirm,
                    text = "Confirm",
                    onClick = {
                        if (error != null) {
                            attemptedConfirm = true
                            return@PrimaryLeadingIconButton
                        }
                        onConfirm(
                            name.trim(),
                            RemoteConnectionConfig(
                                baseUrl.trim(),
                                model.trim(),
                                apiKey.trim()
                            )
                        )
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
            initialName = "Remote",
            initialConfig = RemoteConnectionConfig("", "", ""),
            onDismiss = {},
            onConfirm = { _, _ -> },
        )
    }
}
