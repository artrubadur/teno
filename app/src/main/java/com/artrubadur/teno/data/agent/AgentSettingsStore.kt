package com.artrubadur.teno.data.agent

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesFileSerializer
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import com.artrubadur.teno.agent.orchestration.AgentDefaults
import com.artrubadur.teno.agent.orchestration.AgentOptions
import com.artrubadur.teno.connection.runtime.llm.AgentInstructions
import com.artrubadur.teno.connection.runtime.llm.LlmOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.UUID

class AgentSettingsStore(
    context: Context,
) {
    private val dataStore: DataStore<Preferences> =
        MultiProcessDataStoreFactory.create(
            serializer = PreferencesFileSerializer,
            produceFile = { context.applicationContext.preferencesDataStoreFile("agent_settings") }
        )

    val settings: Flow<AgentSettings> =
        dataStore.data.map { preferences -> preferences.toSettings() }

    suspend fun getSettings(): AgentSettings = settings.first()

    suspend fun setMaxSteps(value: Int) {
        dataStore.edit { preferences ->
            preferences[MAX_STEPS] = value
        }
    }

    suspend fun setUnlimitedMaxSteps(value: Boolean) {
        dataStore.edit { preferences ->
            preferences[UNLIMITED_MAX_STEPS] = value
        }
    }

    suspend fun setTemperature(value: Double) {
        dataStore.edit { preferences ->
            preferences[TEMPERATURE] = value
        }
    }

    suspend fun setTopK(value: Int) {
        dataStore.edit { preferences ->
            preferences[TOP_K] = value
        }
    }

    suspend fun setTopP(value: Double) {
        dataStore.edit { preferences ->
            preferences[TOP_P] = value
        }
    }

    suspend fun setMaxTokens(value: Int) {
        dataStore.edit { preferences ->
            preferences[MAX_TOKENS] = value
        }
    }

    suspend fun setInstructionEnabled(
        kind: AgentInstructionKind,
        id: String,
        enabled: Boolean,
    ) {
        updateInstructions(kind) { entries ->
            entries.map { entry ->
                if (entry.id == id) entry.copy(enabled = enabled) else entry
            }
        }
    }

    suspend fun addInstruction(kind: AgentInstructionKind, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        updateInstructions(kind) { entries ->
            entries + StoredInstruction(
                id = UUID.randomUUID().toString(),
                text = trimmed,
                enabled = true,
            )
        }
    }

    suspend fun deleteInstruction(kind: AgentInstructionKind, id: String) {
        updateInstructions(kind) { entries ->
            entries.filterNot { it.id == id }
        }
    }

    private suspend fun updateInstructions(
        kind: AgentInstructionKind,
        transform: (List<StoredInstruction>) -> List<StoredInstruction>,
    ) {
        dataStore.edit { preferences ->
            val updated = transform(preferences.readInstructions(kind))
            preferences[kind.key] = json.encodeToString(instructionListSerializer, updated)
        }
    }

    private fun Preferences.toSettings(): AgentSettings {
        val identity = readInstructions(AgentInstructionKind.IDENTITY)
        val rules = readInstructions(AgentInstructionKind.RULES)

        return AgentSettings(
            identity = identity.map { it.toEntry() },
            rules = rules.map { it.toEntry() },
            agentOptions = AgentOptions(
                maxSteps = this[MAX_STEPS] ?: AgentDefaults.agentOptions.maxSteps,
                unlimitedMaxSteps = this[UNLIMITED_MAX_STEPS]
                    ?: AgentDefaults.agentOptions.unlimitedMaxSteps,
            ),
            llmOptions = LlmOptions(
                temperature = this[TEMPERATURE] ?: AgentDefaults.llmOptions.temperature,
                topK = this[TOP_K] ?: AgentDefaults.llmOptions.topK,
                topP = this[TOP_P] ?: AgentDefaults.llmOptions.topP,
                maxTokens = this[MAX_TOKENS] ?: AgentDefaults.llmOptions.maxTokens,
            )
        )
    }

    private fun Preferences.readInstructions(kind: AgentInstructionKind): List<StoredInstruction> {
        val stored = this[kind.key]
        if (stored != null) {
            return runCatching {
                json.decodeFromString(instructionListSerializer, stored)
            }.getOrElse { kind.defaultInstructions() }
        }
        return kind.defaultInstructions()
    }

    private fun AgentInstructionKind.defaultInstructions(): List<StoredInstruction> {
        val instructions = when (this) {
            AgentInstructionKind.IDENTITY -> AgentDefaults.instructions.identity
            AgentInstructionKind.RULES -> AgentDefaults.instructions.rules
        }
        return instructions.mapIndexed { index, text ->
            StoredInstruction(
                id = "${name.lowercase()}_$index",
                text = text,
                enabled = true,
            )
        }
    }

    private fun StoredInstruction.toEntry(): AgentInstructionEntry =
        AgentInstructionEntry(
            id = id,
            text = text,
            enabled = enabled,
        )

    private val AgentInstructionKind.key: Preferences.Key<String>
        get() = when (this) {
            AgentInstructionKind.IDENTITY -> IDENTITY
            AgentInstructionKind.RULES -> RULES
        }

    private companion object {
        val IDENTITY = stringPreferencesKey("identity")
        val RULES = stringPreferencesKey("rules")
        val MAX_STEPS = intPreferencesKey("max_steps")
        val UNLIMITED_MAX_STEPS = booleanPreferencesKey("unlimited_max_steps")
        val TEMPERATURE = doublePreferencesKey("temperature")
        val TOP_K = intPreferencesKey("top_k")
        val TOP_P = doublePreferencesKey("top_p")
        val MAX_TOKENS = intPreferencesKey("max_tokens")
    }
}

data class AgentSettings(
    val identity: List<AgentInstructionEntry>,
    val rules: List<AgentInstructionEntry>,
    val agentOptions: AgentOptions,
    val llmOptions: LlmOptions,
) {
    val instructions: AgentInstructions
        get() = AgentInstructions(
            identity = identity.filter { it.enabled }.map { it.text },
            rules = rules.filter { it.enabled }.map { it.text },
        )
}

data class AgentInstructionEntry(
    val id: String,
    val text: String,
    val enabled: Boolean,
)

enum class AgentInstructionKind {
    IDENTITY,
    RULES,
}

@Serializable
private data class StoredInstruction(
    val id: String,
    val text: String,
    val enabled: Boolean,
)

private val json = Json { ignoreUnknownKeys = true }
private val instructionListSerializer = ListSerializer(StoredInstruction.serializer())
