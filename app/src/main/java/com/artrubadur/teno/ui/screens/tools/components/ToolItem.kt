package com.artrubadur.teno.ui.screens.tools.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolSpec
import com.artrubadur.teno.ui.components.Switch
import com.artrubadur.teno.ui.components.buttons.PlainIconButton
import com.artrubadur.teno.ui.screens.tools.ToolItemState
import com.artrubadur.teno.ui.screens.tools.ToolPermissionState
import com.artrubadur.teno.ui.theme.AppTheme
import kotlinx.schema.generator.json.serialization.SerializationClassJsonSchemaGenerator

@Composable
fun ToolItem(
    tool: ToolItemState,
    initialExpanded: Boolean = false,
    onEnabledChange: (Boolean) -> Unit,
    onGrantPermission: (ToolPermission) -> Unit,
) {
    var expanded by rememberSaveable(tool.spec.name) { mutableStateOf(initialExpanded) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) -90f else 90f,
        animationSpec = tween(durationMillis = 150),
        label = "tool_arrow_rotation"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = tool.spec.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )

            Switch(
                checked = tool.enabled,
                onCheckedChange = onEnabledChange,
            )

            PlainIconButton(
                iconRes = R.drawable.ic_kb_arrow,
                contentDescription = if (expanded) "Collapse tool" else "Expand tool",
                onClick = { expanded = !expanded },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                iconModifier = Modifier
                    .size(ButtonDefaults.IconSize)
                    .rotate(arrowRotation)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = tween(durationMillis = 150),
            ) + fadeIn(animationSpec = tween(durationMillis = 150)),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(durationMillis = 150),
            ) + fadeOut(animationSpec = tween(durationMillis = 150)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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

@Preview(showBackground = true)
@Composable
private fun ToolItemCollapsedPreview() {
    AppTheme {
        Surface {
            ToolItem(
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
private fun ToolItemExpandedPreview() {
    AppTheme {
        Surface {
            ToolItem(
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
            group = ToolGroup.SYSTEM,
            argsSchema = SerializationClassJsonSchemaGenerator.Default
                .generateSchema(NoArgs.serializer().descriptor),
            requiredPermissions = setOf(ToolPermission.WRITE_SETTINGS),
        ),
        enabled = false,
        permissions = listOf(
            ToolPermissionState(
                permission = ToolPermission.WRITE_SETTINGS,
                granted = false,
            ),
            ToolPermissionState(
                permission = ToolPermission.WRITE_SETTINGS,
                granted = false,
            )
        ),
    )
}
