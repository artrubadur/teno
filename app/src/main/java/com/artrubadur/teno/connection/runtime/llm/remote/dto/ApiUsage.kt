package com.artrubadur.teno.connection.runtime.llm.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int = 0,

    @SerialName("completion_tokens")
    val completionTokens: Int = 0,

    @SerialName("total_tokens")
    val totalTokens: Int = 0,

    @SerialName("prompt_tokens_details")
    val promptTokensDetails: PromptTokensDetails? = null
)

@Serializable
data class PromptTokensDetails(
    @SerialName("cached_tokens")
    val cachedTokens: Int = 0
)
