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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.ui.components.buttons.ErrorIconButton
import com.artrubadur.tonemo.ui.components.buttons.OutlinedButton
import com.artrubadur.tonemo.ui.components.buttons.OutlinedIconButton
import com.artrubadur.tonemo.ui.components.buttons.PrimaryIconButton
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
fun ChatScreen(
    onBack: () -> Unit = {},
    viewModel: ChatViewModel = koinActivityViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
        }
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
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
                if (!state.isLaunched) {
                    PrimaryIconButton(
                        iconRes = R.drawable.ic_launch,
                        onClick = { viewModel.launchActiveModel() },
                        enabled = state.isActivated && !state.isLoading,
                        contentDescription = "Launch agent",
                        modifier = Modifier.size(48.dp),
                    )
                } else {
                    ErrorIconButton(
                        iconRes = R.drawable.ic_stop,
                        contentDescription = "Terminate agent",
                        onClick = viewModel::terminateModel,
                        modifier = Modifier.size(48.dp),
                    )
                }

                ErrorIconButton(
                    iconRes = R.drawable.ic_delete,
                    contentDescription = "Reset chat",
                    onClick = viewModel::resetConversation,
                    modifier = Modifier.size(48.dp),
                    enabled = !state.isDialogEmpty && !state.isGenerating
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
                            text = state.activeConnection?.name
                                ?: "No active generation model",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (state.isLoading) {
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
                if (!state.isDialogEmpty) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = state.messages,
                            key = { it.index }
                        ) { message ->
                            MessageCard(
                                message = message,
                                onApproveConfirmation = viewModel::approveConfirmation,
                                onRejectConfirmation = viewModel::rejectConfirmation,
                                isGenerating = state.isGenerating
                            )
                        }
                    }
                }

                if (state.isDialogEmpty || !state.isLaunched) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (!state.isActivated) {
                                "Select a generation model."
                            } else if (!state.isLaunched) {
                                "Activate a model before sending messages."
                            } else {
                                "Start the conversation."
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
                    value = state.input,
                    onValueChange = viewModel::onInputChanged,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp),
                    enabled = state.isLaunched,
                    placeholder = {
                        Text(text = "Type a message", style = MaterialTheme.typography.bodyLarge)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = { viewModel.sendMessage() }
                    ),
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp)
                )

                if (state.isGenerating) {
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_stop,
                        contentDescription = "Stop generation",
                        onClick = viewModel::stopGeneration,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_send,
                        contentDescription = "Send message",
                        onClick = viewModel::sendMessage,
                        modifier = Modifier.size(48.dp),
                        enabled = state.isLaunched && state.input.isNotBlank(),
                    )
                }
            }
        }
    }
}

