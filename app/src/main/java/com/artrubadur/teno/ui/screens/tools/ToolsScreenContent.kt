package com.artrubadur.teno.ui.screens.tools

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolSpec
import com.artrubadur.teno.ui.components.buttons.PlainIconButton
import com.artrubadur.teno.ui.screens.tools.components.ToolGroupCard
import com.artrubadur.teno.ui.theme.AppTheme
import kotlinx.schema.generator.json.serialization.SerializationClassJsonSchemaGenerator

@Composable
internal fun ToolsScreenContent(
    state: ToolsState = ToolsState(),
    onBack: () -> Unit = {},
    setToolEnabled: (String, Boolean) -> Unit = { _, _ -> },
    grantPermission: (ToolPermission) -> Unit = {},
) {
    val groupedTools = state.tools
        .groupBy { it.spec.group }
        .toList()
        .sortedBy { (group, _) -> group.ordinal }

    Scaffold(
        contentWindowInsets = WindowInsets(0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tools",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge
                )

                PlainIconButton(
                    iconRes = R.drawable.ic_arrow,
                    contentDescription = "Back",
                    onClick = onBack,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(
                    count = groupedTools.size,
                    key = { index -> groupedTools[index].first.name }
                ) { index ->
                    val (group, tools) = groupedTools[index]
                    ToolGroupCard(
                        group = group,
                        tools = tools,
                        setToolEnabled = setToolEnabled,
                        grantPermission = grantPermission,
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
        ToolsScreenContent(
            state = ToolsState(
                tools = listOf(
                    previewToolItem(),
                    previewToolItem(enabled = true),
                )
            )
        )
    }
}
