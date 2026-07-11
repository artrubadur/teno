package com.artrubadur.tonemo.ui.screens.connections

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.ConnectionKind
import com.artrubadur.tonemo.connection.ConnectionManager
import com.artrubadur.tonemo.connection.ConnectionType
import com.artrubadur.tonemo.connection.LocalConnection
import com.artrubadur.tonemo.connection.RemoteConnection
import com.artrubadur.tonemo.connection.RemoteConnectionConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ConnectionsViewModel(
    private val connectionManager: ConnectionManager
) : ViewModel() {

    private val _state = MutableStateFlow(ConnectionsState())
    val state: StateFlow<ConnectionsState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val connections: StateFlow<List<Connection>> =
        state
            .map { it.typeFilter }
            .distinctUntilChanged()
            .flatMapLatest { typeFilter ->
                connectionManager.observeConnections(typeFilter)
            }
            .map { connections ->
                connections.sortedWith(
                    compareByDescending<Connection> { it.active }
                        .thenByDescending { it.addedAt }
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

//    fun setTypeFilter(typeFilter: ConnectionType?) {
//        _state.update {
//            it.copy(typeFilter = typeFilter)
//        }
//    }

    fun onToggleActive(connection: Connection) {
        viewModelScope.launch {
            try {
                connectionManager.setActiveConnection(connection.id)
            } catch (e: Exception) {
                _events.tryEmit(e.message ?: "Failed to set active connection")
            }
        }
    }

    fun setCardAction(cardAction: CardAction?) {
        _state.update {
            it.copy(cardAction = cardAction)
        }
    }

    fun onCardClick(connection: Connection) {
        when (_state.value.cardAction) {
            CardAction.DELETE -> onDeleteActionClick(connection)
            CardAction.UPDATE -> onUpdateActionClick(connection)
            else -> Unit
        }
    }

    private fun onDeleteActionClick(connection: Connection) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    cardAction = null,
                    isLoading = true
                )
            }
            try {
                connectionManager.deleteConnection(connection.id)
                _state.update {
                    it.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _events.tryEmit(e.message ?: "Failed to delete connection")
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    private fun onUpdateActionClick(connection: Connection) {
        _state.update { state ->
            state.copy(
                dialogState = when (connection) {
                    is LocalConnection -> state.dialogState.copy(
                        updatingId = connection.id,
                        dialogStage = DialogStage.DETAILS,
                        kind = ConnectionKind.LOCAL,
                        name = connection.name,
                        type = connection.type,
                    )

                    is RemoteConnection -> state.dialogState.copy(
                        updatingId = connection.id,
                        dialogStage = DialogStage.REMOTE,
                        kind = ConnectionKind.REMOTE,
                        name = connection.name,
                        type = connection.type,
                        remoteConfig = connection.config,
                    )
                }
            )
        }
    }

    fun setDialogStage(dialogStage: DialogStage?) {
        _state.update {
            it.copy(
                dialogState = it.dialogState.copy(
                    dialogStage = dialogStage
                )
            )
        }
    }

    fun addLocalDraftData(uri: Uri) {
        _state.update {
            it.copy(
                dialogState = it.dialogState.copy(
                    kind = ConnectionKind.LOCAL,
                    modelUri = uri
                )
            )
        }
    }

    fun addRemoteDraftData(remoteConfig: RemoteConnectionConfig): Boolean {
        if (!remoteConfig.isValid) {
            _events.tryEmit("Fill in all remote connection fields")
            return false
        }

        _state.update {
            it.copy(
                dialogState = it.dialogState.copy(
                    kind = ConnectionKind.REMOTE,
                    remoteConfig = remoteConfig
                )
            )
        }
        return true
    }

    fun addDraftDetailsData(
        name: String,
        type: ConnectionType
    ): Boolean {
        if (name.isEmpty()) {
            _events.tryEmit("Connection name cannot be empty")
            return false
        }

        _state.update {
            it.copy(
                dialogState = it.dialogState.copy(
                    name = name,
                    type = type
                )
            )
        }

        return true
    }

    fun clearDraftData() {
        _state.update {
            it.copy(
                dialogState = ConnectionDialogState()
            )
        }
    }

    fun updateConnection() {
        val draft = _state.value.dialogState

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    dialogState = it.dialogState.copy(dialogStage = null)
                )
            }

            try {
                requireNotNull(draft.updatingId) {
                    "Updating id is required"
                }
                requireNotNull(draft.kind) {
                    "Connection kind is required"
                }

                connectionManager.updateConnection(
                    id = draft.updatingId,
                    kind = draft.kind,
                    name = draft.name,
                    type = draft.type,
                    remoteConfig = draft.remoteConfig
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        dialogState = ConnectionDialogState()
                    )
                }
            } catch (e: Exception) {
                _events.tryEmit(e.message ?: "Failed to update connection")

                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun addConnection() {
        val draft = _state.value.dialogState

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    dialogState = it.dialogState.copy(dialogStage = null)
                )
            }

            try {
                requireNotNull(draft.kind) {
                    "Select a connection source"
                }

                connectionManager.addConnection(
                    kind = draft.kind,
                    type = draft.type,
                    name = draft.name,
                    uri = draft.modelUri,
                    remoteConfig = draft.remoteConfig,
                )

                _state.update {
                    it.copy(
                        isLoading = false,
                        dialogState = ConnectionDialogState()
                    )
                }
            } catch (e: Exception) {
                _events.tryEmit(e.message ?: "Failed to add connection")

                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}

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
