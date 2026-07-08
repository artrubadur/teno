package com.artrubadur.tonemo.connection.runtime.llm.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
internal data class ApiTool(
    val type: String = "function",
    val function: ApiFunction
)

@Serializable
internal data class ApiFunction(
    val name: String,
    val description: String? = null,
    val parameters: JsonElement
)
