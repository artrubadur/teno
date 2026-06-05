package com.artrubadur.tonemo.ui.screens.modelManager

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.data.model.ModelService
import com.artrubadur.tonemo.data.model.ModelType
import com.artrubadur.tonemo.data.model.StoredModel
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

        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                modelService.createModelFromUri(
                    context = context,
                    uri = uri,
                    modelType = ModelType.LLM,
                )
                reloadModels()
            } catch (error: Throwable) {
                errorMessage = error.message ?: "Failed to import model"
                isLoading = false
            }
        }
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
                IconButton(
                    onClick = { importModelLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    enabled = !isLoading,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = "Add model",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                IconButton(
                    onClick = { toggleAction(ModelAction.DELETE) },
                    modifier = Modifier
                        .background(
                            color = if (modelAction == ModelAction.DELETE) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            shape = CircleShape
                        ),
                    enabled = !isLoading,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Delete model",
                        tint = if (modelAction == ModelAction.DELETE) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                }

                IconButton(
                    onClick = { toggleAction(ModelAction.EDIT) },
                    modifier = Modifier
                        .background(
                            color = if (modelAction == ModelAction.EDIT) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            shape = CircleShape
                        ),
                    enabled = !isLoading,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Delete model",
                        tint = if (modelAction == ModelAction.EDIT) {
                            MaterialTheme.colorScheme.onSecondary
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                }
            }

            ModelTypeDropdown(
                selectedModelType = selectedModelType,
                onModelTypeSelect = { selectedModelType = it }
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
}

enum class ModelAction {
    DELETE,
    EDIT,
}
