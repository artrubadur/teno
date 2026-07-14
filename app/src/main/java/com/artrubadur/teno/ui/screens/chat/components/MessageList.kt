package com.artrubadur.teno.ui.screens.chat.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.ui.screens.chat.ChatState
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun MessageList(
    state: ChatState,
    onApproveConfirmation: (Int, String) -> Unit,
    onRejectConfirmation: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
    bottomPadding: Dp = 0.dp,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Box(
        modifier = modifier
    ) {
        if (!state.isDialogEmpty) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = bottomPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = state.messages,
                    key = { it.index }
                ) { message ->
                    MessageCard(
                        message = message,
                        onApproveConfirmation = onApproveConfirmation,
                        onRejectConfirmation = onRejectConfirmation,
                        isWorking = state.isWorking
                    )
                }
            }
        }

        if (false) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (!state.isActivated) {
                            "No connection selected"
                        } else if (!state.isReady) {
                            "Activate connection"
                        } else {
                            "Start a conversation"
                        },
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (!state.isActivated) {
                            "Choose a connection before sending messages"
                        } else if (!state.isReady) {
                            "Connect a local or remote model before sending messages"
                        } else {
                            "Ask a question or give tonemo a task"
                        },
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
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
private fun MessageListPreview() {
    AppTheme {
        MessageList(
            state = ChatState(),
            onApproveConfirmation = { _, _ -> },
            onRejectConfirmation = { _, _ -> },
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
private fun MessageListActivatedPreview() {
    AppTheme {
        MessageList(
            state = ChatState(
                activeConnectionName = ""
            ),
            onApproveConfirmation = { _, _ -> },
            onRejectConfirmation = { _, _ -> },
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
private fun MessageListReadyPreview() {
    AppTheme {
        MessageList(
            state = ChatState(
                activeConnectionName = "",
                isReady = true
            ),
            onApproveConfirmation = { _, _ -> },
            onRejectConfirmation = { _, _ -> },
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
private fun MessageListWorkingPreview() {
    AppTheme {
        MessageList(
            state = ChatState(
                activeConnectionName = "",
                isReady = true,
                messages = listOf(
                    ChatMessage(
                        index = 1,
                        "Message text",
                        isUser = true
                    ),
                    ChatMessage(
                        index = 2,
                        "Message text",
                        isUser = false
                    ),
                )
            ),
            onApproveConfirmation = { _, _ -> },
            onRejectConfirmation = { _, _ -> },
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
private fun MessageListDeactivatedPreview() {
    AppTheme {
        MessageList(
            state = ChatState(
                messages = listOf(
                    ChatMessage(
                        index = 1,
                        "Message text",
                        isUser = true
                    ),
                    ChatMessage(
                        index = 2,
                        "Message text",
                        isUser = false
                    ),
                )
            ),
            onApproveConfirmation = { _, _ -> },
            onRejectConfirmation = { _, _ -> },
        )
    }
}
