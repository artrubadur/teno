package com.artrubadur.tonemo.data.model

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException

class ModelRepository(
    private val context: Context
) {
    private val modelsDirectory: File
        get() = File(context.filesDir, MODELS_DIRECTORY_NAME).apply { mkdirs() }

    suspend fun copyModel(sourcePath: String): File = withContext(Dispatchers.IO) {
        val sourceFile = File(sourcePath)
        if (!sourceFile.exists() || !sourceFile.isFile) {
            throw FileNotFoundException("Model file not found: $sourcePath")
        }

        val nextModelIndex = findNextModelIndex()
        val extensionSuffix = sourceFile.extension
            .takeIf { it.isNotEmpty() }
            ?.let { ".${it}" }
            .orEmpty()

        if (extensionSuffix != ALLOWED_SUFFIX) {
            throw IllegalArgumentException(
                "Unsupported model file extension: '$extensionSuffix'. Expected: '$ALLOWED_SUFFIX'"
            )
        }

        val targetFile = File(modelsDirectory, "$nextModelIndex$extensionSuffix")
        sourceFile.inputStream().use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        targetFile
    }

    suspend fun getModel(fileName: String): File = withContext(Dispatchers.IO) {
        val modelFile = File(modelsDirectory, fileName)
        if (!modelFile.exists() || !modelFile.isFile) {
            throw FileNotFoundException("Model file not found: ${modelFile.absolutePath}")
        }
        modelFile
    }

    suspend fun getAllModels(): List<File> = withContext(Dispatchers.IO) {
        modelsDirectory.listFiles()
            .orEmpty()
            .filter { it.isFile && !it.name.endsWith(METADATA_SUFFIX) }
            .sortedBy { it.nameWithoutExtension.toIntOrNull() ?: Int.MAX_VALUE }
    }

    suspend fun deleteModel(fileName: String): Boolean = withContext(Dispatchers.IO) {
        val modelFile = File(modelsDirectory, fileName)
        !modelFile.exists() || modelFile.delete()
    }

    private fun findNextModelIndex(): Int {
        val maxExistingIndex = modelsDirectory.listFiles()
            .orEmpty()
            .asSequence()
            .filter { it.isFile && !it.name.endsWith(METADATA_SUFFIX) }
            .mapNotNull { it.nameWithoutExtension.toIntOrNull() }
            .maxOrNull()
            ?: 0

        return maxExistingIndex + 1
    }

    private companion object {
        const val MODELS_DIRECTORY_NAME = "models"
        const val METADATA_SUFFIX = ".meta"
        const val ALLOWED_SUFFIX = ".litertlm"
    }
}
