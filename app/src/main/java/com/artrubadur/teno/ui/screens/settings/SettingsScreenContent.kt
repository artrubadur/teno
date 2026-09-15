package com.artrubadur.teno.ui.screens.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.connection.runtime.llm.local.LiteRtBackendOption
import com.artrubadur.teno.ui.components.MenuItem
import com.artrubadur.teno.ui.components.ScreenHeader
import com.artrubadur.teno.ui.components.SectionLabel
import com.artrubadur.teno.ui.components.buttons.ErrorIconButton
import com.artrubadur.teno.ui.components.buttons.OutlinedIconButton
import com.artrubadur.teno.ui.screens.settings.components.BackendSelector
import com.artrubadur.teno.ui.screens.settings.components.SettingsTextField
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun SettingsScreenContent(
    state: SettingsState,
    onBack: () -> Unit,
    onOpenTools: () -> Unit,
    onOpenIdentity: () -> Unit,
    onOpenRules: () -> Unit,
    onMaxStepsChange: (String) -> Unit,
    onUnlimitedMaxStepsChange: (Boolean) -> Unit,
    onTemperatureChange: (String) -> Unit,
    onTopKChange: (String) -> Unit,
    onTopPChange: (String) -> Unit,
    onMaxTokensChange: (String) -> Unit,
    onLiteRtBackendChange: (LiteRtBackendOption) -> Unit,
) {
    Scaffold(contentWindowInsets = WindowInsets(0)) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenHeader(title = "Settings", onBack = onBack)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SectionLabel(text = "AGENT")
                    MenuItem(text = "Tools", onClick = onOpenTools)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            MenuItem(text = "Identity", onClick = onOpenIdentity)
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            MenuItem(text = "Rules", onClick = onOpenRules)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        SettingsTextField(
                            value = state.maxStepsText,
                            onValueChange = onMaxStepsChange,
                            label = "Max steps",
                            isError = state.maxStepsError,
                            keyboardType = KeyboardType.Number,
                            enabled = !state.unlimitedMaxSteps,
                            modifier = Modifier.weight(1f),
                        )

                        if (state.unlimitedMaxSteps) {
                            ErrorIconButton(
                                iconRes = R.drawable.ic_infinity,
                                contentDescription = "Disable max steps limit",
                                onClick = { onUnlimitedMaxStepsChange(false) },
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        } else {
                            OutlinedIconButton(
                                iconRes = R.drawable.ic_infinity,
                                contentDescription = "Enable unlimited max steps",
                                onClick = { onUnlimitedMaxStepsChange(true) },
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SectionLabel(text = "GENERATION")
                    SettingsTextField(
                        value = state.temperatureText,
                        onValueChange = onTemperatureChange,
                        label = "Temperature",
                        isError = state.temperatureError,
                        keyboardType = KeyboardType.Decimal,
                    )
                    SettingsTextField(
                        value = state.topKText,
                        onValueChange = onTopKChange,
                        label = "Top K",
                        isError = state.topKError,
                        keyboardType = KeyboardType.Number,
                    )
                    SettingsTextField(
                        value = state.topPText,
                        onValueChange = onTopPChange,
                        label = "Top P",
                        isError = state.topPError,
                        keyboardType = KeyboardType.Decimal,
                    )
                    SettingsTextField(
                        value = state.maxTokensText,
                        onValueChange = onMaxTokensChange,
                        label = "Max tokens",
                        isError = state.maxTokensError,
                        keyboardType = KeyboardType.Number,
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SectionLabel(text = "BACKEND")
                    BackendSelector(
                        selected = state.liteRtBackend,
                        npuSupported = state.npuSupported,
                        supportedSoCs = state.supportedNpuSoCs,
                        onSelected = onLiteRtBackendChange,
                    )
                }

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
private fun SettingsScreenContentPreview() {
    AppTheme {
        Box {
            SettingsScreenContent(
                state = SettingsState(
                    maxStepsText = "5",
                    temperatureText = "0.1",
                    topKText = "40",
                    topPText = "0.9",
                    maxTokensText = "500",
                ),
                onBack = {},
                onOpenTools = {},
                onOpenIdentity = {},
                onOpenRules = {},
                onMaxStepsChange = {},
                onUnlimitedMaxStepsChange = {},
                onTemperatureChange = {},
                onTopKChange = {},
                onTopPChange = {},
                onMaxTokensChange = {},
                onLiteRtBackendChange = {},
            )
        }
    }
}
