package com.artrubadur.teno.data.tools

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ToolSettingsStore(
    context: Context,
) {
    private val dataStore: DataStore<Preferences> =
        MultiProcessDataStoreFactory.create(
            serializer = PreferencesFileSerializer,
            produceFile = { context.applicationContext.preferencesDataStoreFile("tool_settings") }
        )

    val toolOverrides: Flow<Map<String, Boolean>> =
        dataStore.data.map { preferences ->
            preferences[TOOL_OVERRIDES]
                ?.let { json.decodeFromString<Map<String, Boolean>>(it) }
                ?: emptyMap()
        }

    suspend fun getToolOverrides(): Map<String, Boolean> = toolOverrides.first()

    suspend fun setOverride(
        toolName: String,
        enabled: Boolean,
    ) {
        dataStore.edit { preferences ->
            val overrides = preferences[TOOL_OVERRIDES]
                ?.let { json.decodeFromString<Map<String, Boolean>>(it) }
                .orEmpty()
            preferences[TOOL_OVERRIDES] = json.encodeToString(overrides + (toolName to enabled))
        }
    }

    private companion object {
        val TOOL_OVERRIDES = stringPreferencesKey("tool_overrides")
        val json = Json { ignoreUnknownKeys = true }
    }
}
