package com.artrubadur.teno.ui.screens.chat

import com.artrubadur.teno.connection.ConnectionKind
import com.artrubadur.teno.ui.screens.chat.components.ChatMessage
import com.artrubadur.teno.ui.screens.chat.components.ConfirmationRequest
import com.artrubadur.teno.ui.screens.chat.components.ConfirmationStatus

data class ChatState(
    val activeConnectionName: String? = null,
    val activeConnectionKind: ConnectionKind? = null,
    val isReady: Boolean = false,
    val isLoading: Boolean = false,
    val isWorking: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = ""
) {
    val isActivated: Boolean
        get() = activeConnectionName != null

    val canSend: Boolean
        get() = input.isNotBlank() && isReady && !isWorking && !isLoading

    val isDialogEmpty: Boolean
        get() = messages.isEmpty()
}

internal fun ChatState.appendAssistantLine(
    messageIndex: Int,
    line: String
): ChatState {
    val actualIndex = messages.indexOfFirst { it.index == messageIndex }
    if (actualIndex == -1) return this

    val updatedMessages = messages.toMutableList()
    val message = updatedMessages[actualIndex]

    val separator = if (message.text.isBlank()) "" else "\n"
    updatedMessages[actualIndex] = message.copy(text = message.text + separator + line)

    return copy(messages = updatedMessages)
}

internal fun ChatState.setMessageConfirmation(
    messageIndex: Int,
    confirmation: ConfirmationRequest
): ChatState {
    val actualIndex = messages.indexOfFirst { it.index == messageIndex }
    if (actualIndex == -1) return this

    val updatedMessages = messages.toMutableList()
    val message = updatedMessages[actualIndex]

    updatedMessages[actualIndex] = message.copy(
        confirmation = confirmation
    )

    return copy(messages = updatedMessages)
}

internal fun ChatState.resolveConfirmation(
    messageIndex: Int,
    status: ConfirmationStatus
): ChatState {
    val actualIndex = messages.indexOfFirst { it.index == messageIndex }
    if (actualIndex == -1) return this

    val updatedMessages = messages.toMutableList()
    val message = updatedMessages[actualIndex]

    val confirmation = message.confirmation ?: return this

    updatedMessages[actualIndex] = message.copy(
        confirmation = confirmation.copy(status = status)
    )

    return copy(messages = updatedMessages)
}

internal fun ChatState.removeEmptyMessage(
    messageIndex: Int
): ChatState {
    val actualIndex = messages.indexOfFirst { it.index == messageIndex }
    if (actualIndex == -1) return this

    val updatedMessages = messages.toMutableList()
    val message = updatedMessages[actualIndex]
    if (message.text.isNotBlank()) {
        return this
    }

    updatedMessages.removeAt(actualIndex)

    return copy(messages = updatedMessages)
}
