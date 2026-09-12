package com.artrubadur.teno.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onOpenTools: () -> Unit = {},
    onOpenIdentity: () -> Unit = {},
    onOpenRules: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    SettingsScreenContent(
        state = state,
        onBack = onBack,
        onOpenTools = onOpenTools,
        onOpenIdentity = onOpenIdentity,
        onOpenRules = onOpenRules,
        onMaxStepsChange = viewModel::setMaxSteps,
        onUnlimitedMaxStepsChange = viewModel::setUnlimitedMaxSteps,
        onTemperatureChange = viewModel::setTemperature,
        onTopKChange = viewModel::setTopK,
        onTopPChange = viewModel::setTopP,
        onMaxTokensChange = viewModel::setMaxTokens,
        onLiteRtBackendChange = viewModel::setLiteRtBackend,
    )
}
