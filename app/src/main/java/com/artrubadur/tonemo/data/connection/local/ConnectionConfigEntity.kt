package com.artrubadur.tonemo.data.connection.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.artrubadur.tonemo.connection.ModelType

@Entity(
    tableName = "local_connection_configs",
    foreignKeys = [
        ForeignKey(
            entity = ConnectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["connectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("connectionId")
    ]
)
data class LocalConnectionConfigEntity(
    @PrimaryKey val connectionId: String,
    val modelType: ModelType,
    val fileName: String,
)

@Entity(
    tableName = "remote_connection_configs",
    foreignKeys = [
        ForeignKey(
            entity = ConnectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["connectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("connectionId")
    ]
)
data class RemoteConnectionConfigEntity(
    @PrimaryKey val connectionId: String,
    val baseUrl: String,
    val model: String,
    val apiKey: String,
)