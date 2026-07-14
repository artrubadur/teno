package com.artrubadur.teno.connection.runtime.llm.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ApiTool(
    val type: String = "function",
    val function: ApiFunction
)

@Serializable
data class ApiFunction(
    val name: String,
    val description: String? = null,
    val parameters: JsonElement
)
