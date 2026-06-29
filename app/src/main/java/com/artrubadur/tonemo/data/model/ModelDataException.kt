package com.artrubadur.tonemo.data.model

sealed class ModelDataException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    class ModelFileNotFound(path: String) :
        ModelDataException("Model file not found: $path")

    class MissingFileName :
        ModelDataException("Model file name is empty")

    class MissingFileExtension :
        ModelDataException("Model file name does not have an extension.")

    class UnsupportedModelFileExtension(extension: String) :
        ModelDataException("Unsupported model file extension: '$extension'")
}