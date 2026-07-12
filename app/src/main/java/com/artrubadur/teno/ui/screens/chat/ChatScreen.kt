package com.artrubadur.teno.ui.screens.chat

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
fun ChatScreen(
    onBack: () -> Unit = {},
    onOpenConnections: () -> Unit = {},
    viewModel: ChatViewModel = koinActivityViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
        }
    }

    ChatScreenContent(
        snackbarHostState = snackbarHostState,
        state = state,
        onBack = onBack,
        onOpenConnections = onOpenConnections,
        onLaunchActiveConnection = viewModel::launchActiveConnection,
        onTerminateConnection = viewModel::terminateConnection,
        onResetConversation = viewModel::resetConversation,
        onApproveConfirmation = viewModel::approveConfirmation,
        onRejectConfirmation = viewModel::rejectConfirmation,
        onInputChanged = viewModel::onInputChanged,
        onSendMessage = viewModel::sendMessage,
        onStopWork = viewModel::stopWork,
    )
}

