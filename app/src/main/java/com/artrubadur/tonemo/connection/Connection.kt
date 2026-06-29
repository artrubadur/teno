package com.artrubadur.tonemo.connection

sealed interface Connection {
    val id: String
    val kind: ConnectionKind
    val type: ConnectionType
    val name: String
    val active: Boolean
    val addedAt: Long
}

data class LocalConnection(
    override val id: String,
    override val kind: ConnectionKind = ConnectionKind.LOCAL,
    override val type: ConnectionType,
    override val name: String,
    override val active: Boolean,
    override val addedAt: Long,
    val config: LocalConnectionConfig,
) : Connection

data class LocalConnectionConfig(
    val modelType: ModelType,
    val fileName: String
)

data class RemoteConnection(
    override val id: String,
    override val kind: ConnectionKind = ConnectionKind.REMOTE,
    override val type: ConnectionType,
    override val name: String,
    override val active: Boolean,
    override val addedAt: Long,
    val config: RemoteConnectionConfig,
) : Connection

data class RemoteConnectionConfig(
    val baseUrl: String,
    val model: String,
    val authType: String,
    val apiKey: String
) {
    val isValid: Boolean
        get() = baseUrl.isNotBlank() &&
                model.isNotBlank() &&
                authType.isNotBlank() &&
                apiKey.isNotBlank()
}

enum class ConnectionKind {
    LOCAL,
    REMOTE
}

enum class ConnectionType {
    LLM,
}

enum class ModelType {
    LITERTLM
}
