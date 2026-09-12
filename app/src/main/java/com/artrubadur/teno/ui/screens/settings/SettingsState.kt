package com.artrubadur.teno.ui.screens.settings

import com.artrubadur.teno.data.agent.AgentInstructionEntry
import com.artrubadur.teno.data.agent.AgentSettings

data class SettingsState(
    val identity: List<AgentInstructionEntry> = emptyList(),
    val rules: List<AgentInstructionEntry> = emptyList(),
    val maxStepsText: String = "",
    val maxStepsError: Boolean = false,
    val unlimitedMaxSteps: Boolean = false,
    val temperatureText: String = "",
    val temperatureError: Boolean = false,
    val topKText: String = "",
    val topKError: Boolean = false,
    val topPText: String = "",
    val topPError: Boolean = false,
    val maxTokensText: String = "",
    val maxTokensError: Boolean = false,
) {
    companion object {
        fun from(settings: AgentSettings): SettingsState =
            SettingsState(
                identity = settings.identity,
                rules = settings.rules,
                maxStepsText = settings.agentOptions.maxSteps.toString(),
                unlimitedMaxSteps = settings.agentOptions.unlimitedMaxSteps,
                temperatureText = settings.llmOptions.temperature.toString(),
                topKText = settings.llmOptions.topK.toString(),
                topPText = settings.llmOptions.topP.toString(),
                maxTokensText = settings.llmOptions.maxTokens.toString(),
            )
    }
}
