package com.artrubadur.teno.ui.screens.connections.dialog

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.artrubadur.teno.ui.theme.TenoTheme

@Composable
fun RemoteConnectionDialog(
    initialConfig: RemoteConnectionConfig?,
    onDismiss: () -> Unit,
    onConfirm: (RemoteConnectionConfig) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        DialogContent(
            initialConfig = initialConfig ?: RemoteConnectionConfig("", "", ""),
            onDismiss = onDismiss,
            onConfirm = onConfirm
        )
    }
}

@Composable
private fun DialogContent(
    initialConfig: RemoteConnectionConfig,
    onDismiss: () -> Unit,
    onConfirm: (RemoteConnectionConfig) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    var apiKeyVisible by remember { mutableStateOf(false) }

    var baseUrl by remember { mutableStateOf(initialConfig.baseUrl) }
    var model by remember { mutableStateOf(initialConfig.model) }
    var apiKey by remember { mutableStateOf(initialConfig.apiKey) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                label = { Text("Base URL") },
                singleLine = true
            )

            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                label = { Text("Model") },
                singleLine = true
            )


            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                label = { Text("API Key") },
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
                            painter = if (apiKeyVisible) painterResource(R.drawable.ic_hide) else painterResource(
                                R.drawable.ic_show
                            ),
                            contentDescription = null,
                            tint = if (isFocused) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            )


            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PrimaryLeadingIconButton(
                    iconRes = R.drawable.ic_confirm,
                    text = "Confirm",
                    onClick = {
                        onConfirm(
                            RemoteConnectionConfig(
                                baseUrl.trim(),
                                model.trim(),
                                apiKey.trim()
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
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

@Preview
@Composable
private fun DialogPreview() {
    TenoTheme {
        DialogContent(
            initialConfig = RemoteConnectionConfig("", "", ""),
            onDismiss = {},
            onConfirm = {}
        )
    }
}