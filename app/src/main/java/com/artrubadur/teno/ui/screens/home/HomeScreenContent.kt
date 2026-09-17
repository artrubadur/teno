package com.artrubadur.teno.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.connection.ConnectionKind
import com.artrubadur.teno.ui.components.AppLink
import com.artrubadur.teno.ui.components.MenuItem
import com.artrubadur.teno.ui.components.buttons.OutlinedIconButton
import com.artrubadur.teno.ui.overlays.onboarding.tourTarget
import com.artrubadur.teno.ui.screens.home.components.ActiveConnectionCard
import com.artrubadur.teno.ui.screens.home.components.OverlayCard
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun HomeScreenContent(
    state: HomeState,
    activeConnectionName: String?,
    activeConnectionKind: ConnectionKind?,
    onOpenChat: () -> Unit,
    onOpenConnections: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTools: () -> Unit,
    onOverlayEnabledChange: (Boolean) -> Unit,
    onOpenHelp: () -> Unit = {},
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
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
                            text = "Teno",
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
                        modifier = Modifier.tourTarget(
                            "chat",
                            shape = IconButtonDefaults.standardShape,
                            action = onOpenChat
                        ),
                        onClick = onOpenChat
                    )
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_settings,
                        contentDescription = "Settings",
                        modifier = Modifier.tourTarget(
                            "settings",
                            shape = IconButtonDefaults.standardShape,
                            action = onOpenSettings
                        ),
                        onClick = onOpenSettings
                    )
                    OutlinedIconButton(
                        iconRes = R.drawable.ic_computer,
                        contentDescription = "Connections",
                        modifier = Modifier.tourTarget(
                            "connections",
                            shape = IconButtonDefaults.standardShape,
                            action = onOpenConnections
                        ),
                        onClick = onOpenConnections,
                    )
                }
            }

            ActiveConnectionCard(
                activeConnectionName = activeConnectionName,
                activeConnectionKind = activeConnectionKind,
                onOpenConnections = onOpenConnections,
            )

            OverlayCard(
                state = state,
                onOverlayEnabledChange = onOverlayEnabledChange,
            )

            Box(Modifier.tourTarget("tools", action = onOpenTools)) {
                MenuItem(text = "Tools", onClick = onOpenTools)
            }
            MenuItem(text = "How it works", onClick = onOpenHelp)

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "\u00A9 2026 Sergey Suchkov \u00B7 Teno \u00B7 GPL-3.0 \u00B7 ",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )
                AppLink(
                    text = "GitHub",
                    url = "https://github.com/artrubadur/teno",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
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
        HomeScreenContent(
            state = HomeState(),
            activeConnectionName = null,
            activeConnectionKind = null,
            onOpenChat = {},
            onOpenConnections = {},
            onOpenSettings = {},
            onOpenTools = {},
            onOverlayEnabledChange = {},
        )
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
            activeConnectionKind = ConnectionKind.LOCAL,
            onOpenChat = {},
            onOpenConnections = {},
            onOpenSettings = {},
            onOpenTools = {},
            onOverlayEnabledChange = {},
        )
    }
}
