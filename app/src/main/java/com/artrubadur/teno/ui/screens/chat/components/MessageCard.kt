package com.artrubadur.teno.ui.screens.chat.components

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.agent.controller.AgentControllerEvent
import com.artrubadur.teno.ui.components.AgentTimelineColumn
import com.artrubadur.teno.ui.components.eventlist.hasLiveTimer
import com.artrubadur.teno.ui.components.eventlist.toEventEntries
import com.artrubadur.teno.ui.theme.AppTheme
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun MessageCard(
    message: ChatMessage,
    onApproveConfirmation: (Int, String) -> Unit,
    onRejectConfirmation: (Int, String) -> Unit,
) {
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 28.dp,
            bottomEnd = 0.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 0.dp,
            bottomEnd = 28.dp
        )
    }

    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    val hasLiveTimer = remember(message.events) { message.events.toEventEntries().hasLiveTimer() }

    LaunchedEffect(hasLiveTimer) {
        while (hasLiveTimer) {
            now = SystemClock.elapsedRealtime()
            delay(1.seconds)
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            shape = bubbleShape,
            colors = if (message.isUser) CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) else CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!message.isUser && message.events.isNotEmpty()) {
                    AgentTimelineColumn(
                        events = message.events,
                        now = now,
                        onApproveConfirmation = { onApproveConfirmation(message.index, it) },
                        onRejectConfirmation = { onRejectConfirmation(message.index, it) },
                    )
                } else {
                    Text(
                        text = if (message.text.isBlank() && !message.isUser) "" else message.text,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            }
        }
    }
}

data class ChatMessage(
    val index: Int,
    val text: String,
    val isUser: Boolean,
    val events: List<AgentControllerEvent> = emptyList(),
)

@Preview
@Composable
private fun MessageCardModelPreview() {
    AppTheme {
        MessageCard(
            message = ChatMessage(
                index = 1,
                text = "Some text",
                isUser = false
            ),
            onApproveConfirmation = { _, _ -> },
            onRejectConfirmation = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun MessageCardUserPreview() {
    AppTheme {
        MessageCard(
            message = ChatMessage(
                index = 1,
                text = "Some some some text",
                isUser = true
            ),
            onApproveConfirmation = { _, _ -> },
            onRejectConfirmation = { _, _ -> },
        )
    }
}

