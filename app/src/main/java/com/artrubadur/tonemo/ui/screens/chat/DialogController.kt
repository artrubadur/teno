package com.artrubadur.tonemo.ui.screens.chat

import com.artrubadur.tonemo.agent.orchestration.AgentEvent
import com.artrubadur.tonemo.agent.orchestration.AgentOrchestrator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DialogController(
    private val agentOrchestrator: AgentOrchestrator
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow(DialogState())
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)

    val state: StateFlow<DialogState> = _state.asStateFlow()
    val events: SharedFlow<String> = _events.asSharedFlow()

    private var nextMessageId: Long = 0L
    private var generationJob: Job? = null

    fun launchModel(activeModelFileName: String?) {
        if (activeModelFileName == null) {
            _state.update {
                it.copy(
                    loadedModelFileName = null,
                    isLoadingModel = false,
                    isGenerating = false,
                    messages = emptyList()
                )
            }
            _events.tryEmit("No active generation model. Select one in Models.")
            return
        }

        if (_state.value.loadedModelFileName == activeModelFileName && agentOrchestrator.isModelLoaded) {
            return
        }

        scope.launch {
            stopGeneration()

            _state.update {
                it.copy(
                    loadedModelFileName = null,
                    isLoadingModel = true,
                    isGenerating = false,
                )
            }

            try {
                agentOrchestrator.loadModel(activeModelFileName)
                _state.update {
                    it.copy(
                        loadedModelFileName = activeModelFileName,
                        isLoadingModel = false
                    )
                }
            } catch (error: Throwable) {
                _state.update {
                    it.copy(
                        loadedModelFileName = null,
                        isLoadingModel = false
                    )
                }
                _events.tryEmit(error.message ?: "Failed to load model")
            }
        }
    }

    fun terminateModel() {
        stopGeneration()

        agentOrchestrator.terminateModel()
        _state.update {
            it.copy(
                loadedModelFileName = null,
                isLoadingModel = false,
                isGenerating = false,
                messages = emptyList()
            )
        }
    }

    fun resetConversation() {
        if (_state.value.isGenerating) {
            _events.tryEmit("Stop generation before clearing the chat")
            return
        }
        scope.launch {
            val currentGenerationJob = generationJob
            stopGeneration()
            try {
                currentGenerationJob?.join()
                _state.update {
                    it.copy(
                        isGenerating = false,
                        messages = emptyList()
                    )
                }
            } catch (error: Throwable) {
                _state.update {
                    it.copy(
                        isGenerating = false
                    )
                }
                _events.tryEmit(error.message ?: "Failed to reset conversation")
            }
        }
    }

    fun sendMessage(input: String) {
        val state = _state.value
        val prompt = input.trim()
        if (prompt.isEmpty() || state.loadedModelFileName == null || state.isGenerating) {
            return
        }

        val userMessageId = nextId()
        val assistantMessageId = nextId()

        _state.update {
            it.copy(
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

        generationJob = scope.launch {
            val currentJob = coroutineContext[Job]

            try {
                collectAgentEvents(
                    assistantMessageId = assistantMessageId,
                    events = agentOrchestrator.handleUserMessage(prompt)
                )
            } catch (_: CancellationException) {
                _state.update { it.removeEmptyMessage(assistantMessageId) }
            } catch (error: Throwable) {
                _state.update { it.removeEmptyMessage(assistantMessageId) }
                _events.tryEmit(error.message ?: "Generation failed")
            } finally {
                if (generationJob == currentJob) {
                    generationJob = null
                }
                _state.update { it.copy(isGenerating = false) }
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

    fun stopGeneration() {
        generationJob?.cancel()
        agentOrchestrator.stopGeneration()
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

        generationJob = scope.launch {
            val currentJob = coroutineContext[Job]

            try {
                collectAgentEvents(
                    assistantMessageId = assistantMessageId,
                    events = events
                )
            } catch (error: Throwable) {
                if (error is CancellationException) {
                    throw error
                }
                _events.tryEmit(error.message ?: "Confirmation failed")
            } finally {
                if (generationJob == currentJob) {
                    generationJob = null
                }
                _state.update { it.copy(isGenerating = false) }
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
}

data class DialogState(
    val loadedModelFileName: String? = null,
    val isLoadingModel: Boolean = false,
    val isGenerating: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
)

//fun DialogState.appendAssistantChunk(chunk: String): DialogState {
//    if (messages.isEmpty()) return this
//
//    val updated = messages.toMutableList()
//    val last = updated.lastIndex
//    updated[last] = updated[last].copy(text = updated[last].text + chunk)
//
//    return copy(messages = updated)
//}

fun DialogState.appendAssistantLine(
    messageId: Long,
    line: String
): DialogState {
    val target = messages.firstOrNull { it.id == messageId } ?: return this
    val separator = if (target.text.isBlank()) "" else "\n"

    return updateMessage(messageId) { message ->
        message.copy(text = message.text + separator + line)
    }
}

fun DialogState.setMessageConfirmation(
    messageId: Long,
    confirmation: ConfirmationRequest
): DialogState {
    return updateMessage(messageId) { message ->
        message.copy(confirmation = confirmation)
    }
}

fun DialogState.resolveConfirmation(
    confirmationId: String,
    status: ConfirmationStatus
): DialogState {
    return copy(
        messages = messages.map { message ->
            val confirmation = message.confirmation
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

fun DialogState.removeEmptyMessage(
    messageId: Long
): DialogState {
    return copy(
        messages = messages.filterNot { message ->
            message.id == messageId && message.text.isBlank()
        }
    )
}

private fun DialogState.updateMessage(
    messageId: Long,
    transform: (ChatMessage) -> ChatMessage
): DialogState {
    return copy(
        messages = messages.map { message ->
            if (message.id == messageId) transform(message) else message
        }
    )
}
