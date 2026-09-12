package com.artrubadur.teno.ui.screens.settings.instructions

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.data.agent.AgentInstructionEntry
import com.artrubadur.teno.ui.components.ScreenHeader
import com.artrubadur.teno.ui.screens.settings.instructions.components.AddInstructionDialog
import com.artrubadur.teno.ui.screens.settings.instructions.components.InstructionItem
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun AgentInstructionsScreenContent(
    title: String,
    instructions: List<AgentInstructionEntry>,
    onBack: () -> Unit,
    onEnabledChange: (String, Boolean) -> Unit,
    onAddInstruction: (String) -> Unit,
    onDeleteInstruction: (String) -> Unit,
) {
    var adding by remember { mutableStateOf(false) }

    Scaffold(contentWindowInsets = WindowInsets(0)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenHeader(
                title = title,
                onBack = onBack,
                onAdd = { adding = true },
                addContentDescription = "Add instruction",
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (instructions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No instructions",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = instructions,
                            key = { instruction -> instruction.id },
                        ) { instruction ->
                            InstructionItem(
                                instruction = instruction,
                                onEnabledChange = { enabled ->
                                    onEnabledChange(instruction.id, enabled)
                                },
                                onDelete = {
                                    onDeleteInstruction(instruction.id)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (adding) {
        AddInstructionDialog(
            onDismiss = { adding = false },
            onConfirm = { text ->
                onAddInstruction(text)
                adding = false
            },
        )
    }
}

@Preview(
    name = "Light",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AgentInstructionsScreenContentPreview() {
    AppTheme {
        AgentInstructionsScreenContent(
            title = "Rules",
            instructions = listOf(
                AgentInstructionEntry("1", "Use only exposed tools.", true),
                AgentInstructionEntry("2", "Answer only the user's current request.", false),
            ),
            onBack = {},
            onEnabledChange = { _, _ -> },
            onAddInstruction = {},
            onDeleteInstruction = {},
        )
    }
}
