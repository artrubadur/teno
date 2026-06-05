package com.artrubadur.tonemo.data.model

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

class ModelService(
    private val modelRepository: ModelRepository,
    private val modelMetadataRepository: ModelMetadataRepository
) {
    suspend fun createModelFromUri(
        context: Context,
        uri: Uri,
        modelType: ModelType,
    ): StoredModel {
        val sourceFile = createTempModelFile(context, uri)
        return createModel(
            sourcePath = sourceFile.absolutePath,
            modelType = modelType,
            displayName = sourceFile.name
        )
    }

    suspend fun createModel(
        sourcePath: String,
        modelType: ModelType,
        displayName: String,
    ): StoredModel {
        val modelFile = modelRepository.copyModel(sourcePath)
        return try {
            val metadata = modelMetadataRepository.createMetadata(
                modelFileName = modelFile.name,
                modelType = modelType,
                displayName = displayName,
                uploadedAt = System.currentTimeMillis()
            )
            StoredModel(modelFile = modelFile, metadata = metadata)
        } catch (error: Throwable) {
            modelRepository.deleteModel(modelFile.name)
            throw error
        }
    }

    suspend fun getModel(modelFileName: String): StoredModel {
        val modelFile = modelRepository.getModel(modelFileName)
        val metadata = modelMetadataRepository.getMetadata(modelFile.name)
        return StoredModel(modelFile = modelFile, metadata = metadata)
    }

    suspend fun getAllModels(): List<StoredModel> {
        return modelRepository.getAllModels().mapNotNull { modelFile ->
            val metadata = try {
                modelMetadataRepository.getMetadata(modelFile.name)
            } catch (_: FileNotFoundException) {
                modelRepository.deleteModel(modelFile.name)
                null
            }

            metadata?.let { StoredModel(modelFile = modelFile, metadata = it) }
        }
    }

    suspend fun updateModel(
        modelFileName: String,
        modelType: ModelType,
        displayName: String,
        uploadedAt: Long
    ): StoredModel {
        val modelFile = modelRepository.getModel(modelFileName)
        val metadata = modelMetadataRepository.updateMetadata(
            modelFileName = modelFileName,
            modelType = modelType,
            displayName = displayName,
            uploadedAt = uploadedAt
        )
        return StoredModel(modelFile = modelFile, metadata = metadata)
    }

    suspend fun deleteModel(modelFileName: String) {
        modelRepository.getModel(modelFileName)
        modelMetadataRepository.getMetadata(modelFileName)

        val modelDeleted = modelRepository.deleteModel(modelFileName)
        val metadataDeleted = modelMetadataRepository.deleteMetadata(modelFileName)

        if (!metadataDeleted || !modelDeleted) {
            throw IllegalStateException(
                "Failed to delete model '$modelFileName' completely. modelDeleted=$modelDeleted, metadataDeleted=$metadataDeleted"
            )

        }
    }

    private suspend fun createTempModelFile(
        context: Context,
        uri: Uri
    ): File = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val fileName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else {
                    null
                }
            }
            ?: "imported-model"

        val targetFile = File(context.cacheDir, fileName)
        resolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Unable to open selected file")

        targetFile
    }
}

data class StoredModel(
    val modelFile: File,
    val metadata: ModelMetadata
)
