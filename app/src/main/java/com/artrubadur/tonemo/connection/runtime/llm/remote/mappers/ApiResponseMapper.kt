package com.artrubadur.tonemo.connection.runtime.llm.remote.mappers

import com.artrubadur.tonemo.agent.tools.ToolCall
import com.artrubadur.tonemo.connection.runtime.llm.LlmException
import com.artrubadur.tonemo.connection.runtime.llm.LlmResponse
import com.artrubadur.tonemo.connection.runtime.llm.remote.dto.ApiResponse
import com.artrubadur.tonemo.connection.runtime.llm.remote.dto.ApiToolCall
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

internal fun ApiResponse.toLlmResponse(): LlmResponse {
    val choice = choices.firstOrNull()
        ?: throw LlmException.InvalidResponse(
            message = "Model response does not contain any choices.",
            cause = null
        )

    val message = choice.message

    return when {
        choice.finishReason == "tool_calls" || !message.toolCalls.isNullOrEmpty() -> {
            LlmResponse.ToolCalls(
                calls = message.toolCalls.orEmpty().map { it.toToolCall() }
            )
        }

        else -> {
            LlmResponse.Final(
                content = message.content.orEmpty()
            )
        }
    }
}

private fun ApiToolCall.toToolCall(): ToolCall {
    val args = runCatching {
        Json.parseToJsonElement(function.arguments).jsonObject
    }.getOrElse { cause ->
        throw LlmException.InvalidResponse(
            message = "Model returned invalid tool call arguments. Expected a JSON object in function.arguments.",
            cause = cause
        )
    }

    return ToolCall(
        id = id,
        tool = function.name,
        arguments = args
    )
}