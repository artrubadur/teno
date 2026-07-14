package com.artrubadur.teno.ui.screens.connections.details

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.connection.Connection
import com.artrubadur.teno.connection.ConnectionType
import com.artrubadur.teno.connection.RemoteConnection
import com.artrubadur.teno.connection.RemoteConnectionConfig
import com.artrubadur.teno.ui.components.buttons.PlainIconButton
import com.artrubadur.teno.ui.screens.connections.ConnectionsState
import com.artrubadur.teno.ui.screens.connections.components.ConnectionDialog
import com.artrubadur.teno.ui.screens.connections.details.components.ConnectionActionsCard
import com.artrubadur.teno.ui.screens.connections.details.components.ConnectionDetailsCard
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
internal fun ConnectionDetailsScreenContent(
    snackbarHostState: SnackbarHostState,
    state: ConnectionsState,
    connection: Connection?,
    onBack: () -> Unit,
    onToggleActive: (Connection) -> Unit,
    onEditConnection: (Connection) -> Unit,
    onDeleteConnection: (Connection) -> Unit,
    onDismissDialog: () -> Unit,
    onLocalConfirm: (String) -> Unit,
    onRemoteConfirm: (String, RemoteConnectionConfig) -> Unit,
    onDeleteConfirm: () -> Unit,
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
                    text = connection?.name ?: "Connection",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )

                PlainIconButton(
                    iconRes = R.drawable.ic_arrow,
                    contentDescription = "Back",
                    onClick = onBack,
                    tint = MaterialTheme.colorScheme.onBackground
                )
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

                connection == null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Connection not found",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        ConnectionDetailsCard(
                            connection = connection,
                            onToggleActive = onToggleActive
                        )

                        ConnectionActionsCard(
                            onEditClick = { onEditConnection(connection) },
                            onDeleteClick = { onDeleteConnection(connection) },
                        )
                    }
                }
            }
        }
    }

    ConnectionDialog(
        state = state,
        onLocalSelect = {},
        onRemoteSelect = {},
        onDismiss = onDismissDialog,
        onLocalConfirm = onLocalConfirm,
        onRemoteConfirm = onRemoteConfirm,
        onDeleteConfirm = onDeleteConfirm,
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
private fun ConnectionDetailsScreenContentPreview() {
    AppTheme {
        ConnectionDetailsScreenContent(
            snackbarHostState = SnackbarHostState(),
            state = ConnectionsState(),
            connection = RemoteConnection(
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
            onBack = {},
            onDismissDialog = {},
            onToggleActive = {},
            onLocalConfirm = {},
            onEditConnection = {},
            onDeleteConnection = {},
            onRemoteConfirm = { _, _ -> },
            onDeleteConfirm = {},
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
private fun ConnectionDetailsScreenContentActivePreview() {
    AppTheme {
        ConnectionDetailsScreenContent(
            snackbarHostState = SnackbarHostState(),
            state = ConnectionsState(),
            connection = RemoteConnection(
                id = "remote-preview",
                type = ConnectionType.LLM,
                name = "Remote API",
                active = true,
                addedAt = 3L,
                config = RemoteConnectionConfig(
                    baseUrl = "https://api.example.com",
                    model = "model",
                    apiKey = "preview",
                ),
            ),
            onBack = {},
            onDismissDialog = {},
            onToggleActive = {},
            onLocalConfirm = {},
            onEditConnection = {},
            onDeleteConnection = {},
            onRemoteConfirm = { _, _ -> },
            onDeleteConfirm = {},
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
private fun ConnectionDetailsScreenContentEmptyPreview() {
    AppTheme {
        ConnectionDetailsScreenContent(
            snackbarHostState = SnackbarHostState(),
            state = ConnectionsState(),
            onBack = {},
            onDismissDialog = {},
            onToggleActive = {},
            onLocalConfirm = {},
            connection = null,
            onEditConnection = {},
            onDeleteConnection = {},
            onRemoteConfirm = { _, _ -> },
            onDeleteConfirm = {},
        )
    }
}
