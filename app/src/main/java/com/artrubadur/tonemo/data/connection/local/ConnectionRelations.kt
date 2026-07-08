package com.artrubadur.tonemo.data.connection.local

import androidx.room.Embedded
import androidx.room.Relation
import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.ConnectionKind
import com.artrubadur.tonemo.connection.LocalConnection
import com.artrubadur.tonemo.connection.LocalConnectionConfig
import com.artrubadur.tonemo.connection.RemoteConnection
import com.artrubadur.tonemo.connection.RemoteConnectionConfig

data class ConnectionWithConfig(
    @Embedded val connection: ConnectionEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "connectionId"
    )
    val localConfig: LocalConnectionConfigEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "connectionId"
    )
    val remoteConfig: RemoteConnectionConfigEntity?,
)

fun ConnectionWithConfig.toDomain(): Connection {
    return when (connection.kind) {
        ConnectionKind.LOCAL -> toLocalConnection()
        ConnectionKind.REMOTE -> toRemoteConnection()
    }
}

private fun ConnectionWithConfig.toLocalConnection(): LocalConnection {
    val config = requireNotNull(localConfig) {
        "Local connection ${connection.id} must have local config"
    }

    return LocalConnection(
        id = connection.id,
        type = connection.type,
        name = connection.name,
        active = connection.active,
        addedAt = connection.addedAt,
        config = LocalConnectionConfig(
            modelType = config.modelType,
            fileName = config.fileName
        )
    )
}

private fun ConnectionWithConfig.toRemoteConnection(): RemoteConnection {
    val config = requireNotNull(remoteConfig) {
        "Remote connection ${connection.id} must have remote config"
    }

    return RemoteConnection(
        id = connection.id,
        type = connection.type,
        name = connection.name,
        active = connection.active,
        addedAt = connection.addedAt,
        config = RemoteConnectionConfig(
            baseUrl = config.baseUrl,
            model = config.model,
            apiKey = config.apiKey,
        ),
    )
}