package com.artrubadur.teno.connection.runtime.llm

import com.artrubadur.teno.connection.runtime.llm.local.LiteRtBackendOption

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

    class LoadingFailed(
        path: String,
        backend: LiteRtBackendOption? = null,
        cause: Throwable?
    ) : LlmException(
        message = buildString {
            append("Failed to load LLM model `$path`")
            if (backend != null) {
                append(" with ${backend.title} backend. ")
                append(backend.loadingFailureHint())
            }
        },
        cause = cause,
    )

    class GenerationFailed(message: String, cause: Throwable?) :
        LlmException("LLM generation failed: $message", cause)

    class InvalidResponse(message: String, cause: Throwable?) :
        LlmException("Invalid response: $message", cause)
}

private fun LiteRtBackendOption.loadingFailureHint(): String =
    when (this) {
        LiteRtBackendOption.CPU ->
            "The model file may be incompatible or corrupted."

        LiteRtBackendOption.GPU ->
            "GPU acceleration is not available for this model or device. Select CPU or another supported backend in Settings."

        LiteRtBackendOption.NPU ->
            "NPU acceleration is not available for this model or device. Check the supported SoC list in Settings or select CPU/GPU."
    }
