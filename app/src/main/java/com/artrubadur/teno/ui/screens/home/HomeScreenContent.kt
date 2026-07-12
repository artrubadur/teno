package com.artrubadur.teno.ui.screens.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.connection.ConnectionKind
import com.artrubadur.teno.ui.components.buttons.OutlinedIconButton
import com.artrubadur.teno.ui.screens.home.components.ActiveConnectionCard
import com.artrubadur.teno.ui.screens.home.components.OverlayCard
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

            ActiveConnectionCard(
                activeConnectionName = activeConnectionName,
                activeConnectionKind = activeConnectionKind,
                onOpenConnections = onOpenConnections,
            )

            OverlayCard(
                state = state,
                onOverlayEnabledChange = onOverlayEnabledChange,
            )
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
