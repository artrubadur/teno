package com.artrubadur.tonemo.agent.orchestration

import com.artrubadur.tonemo.agent.tools.ToolCall
import com.artrubadur.tonemo.agent.tools.ToolResult

sealed interface AgentEvent {
    data class FinalAnswer(
        val message: String
    ) : AgentEvent

    data class ToolStarted(
        val call: ToolCall
    ) : AgentEvent

    data class ToolExecuted(
        val result: ToolResult
    ) : AgentEvent

    data class ToolFailed(
        val result: ToolResult
    ) : AgentEvent

    data class ToolBlocked(
        val result: ToolResult
    ) : AgentEvent

    data class ConfirmationRequired(
        val confirmationId: String,
        val call: ToolCall,
        val title: String,
        val description: String
    ) : AgentEvent

    data class Failed(
        val reason: String
    ) : AgentEvent
}
