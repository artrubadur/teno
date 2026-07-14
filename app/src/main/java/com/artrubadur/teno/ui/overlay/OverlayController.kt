package com.artrubadur.teno.ui.overlay

import android.app.Application
import com.artrubadur.teno.agent.controller.AgentControllerClient
import com.artrubadur.teno.agent.controller.AgentControllerCommand
import com.artrubadur.teno.agent.controller.AgentControllerEvent
import com.artrubadur.teno.agent.controller.AgentControllerState
import com.artrubadur.teno.agent.orchestration.AgentEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

class OverlayController(
    private val scope: CoroutineScope,
    application: Application,
) {
    private val agentController = AgentControllerClient(application)

    private val _state = MutableStateFlow(OverlayState())
    val state: StateFlow<OverlayState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 1)

    init {
        agentController.state
            .onEach(::applyAgentState)
            .launchIn(scope)

        agentController.events
            .onEach(::handleControllerEvent)
            .launchIn(scope)
    }

    fun onInputChanged(value: String) {
        _state.update { it.copy(input = value) }
    }

    fun launchActiveConnection() {
        agentController.send(AgentControllerCommand.LaunchActiveConnection)
    }

    fun terminateConnection() {
        agentController.send(AgentControllerCommand.TerminateConnection)
        _state.update {
            it.copy(
                isReady = false,
                isWorking = false,
                isLoading = false,
            )
        }
    }

    fun onSend() {
        val current = _state.value
        val prompt = current.input.trim()

        if (!current.canSend) return

        _state.update {
            it.copy(
                input = "",
                isOverlayVisible = true,
                isIslandVisible = true,
                focusInput = false,
            )
        }

        agentController.send(AgentControllerCommand.SendMessage(prompt))
    }

    fun stopWork() {
        agentController.send(AgentControllerCommand.StopWork)
        _state.update {
            it.copy(
                isLoading = false,
                isWorking = false,
            )
        }
    }

    fun close() {
        agentController.close()
    }

    fun onOpenInput() {
        if (!_state.value.isOverlayVisible) {
            onShowIsland(true)
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

    fun onShowIsland(focus: Boolean) {
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
                    focusInput = focus,
                )
            }
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

    private fun applyAgentState(agentState: AgentControllerState) {
        _state.update {
            val shouldCloseOverlay = it.isWorking && !agentState.isWorking && !it.isIslandVisible
            it.copy(
                activeConnectionName = agentState.activeConnectionName,
                isReady = agentState.isReady,
                isLoading = agentState.isLoading,
                isWorking = agentState.isWorking,
                isOverlayVisible = if (shouldCloseOverlay) false else it.isOverlayVisible,
                isIslandVisible = if (!agentState.isWorking && shouldCloseOverlay) true else it.isIslandVisible,
            )
        }
    }

    private fun handleControllerEvent(event: AgentControllerEvent) {
        when (event) {
            is AgentControllerEvent.Agent -> emitEvent(event.event.toOverlayLine())
            is AgentControllerEvent.Message -> emitEvent(event.message)
            is AgentControllerEvent.StateChanged -> Unit
        }
    }

    private fun emitEvent(message: String) {
        _state.update { it.copy(latestEvent = message) }
        _events.tryEmit(message)
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
}
