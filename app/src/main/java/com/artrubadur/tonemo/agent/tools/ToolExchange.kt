package com.artrubadur.tonemo.agent.tools

import kotlinx.serialization.json.JsonObject

data class ToolCall(
    val id: String,
    val tool: String,
    val arguments: JsonObject
)

data class ToolResult(
    val toolCallId: String,
    val tool: String,
    val result: JsonObject,
)
