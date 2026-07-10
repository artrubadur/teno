package com.artrubadur.tonemo.ui.overlay

import android.util.Log
import com.artrubadur.tonemo.agent.orchestration.AgentEvent
import com.artrubadur.tonemo.agent.orchestration.AgentOrchestrator
import com.artrubadur.tonemo.connection.ConnectionManager
import com.artrubadur.tonemo.connection.ConnectionType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class OverlayAgentController(
    private val scope: CoroutineScope,
    connectionManager: ConnectionManager,
    private val agentOrchestrator: AgentOrchestrator,
) {
    private val _state = MutableStateFlow(OverlayState())
    val state: StateFlow<OverlayState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private var workJob: Job? = null
    private var launchJob: Job? = null

    init {
        connectionManager
            .observeActiveConnection(ConnectionType.LLM)
            .onEach { connection ->
                if (_state.value.activeConnection != connection || connection == null) {
                    terminateModel()
                }
                _state.update {
                    it.copy(activeConnection = connection)
                }
            }
            .launchIn(scope)
    }

    fun onInputChanged(value: String) {
        _state.update { it.copy(input = value) }
    }

    fun launchActiveModel() {
        val state = _state.value
        val activeConnection = state.activeConnection

        if (activeConnection == null) {
            emitEvent("No active generation model. Select one in Models.")
            return
        }

        if (state.isLoading || agentOrchestrator.isModelLoaded) return

        stopWork()

        launchJob = scope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isReady = false
                )
            }

            try {
                agentOrchestrator.connect(activeConnection)

                _state.update {
                    it.copy(
                        isLoading = false,
                        isReady = true,
                    )
                }
            } catch (t: CancellationException) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isReady = false,
                    )
                }
                throw t
            } catch (t: Throwable) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isReady = false,
                    )
                }
                emitEvent(
                    "Failed to launch model: ${t.message}: ${t.cause?.message}"
                )
            } finally {
                launchJob = null
            }
        }
    }

    fun terminateModel() {
        stopWork()
        agentOrchestrator.terminateModel()
        _state.update {
            OverlayState(activeConnection = it.activeConnection)
        }
    }

    fun onSend() {
        val current = _state.value
        val prompt = current.input.trim()

        if (current.activeConnection == null) {
            emitEvent(
                "No active generation model"
            )
            return
        }

        if (prompt.isEmpty() || !current.isReady || current.isLoading || current.isWorking) return

        _state.update {
            it.copy(
                input = "",
                isOverlayVisible = true,
                isIslandVisible = true,
                focusInput = false,
                isWorking = true,
            )
        }
        emitEvent(
            "Preparing..."
        )

        workJob = scope.launch {
            val currentJob = coroutineContext[Job]

            try {
                emitEvent(
                    "Agent started"
                )

                collectAgentEvents(agentOrchestrator.handleUserMessage(prompt))
            } catch (t: CancellationException) {
                emitEvent(
                    "Stopped"
                )
                throw t
            } catch (t: Throwable) {
                emitEvent(
                    "Generation failed: ${t.message}: ${t.cause?.message}"
                )
            } finally {
                if (workJob == currentJob) {
                    workJob = null
                }
                _state.update {
                    val shouldCloseOverlay = !it.isIslandVisible
                    it.copy(
                        isOverlayVisible = if (shouldCloseOverlay) false else it.isOverlayVisible,
                        isIslandVisible = true,
                        isWorking = false,
                    )
                }
            }
        }
    }

    fun stopWork() {
        launchJob?.cancel()
        launchJob = null
        workJob?.cancel()
        workJob = null
        agentOrchestrator.stopGeneration()

        _state.update {
            it.copy(
                isLoading = false,
                isWorking = false,
            )
        }
    }

    fun onOpenInput() {
        if (!_state.value.isOverlayVisible) {
            _state.update {
                it.copy(
                    isOverlayVisible = true,
                    isIslandVisible = false,
                    focusInput = false,
                )
            }

            scope.launch {
                yield()
                _state.update {
                    it.copy(
                        isIslandVisible = true,
                        focusInput = true,
                    )
                }
            }
            return
        }

        _state.update {
            it.copy(
                isOverlayVisible = true,
                isIslandVisible = true,
                focusInput = true,
            )
        }
    }

    fun onOutsideClick() {
        if (_state.value.isWorking) {
            _state.update {
                it.copy(
                    isIslandVisible = !it.isIslandVisible,
                    focusInput = false
                )
            }
        } else {
            _state.update {
                it.copy(
                    isIslandVisible = false,
                    focusInput = false
                )
            }
        }
    }

    fun onIslandHidden() {
        if (_state.value.isWorking || _state.value.isIslandVisible) {
            return
        }

        _state.update {
            it.copy(
                isOverlayVisible = false,
                isIslandVisible = true
            )
        }
    }

    private fun emitEvent(message: String) {
        _state.update { it.copy(latestEvent = message) }
        _events.tryEmit(message)
    }

    private suspend fun collectAgentEvents(events: Flow<AgentEvent>) {
        events.collect { event ->
            val line = event.toOverlayLine()
            emitEvent(line)
            Log.d(TAG, line)
        }
    }

    private fun AgentEvent.toOverlayLine(): String {
        return when (this) {
            is AgentEvent.FinalAnswer -> message
            is AgentEvent.ToolStarted -> "${call.tool}: started"
            is AgentEvent.ToolExecuted -> "${result.tool}: done"
            is AgentEvent.ToolFailed -> "${result.tool}: failed"
            is AgentEvent.ToolBlocked -> "${result.tool}: blocked"
            is AgentEvent.ConfirmationRequired -> "Confirmation required: $title"
            is AgentEvent.Failed -> "Agent failed: $reason"
        }
    }

    private companion object {
        const val TAG = "OverlayAgentController"
    }
}
