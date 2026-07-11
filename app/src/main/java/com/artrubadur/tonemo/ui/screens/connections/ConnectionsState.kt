package com.artrubadur.tonemo.ui.screens.connections

import android.net.Uri
import com.artrubadur.tonemo.connection.ConnectionKind
import com.artrubadur.tonemo.connection.ConnectionType
import com.artrubadur.tonemo.connection.RemoteConnectionConfig

data class ConnectionsState(
    val isLoading: Boolean = false,
    val typeFilter: ConnectionType? = null,
    val cardAction: CardAction? = null,
    val dialogState: ConnectionDialogState = ConnectionDialogState()
)

enum class CardAction {
    DELETE,
    UPDATE,
}

data class ConnectionDialogState(
    val dialogStage: DialogStage? = null,
    val updatingId: String? = null,
    val kind: ConnectionKind? = null,
    val type: ConnectionType = ConnectionType.LLM,
    val name: String = "",
    val remoteConfig: RemoteConnectionConfig? = null,
    val modelUri: Uri? = null,
)

enum class DialogStage {
    SOURCE,
    REMOTE,
    DETAILS
}