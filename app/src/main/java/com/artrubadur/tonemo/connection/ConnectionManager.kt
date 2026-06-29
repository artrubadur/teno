package com.artrubadur.tonemo.connection

import android.net.Uri
import com.artrubadur.tonemo.data.connection.ConnectionStore
import com.artrubadur.tonemo.data.model.ModelStore
import kotlinx.coroutines.flow.Flow

class ConnectionManager(
    private val connectionStore: ConnectionStore,
    private val modelStore: ModelStore,
) {
    fun observeConnections(typeFilter: ConnectionType?): Flow<List<Connection>> {
        return when (typeFilter) {
            null -> connectionStore.observeConnections()
            else -> connectionStore.observeConnectionsByType(typeFilter)
        }
    }

    fun observeActiveConnection(type: ConnectionType): Flow<Connection?> {
        return connectionStore.observeActiveConnection(type)
    }

    suspend fun deleteConnection(id: String) {
        val connection = connectionStore.getConnection(id) ?: return

        when (connection.kind) {
            ConnectionKind.LOCAL -> {
                modelStore.deleteModel(id)
                connectionStore.deleteConnection(id)
            }

            ConnectionKind.REMOTE -> {
                connectionStore.deleteConnection(id)
            }
        }
    }

    suspend fun updateConnection(
        id: String,
        kind: ConnectionKind,
        name: String,
        type: ConnectionType,
        remoteConfig: RemoteConnectionConfig?
    ) {
        when (kind) {
            ConnectionKind.LOCAL -> {
                connectionStore.updateLocalConnection(
                    id = id,
                    name = name,
                    type = type
                )
            }

            ConnectionKind.REMOTE -> {
                requireNotNull(remoteConfig) {
                    "Remote config is required"
                }

                connectionStore.updateRemoteConnection(
                    id = id,
                    name = name,
                    type = type,
                    config = remoteConfig
                )
            }
        }
    }

    suspend fun setActiveConnection(id: String) {
        connectionStore.setActiveConnection(id)
    }

    suspend fun addConnection(
        kind: ConnectionKind,
        type: ConnectionType,
        name: String,
        uri: Uri?,
        remoteConfig: RemoteConnectionConfig?
    ) {
        when (kind) {
            ConnectionKind.LOCAL -> {
                requireNotNull(uri) {
                    "Model URI is required"
                }

                val modelInfo = modelStore.inspectModel(uri)
                val localConfig = LocalConnectionConfig(
                    modelType = modelInfo.modelType,
                    fileName = modelInfo.fileName
                )

                val id = connectionStore.addLocalConnection(
                    name = name,
                    type = type,
                    config = localConfig
                )

                try {
                    modelStore.importModel(modelInfo.fileName, uri)
                } catch (e: Exception) {
                    connectionStore.deleteConnection(id)
                    throw e
                }
            }

            ConnectionKind.REMOTE -> {
                requireNotNull(remoteConfig) {
                    "Remote config is required"
                }

                connectionStore.addRemoteConnection(name, type, remoteConfig)
            }
        }
    }
}