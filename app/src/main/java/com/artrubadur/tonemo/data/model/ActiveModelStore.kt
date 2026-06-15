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

    val activeModelFileNames: Flow<Map<ModelType, String>> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            ModelType.entries.mapNotNull { modelType ->
                preferences[activeModelKey(modelType)]?.let { fileName ->
                    modelType to fileName
                }
            }.toMap()
        }

    suspend fun setActiveModel(
        modelType: ModelType,
        modelFileName: String
    ) {
        dataStore.edit { preferences ->
            preferences[activeModelKey(modelType)] = modelFileName
        }
    }

    suspend fun clearActiveModel(modelType: ModelType) {
        dataStore.edit { preferences ->
            preferences.remove(activeModelKey(modelType))
        }
    }

    suspend fun clearActiveModelReferences(modelFileName: String) {
        dataStore.edit { preferences ->
            ModelType.entries.forEach { modelType ->
                val key = activeModelKey(modelType)
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
        if (previousType == updatedType) {
            return
        }

        dataStore.edit { preferences ->
            val previousKey = activeModelKey(previousType)
            val updatedKey = activeModelKey(updatedType)
            val wasActiveForPreviousType = preferences[previousKey] == modelFileName

            ModelType.entries.forEach { modelType ->
                if (modelType == updatedType) {
                    return@forEach
                }

                val key = activeModelKey(modelType)
                if (preferences[key] == modelFileName) {
                    preferences.remove(key)
                }
            }

            if (wasActiveForPreviousType && preferences[updatedKey] == null) {
                preferences[updatedKey] = modelFileName
            }
        }
    }

    private fun activeModelKey(modelType: ModelType): Preferences.Key<String> {
        return stringPreferencesKey("active_model_${modelType.name.lowercase()}")
    }
}
