package com.artrubadur.tonemo.ui.screens.connections

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
import androidx.compose.ui.unit.dp
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.ui.components.buttons.OutlinedButton
import com.artrubadur.tonemo.ui.components.buttons.OutlinedIconButton
import com.artrubadur.tonemo.ui.components.buttons.PrimaryIconButton
import com.artrubadur.tonemo.ui.components.buttons.SecondaryIconButton
import com.artrubadur.tonemo.ui.screens.connections.dialog.ConnectionDetailsDialog
import com.artrubadur.tonemo.ui.screens.connections.dialog.ConnectionSourceDialog
import com.artrubadur.tonemo.ui.screens.connections.dialog.RemoteConnectionDialog
import org.koin.androidx.compose.koinViewModel

@Composable
fun ConnectionsScreen(
    onBack: () -> Unit = {},
    viewModel: ConnectionsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
        }
    }

    val importModelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        viewModel.addLocalDraftData(uri)
        viewModel.setDialogStage(DialogStage.DETAILS)
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
                    text = "Connections",
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
                        contentDescription = "Add connection",
                        onClick = { viewModel.setDialogStage(DialogStage.SOURCE) },
                        modifier = Modifier.size(48.dp),
                        enabled = !state.isLoading,
                    )

                    if (state.cardAction == CardAction.DELETE) {
                        SecondaryIconButton(
                            iconRes = R.drawable.ic_delete,
                            contentDescription = "Delete connection",
                            onClick = { viewModel.setCardAction(null) },
                            modifier = Modifier.size(48.dp),
                            enabled = !state.isLoading,
                        )
                    } else {
                        OutlinedIconButton(
                            iconRes = R.drawable.ic_delete,
                            contentDescription = "Delete connection",
                            onClick = { viewModel.setCardAction(CardAction.DELETE) },
                            modifier = Modifier.size(48.dp),
                            enabled = !state.isLoading and connections.isNotEmpty(),
                        )
                    }

                    if (state.cardAction == CardAction.UPDATE) {
                        SecondaryIconButton(
                            iconRes = R.drawable.ic_edit,
                            contentDescription = "Edit connection",
                            onClick = { viewModel.setCardAction(null) },
                            modifier = Modifier.size(48.dp),
                            enabled = !state.isLoading,
                        )
                    } else {
                        OutlinedIconButton(
                            iconRes = R.drawable.ic_edit,
                            contentDescription = "Edit connection",
                            onClick = { viewModel.setCardAction(CardAction.UPDATE) },
                            modifier = Modifier.size(48.dp),
                            enabled = !state.isLoading and connections.isNotEmpty(),
                        )
                    }
                }
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                connections.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No connections")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = connections,
                            key = { it.id }
                        ) { connection ->
                            ConnectionCard(
                                connection = connection,
                                onClick = viewModel::onCardClick,
                                onToggleActive = viewModel::onToggleActive
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    when (state.dialogState.dialogStage) {
        DialogStage.SOURCE -> ConnectionSourceDialog(
            onLocalSelect = { importModelLauncher.launch(arrayOf("*/*")) },
            onExternalSelect = { viewModel.setDialogStage(DialogStage.REMOTE) },
            onDismiss = { viewModel.setDialogStage(null) }
        )

        DialogStage.REMOTE -> RemoteConnectionDialog(
            initialConfig = state.dialogState.remoteConfig,
            onConfirm = { config ->
                if (!viewModel.addRemoteDraftData(config)) return@RemoteConnectionDialog
                viewModel.setDialogStage(DialogStage.DETAILS)
            },
            onDismiss = {
                viewModel.setDialogStage(null)
                viewModel.clearDraftData()
            }
        )

        DialogStage.DETAILS -> ConnectionDetailsDialog(
            initialName = state.dialogState.name,
            initialType = state.dialogState.type,
            onConfirm = { name, type ->
                if (!viewModel.addDraftDetailsData(name, type)) return@ConnectionDetailsDialog
                if (state.cardAction == CardAction.UPDATE) {
                    viewModel.updateConnection()
                } else {
                    viewModel.addConnection()
                }
                viewModel.setDialogStage(null)
            },
            onDismiss = {
                viewModel.setDialogStage(null)
                viewModel.clearDraftData()
            }
        )

        null -> Unit
    }
}