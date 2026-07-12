package com.artrubadur.teno.ui.screens.chat.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artrubadur.teno.R
import com.artrubadur.teno.ui.components.buttons.OutlinedLeadingIconButton
import com.artrubadur.teno.ui.components.buttons.PrimaryLeadingIconButton
import com.artrubadur.teno.ui.theme.AppTheme

@Composable
fun MessageCard(
    message: ChatMessage,
    onApproveConfirmation: (Int, String) -> Unit,
    onRejectConfirmation: (Int, String) -> Unit,
    isWorking: Boolean
) {
    val bubbleShape = if (message.isUser) {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 16.dp,
            bottomEnd = 0.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 0.dp,
            bottomEnd = 16.dp
        )
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
                Text(
                    text = if (message.text.isBlank() && !message.isUser) "" else message.text,
                    style = MaterialTheme.typography.bodyLarge
                )

                message.confirmation?.let { confirmation ->
                    if (confirmation.status == ConfirmationStatus.PENDING) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PrimaryLeadingIconButton(
                                iconRes = R.drawable.ic_confirm,
                                text = "Approve",
                                onClick = { onApproveConfirmation(message.index, confirmation.id) },
                                enabled = !isWorking,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedLeadingIconButton(
                                iconRes = R.drawable.ic_close,
                                text = "Reject",
                                onClick = { onRejectConfirmation(message.index, confirmation.id) },
                                enabled = !isWorking,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ChatMessage(
    val index: Int,
    val text: String,
    val isUser: Boolean,
    val confirmation: ConfirmationRequest? = null
)

data class ConfirmationRequest(
    val id: String,
    val title: String,
    val description: String,
    val status: ConfirmationStatus = ConfirmationStatus.PENDING
)

enum class ConfirmationStatus {
    PENDING,
    APPROVED,
    REJECTED
}

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
            isWorking = false,
        )
    }
}

@Preview
@Composable
private fun MessageCardConfirmationPreview() {
    AppTheme {
        MessageCard(
            message = ChatMessage(
                index = 1,
                text = "Confirmation required: Confirm tool execution",
                isUser = false,
                confirmation = ConfirmationRequest(
                    id = "confirmation-id",
                    title = "Confirm tool execution",
                    description = "Runs a tool"
                )
            ),
            onApproveConfirmation = { _, _ -> },
            onRejectConfirmation = { _, _ -> },
            isWorking = false,
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
            isWorking = false,
        )
    }
}

