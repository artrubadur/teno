package com.artrubadur.teno.ui.screens.home.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.ui.components.Switch
import com.artrubadur.teno.ui.screens.home.HomeState
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
internal fun OverlayCard(
    state: HomeState,
    onOverlayEnabledChange: (Boolean) -> Unit,
) {
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
            modifier = Modifier.padding(16.dp),
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
                modifier = Modifier.padding(vertical = 8.dp),
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
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun OverlayCardPreview() {
    AppTheme {
        OverlayCard(
            state = HomeState(),
            onOverlayEnabledChange = {},
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
private fun OverlayCardEnabledPreview() {
    AppTheme {
        OverlayCard(
            state = HomeState(
                overlayPermissionGranted = true,
                notificationPermissionGranted = true,
                overlayEnabled = true,
            ),
            onOverlayEnabledChange = {},
        )
    }
}

