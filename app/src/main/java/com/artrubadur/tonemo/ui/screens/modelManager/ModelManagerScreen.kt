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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.artrubadur.tonemo.data.model.ActiveModelStore
import com.artrubadur.tonemo.data.model.ModelService
import com.artrubadur.tonemo.data.model.ModelType
import com.artrubadur.tonemo.data.model.StoredModel
import com.artrubadur.tonemo.data.model.activeModelSlot
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
    val activeModelStore = koinInject<ActiveModelStore>()
    val modelService = koinInject<ModelService>()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val activeModelFileNames by activeModelStore.activeModelFileNames.collectAsState(initial = emptyMap())

    var screenState by remember { mutableStateOf(ModelManagerScreenState()) }
    var dialogState by remember { mutableStateOf(ModelDialogState()) }

    val filteredModels = screenState.models
        .filter { model ->
            screenState.selectedModelType == null || model.metadata.modelType == screenState.selectedModelType
        }
        .sortedByDescending { model ->
            activeModelFileNames[model.metadata.modelType.activeModelSlot()] == model.modelFile.name
        }

    fun isModelActive(model: StoredModel): Boolean {
        val activeFileName = activeModelFileNames[model.metadata.modelType.activeModelSlot()]
        return activeFileName == model.modelFile.name
    }

    fun showMessage(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    suspend fun reloadModels() {
        screenState = screenState.copy(isLoading = true)
        try {
            screenState = screenState.copy(models = modelService.getAllModels())
        } catch (error: Throwable) {
            showMessage(error.message ?: "Failed to load models")
        } finally {
            screenState = screenState.copy(isLoading = false)
        }
    }

    val importModelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            return@rememberLauncherForActivityResult
        }

        dialogState = ModelDialogState(
            pendingUri = uri,
            editingModel = null,
            draftModelName = resolveFileName(context, uri),
            draftModelType = ModelType.LLM
        )
    }

    LaunchedEffect(Unit) {
        reloadModels()
    }

    fun deleteModel(model: StoredModel) {
        if (isModelActive(model)) {
            showMessage("The active model cannot be deleted")
            screenState = screenState.copy(modelAction = null)
            return
        }

        scope.launch {
            screenState = screenState.copy(modelAction = null, isLoading = true)
            try {
                activeModelStore.clearActiveModelReferences(model.modelFile.name)
                modelService.deleteModel(model.modelFile.name)
                reloadModels()
            } catch (error: Throwable) {
                showMessage(error.message ?: "Failed to delete model")
                screenState = screenState.copy(isLoading = false)
            }
        }
    }

    fun editModel(model: StoredModel) {
        if (isModelActive(model)) {
            showMessage("The active model cannot be edited")
            screenState = screenState.copy(modelAction = null)
            return
        }

        screenState = screenState.copy(modelAction = null)
        dialogState = ModelDialogState(
            pendingUri = null,
            editingModel = model,
            draftModelName = model.metadata.displayName,
            draftModelType = model.metadata.modelType
        )
    }

    fun toggleActiveModel(model: StoredModel) {
        scope.launch {
            try {
                if (isModelActive(model)) {
                    activeModelStore.clearActiveModel(model.metadata.modelType)
                } else {
                    activeModelStore.setActiveModel(
                        modelType = model.metadata.modelType,
                        modelFileName = model.modelFile.name
                    )
                }
            } catch (error: Throwable) {
                showMessage(error.message ?: "Failed to update active model")
            }
        }
    }

    val onModelClick: ((StoredModel) -> Unit)? = when (screenState.modelAction) {
        ModelAction.DELETE -> ::deleteModel
        ModelAction.EDIT -> ::editModel
        else -> null
    }

    fun toggleAction(action: ModelAction) {
        screenState = screenState.copy(
            modelAction = if (screenState.modelAction == action) null else action
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        enabled = !screenState.isLoading,
                    )

                    if (screenState.modelAction == ModelAction.DELETE) {
                        SecondaryIconButton(
                            iconRes = R.drawable.ic_delete,
                            contentDescription = "Delete model",
                            onClick = { toggleAction(ModelAction.DELETE) },
                            modifier = Modifier.size(48.dp),
                            enabled = !screenState.isLoading,
                        )
                    } else {
                        OutlinedIconButton(
                            iconRes = R.drawable.ic_delete,
                            contentDescription = "Delete model",
                            onClick = { toggleAction(ModelAction.DELETE) },
                            modifier = Modifier.size(48.dp),
                            enabled = !screenState.isLoading,
                        )
                    }

                    if (screenState.modelAction == ModelAction.EDIT) {
                        SecondaryIconButton(
                            iconRes = R.drawable.ic_edit,
                            contentDescription = "Edit model",
                            onClick = { toggleAction(ModelAction.EDIT) },
                            modifier = Modifier.size(48.dp),
                            enabled = !screenState.isLoading,
                        )
                    } else {
                        OutlinedIconButton(
                            iconRes = R.drawable.ic_edit,
                            contentDescription = "Edit model",
                            onClick = { toggleAction(ModelAction.EDIT) },
                            modifier = Modifier.size(48.dp),
                            enabled = !screenState.isLoading,
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_refresh,
                        contentDescription = "Refresh models",
                        onClick = {
                            scope.launch {
                                reloadModels()
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        enabled = !screenState.isLoading,
                    )

                    DropdownMenu(
                        options = ModelType.entries.map(ModelType::name),
                        selectedOption = screenState.selectedModelType?.name,
                        onSelect = { selectedName ->
                            screenState = screenState.copy(
                                selectedModelType = ModelType.entries.firstOrNull { it.name == selectedName }
                            )
                        },
                        buttonModifier = Modifier.size(48.dp),
                        emptyOption = "All",
                    )
                }
            }

            when {
                screenState.isLoading -> {
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
                                isActive = activeModelFileNames[model.metadata.modelType.activeModelSlot()] == model.modelFile.name,
                                onClick = onModelClick,
                                onToggleActive = ::toggleActiveModel
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (dialogState.isVisible) {
        ModelDialog(
            modelName = dialogState.draftModelName,
            modelType = dialogState.draftModelType,
            onNameChange = { value ->
                dialogState = dialogState.copy(draftModelName = value)
            },
            onTypeChange = { value ->
                dialogState = dialogState.copy(draftModelType = value)
            },
            onDismiss = {
                dialogState = ModelDialogState()
            },
            onConfirm = {
                val displayName = dialogState.draftModelName.trim()
                val modelType = dialogState.draftModelType
                val modelToEdit = dialogState.editingModel
                val uri = dialogState.pendingUri

                if (modelToEdit != null) {
                    dialogState = ModelDialogState()

                    scope.launch {
                        screenState = screenState.copy(isLoading = true)
                        try {
                            val updatedModel = modelService.updateModel(
                                modelFileName = modelToEdit.modelFile.name,
                                modelType = modelType,
                                displayName = displayName,
                                uploadedAt = modelToEdit.metadata.uploadedAt
                            )
                            activeModelStore.reconcileModelTypeChange(
                                modelFileName = updatedModel.modelFile.name,
                                previousType = modelToEdit.metadata.modelType,
                                updatedType = updatedModel.metadata.modelType
                            )
                            reloadModels()
                        } catch (error: Throwable) {
                            showMessage(error.message ?: "Failed to update model metadata")
                            screenState = screenState.copy(isLoading = false)
                        }
                    }
                } else if (uri != null) {
                    dialogState = ModelDialogState()

                    scope.launch {
                        screenState = screenState.copy(isLoading = true)
                        try {
                            modelService.createModelFromUri(
                                context = context,
                                uri = uri,
                                modelType = modelType,
                                displayName = displayName,
                            )
                            reloadModels()
                        } catch (error: Throwable) {
                            showMessage(error.message ?: "Failed to import model")
                            screenState = screenState.copy(isLoading = false)
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

data class ModelManagerScreenState(
    val models: List<StoredModel> = emptyList(),
    val isLoading: Boolean = true,
    val modelAction: ModelAction? = null,
    val selectedModelType: ModelType? = null,
)

data class ModelDialogState(
    val pendingUri: Uri? = null,
    val editingModel: StoredModel? = null,
    val draftModelName: String = "",
    val draftModelType: ModelType = ModelType.LLM,
) {
    val isVisible: Boolean
        get() = pendingUri != null || editingModel != null
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
