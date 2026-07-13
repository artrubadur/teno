package com.artrubadur.teno.ui.screens.tools.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolSpec
import com.artrubadur.teno.ui.screens.tools.ToolItemState
import com.artrubadur.teno.ui.screens.tools.ToolPermissionState
import com.artrubadur.teno.ui.theme.AppTheme
import kotlinx.schema.generator.json.serialization.SerializationClassJsonSchemaGenerator

@Composable
internal fun ToolGroupCard(
    group: ToolGroup,
    tools: List<ToolItemState>,
    setToolEnabled: (String, Boolean) -> Unit,
    grantPermission: (ToolPermission) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = group.name,
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                tools.forEachIndexed { index, tool ->
                    if (index > 0) HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    ToolItem(
                        tool = tool,
                        onEnabledChange = { enabled ->
                            setToolEnabled(tool.spec.name, enabled)
                        },
                        onGrantPermission = grantPermission,
                    )
                }
            }
        }
    }
}

private fun previewToolItem(enabled: Boolean = false): ToolItemState {
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
        enabled = enabled,
        permissions = listOf(
            ToolPermissionState(
                permission = ToolPermission.WRITE_SETTINGS,
                granted = false,
            )
        ),
    )
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
private fun ToolGroupCardPreview() {
    AppTheme {
        ToolGroupCard(
            group = ToolGroup.SYSTEM,
            tools = listOf(previewToolItem(), previewToolItem(enabled = true)),
            setToolEnabled = { _, _ -> },
            grantPermission = {},
        )
    }
}
