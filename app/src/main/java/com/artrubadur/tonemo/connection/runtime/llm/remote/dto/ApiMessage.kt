package com.artrubadur.tonemo.connection.runtime.llm.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiMessage(
    val role: String,
    val content: String? = null,
    @SerialName("tool_calls")
    val toolCalls: List<ApiToolCall>? = null,
    @SerialName("tool_call_id")
    val toolCallId: String? = null,
)

