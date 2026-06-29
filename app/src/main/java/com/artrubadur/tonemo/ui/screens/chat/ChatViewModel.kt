package com.artrubadur.tonemo.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artrubadur.tonemo.agent.orchestration.AgentEvent
import com.artrubadur.tonemo.agent.orchestration.AgentOrchestrator
import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.ConnectionManager
import com.artrubadur.tonemo.connection.ConnectionType
import com.artrubadur.tonemo.connection.LocalConnection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val connectionManager: ConnectionManager,
    private val agentOrchestrator: AgentOrchestrator
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var nextMessageId: Long = 0L
    private var generationJob: Job? = null

    init {
        connectionManager
            .observeActiveConnection(ConnectionType.LLM)
            .onEach { connection ->
                connection ?: terminateModel()
                _state.update { state ->
                    state.copy(activeConnection = connection)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onInputChanged(value: String) {
        _state.update { state ->
            state.copy(input = value)
        }
    }

    fun launchActiveModel() {
        if (!_state.value.isActivated) {
            _events.tryEmit("No active generation model. Select one in Models.")
            return
        }

        if (agentOrchestrator.isModelLoaded) {
            return
        }

        val localConnection = _state.value.activeConnection as? LocalConnection

        // TODO("Add remote connection support")
        if (localConnection == null) {
            _events.tryEmit("Remote LLM connections is not supported yet.")
            return
        }

        stopGeneration()

        _state.update {
            it.copy(
                isLoading = true,
            )
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isLaunched = false
                )
            }

            try {
                agentOrchestrator.loadModel(localConnection.config.fileName)

                _state.update {
                    it.copy(
                        isLaunched = true,
                        isLoading = false
                    )
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isLaunched = false,
                        isLoading = false
                    )
                }
                _events.tryEmit(
                    t.cause?.message
                        ?: t.message ?: "Failed to launch model"
                )
            }
        }
    }

    fun terminateModel() {
        resetConversation()

        agentOrchestrator.terminateModel()

        _state.update {
            it.copy(
                isLaunched = false,
            )
        }
    }

    fun resetConversation() {
        viewModelScope.launch {
            stopGeneration()

            _state.update {
                it.copy(
                    messages = emptyList()
                )
            }
        }
    }

    fun stopGeneration() {
        generationJob?.cancel()
        generationJob = null
        agentOrchestrator.stopGeneration()

        _state.update {
            it.copy(isGenerating = false)
        }
    }

    fun sendMessage() {
        val state = _state.value
        val prompt = state.input.trim()

        if (
            prompt.isEmpty() ||
            state.activeConnection == null ||
            state.isGenerating
        ) {
            return
        }

        val userMessageId = nextId()
        val assistantMessageId = nextId()

        _state.update {
            it.copy(
                input = "",
                isGenerating = true,
                messages = it.messages + listOf(
                    ChatMessage(
                        id = userMessageId,
                        text = prompt,
                        isUser = true
                    ),
                    ChatMessage(
                        id = assistantMessageId,
                        text = "",
                        isUser = false
                    )
                )
            )
        }

        generationJob = viewModelScope.launch {
            val currentJob = coroutineContext[Job]

            try {
                collectAgentEvents(
                    assistantMessageId = assistantMessageId,
                    events = agentOrchestrator.handleUserMessage(prompt)
                )
            } catch (_: CancellationException) {
                _state.update {
                    it.removeEmptyMessage(assistantMessageId)
                }
            } catch (t: Throwable) {
                _state.update {
                    it.removeEmptyMessage(assistantMessageId)
                }

                _events.tryEmit(
                    t.cause?.message
                        ?: t.message ?: "Generation failed"
                )
            } finally {
                if (generationJob == currentJob) {
                    generationJob = null
                }

                _state.update {
                    it.copy(isGenerating = false)
                }
            }
        }
    }

    fun approveConfirmation(confirmationId: String) {
        respondToConfirmation(
            confirmationId = confirmationId,
            status = ConfirmationStatus.APPROVED,
            events = agentOrchestrator.approveConfirmation(confirmationId)
        )
    }

    fun rejectConfirmation(confirmationId: String) {
        respondToConfirmation(
            confirmationId = confirmationId,
            status = ConfirmationStatus.REJECTED,
            events = agentOrchestrator.rejectConfirmation(confirmationId)
        )
    }

    private fun respondToConfirmation(
        confirmationId: String,
        status: ConfirmationStatus,
        events: Flow<AgentEvent>
    ) {
        val state = _state.value

        if (state.isGenerating) {
            return
        }

        val assistantMessageId = state.messages
            .firstOrNull { message ->
                message.confirmation?.id == confirmationId &&
                        message.confirmation.status == ConfirmationStatus.PENDING
            }
            ?.id
            ?: return

        _state.update {
            it.resolveConfirmation(
                confirmationId = confirmationId,
                status = status
            ).copy(isGenerating = true)
        }

        generationJob = viewModelScope.launch {
            val currentJob = coroutineContext[Job]

            try {
                collectAgentEvents(
                    assistantMessageId = assistantMessageId,
                    events = events
                )
            } catch (t: Throwable) {
                if (t is CancellationException) {
                    throw t
                }

                _events.tryEmit(t.message ?: "Confirmation failed")
            } finally {
                if (generationJob == currentJob) {
                    generationJob = null
                }

                _state.update {
                    it.copy(isGenerating = false)
                }
            }
        }
    }

    private suspend fun collectAgentEvents(
        assistantMessageId: Long,
        events: Flow<AgentEvent>
    ) {
        events.collect { event ->
            _state.update { state ->
                val withLine = state.appendAssistantLine(
                    messageId = assistantMessageId,
                    line = event.toMessageLine()
                )

                when (event) {
                    is AgentEvent.ConfirmationRequired -> {
                        withLine.setMessageConfirmation(
                            messageId = assistantMessageId,
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
    }

    private fun AgentEvent.toMessageLine(): String {
        return when (this) {
            is AgentEvent.FinalAnswer -> message
            is AgentEvent.ToolCallStarted -> "Tool call: $tool ${argsJson ?: "{}"}"
            is AgentEvent.ToolExecuted -> "Tool executed: $tool $resultJson"
            is AgentEvent.ToolFailed -> "Tool failed: $tool - $reason"
            is AgentEvent.ToolBlocked -> "Tool blocked: $tool - $reason"
            is AgentEvent.ConfirmationRequired -> "Confirmation required: $title\n$description"
            is AgentEvent.Failed -> "Agent failed: $reason"
        }
    }

    private fun nextId(): Long {
        val value = nextMessageId
        nextMessageId += 1
        return value
    }

    override fun onCleared() {
        terminateModel()
        super.onCleared()
    }
}


data class ChatState(
    val activeConnection: Connection? = null,
    val isLaunched: Boolean = false,
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
    val input: String = ""
) {
    val isActivated: Boolean
        get() = activeConnection != null

    val isDialogEmpty: Boolean
        get() = messages.isEmpty()
}

private fun ChatState.appendAssistantLine(
    messageId: Long,
    line: String
): ChatState {
    val target = messages.firstOrNull { it.id == messageId } ?: return this
    val separator = if (target.text.isBlank()) "" else "\n"

    return updateMessage(messageId) { message ->
        message.copy(text = message.text + separator + line)
    }
}

private fun ChatState.setMessageConfirmation(
    messageId: Long,
    confirmation: ConfirmationRequest
): ChatState {
    return updateMessage(messageId) { message ->
        message.copy(confirmation = confirmation)
    }
}

private fun ChatState.resolveConfirmation(
    confirmationId: String,
    status: ConfirmationStatus
): ChatState {
    return copy(
        messages = messages.map { message ->
            val confirmation = message.confirmation

            // TODO("Replace with message id based search")
            if (confirmation?.id == confirmationId) {
                message.copy(
                    confirmation = confirmation.copy(status = status)
                )
            } else {
                message
            }
        }
    )
}

private fun ChatState.removeEmptyMessage(
    messageId: Long
): ChatState {
    return copy(
        messages = messages.filterNot { message ->
            message.id == messageId && message.text.isBlank()
        }
    )
}

private fun ChatState.updateMessage(
    messageId: Long,
    transform: (ChatMessage) -> ChatMessage
): ChatState {
    return copy(
        messages = messages.map { message ->
            if (message.id == messageId) transform(message) else message
        }
    )
}