package com.artrubadur.tonemo.data.model

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ModelRepository(
    private val context: Context
) {
    private val modelsDirectory: File
        get() = File(context.filesDir, DIRECTORY_NAME).apply { mkdirs() }

    suspend fun importModelFromUri(
        uri: Uri,
        fileName: String
    ): File = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw ModelDataException.ModelFileNotFound(uri.toString())

        val targetFile = File(modelsDirectory, fileName)

        inputStream.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        targetFile
    }

    suspend fun getModelByFileName(fileName: String): File = withContext(Dispatchers.IO) {
        val modelFile = File(modelsDirectory, fileName)
        if (!modelFile.exists() || !modelFile.isFile) {
            throw ModelDataException.ModelFileNotFound(modelFile.absolutePath)
        }
        modelFile
    }

    suspend fun deleteModelById(id: String): Boolean = withContext(Dispatchers.IO) {
        val modelFile = File(modelsDirectory, id)
        !modelFile.exists() || modelFile.delete()
    }

    private companion object {
        const val DIRECTORY_NAME = "models"
    }
}



