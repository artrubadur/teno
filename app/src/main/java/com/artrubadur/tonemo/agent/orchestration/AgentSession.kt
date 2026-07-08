package com.artrubadur.tonemo.agent.orchestration

import com.artrubadur.tonemo.agent.tools.ToolCall
import com.artrubadur.tonemo.agent.tools.ToolResult
import com.artrubadur.tonemo.agent.tools.ToolSpec
import com.artrubadur.tonemo.connection.runtime.llm.LlmMessage
import com.artrubadur.tonemo.connection.runtime.llm.LlmRequest

class AgentSession(
    val id: String,
    val userRequest: String,
    val tools: List<ToolSpec>,
) {
    var stepCount: Int = 0
        private set

    private val _messages = mutableListOf<LlmMessage>(
        LlmMessage.User(userRequest)
    )
    val messages: List<LlmMessage>
        get() = _messages

    private val pendingToolCalls = ArrayDeque<ToolCall>()

    fun addToolCalls(calls: List<ToolCall>) {
        pendingToolCalls.addAll(calls)
        _messages += LlmMessage.AssistantToolCalls(calls)
        stepCount += 1
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
            instructions = AgentDefaults.instructions,
            messages = messages,
            tools = tools,
            options = AgentDefaults.options.llmOptions
        )
    }
}