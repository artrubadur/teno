package com.artrubadur.tonemo.connection.runtime.llm

import com.artrubadur.tonemo.connection.Connection


interface LlmRuntime : AutoCloseable {

    val isLoaded: Boolean

    suspend fun load(connection: Connection)

    suspend fun generate(
        prompt: String,
        options: LlmGenerationOptions = LlmGenerationOptions()
    ): String

    fun stopGeneration()
}

data class LlmGenerationOptions(
    val systemPrompt: String = "You are a assistant.",
    val temperature: Double = 0.7,
    val topK: Int = 40,
    val topP: Double = 0.95,
)

sealed class LlmRuntimeException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    class UnsupportedConnectionType :
        LlmRuntimeException("Unsupported connection type")

    class RuntimeIsNotLoaded :
        LlmRuntimeException("LLM runtime is not loaded")

    class ModelFileNotFound(path: String) :
        LlmRuntimeException("LLM model file not found `$path`")

    class LoadFailed(path: String, cause: Throwable) :
        LlmRuntimeException("Failed to load LLM model `$path`", cause)

    class GenerationFailed(cause: Throwable) :
        LlmRuntimeException("LLM generation failed", cause)
}
