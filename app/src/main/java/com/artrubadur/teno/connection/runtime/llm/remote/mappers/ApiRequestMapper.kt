package com.artrubadur.teno.connection.runtime.llm.remote.mappers

import com.artrubadur.teno.agent.tools.ToolCall
import com.artrubadur.teno.agent.tools.ToolSpec
import com.artrubadur.teno.agent.tools.toJsonObject
import com.artrubadur.teno.connection.runtime.llm.AgentInstructions
import com.artrubadur.teno.connection.runtime.llm.LlmMessage
import com.artrubadur.teno.connection.runtime.llm.LlmRequest
import com.artrubadur.teno.connection.runtime.llm.remote.dto.ApiFunction
import com.artrubadur.teno.connection.runtime.llm.remote.dto.ApiFunctionCall
import com.artrubadur.teno.connection.runtime.llm.remote.dto.ApiMessage
import com.artrubadur.teno.connection.runtime.llm.remote.dto.ApiRequest
import com.artrubadur.teno.connection.runtime.llm.remote.dto.ApiTool
import com.artrubadur.teno.connection.runtime.llm.remote.dto.ApiToolCall

fun LlmRequest.toApiRequest(model: String): ApiRequest =
    ApiRequest(
        model = model,
        messages = listOf(instructions.toSystemMessage()) +
                messages.map { it.toApiMessage() },
        tools = tools.map { it.toApiTool() },
        temperature = options.temperature,
        topP = options.topP
    )


private fun AgentInstructions.toSystemMessage(): ApiMessage =
    ApiMessage(
        role = "system",
        content = this.render()
    )

private fun LlmMessage.toApiMessage(): ApiMessage =
    when (this) {
        is LlmMessage.User -> ApiMessage(
            role = "user",
            content = content
        )

        is LlmMessage.AssistantFinal -> ApiMessage(
            role = "assistant",
            content = content
        )

        is LlmMessage.AssistantToolCalls -> ApiMessage(
            role = "assistant",
            toolCalls = calls.map { it.toApiToolCall() }
        )

        is LlmMessage.Tool -> ApiMessage(
            role = "tool",
            content = result.result.toString(),
            toolCallId = result.toolCallId
        )
    }

private fun ToolCall.toApiToolCall(): ApiToolCall =
    ApiToolCall(
        id = id,
        function = ApiFunctionCall(
            name = tool,
            arguments = arguments.toString()
        )
    )

private fun ToolSpec.toApiTool(): ApiTool =
    ApiTool(
        function = ApiFunction(
            name = name,
            description = description,
            parameters = argsSchema.toJsonObject()
        )
    )