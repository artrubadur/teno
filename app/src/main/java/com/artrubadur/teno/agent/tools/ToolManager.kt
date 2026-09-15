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
        settingsStore.toolOverrides.map { overrides ->
            specs.filter { spec -> overrides[spec.name] ?: spec.enabled }
                .map { it.name }
                .toSet()
        }

    fun allSpecs(): List<ToolSpec> = specs

    suspend fun enabledSpecs(): List<ToolSpec> {
        val overrides = settingsStore.getToolOverrides()
        return specs.filter { spec ->
            (overrides[spec.name] ?: spec.enabled) &&
                    spec.requiredPermissions.all { it.isGranted(context) }
        }
    }

    suspend fun isEnabled(tool: Tool<*>): Boolean {
        val spec = tool.toSpec()
        val overrides = settingsStore.getToolOverrides()
        return (overrides[spec.name] ?: spec.enabled) &&
                spec.requiredPermissions.all { it.isGranted(context) }
    }

    suspend fun setEnabled(toolName: String, enabled: Boolean) {
        settingsStore.setOverride(
            toolName = toolName,
            enabled = enabled,
        )
    }
}
