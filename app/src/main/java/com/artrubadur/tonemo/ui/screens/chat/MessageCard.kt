package com.artrubadur.tonemo.ui.screens.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.tonemo.ui.theme.TonemoTheme

@Composable
fun MessageCard(
    message: ChatMessage
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.85f),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (message.isUser) "You" else "Model",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (message.text.isBlank() && !message.isUser) "..." else message.text,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean
)

@Preview
@Composable
private fun MessageCardModelPreview() {
    TonemoTheme {
        MessageCard(
            message = ChatMessage(
                id = 1,
                text = "Some text",
                isUser = false
            )
        )
    }
}

@Preview
@Composable
private fun MessageCardUserPreview() {
    TonemoTheme {
        MessageCard(
            message = ChatMessage(
                id = 1,
                text = "Some text",
                isUser = true
            )
        )
    }
}

