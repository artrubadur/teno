package com.artrubadur.teno.ui.screens.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolSpec
import com.artrubadur.teno.ui.components.buttons.OutlinedIconButton
import com.artrubadur.teno.ui.theme.AppTheme
import kotlinx.schema.generator.json.serialization.SerializationClassJsonSchemaGenerator

@Composable
fun ToolCard(
    tool: ToolItemState,
    initialExpanded: Boolean = false,
    onEnabledChange: (Boolean) -> Unit,
    onGrantPermission: (ToolPermission) -> Unit,
) {
    var expanded by rememberSaveable(tool.spec.name) {
        mutableStateOf(initialExpanded)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedIconButton(
                iconRes = if (expanded) R.drawable.ic_collapse else R.drawable.ic_expand,
                contentDescription = if (expanded) "Collapse tool" else "Expand tool",
                onClick = { expanded = !expanded },
            )

            Text(
                text = tool.spec.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Switch(
                checked = tool.enabled,
                onCheckedChange = onEnabledChange,
            )
        }

        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 52.dp, end = 0.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = tool.spec.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!tool.permissions.isEmpty()) {
                    tool.permissions.forEach { permission ->
                        PermissionItem(
                            permission = permission,
                            onGrantPermission = onGrantPermission,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionItem(
    permission: ToolPermissionState,
    onGrantPermission: (ToolPermission) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = permission.permission.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = permission.permission.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedIconButton(
            iconRes = R.drawable.ic_grant,
            contentDescription = "Grant permission",
            onClick = { onGrantPermission(permission.permission) },
            enabled = !permission.granted,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ToolCardCollapsedPreview() {
    AppTheme {
        Surface {
            ToolCard(
                tool = previewToolItem(),
                initialExpanded = false,
                onEnabledChange = {},
                onGrantPermission = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToolCardExpandedPreview() {
    AppTheme {
        Surface {
            ToolCard(
                tool = previewToolItem(),
                initialExpanded = true,
                onEnabledChange = {},
                onGrantPermission = {},
            )
        }
    }
}

private fun previewToolItem(): ToolItemState {
    return ToolItemState(
        spec = ToolSpec(
            name = "set_brightness",
            title = "Set brightness",
            description = "Sets system screen brightness as a value from 0.0 to 1.0",
            argsSchema = SerializationClassJsonSchemaGenerator.Default
                .generateSchema(NoArgs.serializer().descriptor),
            requiredPermissions = setOf(ToolPermission.WRITE_SETTINGS),
        ),
        enabled = false,
        permissions = listOf(
            ToolPermissionState(
                permission = ToolPermission.WRITE_SETTINGS,
                granted = false,
            )
        ),
    )
}
