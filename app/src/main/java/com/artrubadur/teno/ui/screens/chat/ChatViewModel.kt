package com.artrubadur.teno.ui.screens.chat

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artrubadur.teno.agent.controller.AgentControllerClient
import com.artrubadur.teno.agent.controller.AgentControllerCommand
import com.artrubadur.teno.agent.controller.AgentControllerEvent
import com.artrubadur.teno.agent.controller.AgentControllerState
import com.artrubadur.teno.agent.orchestration.AgentEvent
import com.artrubadur.teno.ui.screens.chat.components.ChatMessage
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

    fun onLaunchActiveConnection() {
        agentController.send(AgentControllerCommand.LaunchActiveConnection)
    }

    fun onTerminateConnection() {
        agentController.send(AgentControllerCommand.TerminateConnection)
        _state.update {
            it.copy(
                isReady = false,
                isWorking = false,
                isLoading = false,
            )
        }
    }

    fun onResetConversation() {
        onStopWork()
        activeAssistantMessageIndex = null
        _state.update {
            it.copy(messages = emptyList())
        }
    }

    fun onStopWork() {
        agentController.send(AgentControllerCommand.StopWork)
        _state.update {
            it.copy(isWorking = false)
        }
    }

    fun onSend() {
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

    fun onApproveConfirmation(messageIndex: Int, confirmationId: String) {
        respondToConfirmation(
            messageIndex = messageIndex,
            command = AgentControllerCommand.ApproveConfirmation(confirmationId)
        )
    }

    fun onRejectConfirmation(messageIndex: Int, confirmationId: String) {
        respondToConfirmation(
            messageIndex = messageIndex,
            command = AgentControllerCommand.RejectConfirmation(confirmationId)
        )
    }

    private fun respondToConfirmation(
        messageIndex: Int,
        command: AgentControllerCommand
    ) {
        val current = _state.value
        if (current.isWorking) return

        activeAssistantMessageIndex = messageIndex
        _state.update { it.copy(isWorking = true) }
        agentController.send(command)
    }

    private fun applyAgentState(agentState: AgentControllerState) {
        _state.update {
            it.copy(
                activeConnectionName = agentState.activeConnectionName,
                activeConnectionKind = agentState.activeConnectionKind,
                isReady = agentState.isReady,
                isLoading = agentState.isLoading,
                isWorking = agentState.isWorking,
            )
        }
    }

    private fun handleControllerEvent(event: AgentControllerEvent) {
        when (event) {
            is AgentControllerEvent.Agent -> collectAgentEvent(event)
            is AgentControllerEvent.Message -> collectServiceMessage(event)
            is AgentControllerEvent.StateChanged -> Unit
        }
    }

    private fun collectServiceMessage(event: AgentControllerEvent.Message) {
        val assistantMessageIndex = activeAssistantMessageIndex
        if (assistantMessageIndex == null) {
            _events.tryEmit(event.message)
            return
        }

        _state.update { state ->
            state.appendAssistantEvent(
                messageIndex = assistantMessageIndex,
                event = event,
            )
        }
        activeAssistantMessageIndex = null
    }

    private fun collectAgentEvent(event: AgentControllerEvent.Agent) {
        val assistantMessageIndex = activeAssistantMessageIndex ?: return
        _state.update { state ->
            state.appendAssistantEvent(
                messageIndex = assistantMessageIndex,
                event = event,
            )
        }

        if (event.event is AgentEvent.FinalAnswer || event.event is AgentEvent.Failed) {
            activeAssistantMessageIndex = null
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
