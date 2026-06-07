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

enum class ActiveModelSlot {
    GENERATION,
    TTS,
    STT
}

fun ModelType.activeModelSlot(): ActiveModelSlot = when (this) {
    ModelType.LLM,
    ModelType.VLM -> ActiveModelSlot.GENERATION

    ModelType.TTS -> ActiveModelSlot.TTS
    ModelType.STT -> ActiveModelSlot.STT
}
