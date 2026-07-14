package com.artrubadur.teno.ui.screens.chat

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.connection.ConnectionKind
import com.artrubadur.teno.ui.components.buttons.ErrorIconButton
import com.artrubadur.teno.ui.components.buttons.PlainIconButton
import com.artrubadur.teno.ui.screens.chat.components.ActiveConnectionCard
import com.artrubadur.teno.ui.screens.chat.components.ChatInput
import com.artrubadur.teno.ui.screens.chat.components.ChatMessage
import com.artrubadur.teno.ui.screens.chat.components.MessageList
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
internal fun ChatScreenContent(
    snackbarHostState: SnackbarHostState,
    state: ChatState,
    onBack: () -> Unit,
    onOpenConnections: () -> Unit,
    onLaunchActiveConnection: () -> Unit,
    onTerminateConnection: () -> Unit,
    onResetConversation: () -> Unit,
    onApproveConfirmation: (Int, String) -> Unit,
    onRejectConfirmation: (Int, String) -> Unit,
    onInputChanged: (String) -> Unit,
    onSendMessage: () -> Unit,
    onStopWork: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Chat",
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

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ErrorIconButton(
                        iconRes = R.drawable.ic_delete,
                        contentDescription = "Reset chat",
                        onClick = onResetConversation,
                        enabled = !state.isDialogEmpty && !state.isWorking
                    )


                    ActiveConnectionCard(
                        state = state,
                        onOpenConnections = onOpenConnections,
                        onLaunchActiveConnection = onLaunchActiveConnection,
                        onTerminateConnection = onTerminateConnection,
                    )
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                MessageList(
                    state = state,
                    onApproveConfirmation = onApproveConfirmation,
                    onRejectConfirmation = onRejectConfirmation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )


                ChatInput(
                    state = state,
                    onInputChanged = onInputChanged,
                    onSendMessage = onSendMessage,
                    onStopWork = onStopWork
                )
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
private fun HomeScreenPreview() {
    AppTheme {
        ChatScreenContent(
            snackbarHostState = SnackbarHostState(),
            state = ChatState(),
            onBack = {},
            onOpenConnections = {},
            onLaunchActiveConnection = {},
            onTerminateConnection = {},
            onResetConversation = {},
            onApproveConfirmation = { _, _ -> },
            onRejectConfirmation = { _, _ -> },
            onInputChanged = {},
            onSendMessage = {},
            onStopWork = {},
        )
    }
}

@Preview(
    name = "Light Enabled",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "Dark Enabled",
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun HomeScreenEnabledPreview() {
    AppTheme {
        ChatScreenContent(
            snackbarHostState = SnackbarHostState(),
            state = ChatState(
                activeConnectionName = "Connection Name",
                activeConnectionKind = ConnectionKind.REMOTE,
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
            onBack = {},
            onOpenConnections = {},
            onLaunchActiveConnection = {},
            onTerminateConnection = {},
            onResetConversation = {},
            onApproveConfirmation = { _, _ -> },
            onRejectConfirmation = { _, _ -> },
            onInputChanged = {},
            onSendMessage = {},
            onStopWork = {},
        )
    }
}
