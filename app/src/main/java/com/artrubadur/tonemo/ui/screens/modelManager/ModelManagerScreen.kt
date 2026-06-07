package com.artrubadur.tonemo.ui.screens.modelManager

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.data.model.ModelService
import com.artrubadur.tonemo.data.model.ModelType
import com.artrubadur.tonemo.data.model.StoredModel
import com.artrubadur.tonemo.ui.components.DropdownMenu
import com.artrubadur.tonemo.ui.components.buttons.OutlinedButton
import com.artrubadur.tonemo.ui.components.buttons.OutlinedIconButton
import com.artrubadur.tonemo.ui.components.buttons.PrimaryIconButton
import com.artrubadur.tonemo.ui.components.buttons.SecondaryIconButton
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ModelManagerScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val modelService = koinInject<ModelService>()
    val scope = rememberCoroutineScope()

    var models by remember { mutableStateOf<List<StoredModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var modelAction by remember { mutableStateOf<ModelAction?>(null) }
    var selectedModelType by remember { mutableStateOf<ModelType?>(null) }

    var pendingUri by remember { mutableStateOf<Uri?>(null) }
    var editingModel by remember { mutableStateOf<StoredModel?>(null) }
    var draftModelName by remember { mutableStateOf("") }
    var draftModelType by remember { mutableStateOf(ModelType.LLM) }

    val filteredModels = models.filter { model ->
        selectedModelType == null || model.metadata.modelType == selectedModelType
    }

    suspend fun reloadModels() {
        isLoading = true
        errorMessage = null
        try {
            models = modelService.getAllModels()
        } catch (error: Throwable) {
            errorMessage = error.message ?: "Failed to load models"
        } finally {
            isLoading = false
        }
    }

    val importModelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }
        pendingUri = uri
        editingModel = null
        draftModelName = resolveFileName(context, uri)
        draftModelType = ModelType.LLM
    }

    LaunchedEffect(Unit) {
        reloadModels()
    }

    fun deleteModel(model: StoredModel) {
        scope.launch {
            modelAction = null
            isLoading = true
            errorMessage = null
            try {
                modelService.deleteModel(model.modelFile.name)
                reloadModels()
            } catch (error: Throwable) {
                errorMessage =
                    error.message ?: "Failed to delete model"
                isLoading = false
            }
        }
    }

    fun editModel(model: StoredModel) {
        modelAction = null
        pendingUri = null
        editingModel = model
        draftModelName = model.metadata.displayName
        draftModelType = model.metadata.modelType
    }

    val onModelClick: ((StoredModel) -> Unit)? = when (modelAction) {
        ModelAction.DELETE -> ::deleteModel
        ModelAction.EDIT -> ::editModel
        else -> null
    }

    fun toggleAction(action: ModelAction) {
        modelAction = if (modelAction == action) null else action
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Models",
                style = MaterialTheme.typography.headlineMedium
            )
            OutlinedButton(onClick = onBack) {
                Text(text = "Back")
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PrimaryIconButton(
                    iconRes = R.drawable.ic_add,
                    contentDescription = "Add model",
                    onClick = { importModelLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    enabled = !isLoading,
                )

                if (modelAction == ModelAction.DELETE) {
                    SecondaryIconButton(
                        iconRes = R.drawable.ic_delete,
                        contentDescription = "Delete model",
                        onClick = { toggleAction(ModelAction.DELETE) },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        enabled = !isLoading,
                    )
                } else {
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_delete,
                        contentDescription = "Delete model",
                        onClick = { toggleAction(ModelAction.DELETE) },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        enabled = !isLoading,
                    )
                }

                if (modelAction == ModelAction.EDIT) {
                    SecondaryIconButton(
                        iconRes = R.drawable.ic_edit,
                        contentDescription = "Edit model",
                        onClick = { toggleAction(ModelAction.EDIT) },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        enabled = !isLoading,
                    )
                } else {
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_edit,
                        contentDescription = "Edit model",
                        onClick = { toggleAction(ModelAction.EDIT) },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        enabled = !isLoading,
                    )
                }
            }

            DropdownMenu(
                options = ModelType.entries.map(ModelType::name),
                selectedOption = selectedModelType?.name,
                onSelect = { selectedName ->
                    selectedModelType = (
                            ModelType.entries.firstOrNull { it.name == selectedName }
                            )
                },
                buttonModifier = Modifier.size(48.dp),
                emptyOption = "All",
            )
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            filteredModels.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No models")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = filteredModels,
                        key = { it.modelFile.name }
                    ) { model ->
                        ModelCard(
                            model = model,
                            onClick = onModelClick,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    if (pendingUri != null || editingModel != null) {
        ModelDialog(
            modelName = draftModelName,
            modelType = draftModelType,
            onNameChange = { draftModelName = it },
            onTypeChange = { draftModelType = it },
            onDismiss = {
                pendingUri = null
                editingModel = null
                draftModelName = ""
                draftModelType = ModelType.LLM
            },
            onConfirm = {
                val displayName = draftModelName.trim()
                val modelType = draftModelType
                val modelToEdit = editingModel
                val uri = pendingUri

                if (modelToEdit != null) {
                    editingModel = null
                    pendingUri = null
                    draftModelName = ""
                    draftModelType = ModelType.LLM

                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            modelService.updateModel(
                                modelFileName = modelToEdit.modelFile.name,
                                modelType = modelType,
                                displayName = displayName,
                                uploadedAt = modelToEdit.metadata.uploadedAt
                            )
                            reloadModels()
                        } catch (error: Throwable) {
                            errorMessage = error.message ?: "Failed to update model metadata"
                            isLoading = false
                        }
                    }
                } else if (uri != null) {
                    pendingUri = null
                    draftModelName = ""
                    draftModelType = ModelType.LLM

                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        try {
                            modelService.createModelFromUri(
                                context = context,
                                uri = uri,
                                modelType = modelType,
                                displayName = displayName,
                            )
                            reloadModels()
                        } catch (error: Throwable) {
                            errorMessage = error.message ?: "Failed to import model"
                            isLoading = false
                        }
                    }
                }
            },
        )
    }
}

enum class ModelAction {
    DELETE,
    EDIT,
}

private fun resolveFileName(
    context: android.content.Context,
    uri: Uri,
): String {
    val resolver = context.contentResolver
    return resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }
        ?: "Imported model"
}
