package com.artrubadur.teno.agent.orchestration

import com.artrubadur.teno.agent.tools.ToolCall
import com.artrubadur.teno.agent.tools.ToolResult
import com.artrubadur.teno.agent.tools.ToolSpec
import com.artrubadur.teno.connection.runtime.llm.AgentInstructions
import com.artrubadur.teno.connection.runtime.llm.LlmMessage
import com.artrubadur.teno.connection.runtime.llm.LlmOptions
import com.artrubadur.teno.connection.runtime.llm.LlmRequest

class AgentSession(
    val id: String,
    val userRequest: String,
    val tools: List<ToolSpec>,
    val instructions: AgentInstructions,
    val agentOptions: AgentOptions,
    val llmOptions: LlmOptions,
) {
    var stepCount: Int = 0
        private set

    private val _messages = mutableListOf<LlmMessage>(
        LlmMessage.User(userRequest)
    )
    val messages: List<LlmMessage>
        get() = _messages

    private val pendingToolCalls = ArrayDeque<ToolCall>()
    private var nextToolCallIndex = 1

    fun addToolCalls(calls: List<ToolCall>) {
        val uniqueCalls = calls.map(::withUniqueId)
        pendingToolCalls.addAll(uniqueCalls)
        _messages += LlmMessage.AssistantToolCalls(uniqueCalls)
        stepCount += 1
    }

    private fun withUniqueId(call: ToolCall): ToolCall {
        val baseId = call.id.ifBlank { "tool-${call.tool}" }
        return call.copy(id = "$baseId-${nextToolCallIndex++}")
    }

    fun consumeToolCall(): ToolCall? {
        return pendingToolCalls.removeFirstOrNull()
    }

    fun addToolResult(result: ToolResult) {
        _messages += LlmMessage.Tool(result)
    }

    fun toLlmRequest(): LlmRequest {
        return LlmRequest(
            sessionId = id,
            instructions = instructions,
            messages = messages,
            tools = tools,
            options = llmOptions,
        )
    }
}
