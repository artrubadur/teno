package com.artrubadur.teno.connection.runtime.llm.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ApiToolCall(
    val id: String,
    val type: String = "function",
    val function: ApiFunctionCall
)

@Serializable
data class ApiFunctionCall(
    val name: String,
    val arguments: String
)
