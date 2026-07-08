package com.artrubadur.tonemo.connection.runtime.llm.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiResponse(
    val choices: List<ApiChoice> = emptyList(),
    val usage: ApiUsage
)

@Serializable
internal data class ApiChoice(
    @SerialName("finish_reason")
    val finishReason: String? = null,
    val message: ApiMessage
)
