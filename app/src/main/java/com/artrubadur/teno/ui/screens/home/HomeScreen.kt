package com.artrubadur.teno.ui.screens.home

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
    val activeConnection by viewModel.activeConnection.collectAsState()

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

    fun onOverlayEnabledChange(enabled: Boolean) {
        when (viewModel.setOverlayEnabled(enabled)) {
            HomeCommand.RequestOverlayPermission -> requestOverlayPermission()
            HomeCommand.RequestNotificationPermission -> requestNotificationPermission()
            else -> Unit
        }
    }

    HomeScreenContent(
        state = state,
        activeConnectionName = activeConnection?.name,
        activeConnectionKind = activeConnection?.kind,
        onOpenChat = onOpenChat,
        onOpenTools = onOpenTools,
        onOpenConnections = onOpenConnections,
        onOverlayEnabledChange = ::onOverlayEnabledChange
    )
}

