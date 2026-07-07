package com.artrubadur.tonemo.agent.tools

import java.util.UUID

data class ToolCall(
    val id: String = UUID.randomUUID().toString(),
    val tool: String,
    val arguments: Map<String, Any?> = emptyMap()
)

data class ToolResult(
    val toolCallId: String,
    val tool: String,
    val result: Any?,
)
