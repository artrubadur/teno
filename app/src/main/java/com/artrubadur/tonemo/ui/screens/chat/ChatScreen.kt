package com.artrubadur.tonemo.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.data.model.ActiveModelStore
import com.artrubadur.tonemo.data.model.ModelService
import com.artrubadur.tonemo.data.model.ModelType
import com.artrubadur.tonemo.ui.components.buttons.ErrorIconButton
import com.artrubadur.tonemo.ui.components.buttons.OutlinedButton
import com.artrubadur.tonemo.ui.components.buttons.OutlinedIconButton
import com.artrubadur.tonemo.ui.components.buttons.PrimaryIconButton
import org.koin.compose.koinInject

@Composable
fun ChatScreen(
    onBack: () -> Unit = {}
) {
    val activeModelStore = koinInject<ActiveModelStore>()
    val chatController = koinInject<DialogController>()
    val modelService = koinInject<ModelService>()

    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val dialogState by chatController.state.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }

    val activeModelFileNames by activeModelStore.activeModelFileNames.collectAsState(initial = emptyMap())
    val activeModelFileName = activeModelFileNames[ModelType.LLM]
    val activeModelDisplayName by produceState(
        initialValue = activeModelFileName,
        key1 = activeModelFileName
    ) {
        value = activeModelFileName?.let { fileName ->
            runCatching {
                modelService.getModel(fileName).metadata.displayName
            }.getOrDefault(fileName)
        }
    }

    LaunchedEffect(chatController) {
        chatController.events.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(dialogState.messages.size) {
        if (dialogState.messages.isNotEmpty()) {
            listState.animateScrollToItem(dialogState.messages.lastIndex)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        val isModelReady = dialogState.loadedModelFileName != null &&
                dialogState.loadedModelFileName == activeModelFileName

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
                    text = "Chat",
                    style = MaterialTheme.typography.headlineMedium
                )
                OutlinedButton(onClick = onBack) {
                    Text(text = "Back")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (dialogState.loadedModelFileName == null && !dialogState.isGenerating) {
                    PrimaryIconButton(
                        iconRes = R.drawable.ic_launch,
                        onClick = { chatController.launchModel(activeModelFileName) },
                        enabled = activeModelFileName != null && !dialogState.isLoadingModel,
                        contentDescription = "Launch agent",
                        modifier = Modifier.size(48.dp),
                    )
                } else {
                    ErrorIconButton(
                        iconRes = R.drawable.ic_stop,
                        contentDescription = "Terminate agent",
                        onClick = chatController::terminateModel,
                        modifier = Modifier.size(48.dp),
                    )
                }

                ErrorIconButton(
                    iconRes = R.drawable.ic_delete,
                    contentDescription = "Reset chat",
                    onClick = chatController::resetConversation,
                    modifier = Modifier.size(48.dp),
                    enabled = dialogState.messages.isNotEmpty() && !dialogState.isGenerating
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    modifier = Modifier.height(48.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp, 0.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = activeModelDisplayName
                                ?: "No active generation model",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (dialogState.isLoadingModel) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (dialogState.messages.isNotEmpty()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = dialogState.messages,
                            key = { it.id }
                        ) { message ->
                            MessageCard(
                                message = message,
                                onApproveConfirmation = chatController::approveConfirmation,
                                onRejectConfirmation = chatController::rejectConfirmation,
                                confirmationActionsEnabled = !dialogState.isGenerating
                            )
                        }
                    }
                }

                if (dialogState.messages.isEmpty() || !isModelReady) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (activeModelDisplayName == null) {
                                "Select a generation model."
                            } else if (isModelReady) {
                                "Start the conversation."
                            } else {
                                "Activate a model before sending messages."
                            },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    enabled = isModelReady && !dialogState.isLoadingModel,
                    placeholder = {
                        Text(text = "Type a message", style = MaterialTheme.typography.bodyLarge)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (!dialogState.isGenerating) {
                                chatController.sendMessage(input)
                                input = ""
                            }
                        }
                    ),
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )

                if (dialogState.isGenerating) {
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_stop,
                        contentDescription = "Stop generation",
                        onClick = chatController::stopGeneration,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_send,
                        contentDescription = "Send message",
                        onClick = { chatController.sendMessage(input); input = "" },
                        modifier = Modifier.size(48.dp),
                        enabled = isModelReady && input.isNotBlank(),
                    )
                }
            }
        }
    }
}

