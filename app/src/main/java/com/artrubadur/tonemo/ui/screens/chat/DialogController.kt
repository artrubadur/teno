package com.artrubadur.tonemo.ui.screens.chat

import com.artrubadur.tonemo.runtime.llm.LlmGenerationOptions
import com.artrubadur.tonemo.runtime.llm.LlmRuntime
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DialogController(
    private val llmRuntime: LlmRuntime
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

        if (_state.value.loadedModelFileName == activeModelFileName && llmRuntime.isLoaded) {
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
                llmRuntime.load(activeModelFileName)
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

        llmRuntime.close()
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
                llmRuntime.resetConversation()
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
                llmRuntime.generateStream(
                    prompt = prompt,
                    options = ChatGenerationOptions
                ).collect { chunk ->
                    _state.update {
                        it.appendAssistantChunk(
                            chunk = chunk
                        )
                    }
                }
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

    fun stopGeneration() {
        generationJob?.cancel()
        llmRuntime.stopGeneration()
    }

    private fun nextId(): Long {
        val value = nextMessageId
        nextMessageId += 1
        return value
    }
}

val ChatGenerationOptions = LlmGenerationOptions(
    systemPrompt = "You are a local Android assistant. Answer briefly and directly.",
    temperature = 0.7,
    topK = 40,
    topP = 0.95,
)

data class DialogState(
    val loadedModelFileName: String? = null,
    val isLoadingModel: Boolean = false,
    val isGenerating: Boolean = false,
    val messages: List<ChatMessage> = emptyList(),
)

fun DialogState.appendAssistantChunk(chunk: String): DialogState {
    if (messages.isEmpty()) return this

    val updated = messages.toMutableList()
    val last = updated.lastIndex
    updated[last] = updated[last].copy(text = updated[last].text + chunk)

    return copy(messages = updated)
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
