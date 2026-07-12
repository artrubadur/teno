package com.artrubadur.teno.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.artrubadur.teno.data.connection.local.ConnectionConverters
import com.artrubadur.teno.data.connection.local.ConnectionDao
import com.artrubadur.teno.data.connection.local.ConnectionEntity
import com.artrubadur.teno.data.connection.local.LocalConnectionConfigEntity
import com.artrubadur.teno.data.connection.local.RemoteConnectionConfigEntity

@Database(
    entities = [
        ConnectionEntity::class,
        LocalConnectionConfigEntity::class,
        RemoteConnectionConfigEntity::class,
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(ConnectionConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun connectionDao(): ConnectionDao
}