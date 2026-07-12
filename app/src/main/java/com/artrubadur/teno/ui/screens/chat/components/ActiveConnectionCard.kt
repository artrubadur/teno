package com.artrubadur.teno.ui.screens.chat.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.artrubadur.teno.ui.components.buttons.ErrorIconButton
import com.artrubadur.teno.ui.components.buttons.PrimaryIconButton
import com.artrubadur.teno.ui.screens.chat.ChatState
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
internal fun ActiveConnectionCard(
    state: ChatState,
    onOpenConnections: () -> Unit,
    onLaunchActiveConnection: () -> Unit,
    onTerminateConnection: () -> Unit,
) {
    Card(
        onClick = onOpenConnections,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.height(56.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.isReady) {
                ErrorIconButton(
                    iconRes = R.drawable.ic_stop,
                    contentDescription = "Terminate agent",
                    onClick = onTerminateConnection,
                    modifier = Modifier.size(40.dp)
                )
            } else if (!state.isLoading) {
                PrimaryIconButton(
                    iconRes = if (state.activeConnectionName != null) R.drawable.ic_launch else R.drawable.ic_link,
                    onClick = onLaunchActiveConnection,
                    enabled = state.isActivated,
                    contentDescription = "Activate agent",
                    modifier = Modifier.size(40.dp)
                )
            } else {
                CircularProgressIndicator(modifier = Modifier.size(40.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = state.activeConnectionName ?: "No active connection",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = when (state.activeConnectionKind) {
                        ConnectionKind.REMOTE -> "remote"
                        ConnectionKind.LOCAL -> "Local"
                        else -> "Select to start chatting"
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
            state = ChatState(),
            onOpenConnections = {},
            onLaunchActiveConnection = {},
            onTerminateConnection = {},
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
private fun ActiveConnectionCardLoadingPreview() {
    AppTheme {
        ActiveConnectionCard(
            state = ChatState(
                activeConnectionName = "Connection Name",
                activeConnectionKind = ConnectionKind.LOCAL,
                isLoading = true
            ),
            onOpenConnections = {},
            onLaunchActiveConnection = {},
            onTerminateConnection = {},
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
private fun ActiveConnectionCardReadyPreview() {
    AppTheme {
        ActiveConnectionCard(
            state = ChatState(
                activeConnectionName = "Connection Name",
                activeConnectionKind = ConnectionKind.LOCAL,
                isReady = true
            ),
            onOpenConnections = {},
            onLaunchActiveConnection = {},
            onTerminateConnection = {},
        )
    }
}
