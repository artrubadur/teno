package com.artrubadur.tonemo.agent.tools

import kotlinx.schema.generator.json.serialization.SerializationClassJsonSchemaGenerator
import kotlinx.schema.json.JsonSchema
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

private val ToolJson = Json { ignoreUnknownKeys = true }

interface Tool<TArgs : Any> {
    val name: String
    val title: String
    val description: String
    val risk: ToolRisk
    val requiredPermissions: Set<ToolPermission>
        get() = emptySet()
    val argsSerializer: KSerializer<TArgs>
    val argsSchema: JsonSchema
        get() = SerializationClassJsonSchemaGenerator.Default
            .generateSchema(argsSerializer.descriptor)

    suspend fun executeTyped(args: TArgs): JsonObject

    suspend fun execute(arguments: JsonObject): JsonObject {
        val decodedArguments = ToolJson.decodeFromJsonElement(argsSerializer, arguments)
        return executeTyped(decodedArguments)
    }
}

enum class ToolRisk {
    SAFE,
    REQUIRES_CONFIRMATION,
    FORBIDDEN
}

fun Tool<*>.toSpec() = ToolSpec(
    name = name,
    title = title,
    description = description,
    argsSchema = argsSchema,
    requiredPermissions = requiredPermissions,
)

data class ToolSpec(
    val name: String,
    val title: String,
    val description: String,
    val argsSchema: JsonSchema,
    val requiredPermissions: Set<ToolPermission> = emptySet(),
)
