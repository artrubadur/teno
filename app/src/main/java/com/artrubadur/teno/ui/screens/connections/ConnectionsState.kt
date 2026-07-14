package com.artrubadur.teno.ui.screens.connections

import android.net.Uri
import com.artrubadur.teno.connection.ConnectionKind
import com.artrubadur.teno.connection.ConnectionType
import com.artrubadur.teno.connection.RemoteConnectionConfig

data class ConnectionsState(
    val isLoading: Boolean = false,
    val typeFilter: ConnectionType? = null,
    val dialogState: ConnectionDialogState = ConnectionDialogState()
)

data class ConnectionDialogState(
    val dialog: ConnectionDialogType? = null,
    val updatingId: String? = null,
    val kind: ConnectionKind? = null,
    val type: ConnectionType = ConnectionType.LLM,
    val name: String = "",
    val remoteConfig: RemoteConnectionConfig? = null,
    val modelUri: Uri? = null,
)

enum class ConnectionDialogType {
    SOURCE,
    LOCAL,
    REMOTE,
    DELETE,
}
