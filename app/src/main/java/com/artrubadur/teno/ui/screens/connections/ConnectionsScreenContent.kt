package com.artrubadur.teno.ui.screens.connections

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.connection.Connection
import com.artrubadur.teno.connection.ConnectionType
import com.artrubadur.teno.connection.LocalConnection
import com.artrubadur.teno.connection.LocalConnectionConfig
import com.artrubadur.teno.connection.ModelType
import com.artrubadur.teno.connection.RemoteConnection
import com.artrubadur.teno.connection.RemoteConnectionConfig
import com.artrubadur.teno.ui.components.buttons.PlainIconButton
import com.artrubadur.teno.ui.components.buttons.PrimaryIconButton
import com.artrubadur.teno.ui.screens.connections.components.ConnectionDialog
import com.artrubadur.teno.ui.screens.connections.components.ConnectionList
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
internal fun ConnectionsScreenContent(
    snackbarHostState: SnackbarHostState,
    state: ConnectionsState,
    connections: List<Connection>,
    onBack: () -> Unit,
    onOpenConnection: (String) -> Unit,
    onToggleActive: (Connection) -> Unit,
    onAddConnection: () -> Unit,
    onDismissDialog: () -> Unit,
    onLocalSelect: () -> Unit,
    onRemoteSelect: () -> Unit,
    onLocalConfirm: (String) -> Unit,
    onRemoteConfirm: (String, RemoteConnectionConfig) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Connections",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f)
                )

                Row {
                    PrimaryIconButton(
                        iconRes = R.drawable.ic_add,
                        contentDescription = "Add connection",
                        onClick = onAddConnection,
                        enabled = !state.isLoading,
                    )

                    PlainIconButton(
                        iconRes = R.drawable.ic_arrow,
                        contentDescription = "Back",
                        onClick = onBack,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                connections.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No connections",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Add a local or remote model",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }

                else -> {

                    ConnectionList(
                        modifier = Modifier.weight(1f),
                        connections = connections,
                        onOpenConnection = onOpenConnection,
                        onToggleActive = onToggleActive
                    )
                }
            }
        }
    }

    ConnectionDialog(
        state = state,
        onLocalSelect = onLocalSelect,
        onRemoteSelect = onRemoteSelect,
        onDismiss = onDismissDialog,
        onLocalConfirm = onLocalConfirm,
        onRemoteConfirm = onRemoteConfirm,
        onDeleteConfirm = {},
    )
}

@Preview(
    name = "Light",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ConnectionsScreenContentPreview() {
    AppTheme {
        ConnectionsScreenContent(
            snackbarHostState = SnackbarHostState(),
            state = ConnectionsState(),
            connections = listOf(
                LocalConnection(
                    id = "local-preview-1",
                    type = ConnectionType.LLM,
                    name = "Local model",
                    active = true,
                    addedAt = 1L,
                    config = LocalConnectionConfig(
                        modelType = ModelType.LITERTLM,
                        fileName = "model.litertlm",
                    ),
                ),
                LocalConnection(
                    id = "local-preview-2",
                    type = ConnectionType.LLM,
                    name = "Local model",
                    active = false,
                    addedAt = 2L,
                    config = LocalConnectionConfig(
                        modelType = ModelType.LITERTLM,
                        fileName = "model.litertlm",
                    ),
                ),
                RemoteConnection(
                    id = "remote-preview",
                    type = ConnectionType.LLM,
                    name = "Remote API",
                    active = false,
                    addedAt = 3L,
                    config = RemoteConnectionConfig(
                        baseUrl = "https://api.example.com",
                        model = "model",
                        apiKey = "preview",
                    ),
                ),
            ),
            onBack = {},
            onOpenConnection = {},
            onLocalSelect = {},
            onAddConnection = {},
            onRemoteSelect = {},
            onDismissDialog = {},
            onToggleActive = {},
            onLocalConfirm = {},
            onRemoteConfirm = { _, _ -> },
        )
    }
}

@Preview(
    name = "Light",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ConnectionsScreenContentEmptyPreview() {
    AppTheme {
        ConnectionsScreenContent(
            snackbarHostState = SnackbarHostState(),
            state = ConnectionsState(),
            connections = emptyList(),
            onBack = {},
            onOpenConnection = {},
            onLocalSelect = {},
            onAddConnection = {},
            onRemoteSelect = {},
            onDismissDialog = {},
            onToggleActive = {},
            onLocalConfirm = {},
            onRemoteConfirm = { _, _ -> },
        )
    }
}
