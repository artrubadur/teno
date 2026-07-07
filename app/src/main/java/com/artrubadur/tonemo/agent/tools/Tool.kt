package com.artrubadur.tonemo.agent.tools

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val ToolJson = Json { ignoreUnknownKeys = true }

interface Tool<TArgs : Any> {
    val name: String
    val description: String
    val risk: ToolRisk
    val argsSerializer: KSerializer<TArgs>
    val argsSchema: String

    private fun decodeArgs(arguments: Map<String, Any?>): TArgs {
        return ToolJson.decodeFromJsonElement(argsSerializer, arguments.toJsonObject())
    }

    suspend fun executeTyped(args: TArgs): Any?

    suspend fun execute(arguments: Map<String, Any?>): Any? {
        return executeTyped(decodeArgs(arguments))
    }
}

enum class ToolRisk {
    SAFE,
    REQUIRES_CONFIRMATION,
    FORBIDDEN
}

fun Tool<*>.toSpec() = ToolSpec(
    name = name,
    description = description,
    argsSchema = argsSchema,
)

data class ToolSpec(
    val name: String,
    val description: String,
    val argsSchema: String,
)

private fun Map<String, Any?>.toJsonObject(): JsonObject {
    return JsonObject(mapValues { (_, value) -> value.toJsonElement() })
}

private fun Any?.toJsonElement(): JsonElement {
    return when (this) {
        null -> JsonNull
        is JsonElement -> this
        is Map<*, *> -> JsonObject(
            entries.associate { (key, value) -> key.toString() to value.toJsonElement() }
        )

        is Iterable<*> -> JsonArray(map { value -> value.toJsonElement() })
        is Array<*> -> JsonArray(map { value -> value.toJsonElement() })
        is String -> JsonPrimitive(this)
        is Boolean -> JsonPrimitive(this)
        is Number -> JsonPrimitive(this)
        else -> JsonPrimitive(toString())
    }
}
