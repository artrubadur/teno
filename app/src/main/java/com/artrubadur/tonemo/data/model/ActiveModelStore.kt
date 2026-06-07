package com.artrubadur.tonemo.data.model

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.activeModelDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "active_model_preferences"
)

class ActiveModelStore(
    context: Context
) {
    private val dataStore = context.activeModelDataStore

    val activeModelFileNames: Flow<Map<ActiveModelSlot, String>> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            ActiveModelSlot.entries.mapNotNull { slot ->
                preferences[activeModelKey(slot)]?.let { fileName ->
                    slot to fileName
                }
            }.toMap()
        }

    suspend fun setActiveModel(
        modelType: ModelType,
        modelFileName: String
    ) {
        dataStore.edit { preferences ->
            preferences[activeModelKey(modelType.activeModelSlot())] = modelFileName
        }
    }

    suspend fun clearActiveModel(modelType: ModelType) {
        dataStore.edit { preferences ->
            preferences.remove(activeModelKey(modelType.activeModelSlot()))
        }
    }

    suspend fun clearActiveModelReferences(modelFileName: String) {
        dataStore.edit { preferences ->
            ActiveModelSlot.entries.forEach { slot ->
                val key = activeModelKey(slot)
                if (preferences[key] == modelFileName) {
                    preferences.remove(key)
                }
            }
        }
    }

    suspend fun reconcileModelTypeChange(
        modelFileName: String,
        previousType: ModelType,
        updatedType: ModelType
    ) {
        val previousSlot = previousType.activeModelSlot()
        val updatedSlot = updatedType.activeModelSlot()

        if (previousSlot == updatedSlot) {
            return
        }

        dataStore.edit { preferences ->
            val previousKey = activeModelKey(previousSlot)
            val updatedKey = activeModelKey(updatedSlot)
            val wasActiveForPreviousType = preferences[previousKey] == modelFileName

            ActiveModelSlot.entries.forEach { slot ->
                if (slot == updatedSlot) {
                    return@forEach
                }

                val key = activeModelKey(slot)
                if (preferences[key] == modelFileName) {
                    preferences.remove(key)
                }
            }

            if (wasActiveForPreviousType && preferences[updatedKey] == null) {
                preferences[updatedKey] = modelFileName
            }
        }
    }

    private fun activeModelKey(slot: ActiveModelSlot): Preferences.Key<String> {
        return stringPreferencesKey("active_model_${slot.name.lowercase()}")
    }
}
