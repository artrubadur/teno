package com.artrubadur.teno.ui.screens.connections.components

import androidx.compose.runtime.Composable
import com.artrubadur.teno.connection.RemoteConnectionConfig
import com.artrubadur.teno.ui.screens.connections.ConnectionDialogType
import com.artrubadur.teno.ui.screens.connections.ConnectionsState
import com.artrubadur.teno.ui.screens.connections.components.dialog.ConnectionDeleteDialog
import com.artrubadur.teno.ui.screens.connections.components.dialog.ConnectionSourceDialog
import com.artrubadur.teno.ui.screens.connections.components.dialog.LocalConnectionDialog
import com.artrubadur.teno.ui.screens.connections.components.dialog.RemoteConnectionDialog

@Composable
internal fun ConnectionDialog(
    state: ConnectionsState,
    onLocalSelect: () -> Unit,
    onRemoteSelect: () -> Unit,
    onDismiss: () -> Unit,
    onLocalConfirm: (String) -> Unit,
    onRemoteConfirm: (String, RemoteConnectionConfig) -> Unit,
    onDeleteConfirm: () -> Unit,
) {
    val dialogState = state.dialogState

    when (dialogState.dialog) {
        ConnectionDialogType.SOURCE -> ConnectionSourceDialog(
            onLocalSelect = onLocalSelect,
            onRemoteSelect = onRemoteSelect,
            onDismiss = onDismiss,
        )

        ConnectionDialogType.LOCAL -> LocalConnectionDialog(
            initialName = dialogState.name,
            onDismiss = onDismiss,
            onConfirm = onLocalConfirm,
        )

        ConnectionDialogType.REMOTE -> RemoteConnectionDialog(
            initialName = dialogState.name,
            initialConfig = dialogState.remoteConfig,
            onDismiss = onDismiss,
            onConfirm = onRemoteConfirm,
        )

        ConnectionDialogType.DELETE -> ConnectionDeleteDialog(
            connectionName = dialogState.name,
            onDismiss = onDismiss,
            onConfirm = onDeleteConfirm,
        )

        null -> Unit
    }
}
