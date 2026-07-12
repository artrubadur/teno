package com.artrubadur.teno.ui.screens.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.artrubadur.teno.R
import com.artrubadur.teno.ui.components.buttons.OutlinedIconButton
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onOpenChat: () -> Unit = {},
    onOpenConnections: () -> Unit = {},
    onOpenTools: () -> Unit = {},
    viewModel: HomeViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        viewModel.retryEnableOverlay()
    }

    fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.retryEnableOverlay()
        }
    }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        when (viewModel.retryEnableOverlay()) {
            HomeCommand.RequestNotificationPermission -> requestNotificationPermission()
            else -> Unit
        }
    }

    fun requestOverlayPermission() {
        overlayPermissionLauncher.launch(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:${context.packageName}".toUri()
            )
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
                text = "Home",
                style = MaterialTheme.typography.headlineMedium
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedIconButton(
                    iconRes = R.drawable.ic_chat,
                    contentDescription = "Chat",
                    onClick = onOpenChat
                )
                OutlinedIconButton(
                    iconRes = R.drawable.ic_tools,
                    contentDescription = "Connections",
                    onClick = onOpenTools
                )
                OutlinedIconButton(
                    iconRes = R.drawable.ic_storage,
                    contentDescription = "Tools",
                    onClick = onOpenConnections
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "Overlay service")
                Text(
                    text = overlayStatusText(state),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = state.overlayEnabled,
                onCheckedChange = { enabled ->
                    when (viewModel.setOverlayEnabled(enabled)) {
                        HomeCommand.RequestOverlayPermission -> requestOverlayPermission()
                        HomeCommand.RequestNotificationPermission -> requestNotificationPermission()
                        else -> Unit
                    }
                }
            )
        }
    }
}

private fun overlayStatusText(state: HomeState): String {
    return when {
        !state.overlayPermissionGranted -> "Overlay permission is required"
        !state.notificationPermissionGranted -> "Notification permission is required"
        state.overlayChanging && state.overlayEnabled -> "Stopping overlay..."
        state.overlayChanging -> "Starting overlay..."
        state.overlayEnabled -> "Overlay is enabled"
        else -> "Overlay is disabled"
    }
}
