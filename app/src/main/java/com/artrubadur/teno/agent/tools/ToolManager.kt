package com.artrubadur.teno.agent.tools

import android.content.Context
import com.artrubadur.teno.data.tools.ToolSettingsStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ToolManager(
    private val context: Context,
    private val registry: ToolRegistry,
    private val settingsStore: ToolSettingsStore,
) {
    private val specs: List<ToolSpec> by lazy {
        registry.all().map { it.toSpec() }.sortedBy { it.title }
    }

    val enabledToolNames: Flow<Set<String>> =
        settingsStore.enabledToolNames.map { it ?: defaultEnabledToolNames() }

    fun allSpecs(): List<ToolSpec> = specs

    suspend fun enabledSpecs(): List<ToolSpec> {
        val enabledNames = settingsStore.getEnabledToolNames(defaultEnabledToolNames())
        return specs.filter { spec ->
            spec.name in enabledNames && spec.requiredPermissions.all { it.isGranted(context) }
        }
    }

    suspend fun isEnabled(tool: Tool<*>): Boolean {
        val spec = tool.toSpec()
        val enabledNames = settingsStore.getEnabledToolNames(defaultEnabledToolNames())
        return spec.name in enabledNames && spec.requiredPermissions.all { it.isGranted(context) }
    }

    suspend fun setEnabled(toolName: String, enabled: Boolean) {
        settingsStore.setEnabled(
            toolName = toolName,
            enabled = enabled,
            defaultNames = defaultEnabledToolNames()
        )
    }

    private fun defaultEnabledToolNames(): Set<String> {
        return specs
            .filter { it.requiredPermissions.isEmpty() }
            .map { it.name }
            .toSet()
    }
}
