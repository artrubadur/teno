package com.artrubadur.teno.ui.screens.chat

import com.artrubadur.teno.agent.controller.AgentControllerEvent
import com.artrubadur.teno.connection.ConnectionKind
import com.artrubadur.teno.ui.screens.chat.components.ChatMessage

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

fun ChatState.appendAssistantEvent(
    messageIndex: Int,
    event: AgentControllerEvent,
): ChatState {
    val actualIndex = messages.indexOfFirst { it.index == messageIndex }
    if (actualIndex == -1) return this

    val updatedMessages = messages.toMutableList()
    val message = updatedMessages[actualIndex]

    updatedMessages[actualIndex] = message.copy(
        events = message.events + event
    )

    return copy(messages = updatedMessages)
}
