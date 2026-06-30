package com.artrubadur.tonemo.data.model

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.artrubadur.tonemo.connection.ModelType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ModelStore(
    private val modelRepository: ModelRepository,
    private val context: Context
) {
    suspend fun importModel(
        fileName: String,
        uri: Uri,
    ): File = withContext(Dispatchers.IO) {
        modelRepository.importModelFromUri(
            uri = uri,
            fileName = fileName,
        )
    }

    suspend fun getModel(fileName: String): File {
        return modelRepository.getModelByFileName(fileName)
    }

    suspend fun deleteModel(id: String) {
        modelRepository.deleteModelById(id)
    }

    suspend fun inspectModel(uri: Uri): ModelImportInfo = withContext(Dispatchers.IO) {
        val fileName = getFileName(uri)
            ?: throw ModelDataException.MissingFileName()

        val extension = fileName
            .substringAfterLast('.', missingDelimiterValue = "")
            .takeIf { it.isNotEmpty() }
            ?.lowercase()
            ?: throw ModelDataException.MissingFileExtension()

        val modelType = extension.toModelType()

        ModelImportInfo(
            fileName = fileName,
            extension = extension,
            modelType = modelType
        )
    }

    private fun getFileName(
        uri: Uri
    ): String? {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    return cursor.getString(nameIndex)
                }
            }
        }

        if (uri.scheme == ContentResolver.SCHEME_FILE) {
            return uri.path?.let { File(it).name }
        }

        return null
    }

    private fun String.toModelType(): ModelType {
        return when (this) {
            "litertlm" -> ModelType.LITERTLM
            else -> throw ModelDataException.UnsupportedModelFileExtension(this)
        }
    }
}

data class ModelImportInfo(
    val fileName: String,
    val extension: String,
    val modelType: ModelType,
)