package com.artrubadur.tonemo.connection.runtime.llm

sealed class LlmException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    class UnsupportedConnectionType :
        LlmException("Unsupported connection type")

    class RuntimeIsNotLoaded :
        LlmException("LLM runtime is not loaded")

    class ModelFileNotFound(path: String) :
        LlmException("LLM model file not found `$path`")

    class LoadFailed(path: String, cause: Throwable?) :
        LlmException("Failed to load LLM model `$path`", cause)

    class GenerationFailed(cause: Throwable?) :
        LlmException("LLM generation failed", cause)
}