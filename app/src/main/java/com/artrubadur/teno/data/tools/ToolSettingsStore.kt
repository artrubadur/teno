package com.artrubadur.teno.data.tools

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ToolSettingsStore(
    context: Context,
) {
    private val dataStore: DataStore<Preferences> =
        MultiProcessDataStoreFactory.create(
            serializer = PreferencesFileSerializer,
            produceFile = { context.applicationContext.preferencesDataStoreFile("tool_settings") }
        )

    val enabledToolNames: Flow<Set<String>?> =
        dataStore.data.map { preferences ->
            preferences[ENABLED_TOOLS]
        }

    suspend fun getEnabledToolNames(defaultNames: Set<String>): Set<String> {
        return dataStore.data.first()[ENABLED_TOOLS] ?: defaultNames
    }

    suspend fun setEnabled(
        toolName: String,
        enabled: Boolean,
        defaultNames: Set<String>,
    ) {
        dataStore.edit { preferences ->
            val current = preferences[ENABLED_TOOLS] ?: defaultNames
            preferences[ENABLED_TOOLS] = if (enabled) {
                current + toolName
            } else {
                current - toolName
            }
        }
    }

    private companion object {
        val ENABLED_TOOLS = stringSetPreferencesKey("enabled_tools")
    }
}
