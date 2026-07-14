package com.artrubadur.teno.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ButtonDefaults
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
fun PromptInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onStopWork: () -> Unit,
    isWorking: Boolean,
    canSend: Boolean
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
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = inputValue,
                    onValueChange = {
                        inputValue = it
                        onValueChange(it.text)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
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
                            if (inputValue.text.isEmpty()) {
                                Text(
                                    text = "Ask Teno",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            inner()
                        }
                    },
                )

                if (!multiline) {
                    ChatInputActionButton(
                        isWorking = isWorking,
                        canSend = canSend,
                        onSendMessage = onSend,
                        onStopWork = onStopWork,
                    )
                }
            }

            if (multiline) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    ChatInputActionButton(
                        isWorking = isWorking,
                        canSend = canSend,
                        onSendMessage = onSend,
                        onStopWork = onStopWork,
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputActionButton(
    isWorking: Boolean,
    canSend: Boolean,
    onSendMessage: () -> Unit,
    onStopWork: () -> Unit,
) {
    if (isWorking) {
        OutlinedIconButton(
            iconRes = R.drawable.ic_stop,
            contentDescription = "Stop work",
            onClick = onStopWork,
            modifier = Modifier.size(48.dp)
        )
    } else {
        PrimaryIconButton(
            iconRes = R.drawable.ic_arrow,
            contentDescription = "Send message",
            onClick = onSendMessage,
            modifier = Modifier.size(48.dp),
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
private fun PromptInputEmptyPreview() {
    AppTheme {
        PromptInput(
            value = "",
            onValueChange = { _ -> },
            onSend = {},
            onStopWork = {},
            isWorking = false,
            canSend = false
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
private fun PromptInputShortPreview() {
    AppTheme {
        PromptInput(
            value = "Short input",
            onValueChange = { _ -> },
            onSend = {},
            onStopWork = {},
            isWorking = false,
            canSend = true
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
private fun PromptInputLongPreview() {
    AppTheme {
        PromptInput(
            value = "Long long long long long long long long long " +
                    "long long long long long long long long input",
            onValueChange = { _ -> },
            onSend = {},
            onStopWork = {},
            isWorking = false,
            canSend = true
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
private fun PromptInputWorkingPreview() {
    AppTheme {
        PromptInput(
            value = "",
            onValueChange = { _ -> },
            onSend = {},
            onStopWork = {},
            isWorking = true,
            canSend = false
        )
    }
}