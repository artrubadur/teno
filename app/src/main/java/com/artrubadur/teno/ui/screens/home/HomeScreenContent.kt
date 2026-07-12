package com.artrubadur.teno.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.connection.ConnectionKind
import com.artrubadur.teno.ui.components.Switch
import com.artrubadur.teno.ui.components.buttons.OutlinedIconButton
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
internal fun HomeScreenContent(
    state: HomeState = HomeState(),
    activeConnectionName: String? = null,
    activeConnectionKind: ConnectionKind? = null,
    onOpenChat: () -> Unit = {},
    onOpenConnections: () -> Unit = {},
    onOpenTools: () -> Unit = {},
    onOverlayEnabledChange: (Boolean) -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row {
                        Text(
                            text = "Temo",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            text = ".",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                    Text(
                        text = "Yours, by design.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_chat,
                        contentDescription = "Chat",
                        onClick = onOpenChat
                    )
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_tools,
                        contentDescription = "Tools",
                        onClick = onOpenTools
                    )
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_link,
                        contentDescription = "Connections",
                        onClick = onOpenConnections
                    )
                }
            }

            Card(
                onClick = onOpenConnections,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
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
                            text = activeConnectionName ?: "No active connection",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = when (activeConnectionKind) {
                                ConnectionKind.REMOTE -> "remote"
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 16.dp),
                        ) {
                            Text(
                                text = "Overlay",
                                style = MaterialTheme.typography.titleLarge,
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "Keep Teno available from the notification.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Switch(
                            checked = state.overlayEnabled,
                            onCheckedChange = onOverlayEnabledChange
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.size(24.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (state.overlayEnabled) Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .blur(
                                        radius = 6.dp,
                                        edgeTreatment = BlurredEdgeTreatment.Unbounded,
                                    )
                                    .background(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                        shape = CircleShape,
                                    ),
                            )

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (state.overlayEnabled) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        shape = CircleShape,
                                    ),
                            )
                        }

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = overlayStatusText(state),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (state.overlayEnabled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun overlayStatusText(state: HomeState): String {
    return when {
        !state.overlayPermissionGranted -> "Overlay permission is required"
        !state.notificationPermissionGranted -> "Notification permission is required"
        state.overlayChanging && state.overlayEnabled -> "Stopping overlay..."
        state.overlayChanging -> "Starting overlay..."
        state.overlayEnabled -> "Overlay is active"
        else -> "Overlay is inactive"
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
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreenContent()
    }
}

@Preview(
    name = "Light Enabled",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark Enabled",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HomeScreenEnabledPreview() {
    AppTheme {
        HomeScreenContent(
            state = HomeState(
                overlayPermissionGranted = true,
                notificationPermissionGranted = true,
                overlayEnabled = true
            ),
            activeConnectionName = "Connection Name",
            activeConnectionKind = ConnectionKind.LOCAL
        )
    }
}