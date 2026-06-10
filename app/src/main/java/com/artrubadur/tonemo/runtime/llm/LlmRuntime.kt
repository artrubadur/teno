package com.artrubadur.tonemo.runtime.llm

import kotlinx.coroutines.flow.Flow


interface LlmRuntime : AutoCloseable {

    val isLoaded: Boolean

    suspend fun load(modelPath: String)

    suspend fun generate(
        prompt: String,
        options: LlmGenerationOptions = LlmGenerationOptions()
    ): String

    fun generateStream(
        prompt: String,
        options: LlmGenerationOptions = LlmGenerationOptions()
    ): Flow<String>

    suspend fun resetConversation()

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

    class ModelNotLoaded :
        LlmRuntimeException("LLM model is not loaded")

    class ModelFileNotFound(path: String) :
        LlmRuntimeException("LLM model file not found: $path")

    class LoadFailed(path: String, cause: Throwable) :
        LlmRuntimeException("Failed to load LLM model: $path", cause)

    class GenerationFailed(cause: Throwable) :
        LlmRuntimeException("LLM generation failed", cause)
}