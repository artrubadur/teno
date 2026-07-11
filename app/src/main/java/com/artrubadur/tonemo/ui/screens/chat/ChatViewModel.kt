package com.artrubadur.tonemo.ui.screens.chat

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artrubadur.tonemo.agent.controller.AgentControllerClient
import com.artrubadur.tonemo.agent.controller.AgentControllerCommand
import com.artrubadur.tonemo.agent.controller.AgentControllerEvent
import com.artrubadur.tonemo.agent.controller.AgentControllerState
import com.artrubadur.tonemo.agent.orchestration.AgentEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class ChatViewModel(
    application: Application,
) : ViewModel() {

    private val agentController = AgentControllerClient(application)

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var nextMessageId: Int = 0
    private var activeAssistantMessageIndex: Int? = null

    init {
        agentController.state
            .onEach(::applyAgentState)
            .launchIn(viewModelScope)

        agentController.events
            .onEach(::handleControllerEvent)
            .launchIn(viewModelScope)
    }

    fun onInputChanged(value: String) {
        _state.update { state ->
            state.copy(input = value)
        }
    }

    fun launchActiveConnection() {
        agentController.send(AgentControllerCommand.LaunchActiveConnection)
    }

    fun terminateConnection() {
        resetConversation()
        agentController.send(AgentControllerCommand.TerminateConnection)
        _state.update {
            it.copy(
                isReady = false,
                isWorking = false,
                isLoading = false,
            )
        }
    }

    fun resetConversation() {
        stopWork()
        activeAssistantMessageIndex = null
        _state.update {
            it.copy(messages = emptyList())
        }
    }

    fun stopWork() {
        agentController.send(AgentControllerCommand.StopWork)
        _state.update {
            it.copy(isWorking = false)
        }
    }

    fun sendMessage() {
        val current = _state.value
        val prompt = current.input.trim()

        if (!current.canSend) return

        val userMessageIndex = nextId()
        val assistantMessageIndex = nextId()
        activeAssistantMessageIndex = assistantMessageIndex

        _state.update {
            it.copy(
                input = "",
                isWorking = true,
                messages = it.messages + listOf(
                    ChatMessage(
                        index = userMessageIndex,
                        text = prompt,
                        isUser = true
                    ),
                    ChatMessage(
                        index = assistantMessageIndex,
                        text = "",
                        isUser = false
                    )
                )
            )
        }

        agentController.send(AgentControllerCommand.SendMessage(prompt))
    }

    fun approveConfirmation(messageIndex: Int, confirmationId: String) {
        respondToConfirmation(
            messageIndex = messageIndex,
            status = ConfirmationStatus.APPROVED,
            command = AgentControllerCommand.ApproveConfirmation(confirmationId)
        )
    }

    fun rejectConfirmation(messageIndex: Int, confirmationId: String) {
        respondToConfirmation(
            messageIndex = messageIndex,
            status = ConfirmationStatus.REJECTED,
            command = AgentControllerCommand.RejectConfirmation(confirmationId)
        )
    }

    private fun respondToConfirmation(
        messageIndex: Int,
        status: ConfirmationStatus,
        command: AgentControllerCommand
    ) {
        val current = _state.value
        if (current.isWorking) return

        activeAssistantMessageIndex = messageIndex
        _state.update {
            it.resolveConfirmation(
                messageIndex = messageIndex,
                status = status
            ).copy(isWorking = true)
        }
        agentController.send(command)
    }

    private fun applyAgentState(agentState: AgentControllerState) {
        val messageIndex = activeAssistantMessageIndex
        val shouldRemoveEmptyMessage =
            _state.value.isWorking && !agentState.isWorking && messageIndex != null

        _state.update {
            val updated = if (shouldRemoveEmptyMessage) {
                it.removeEmptyMessage(messageIndex)
            } else {
                it
            }

            updated.copy(
                activeConnectionName = agentState.activeConnectionName,
                isReady = agentState.isReady,
                isLoading = agentState.isLoading,
                isWorking = agentState.isWorking,
            )
        }

        if (!agentState.isWorking) {
            activeAssistantMessageIndex = null
        }
    }

    private fun handleControllerEvent(event: AgentControllerEvent) {
        when (event) {
            is AgentControllerEvent.Agent -> collectAgentEvent(event.event)
            is AgentControllerEvent.Message -> _events.tryEmit(event.message)
            is AgentControllerEvent.StateChanged -> Unit
        }
    }

    private fun collectAgentEvent(event: AgentEvent) {
        val assistantMessageIndex = activeAssistantMessageIndex ?: return
        _state.update { state ->
            val withLine = state.appendAssistantLine(
                messageIndex = assistantMessageIndex,
                line = event.toMessageLine()
            )

            when (event) {
                is AgentEvent.ConfirmationRequired -> {
                    withLine.setMessageConfirmation(
                        messageIndex = assistantMessageIndex,
                        confirmation = ConfirmationRequest(
                            id = event.confirmationId,
                            title = event.title,
                            description = event.description
                        )
                    )
                }

                else -> withLine
            }
        }
    }

    private fun AgentEvent.toMessageLine(): String {
        return when (this) {
            is AgentEvent.FinalAnswer -> message
            is AgentEvent.ToolStarted -> "#${call.id} '${call.tool}' tool call: ${call.arguments}"
            is AgentEvent.ToolExecuted -> "#${result.toolCallId} '${result.tool}' tool executed: ${result.result}"
            is AgentEvent.ToolFailed -> "#${result.toolCallId} '${result.tool}' tool failed: ${result.result}"
            is AgentEvent.ToolBlocked -> "#${result.toolCallId} '${result.tool}' tool blocked: ${result.result}"
            is AgentEvent.ConfirmationRequired -> "#${call.id} '${call.tool}' confirmation required: $title\n$description"
            is AgentEvent.Failed -> "Agent failed: $reason"
        }
    }

    private fun nextId(): Int {
        val value = nextMessageId
        nextMessageId += 1
        return value
    }

    override fun onCleared() {
        agentController.close()
    }
}
