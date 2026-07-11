package com.artrubadur.tonemo.agent.orchestration

import com.artrubadur.tonemo.agent.tools.ToolCall
import com.artrubadur.tonemo.agent.tools.ToolResult
import kotlinx.serialization.Serializable

@Serializable
sealed interface AgentEvent {
    @Serializable
    data class FinalAnswer(
        val message: String
    ) : AgentEvent

    @Serializable
    data class ToolStarted(
        val call: ToolCall
    ) : AgentEvent

    @Serializable
    data class ToolExecuted(
        val result: ToolResult
    ) : AgentEvent

    @Serializable
    data class ToolFailed(
        val result: ToolResult
    ) : AgentEvent

    @Serializable
    data class ToolBlocked(
        val result: ToolResult
    ) : AgentEvent

    @Serializable
    data class ConfirmationRequired(
        val confirmationId: String,
        val call: ToolCall,
        val title: String,
        val description: String
    ) : AgentEvent

    @Serializable
    data class Failed(
        val reason: String
    ) : AgentEvent
}
