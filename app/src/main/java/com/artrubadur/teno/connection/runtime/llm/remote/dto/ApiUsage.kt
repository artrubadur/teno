package com.artrubadur.teno.connection.runtime.llm.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiUsage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,

    @SerialName("completion_tokens")
    val completionTokens: Int,

    @SerialName("total_tokens")
    val totalTokens: Int,

    @SerialName("prompt_tokens_details")
    val promptTokensDetails: PromptTokensDetails? = null
)

@Serializable
data class PromptTokensDetails(
    @SerialName("cached_tokens")
    val cachedTokens: Int = 0
)