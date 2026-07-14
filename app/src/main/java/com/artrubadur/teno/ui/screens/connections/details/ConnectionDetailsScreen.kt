package com.artrubadur.teno.ui.screens.connections.details

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.artrubadur.teno.ui.screens.connections.ConnectionsViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConnectionDetailsScreen(
    onBack: () -> Unit = {},
    connectionId: String? = null,
    viewModel: ConnectionsViewModel = koinViewModel()
) {

    val state by viewModel.state.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val connection = connections.firstOrNull({ it.id == connectionId })
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
        }
    }

    ConnectionDetailsScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        connection = connection,
        onBack = onBack,
        onToggleActive = viewModel::onToggleActive,
        onEditConnection = viewModel::openEditDialog,
        onDeleteConnection = viewModel::openDeleteDialog,
        onDismissDialog = viewModel::dismissDialog,
        onLocalConfirm = viewModel::submitLocalConnection,
        onRemoteConfirm = viewModel::submitRemoteConnection,
        onDeleteConfirm = { viewModel.deleteSelectedConnection(onBack) },
    )
}
