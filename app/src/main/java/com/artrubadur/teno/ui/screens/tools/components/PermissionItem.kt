package com.artrubadur.teno.ui.screens.tools.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.ui.components.buttons.PlainIconButton
import com.artrubadur.teno.ui.screens.tools.ToolPermissionState
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun PermissionItem(
    permission: ToolPermissionState,
    onGrantPermission: (ToolPermission) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = permission.permission.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = permission.permission.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (!permission.granted) {
            PlainIconButton(
                iconRes = R.drawable.ic_open,
                contentDescription = "Grant permission",
                onClick = { onGrantPermission(permission.permission) },
            )
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

@Preview(
    name = "Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun PermissionItemPreview() {
    AppTheme {
        PermissionItem(
            permission = ToolPermissionState(
                permission = ToolPermission.WRITE_SETTINGS,
                granted = false,
            ),
            onGrantPermission = {}
        )
    }
}

@Preview(
    name = "Light",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun PermissionGrantedPreview() {
    AppTheme {
        PermissionItem(
            permission = ToolPermissionState(
                permission = ToolPermission.WRITE_SETTINGS,
                granted = true,
            ),
            onGrantPermission = {}
        )
    }
}
