package com.artrubadur.tonemo.agent.orchestration

import com.artrubadur.tonemo.agent.tools.ToolCall
import com.artrubadur.tonemo.agent.tools.ToolResult
import com.artrubadur.tonemo.agent.tools.ToolSpec
import com.artrubadur.tonemo.connection.runtime.llm.LlmRequest

class AgentSession(
    val id: String,
    val userRequest: String,
    val tools: List<ToolSpec>,
) {
    var stepCount: Int = 0
        private set

    private val _toolCalls = mutableListOf<ToolCall>()
    val toolCalls: List<ToolCall>
        get() = _toolCalls

    private val _toolResults = mutableListOf<ToolResult>()
    val toolResults: List<ToolResult>
        get() = _toolResults

    private val pendingToolCalls = ArrayDeque<ToolCall>()

    fun addToolCalls(calls: List<ToolCall>) {
        pendingToolCalls.addAll(calls)
        _toolCalls += calls
        stepCount += 1
    }

    fun consumeToolCall(): ToolCall? {
        return pendingToolCalls.removeFirstOrNull()
    }

    fun addToolResult(result: ToolResult) {
        _toolResults += result
    }

    fun toLlmRequest(): LlmRequest {
        return LlmRequest(
            sessionId = id,
            instructions = AgentDefaults.instructions,
            userRequest = userRequest,
            tools = tools,
            toolCalls = toolCalls,
            toolResults = toolResults,
            options = AgentDefaults.options.llmOptions
        )
    }
}
