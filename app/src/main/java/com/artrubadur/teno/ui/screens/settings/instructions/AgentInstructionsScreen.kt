package com.artrubadur.teno.ui.screens.settings.instructions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.artrubadur.teno.data.agent.AgentInstructionKind
import com.artrubadur.teno.ui.screens.settings.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AgentInstructionsScreen(
    kind: AgentInstructionKind,
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    AgentInstructionsScreenContent(
        title = when (kind) {
            AgentInstructionKind.IDENTITY -> "Identity"
            AgentInstructionKind.RULES -> "Rules"
        },
        instructions = when (kind) {
            AgentInstructionKind.IDENTITY -> state.identity
            AgentInstructionKind.RULES -> state.rules
        },
        onBack = onBack,
        onEnabledChange = { id, enabled ->
            viewModel.setInstructionEnabled(kind, id, enabled)
        },
        onAddInstruction = { text ->
            viewModel.addInstruction(kind, text)
        },
        onDeleteInstruction = { id ->
            viewModel.deleteInstruction(kind, id)
        },
    )
}
