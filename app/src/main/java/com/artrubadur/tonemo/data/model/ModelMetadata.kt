package com.artrubadur.tonemo.data.model

data class ModelMetadata(
    val modelType: ModelType,
    val modelFileName: String,
    val displayName: String,
    val uploadedAt: Long
)

enum class ModelType {
    LLM,
    VLM,
    TTS,
    STT
}
