package com.artrubadur.teno.agent.tools

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ToolCall(
    val id: String,
    val tool: String,
    val arguments: JsonObject
)

@Serializable
data class ToolResult(
    val toolCallId: String,
    val tool: String,
    val result: JsonObject,
)
