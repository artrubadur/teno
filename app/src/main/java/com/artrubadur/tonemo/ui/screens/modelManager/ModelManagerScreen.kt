package com.artrubadur.tonemo.ui.screens.modelManager

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

        Button(
            onClick = { importModelLauncher.launch(arrayOf("*/*")) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Add model")
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

            models.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No models added")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = models,
                        key = { it.modelFile.name }
                    ) { model ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = model.metadata.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = model.modelFile.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = model.metadata.modelType.name,
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Button(
                                    onClick = {
                                        scope.launch {
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
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(text = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
