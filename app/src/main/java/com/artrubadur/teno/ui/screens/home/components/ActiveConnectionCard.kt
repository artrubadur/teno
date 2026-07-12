package com.artrubadur.teno.ui.screens.home.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.artrubadur.teno.connection.ConnectionKind
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
internal fun ActiveConnectionCard(
    activeConnectionName: String?,
    activeConnectionKind: ConnectionKind?,
    onOpenConnections: () -> Unit,
) {
    Card(
        onClick = onOpenConnections,
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
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = "ACTIVE CONNECTION",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (activeConnectionName != null) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = activeConnectionName ?: "No connection selected",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = when (activeConnectionKind) {
                        ConnectionKind.REMOTE -> "Remote"
                        ConnectionKind.LOCAL -> "Local"
                        else -> "Select a local or remote connection"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_arrow),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun ActiveConnectionCardPreview() {
    AppTheme {
        ActiveConnectionCard(
            activeConnectionName = null,
            activeConnectionKind = null,
            onOpenConnections = {},
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
private fun ActiveConnectionCardSelectedPreview() {
    AppTheme {
        ActiveConnectionCard(
            activeConnectionName = "Connection Name",
            activeConnectionKind = ConnectionKind.LOCAL,
            onOpenConnections = {},
        )
    }
}
