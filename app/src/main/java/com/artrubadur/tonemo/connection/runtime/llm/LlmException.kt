package com.artrubadur.tonemo.connection.runtime.llm

sealed class LlmException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    class UnsupportedConnectionType :
        LlmException("Unsupported connection type")

    class RuntimeIsNotReady :
        LlmException("LLM runtime is not ready")

    class ModelFileNotFound(path: String) :
        LlmException("LLM model file not found `$path`")

    class LoadingFailed(path: String, cause: Throwable?) :
        LlmException("Failed to load LLM model `$path`", cause)

    class GenerationFailed(message: String, cause: Throwable?) :
        LlmException("LLM generation failed: $message", cause)

    class InvalidResponse(message: String, cause: Throwable?) :
        LlmException("Invalid response: $message", cause)
}