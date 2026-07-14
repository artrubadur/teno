package com.artrubadur.teno.ui.screens.chat.components

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
import com.artrubadur.teno.ui.screens.chat.ChatState
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun ChatInput(
    state: ChatState,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onStopWork: () -> Unit,
) {
    var multiline by remember { mutableStateOf(false) }
    var inputValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = state.input,
                selection = TextRange(state.input.length),
            )
        )
    }

    LaunchedEffect(state.input) {
        if (state.input != inputValue.text) {
            inputValue = inputValue.copy(
                text = state.input,
                selection = TextRange(state.input.length),
            )
        }

        if (state.input.isEmpty()) {
            multiline = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
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
                        onInputChanged(it.text)
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
                        isWorking = state.isWorking,
                        canSend = state.canSend,
                        onSendMessage = onSendMessage,
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
                        isWorking = state.isWorking,
                        canSend = state.canSend,
                        onSendMessage = onSendMessage,
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
private fun ChatInputEmptyPreview() {
    AppTheme {
        ChatInput(
            state = ChatState(),
            onInputChanged = { _ -> },
            onSendMessage = {},
            onStopWork = {}
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
private fun ChatInputShortPreview() {
    AppTheme {
        ChatInput(
            state = ChatState(
                input = "Short input"
            ),
            onInputChanged = { _ -> },
            onSendMessage = {},
            onStopWork = {}
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
private fun ChatInputLongPreview() {
    AppTheme {
        ChatInput(
            state = ChatState(
                input = "Long long long long long long long long long " +
                        "long long long long long long long long input",
                isReady = true
            ),
            onInputChanged = { _ -> },
            onSendMessage = {},
            onStopWork = {}
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
private fun ChatInputWorkingPreview() {
    AppTheme {
        ChatInput(
            state = ChatState(
                isWorking = true
            ),
            onInputChanged = { _ -> },
            onSendMessage = {},
            onStopWork = {}
        )
    }
}