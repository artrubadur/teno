package com.artrubadur.tonemo.agent.tools

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface AgentTool<TArgs : Any> {
    val name: String
    val description: String
    val risk: ToolRisk
    val argsSerializer: KSerializer<TArgs>
    val argsSchema: String

    suspend fun execute(args: TArgs): ToolResult

    fun decodeArgs(argsJson: String?): TArgs {
        return Json.decodeFromString(argsSerializer, argsJson ?: "{}")
    }

    @Suppress("UNCHECKED_CAST")
    suspend fun executeUntyped(args: Any): ToolResult {
        return execute(args as TArgs)
    }
}

@Serializable
class NoToolArgs

fun AgentTool<*>.toDescriptor() = ToolDescriptor(
    name = name,
    description = description,
    argsSchema = argsSchema,
    risk = risk
)

data class ToolDescriptor(
    val name: String,
    val description: String,
    val argsSchema: String,
    val risk: ToolRisk
)

enum class ToolRisk {
    SAFE,
    REQUIRES_CONFIRMATION,
    FORBIDDEN
}

sealed interface ToolResult {
    data class Success(val resultJson: String) : ToolResult
    data class Error(val message: String) : ToolResult
}
