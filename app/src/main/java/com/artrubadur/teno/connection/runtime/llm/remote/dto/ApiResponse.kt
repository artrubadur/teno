package com.artrubadur.teno.connection.runtime.llm.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val choices: List<ApiChoice> = emptyList(),
    val usage: ApiUsage
)

@Serializable
data class ApiChoice(
    @SerialName("finish_reason")
    val finishReason: String? = null,
    val message: ApiMessage
)
