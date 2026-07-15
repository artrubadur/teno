package com.artrubadur.teno.ui.overlay

import android.app.Application
import com.artrubadur.teno.agent.controller.AgentControllerClient
import com.artrubadur.teno.agent.controller.AgentControllerCommand
import com.artrubadur.teno.agent.controller.AgentControllerEvent
import com.artrubadur.teno.agent.controller.AgentControllerState
import kotlinx.coroutines.CoroutineScope
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
                controllerEvents = emptyList(),
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

    fun approveConfirmation(confirmationId: String) {
        agentController.send(AgentControllerCommand.ApproveConfirmation(confirmationId))
    }

    fun rejectConfirmation(confirmationId: String) {
        agentController.send(AgentControllerCommand.RejectConfirmation(confirmationId))
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
        if (event is AgentControllerEvent.StateChanged) {
            return
        }

        _state.update { it.copy(controllerEvents = it.controllerEvents + event) }
    }
}
