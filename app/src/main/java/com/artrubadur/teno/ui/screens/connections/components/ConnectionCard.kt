package com.artrubadur.teno.ui.screens.connections.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.connection.Connection
import com.artrubadur.teno.connection.ConnectionType
import com.artrubadur.teno.connection.LocalConnection
import com.artrubadur.teno.connection.LocalConnectionConfig
import com.artrubadur.teno.connection.ModelType
import com.artrubadur.teno.ui.components.Switch
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun ConnectionCard(
    connection: Connection,
    onOpenConnection: (String) -> Unit,
    onToggleActive: (Connection) -> Unit
) {
    Card(
        onClick = { onOpenConnection(connection.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = connection.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )

            Switch(
                checked = connection.active,
                onCheckedChange = { onToggleActive(connection) },
            )

            Icon(
                painter = painterResource(R.drawable.ic_kb_arrow),
                contentDescription = null,
                tint = if (connection.active) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ConnectionCardActivePreview() {
    AppTheme {
        ConnectionCard(
            connection = LocalConnection(
                id = "123",
                type = ConnectionType.LLM,
                name = "Connection name",
                active = true,
                addedAt = 1,
                config = LocalConnectionConfig(
                    modelType = ModelType.LITERTLM,
                    fileName = "model.litertlm"
                )
            ),
            onOpenConnection = {},
            onToggleActive = {}
        )
    }
}

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ConnectionCardInactivePreview() {
    AppTheme {
        ConnectionCard(
            connection = LocalConnection(
                id = "123",
                type = ConnectionType.LLM,
                name = "Connection name",
                active = false,
                addedAt = 1,
                config = LocalConnectionConfig(
                    modelType = ModelType.LITERTLM,
                    fileName = "model.litertlm"
                )
            ),
            onOpenConnection = {},
            onToggleActive = {}
        )
    }
}
