package com.artrubadur.teno.connection.runtime.llm.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ApiRequest(
    val model: String,
    val messages: List<ApiMessage>,
    val tools: List<ApiTool>? = null,
    val temperature: Double? = null,
    @SerialName("top_p")
    val topP: Double? = null
)




