package com.artrubadur.teno.ui.screens.settings.instructions.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.data.agent.AgentInstructionEntry
import com.artrubadur.teno.ui.components.Switch
import com.artrubadur.teno.ui.components.buttons.ErrorIconButton

@Composable
fun InstructionItem(
    instruction: AgentInstructionEntry,
    onEnabledChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = { onEnabledChange(!instruction.enabled) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ErrorIconButton(
                iconRes = R.drawable.ic_delete,
                contentDescription = "Delete instruction",
                onClick = onDelete,
            )

            Text(
                text = instruction.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )

            Switch(
                checked = instruction.enabled,
                onCheckedChange = onEnabledChange,
            )
        }
    }
}
