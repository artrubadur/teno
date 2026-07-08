package com.artrubadur.tonemo.connection.runtime.llm.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ApiToolCall(
    val id: String,
    val type: String = "function",
    val function: ApiFunctionCall
)

@Serializable
internal data class ApiFunctionCall(
    val name: String,
    val arguments: String
)
