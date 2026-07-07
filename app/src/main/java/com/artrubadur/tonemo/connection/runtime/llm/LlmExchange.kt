package com.artrubadur.tonemo.connection.runtime.llm

import com.artrubadur.tonemo.agent.tools.ToolCall
import com.artrubadur.tonemo.agent.tools.ToolResult
import com.artrubadur.tonemo.agent.tools.ToolSpec

data class AgentInstructions(
    val identity: List<String>,
    val rules: List<String>
) {
    fun render(): String = buildString {
        appendLine("SYSTEM:")
        identity.forEach { identity ->
            appendLine(identity)
        }

        appendLine()
        appendLine("RULES:")
        rules.forEach { rule ->
            appendLine("- $rule")
        }
    }
}

data class LlmRequest(
    val sessionId: String,
    val instructions: AgentInstructions,
    val userRequest: String,
    val tools: List<ToolSpec> = emptyList(),
    val toolCalls: List<ToolCall> = emptyList(),
    val toolResults: List<ToolResult> = emptyList(),
    val options: LlmOptions = LlmOptions()
)

sealed interface LlmResponse {
    data class Final(
        val content: String
    ) : LlmResponse

    data class ToolCalls(
        val calls: List<ToolCall>
    ) : LlmResponse
}