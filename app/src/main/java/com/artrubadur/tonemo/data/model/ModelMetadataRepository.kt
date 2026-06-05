package com.artrubadur.tonemo.data.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException

class ModelMetadataRepository(
    private val context: Context
) {
    private val modelsDirectory: File
        get() = File(context.filesDir, MODELS_DIRECTORY_NAME).apply { mkdirs() }

    suspend fun createMetadata(
        modelFileName: String,
        modelType: ModelType,
        displayName: String,
        uploadedAt: Long
    ): ModelMetadata = withContext(Dispatchers.IO) {
        val metadata = ModelMetadata(
            modelType = modelType,
            modelFileName = modelFileName,
            displayName = displayName,
            uploadedAt = uploadedAt
        )

        writeMetadata(modelFileName, metadata)
        metadata
    }

    suspend fun getMetadata(modelFileName: String): ModelMetadata = withContext(Dispatchers.IO) {
        val metadataFile = readMetadata(modelFileName)
        if (!metadataFile.exists() || !metadataFile.isFile) {
            throw FileNotFoundException("Metadata file not found: ${metadataFile.absolutePath}")
        }

        metadataFile.readText()
            .takeIf { it.isNotBlank() }
            ?.let(::parseMetadata)
            ?: throw IllegalStateException("Metadata file is empty: ${metadataFile.absolutePath}")
    }

    suspend fun updateMetadata(
        modelFileName: String,
        modelType: ModelType,
        displayName: String,
        uploadedAt: Long
    ): ModelMetadata = withContext(Dispatchers.IO) {
        val metadataFile = readMetadata(modelFileName)
        if (!metadataFile.exists() || !metadataFile.isFile) {
            throw FileNotFoundException("Metadata file not found: ${metadataFile.absolutePath}")
        }

        val metadata = ModelMetadata(
            modelType = modelType,
            modelFileName = modelFileName,
            displayName = displayName,
            uploadedAt = uploadedAt
        )

        writeMetadata(modelFileName, metadata)
        metadata
    }

    suspend fun deleteMetadata(modelFileName: String): Boolean = withContext(Dispatchers.IO) {
        val metadataFile = readMetadata(modelFileName)
        !metadataFile.exists() || metadataFile.delete()
    }

    suspend fun getAllMetadata(): List<ModelMetadata> = withContext(Dispatchers.IO) {
        modelsDirectory.listFiles()
            .orEmpty()
            .filter { it.isFile && it.name.endsWith(METADATA_SUFFIX) }
            .mapNotNull { file ->
                file.readText()
                    .takeIf { it.isNotBlank() }
                    ?.let(::parseMetadata)
            }
    }

    private fun readMetadata(modelFileName: String): File {
        return File(modelsDirectory, "$modelFileName$METADATA_SUFFIX")
    }

    private fun writeMetadata(modelFileName: String, metadata: ModelMetadata) {
        readMetadata(modelFileName).writeText(
            JSONObject()
                .put("modelType", metadata.modelType.name)
                .put("modelFileName", metadata.modelFileName)
                .put("displayName", metadata.displayName)
                .put("uploadedAt", metadata.uploadedAt)
                .toString()
        )
    }

    private fun parseMetadata(json: String): ModelMetadata {
        val payload = JSONObject(json)
        return ModelMetadata(
            modelType = ModelType.valueOf(payload.getString("modelType")),
            modelFileName = payload.getString("modelFileName"),
            displayName = payload.getString("displayName"),
            uploadedAt = payload.getLong("uploadedAt")
        )
    }

    private companion object {
        const val MODELS_DIRECTORY_NAME = "models"
        const val METADATA_SUFFIX = ".meta"
    }
}
