package com.artrubadur.teno.ui.screens.connections.details.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.artrubadur.teno.connection.ConnectionKind
import com.artrubadur.teno.connection.ConnectionType
import com.artrubadur.teno.connection.RemoteConnection
import com.artrubadur.teno.connection.RemoteConnectionConfig
import com.artrubadur.teno.ui.components.Switch
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
internal fun ConnectionDetailsCard(
    connection: Connection,
    onToggleActive: (Connection) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "DETAILS",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )

        Card(
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_star),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "STATUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )

                    Text(
                        text = if (connection.active) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                Switch(
                    checked = connection.active,
                    onCheckedChange = { onToggleActive(connection) },
                    modifier = Modifier.height(40.dp)
                )
            }
        }

        Card(
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                DetailRow(
                    iconRes = when (connection.kind) {
                        ConnectionKind.LOCAL -> R.drawable.ic_mobile
                        ConnectionKind.REMOTE -> R.drawable.ic_cloud
                    },
                    label = "CONNECTION KIND",
                    value = connection.kind.name.lowercase().replaceFirstChar { it.uppercase() },
                )

                if (connection is RemoteConnection) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    DetailRow(
                        iconRes = R.drawable.ic_link,
                        label = "URL",
                        value = connection.config.baseUrl.removePrefix("https://"),
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    DetailRow(
                        iconRes = R.drawable.ic_cube,
                        label = "MODEL",
                        value = connection.config.model,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    iconRes: Int,
    label: String,
    value: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
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
private fun ConnectionDetailsCardPreview() {
    AppTheme {
        ConnectionDetailsCard(
            connection = RemoteConnection(
                id = "remote-preview",
                type = ConnectionType.LLM,
                name = "Remote API",
                active = true,
                addedAt = 3L,
                config = RemoteConnectionConfig(
                    baseUrl = "https://api.example.com",
                    model = "model",
                    apiKey = "preview",
                ),
            ),
            onToggleActive = {}
        )
    }
}
