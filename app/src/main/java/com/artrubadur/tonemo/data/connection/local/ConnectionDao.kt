package com.artrubadur.tonemo.data.connection.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.artrubadur.tonemo.connection.ConnectionType
import kotlinx.coroutines.flow.Flow

@Dao
interface ConnectionDao {

    @Transaction
    @Query("SELECT * FROM connections ORDER BY addedAt DESC")
    fun observeConnections(): Flow<List<ConnectionWithConfig>>

    @Transaction
    @Query("SELECT * FROM connections WHERE type = :type ORDER BY addedAt DESC")
    fun observeConnectionsByType(type: ConnectionType): Flow<List<ConnectionWithConfig>>

    @Transaction
    @Query("SELECT * FROM connections WHERE type = :type AND active = 1 LIMIT 1")
    fun observeActiveConnectionByType(type: ConnectionType): Flow<ConnectionWithConfig?>

    @Transaction
    @Query("SELECT * FROM connections WHERE id = :id LIMIT 1")
    suspend fun getConnectionById(id: String): ConnectionWithConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConnection(connection: ConnectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalConfig(config: LocalConnectionConfigEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemoteConfig(config: RemoteConnectionConfigEntity)

    @Transaction
    suspend fun insertLocalConnection(
        connection: ConnectionEntity,
        config: LocalConnectionConfigEntity
    ) {
        insertConnection(connection)
        insertLocalConfig(config)
    }

    @Transaction
    suspend fun insertRemoteConnection(
        connection: ConnectionEntity,
        config: RemoteConnectionConfigEntity
    ) {
        insertConnection(connection)
        insertRemoteConfig(config)
    }

    @Query("DELETE FROM connections WHERE id = :id")
    suspend fun deleteConnectionById(id: String)

    @Query(
        """
        UPDATE connections
        SET name = :name,
            type = :type
        WHERE id = :id
    """
    )
    suspend fun updateConnectionDetails(
        id: String,
        name: String,
        type: ConnectionType,
    )

    @Query(
        """
        UPDATE remote_connection_configs
        SET baseUrl = :baseUrl,
            model = :model,
            authType = :authType,
            apiKey = :apiKey
        WHERE connectionId = :id
    """
    )
    suspend fun updateRemoteConfig(
        id: String,
        baseUrl: String,
        model: String,
        authType: String,
        apiKey: String,
    )

    @Transaction
    suspend fun updateRemoteConnection(
        id: String,
        name: String,
        type: ConnectionType,
        config: RemoteConnectionConfigEntity,
    ) {
        updateConnectionDetails(id, name, type)
        updateRemoteConfig(
            id = id,
            baseUrl = config.baseUrl,
            model = config.model,
            authType = config.authType,
            apiKey = config.apiKey,
        )
    }

    @Transaction
    suspend fun updateLocalConnection(
        id: String,
        name: String,
        type: ConnectionType,
    ) {
        updateConnectionDetails(id, name, type)
    }

    @Query("UPDATE connections SET active = 0 WHERE id = :id")
    suspend fun deactivateConnectionById(id: String)

    @Query("UPDATE connections SET active = 0 WHERE type = :type")
    suspend fun deactivateConnectionsByType(type: ConnectionType)

    @Query("UPDATE connections SET active = 1 WHERE id = :id")
    suspend fun activateConnectionById(id: String)

    @Transaction
    suspend fun toggleActiveConnection(id: String) {
        val connection = getConnectionById(id)?.connection ?: return

        if (connection.active) {
            deactivateConnectionById(id)
        } else {
            deactivateConnectionsByType(connection.type)
            activateConnectionById(id)
        }
    }
}