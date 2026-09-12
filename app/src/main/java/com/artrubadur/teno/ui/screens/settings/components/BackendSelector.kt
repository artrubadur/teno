package com.artrubadur.teno.ui.screens.settings.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.connection.runtime.llm.local.LiteRtBackendOption
import com.artrubadur.teno.connection.runtime.llm.local.SupportedSoC
import com.artrubadur.teno.ui.components.buttons.OutlinedButton
import com.artrubadur.teno.ui.components.buttons.PlainButton
import com.artrubadur.teno.ui.components.buttons.PlainIconButton
import com.artrubadur.teno.ui.components.buttons.PrimaryButton
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun BackendSelector(
    selected: LiteRtBackendOption,
    npuSupported: Boolean,
    supportedSoCs: List<SupportedSoC>,
    onSelected: (LiteRtBackendOption) -> Unit,
) {
    var showHelp by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LiteRtBackendButton(
            option = LiteRtBackendOption.CPU,
            selected = selected == LiteRtBackendOption.CPU,
            enabled = true,
            onSelected = onSelected,
            modifier = Modifier.weight(1f),
        )
        LiteRtBackendButton(
            option = LiteRtBackendOption.GPU,
            selected = selected == LiteRtBackendOption.GPU,
            enabled = true,
            onSelected = onSelected,
            modifier = Modifier.weight(1f),
        )
        LiteRtBackendButton(
            option = LiteRtBackendOption.NPU,
            selected = selected == LiteRtBackendOption.NPU,
            enabled = npuSupported,
            onSelected = onSelected,
            modifier = Modifier.weight(1f),
        )
        PlainIconButton(
            iconRes = R.drawable.ic_circle_question,
            contentDescription = "NPU support details",
            onClick = { showHelp = true },
            tint = MaterialTheme.colorScheme.primary,
        )
    }

    if (showHelp) {
        NpuHelpDialog(
            npuSupported = npuSupported,
            supportedSoCs = supportedSoCs,
            onDismiss = { showHelp = false },
        )
    }
}

@Composable
private fun LiteRtBackendButton(
    option: LiteRtBackendOption,
    selected: Boolean,
    enabled: Boolean,
    onSelected: (LiteRtBackendOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content: @Composable () -> Unit = {
        Text(option.title)
    }

    if (selected) {
        PrimaryButton(
            onClick = { onSelected(option) },
            modifier = modifier,
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            content = content,
        )
    } else {
        OutlinedButton(
            onClick = { onSelected(option) },
            modifier = modifier,
            enabled = enabled,
            shape = RoundedCornerShape(8.dp),
            content = content,
        )
    }
}

@Composable
private fun NpuHelpDialog(
    npuSupported: Boolean,
    supportedSoCs: List<SupportedSoC>,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PlainButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        title = {
            Text("NPU support")
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "An NPU is a neural processing unit: dedicated hardware for faster, more efficient ML inference.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = if (npuSupported) {
                        "This app detected a supported NPU SoC on your device."
                    } else {
                        "This app did not detect a supported NPU SoC on your device. If you believe this is wrong, check your phone SoC model against the official LiteRT documentation. If the app misdetected your device, please report it."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )

                Text(
                    text = "Supported SoCs checked by the app:",
                    style = MaterialTheme.typography.labelMedium,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    supportedSoCs.forEach { soc ->
                        Text(
                            text = "${soc.vendor} ${soc.code} - ${soc.name}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }

                Text(
                    text = "Official lists:",
                    style = MaterialTheme.typography.labelMedium,
                )

                NpuLink("LiteRT-LM Android", LITERT_LM_ANDROID_URL, uriHandler::openUri)
                NpuLink("Qualcomm SoCs", QUALCOMM_NPU_URL, uriHandler::openUri)
                NpuLink("MediaTek SoCs", MEDIATEK_NPU_URL, uriHandler::openUri)
                NpuLink("LiteRT-LM NPU", LITERT_LM_NPU_URL, uriHandler::openUri)
                NpuLink("Report detection issue", ISSUES_URL, uriHandler::openUri)
            }
        },
    )
}

@Composable
private fun NpuLink(
    title: String,
    url: String,
    openUri: (String) -> Unit,
) {
    Text(
        text = title,
        modifier = Modifier.clickable { openUri(url) },
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium,
        textDecoration = TextDecoration.Underline,
    )
}

private const val LITERT_LM_ANDROID_URL = "https://developers.google.com/edge/litert-lm/android"
private const val QUALCOMM_NPU_URL = "https://developers.google.com/edge/litert/next/qualcomm"
private const val MEDIATEK_NPU_URL = "https://developers.google.com/edge/litert/next/mediatek"
private const val LITERT_LM_NPU_URL = "https://developers.google.com/edge/litert/next/litert_lm_npu"
private const val ISSUES_URL = "https://github.com/artrubadur/teno/issues"

@Preview(
    name = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BackendSelectorPreview() {
    AppTheme {
        BackendSelector(
            selected = LiteRtBackendOption.CPU,
            npuSupported = false,
            supportedSoCs = listOf(
                SupportedSoC("Qualcomm", "SM8650", "Snapdragon 8 Gen 3"),
                SupportedSoC("MediaTek", "MT6991", "Dimensity 9400"),
            ),
            onSelected = {},
        )
    }
}
