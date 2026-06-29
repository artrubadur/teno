package com.artrubadur.tonemo.data.connection.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.artrubadur.tonemo.connection.ConnectionKind
import com.artrubadur.tonemo.connection.ConnectionType

@Entity(tableName = "connections")
data class ConnectionEntity(
    @PrimaryKey val id: String,

    val kind: ConnectionKind,
    val type: ConnectionType,
    val active: Boolean = false,
    val name: String,
    val addedAt: Long,
)