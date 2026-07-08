package com.artrubadur.tonemo.connection.runtime.llm

import com.artrubadur.tonemo.connection.Connection


interface LlmRuntime : AutoCloseable {

    val isReady: Boolean

    suspend fun connect(connection: Connection)

    suspend fun generate(
        request: LlmRequest
    ): LlmResponse

    fun stopGeneration()
}

data class LlmOptions(
    val temperature: Double = 0.1,
    val topK: Int = 40,
    val topP: Double = 0.9,
    val maxTokens: Int = 500
)

