package com.artrubadur.teno.ui.screens.tools

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.artrubadur.teno.agent.tools.PermissionGrantType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ToolsScreen(
    onBack: () -> Unit = {},
    viewModel: ToolsViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by viewModel.state.collectAsState()

    val intentPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            viewModel.onPermissionResult()
        }

    val runtimePermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            viewModel.onPermissionResult()
        }

    LaunchedEffect(viewModel) {
        viewModel.permissions.collect { permission ->

            when (permission.grantType) {

                PermissionGrantType.INTENT -> {
                    permission.grantIntent(context)?.let {
                        intentPermissionLauncher.launch(it)
                    }
                }

                PermissionGrantType.RUNTIME -> {
                    permission.manifestPermission()?.let {
                        runtimePermissionLauncher.launch(it)
                    }
                }

                PermissionGrantType.NONE -> {
                    viewModel.onPermissionResult()
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner, viewModel) {
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

    ToolsScreenContent(
        state = state,
        onBack = onBack,
        setToolEnabled = viewModel::setToolEnabled,
        grantPermission = viewModel::grantPermission
    )
}
