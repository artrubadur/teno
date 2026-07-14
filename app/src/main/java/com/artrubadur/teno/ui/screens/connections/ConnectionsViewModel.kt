package com.artrubadur.teno.ui.screens.connections

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artrubadur.teno.connection.Connection
import com.artrubadur.teno.connection.ConnectionKind
import com.artrubadur.teno.connection.ConnectionManager
import com.artrubadur.teno.connection.ConnectionType
import com.artrubadur.teno.connection.LocalConnection
import com.artrubadur.teno.connection.RemoteConnection
import com.artrubadur.teno.connection.RemoteConnectionConfig
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
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun onToggleActive(connection: Connection) {
        viewModelScope.launch {
            try {
                connectionManager.setActiveConnection(connection.id)
            } catch (e: Exception) {
                _events.tryEmit(e.message ?: "Failed to set active connection")
            }
        }
    }

    fun openSourceDialog() {
        _state.update {
            it.copy(dialogState = ConnectionDialogState(dialog = ConnectionDialogType.SOURCE))
        }
    }

    fun openRemoteAddDialog() {
        _state.update {
            it.copy(
                dialogState = ConnectionDialogState(
                    dialog = ConnectionDialogType.REMOTE,
                    kind = ConnectionKind.REMOTE,
                )
            )
        }
    }

    fun openEditDialog(connection: Connection) {
        _state.update {
            it.copy(
                dialogState = when (connection) {
                    is LocalConnection -> ConnectionDialogState(
                        dialog = ConnectionDialogType.LOCAL,
                        updatingId = connection.id,
                        kind = ConnectionKind.LOCAL,
                        type = connection.type,
                        name = connection.name,
                    )

                    is RemoteConnection -> ConnectionDialogState(
                        dialog = ConnectionDialogType.REMOTE,
                        updatingId = connection.id,
                        kind = ConnectionKind.REMOTE,
                        type = connection.type,
                        name = connection.name,
                        remoteConfig = connection.config,
                    )
                }
            )
        }
    }

    fun openDeleteDialog(connection: Connection) {
        _state.update {
            it.copy(
                dialogState = ConnectionDialogState(
                    dialog = ConnectionDialogType.DELETE,
                    updatingId = connection.id,
                    kind = connection.kind,
                    type = connection.type,
                    name = connection.name,
                )
            )
        }
    }

    fun addLocalDraftData(uri: Uri) {
        _state.update {
            it.copy(
                dialogState = ConnectionDialogState(
                    dialog = ConnectionDialogType.LOCAL,
                    kind = ConnectionKind.LOCAL,
                    modelUri = uri,
                )
            )
        }
    }

    fun dismissDialog() {
        _state.update {
            it.copy(dialogState = ConnectionDialogState())
        }
    }

    fun submitLocalConnection(name: String) {
        submitConnection(
            draft = _state.value.dialogState.copy(
                name = name,
                type = ConnectionType.LLM,
            )
        )
    }

    fun submitRemoteConnection(
        name: String,
        remoteConfig: RemoteConnectionConfig,
    ) {
        submitConnection(
            draft = _state.value.dialogState.copy(
                name = name,
                type = ConnectionType.LLM,
                kind = ConnectionKind.REMOTE,
                remoteConfig = remoteConfig,
            )
        )
    }

    private fun submitConnection(draft: ConnectionDialogState) {
        if (draft.name.isBlank()) {
            _events.tryEmit("Connection name cannot be empty")
            return
        }
        if (draft.kind == ConnectionKind.REMOTE && draft.remoteConfig?.isValid != true) {
            _events.tryEmit("Fill in all remote connection fields")
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    dialogState = it.dialogState.copy(dialog = null)
                )
            }

            try {
                val kind = requireNotNull(draft.kind) { "Connection kind is required" }

                if (draft.updatingId == null) {
                    connectionManager.addConnection(
                        kind = kind,
                        type = draft.type,
                        name = draft.name,
                        uri = draft.modelUri,
                        remoteConfig = draft.remoteConfig,
                    )
                } else {
                    connectionManager.updateConnection(
                        id = draft.updatingId,
                        kind = kind,
                        name = draft.name,
                        type = draft.type,
                        remoteConfig = draft.remoteConfig,
                    )
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        dialogState = ConnectionDialogState()
                    )
                }
            } catch (e: Exception) {
                _events.tryEmit(e.message ?: "Failed to save connection")
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun deleteSelectedConnection(onDeleted: () -> Unit = {}) {
        val id = _state.value.dialogState.updatingId ?: return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    dialogState = it.dialogState.copy(dialog = null)
                )
            }

            try {
                connectionManager.deleteConnection(id)
                _state.update {
                    it.copy(
                        isLoading = false,
                        dialogState = ConnectionDialogState()
                    )
                }
                onDeleted()
            } catch (e: Exception) {
                _events.tryEmit(e.message ?: "Failed to delete connection")
                _state.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}
