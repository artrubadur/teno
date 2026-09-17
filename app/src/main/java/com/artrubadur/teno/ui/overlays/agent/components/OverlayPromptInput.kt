package com.artrubadur.teno.ui.overlays.agent.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.ui.components.buttons.OutlinedIconButton
import com.artrubadur.teno.ui.components.buttons.PrimaryIconButton
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun OverlayPromptInput(
    modifier: Modifier = Modifier,
    inputFieldModifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onStopWork: () -> Unit,
    onLaunchActiveConnection: () -> Unit,
    isWorking: Boolean,
    isConfirmationRequired: Boolean,
    isReady: Boolean,
    isLoading: Boolean,
    canSend: Boolean,
    isActivated: Boolean,
    expansion: Float = if (isWorking) 0f else 1f,
) {
    val shape = RoundedCornerShape(28.dp)

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomEnd,
    ) {
        val width = 56.dp + (maxWidth - 56.dp) * expansion
        val showInput = !isWorking && width > 180.dp

        OverlayPromptInputContent(
            value = value,
            onValueChange = onValueChange,
            onSend = onSend,
            onStopWork = onStopWork,
            onLaunchActiveConnection = onLaunchActiveConnection,
            isWorking = isWorking,
            isConfirmationRequired = isConfirmationRequired,
            isReady = isReady,
            isLoading = isLoading,
            canSend = canSend,
            isActivated = isActivated,
            showInput = showInput,
            modifier = Modifier
                .width(width)
                .shadow(elevation = 8.dp, shape = shape),
            inputFieldModifier = inputFieldModifier,
        )
    }
}

@Composable
private fun OverlayPromptInputContent(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onStopWork: () -> Unit,
    onLaunchActiveConnection: () -> Unit,
    isWorking: Boolean,
    isConfirmationRequired: Boolean,
    isReady: Boolean,
    isLoading: Boolean,
    canSend: Boolean,
    isActivated: Boolean,
    showInput: Boolean,
    modifier: Modifier = Modifier,
    inputFieldModifier: Modifier = Modifier,
) {
    var multiline by remember { mutableStateOf(false) }
    var inputValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = value,
                selection = TextRange(value.length),
            )
        )
    }

    LaunchedEffect(value) {
        if (value != inputValue.text) {
            inputValue = inputValue.copy(
                text = value,
                selection = TextRange(value.length),
            )
        }

        if (value.isEmpty()) {
            multiline = false
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showInput) {
                    BasicTextField(
                        value = inputValue,
                        enabled = !isConfirmationRequired,
                        onValueChange = {
                            inputValue = it
                            onValueChange(it.text)
                        },
                        modifier = inputFieldModifier
                            .weight(1f)
                            .padding(horizontal = 8.dp, vertical = if (multiline) 8.dp else 0.dp),
                        minLines = 1,
                        maxLines = 5,
                        textStyle = LocalTextStyle.current.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        onTextLayout = {
                            if (it.lineCount > 1) {
                                multiline = true
                            }
                        },
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                if (inputValue.text.isEmpty() && !isConfirmationRequired) {
                                    Text(
                                        text = "Ask Teno",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                inner()
                            }
                        },
                    )
                }

                if (!showInput || !multiline) {
                    OverlayPromptActionButton(
                        isWorking = isWorking,
                        isConfirmationRequired = isConfirmationRequired,
                        isReady = isReady,
                        isLoading = isLoading,
                        canSend = canSend,
                        isActivated = isActivated,
                        onSendMessage = onSend,
                        onStopWork = onStopWork,
                        onLaunchActiveConnection = onLaunchActiveConnection,
                    )
                }
            }

            if (showInput && multiline) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    OverlayPromptActionButton(
                        isWorking = isWorking,
                        isConfirmationRequired = isConfirmationRequired,
                        isReady = isReady,
                        isLoading = isLoading,
                        canSend = canSend,
                        isActivated = isActivated,
                        onSendMessage = onSend,
                        onStopWork = onStopWork,
                        onLaunchActiveConnection = onLaunchActiveConnection,
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlayPromptActionButton(
    isWorking: Boolean,
    isConfirmationRequired: Boolean,
    isReady: Boolean,
    isLoading: Boolean,
    canSend: Boolean,
    isActivated: Boolean,
    onSendMessage: () -> Unit,
    onStopWork: () -> Unit,
    onLaunchActiveConnection: () -> Unit,
) {
    when {
        isConfirmationRequired -> OutlinedIconButton(
            iconRes = R.drawable.ic_stop,
            contentDescription = "Stop work",
            onClick = onStopWork,
            modifier = Modifier.size(40.dp),
        )

        isWorking || isLoading -> Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        }

        !isReady -> PrimaryIconButton(
            iconRes = R.drawable.ic_launch,
            contentDescription = "Launch model",
            onClick = onLaunchActiveConnection,
            modifier = Modifier.size(40.dp),
            enabled = isActivated,
        )

        else -> PrimaryIconButton(
            iconRes = R.drawable.ic_arrow,
            contentDescription = "Send message",
            onClick = onSendMessage,
            modifier = Modifier.size(40.dp),
            enabled = canSend,
            iconModifier = Modifier
                .size(ButtonDefaults.IconSize)
                .rotate(90f)
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
private fun OverlayPromptInputPreview() {
    AppTheme {
        OverlayPromptInput(
            value = "Open settings",
            onValueChange = {},
            onSend = {},
            onStopWork = {},
            onLaunchActiveConnection = {},
            isWorking = false,
            isConfirmationRequired = false,
            isReady = true,
            isLoading = false,
            canSend = true,
            isActivated = true,
        )
    }
}

@Preview(
    name = "Working",
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun OverlayPromptInputWorkingPreview() {
    AppTheme {
        OverlayPromptInput(
            value = "",
            onValueChange = {},
            onSend = {},
            onStopWork = {},
            onLaunchActiveConnection = {},
            isWorking = true,
            isConfirmationRequired = false,
            isReady = true,
            isLoading = false,
            canSend = false,
            isActivated = true,
        )
    }
}
