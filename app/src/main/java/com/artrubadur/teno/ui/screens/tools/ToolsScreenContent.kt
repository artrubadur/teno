package com.artrubadur.teno.ui.screens.tools

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolSpec
import com.artrubadur.teno.ui.components.ScreenHeader
import com.artrubadur.teno.ui.screens.tools.components.ToolGroupList
import com.artrubadur.teno.ui.theme.AppTheme
import kotlinx.schema.generator.json.serialization.SerializationClassJsonSchemaGenerator

@Composable
fun ToolsScreenContent(
    state: ToolsState,
    onBack: () -> Unit,
    setToolEnabled: (String, Boolean) -> Unit,
    grantPermission: (ToolPermission) -> Unit,
) {
    val groupedTools = state.tools
        .groupBy { it.spec.group }
        .toList()
        .sortedBy { (group, _) ->
            when (group) {
                ToolGroup.SCREEN -> 0
                ToolGroup.DEVICE -> 1
                ToolGroup.SYSTEM -> 2
                ToolGroup.CONTACTS -> 3
                ToolGroup.PHONE -> 4
                ToolGroup.SMS -> 5
                ToolGroup.APPS -> 6
                ToolGroup.CALENDAR -> 7
                ToolGroup.DIAGNOSTICS -> 8
            }
        }

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
            ScreenHeader(title = "Tools", onBack = onBack)

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(
                    count = groupedTools.size,
                    key = { index -> groupedTools[index].first.name }
                ) { index ->
                    val (group, tools) = groupedTools[index]
                    ToolGroupList(
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
            ),
            onBack = {},
            setToolEnabled = { _, _ -> },
            grantPermission = {},
        )
    }
}
