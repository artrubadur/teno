package com.artrubadur.tonemo.data.connection.local

import androidx.room.TypeConverter
import com.artrubadur.tonemo.connection.ConnectionKind
import com.artrubadur.tonemo.connection.ConnectionType
import com.artrubadur.tonemo.connection.ModelType

class ConnectionConverters {

    @TypeConverter
    fun connectionKindToString(value: ConnectionKind): String {
        return value.name
    }

    @TypeConverter
    fun stringToConnectionKind(value: String): ConnectionKind {
        return ConnectionKind.valueOf(value)
    }

    @TypeConverter
    fun connectionTypeToString(value: ConnectionType): String {
        return value.name
    }

    @TypeConverter
    fun stringToConnectionType(value: String): ConnectionType {
        return ConnectionType.valueOf(value)
    }

    @TypeConverter
    fun modelTypeToString(value: ModelType): String {
        return value.name
    }

    @TypeConverter
    fun stringToModelType(value: String): ModelType {
        return ModelType.valueOf(value)
    }
}