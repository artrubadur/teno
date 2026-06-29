package com.artrubadur.tonemo.ui.screens.connections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.tonemo.R
import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.ConnectionType
import com.artrubadur.tonemo.connection.LocalConnection
import com.artrubadur.tonemo.connection.LocalConnectionConfig
import com.artrubadur.tonemo.connection.ModelType
import com.artrubadur.tonemo.ui.components.buttons.PrimaryIconButton
import com.artrubadur.tonemo.ui.theme.TonemoTheme

@Composable
fun ConnectionCard(
    connection: Connection,
    onClick: ((model: Connection) -> Unit),
    onToggleActive: ((model: Connection) -> Unit)
) {
    Card(
        onClick = { onClick(connection) },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = connection.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = connection.type.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = connection.kind.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (connection.active) {
                        Text(
                            text = "Active",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            PrimaryIconButton(
                iconRes = if (connection.active) R.drawable.ic_close else R.drawable.ic_confirm,
                contentDescription = if (connection.active) "Activate" else "Deactivate",
                onClick = { onToggleActive(connection) },
            )
        }
    }
}

@Preview
@Composable
private fun ConnectionCardActivePreview() {
    TonemoTheme {
        ConnectionCard(
            connection = LocalConnection(
                id = "123",
                type = ConnectionType.LLM,
                name = "Model name",
                active = true,
                addedAt = 1,
                config = LocalConnectionConfig(
                    modelType = ModelType.LITERTLM,
                    fileName = "model.litertlm"
                )
            ),
            onClick = {},
            onToggleActive = {}
        )
    }
}

@Preview
@Composable
private fun ConnectionCardInactivePreview() {
    TonemoTheme {
        ConnectionCard(
            connection = LocalConnection(
                id = "123",
                type = ConnectionType.LLM,
                name = "Model name",
                active = false,
                addedAt = 1,
                config = LocalConnectionConfig(
                    modelType = ModelType.LITERTLM,
                    fileName = "model.litertlm"
                )
            ),
            onClick = {},
            onToggleActive = {}
        )
    }
}

@Preview
@Composable
private fun ConnectionCardLongNamePreview() {
    TonemoTheme {
        ConnectionCard(
            connection = LocalConnection(
                id = "123",
                type = ConnectionType.LLM,
                name = "Looooooooooooooooooooong connection name",
                active = true,
                addedAt = 1,
                config = LocalConnectionConfig(
                    modelType = ModelType.LITERTLM,
                    fileName = "model.litertlm"
                )
            ),
            onClick = {},
            onToggleActive = {}
        )
    }
}