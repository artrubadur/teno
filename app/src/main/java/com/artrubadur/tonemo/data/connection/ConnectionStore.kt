package com.artrubadur.tonemo.data.connection

import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.ConnectionKind
import com.artrubadur.tonemo.connection.ConnectionType
import com.artrubadur.tonemo.connection.LocalConnectionConfig
import com.artrubadur.tonemo.connection.RemoteConnectionConfig
import com.artrubadur.tonemo.data.connection.local.ConnectionDao
import com.artrubadur.tonemo.data.connection.local.ConnectionEntity
import com.artrubadur.tonemo.data.connection.local.LocalConnectionConfigEntity
import com.artrubadur.tonemo.data.connection.local.RemoteConnectionConfigEntity
import com.artrubadur.tonemo.data.connection.local.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ConnectionStore(
    private val connectionDao: ConnectionDao
) {

    fun observeConnections(): Flow<List<Connection>> =
        connectionDao.observeConnections().map { entities ->
            entities.map { it.toDomain() }
        }

    fun observeConnectionsByType(type: ConnectionType): Flow<List<Connection>> {
        return connectionDao.observeConnectionsByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun observeActiveConnection(type: ConnectionType): Flow<Connection?> {
        return connectionDao.observeActiveConnectionByType(type).map { entity ->
            entity?.toDomain()
        }
    }

    suspend fun getConnection(id: String): Connection? {
        return connectionDao.getConnectionById(id)?.toDomain()
    }


    suspend fun addLocalConnection(
        name: String,
        type: ConnectionType,
        config: LocalConnectionConfig,
    ): String {
        val id = UUID.randomUUID().toString()
        connectionDao.insertLocalConnection(
            connection = ConnectionEntity(
                id = id,
                kind = ConnectionKind.LOCAL,
                type = type,
                name = name,
                addedAt = System.currentTimeMillis(),
            ),
            config = LocalConnectionConfigEntity(
                connectionId = id,
                modelType = config.modelType,
                fileName = config.fileName
            )
        )
        return id
    }

    suspend fun addRemoteConnection(
        name: String,
        type: ConnectionType,
        config: RemoteConnectionConfig
    ): String {
        val id = UUID.randomUUID().toString()
        connectionDao.insertRemoteConnection(
            ConnectionEntity(
                id = id,
                kind = ConnectionKind.REMOTE,
                type = type,
                name = name,
                addedAt = System.currentTimeMillis(),
            ),
            config = RemoteConnectionConfigEntity(
                connectionId = id,
                baseUrl = config.baseUrl,
                model = config.model,
                apiKey = config.apiKey,
            )
        )
        return id
    }

    suspend fun updateLocalConnection(
        id: String,
        name: String,
        type: ConnectionType,
    ) {
        connectionDao.updateLocalConnection(
            id = id,
            name = name,
            type = type,
        )
    }

    suspend fun updateRemoteConnection(
        id: String,
        name: String,
        type: ConnectionType,
        config: RemoteConnectionConfig
    ) {
        connectionDao.updateRemoteConnection(
            id = id,
            name = name,
            type = type,
            config = RemoteConnectionConfigEntity(
                connectionId = id,
                baseUrl = config.baseUrl,
                model = config.model,
                apiKey = config.apiKey,
            )
        )
    }

    suspend fun deleteConnection(id: String) {
        connectionDao.deleteConnectionById(id)
    }

    suspend fun setActiveConnection(id: String) {
        connectionDao.toggleActiveConnection(id)
    }
}