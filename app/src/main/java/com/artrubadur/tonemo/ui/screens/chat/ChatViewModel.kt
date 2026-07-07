package com.artrubadur.tonemo.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artrubadur.tonemo.agent.orchestration.AgentEvent
import com.artrubadur.tonemo.agent.orchestration.AgentOrchestrator
import com.artrubadur.tonemo.connection.Connection
import com.artrubadur.tonemo.connection.ConnectionManager
import com.artrubadur.tonemo.connection.ConnectionType
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
    connectionManager: ConnectionManager,
    private val agentOrchestrator: AgentOrchestrator
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var nextMessageId: Int = 0
    private var generationJob: Job? = null

    init {
        connectionManager
            .observeActiveConnection(ConnectionType.LLM)
            .onEach { connection ->
                if (_state.value.activeConnection != connection || connection == null) {
                    terminateModel()
                }
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
        val activeConnection = _state.value.activeConnection

        if (activeConnection == null) {
            _events.tryEmit("No active generation model. Select one in Models.")
            return
        }

        if (agentOrchestrator.isModelLoaded) {
            return
        }

        stopGeneration()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isLaunched = false
                )
            }

            try {
                agentOrchestrator.connect(activeConnection)

                _state.update {
                    it.copy(
                        isLoading = false,
                        isLaunched = true,
                    )
                }
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isLaunched = false,
                    )
                }
                _events.tryEmit(
                    "Failed to launch model: ${t.message}: ${t.cause?.message}"
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

        val userMessageIndex = nextId()
        val assistantMessageIndex = nextId()

        _state.update {
            it.copy(
                input = "",
                isGenerating = true,
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

        generationJob = viewModelScope.launch {
            val currentJob = coroutineContext[Job]

            try {
                collectAgentEvents(
                    assistantMessageIndex = assistantMessageIndex,
                    events = agentOrchestrator.handleUserMessage(prompt)
                )
            } catch (_: CancellationException) {
                _state.update {
                    it.removeEmptyMessage(assistantMessageIndex)
                }
            } catch (t: Throwable) {
                _state.update {
                    it.removeEmptyMessage(assistantMessageIndex)
                }

                _events.tryEmit(
                    "Generation failed: ${t.message}: ${t.cause?.message}"
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

    fun approveConfirmation(messageIndex: Int, confirmationId: String) {
        respondToConfirmation(
            messageIndex = messageIndex,
            status = ConfirmationStatus.APPROVED,
            events = agentOrchestrator.approveConfirmation(confirmationId)
        )
    }

    fun rejectConfirmation(messageIndex: Int, confirmationId: String) {
        respondToConfirmation(
            messageIndex = messageIndex,
            status = ConfirmationStatus.REJECTED,
            events = agentOrchestrator.rejectConfirmation(confirmationId)
        )
    }

    private fun respondToConfirmation(
        messageIndex: Int,
        status: ConfirmationStatus,
        events: Flow<AgentEvent>
    ) {
        val state = _state.value

        if (state.isGenerating) {
            return
        }

        _state.update {
            it.resolveConfirmation(
                messageIndex = messageIndex,
                status = status
            ).copy(isGenerating = true)
        }

        generationJob = viewModelScope.launch {
            val currentJob = coroutineContext[Job]

            try {
                collectAgentEvents(
                    assistantMessageIndex = messageIndex,
                    events = events
                )
            } catch (t: Throwable) {
                if (t is CancellationException) {
                    throw t
                }

                _events.tryEmit(
                    "Confirmation failed: ${t.message}: ${t.cause?.message}"
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

    private suspend fun collectAgentEvents(
        assistantMessageIndex: Int,
        events: Flow<AgentEvent>
    ) {
        events.collect { event ->
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

private fun ChatState.setMessageConfirmation(
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

private fun ChatState.resolveConfirmation(
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

private fun ChatState.removeEmptyMessage(
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