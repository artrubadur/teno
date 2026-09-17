package com.artrubadur.teno.ui.overlays.agent

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

class OverlayController(
    scope: CoroutineScope,
    application: Application,
) {
    private val agentController = AgentControllerClient(application)
    private var pendingWorkingCommand: AgentControllerCommand? = null

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
        _state.update {
            if (it.activeConnectionName == null || it.isReady) {
                it
            } else {
                it.copy(isLoading = true, isReady = false)
            }
        }
    }

    fun terminateConnection() {
        pendingWorkingCommand = null
        agentController.send(AgentControllerCommand.TerminateConnection)
    }

    fun onSend() {
        val current = _state.value
        val prompt = current.input.trim()

        if (!current.canSend) return

        pendingWorkingCommand = AgentControllerCommand.SendMessage(prompt)
        _state.update {
            it.copy(
                input = "",
                isOverlayVisible = true,
                isIslandVisible = true,
                isWorking = true,
                focusInput = false,
                controllerEvents = emptyList(),
            )
        }
    }

    fun stopWork() {
        pendingWorkingCommand = null
        agentController.send(AgentControllerCommand.StopWork)
        _state.update {
            it.copy(
                isLoading = false,
                isWorking = false,
            )
        }
    }

    fun approveConfirmation(confirmationId: String) {
        resumeAfterOverlayCollapse(AgentControllerCommand.ApproveConfirmation(confirmationId))
    }

    fun rejectConfirmation(confirmationId: String) {
        resumeAfterOverlayCollapse(AgentControllerCommand.RejectConfirmation(confirmationId))
    }

    private fun resumeAfterOverlayCollapse(command: AgentControllerCommand) {
        if (_state.value.isWorking) return
        pendingWorkingCommand = command
        _state.update {
            it.copy(isWorking = true, focusInput = false)
        }
    }

    fun onOverlayCollapsed() {
        val command = pendingWorkingCommand ?: return
        pendingWorkingCommand = null
        agentController.send(command)
    }

    fun close() {
        pendingWorkingCommand = null
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
                isIslandVisible = true,
                focusInput = focus,
            )
        }
    }

    fun onOutsideClick() {
        if (_state.value.isWorking) return

        _state.update {
            it.copy(
                isIslandVisible = false,
                focusInput = false,
            )
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
                isWorking = agentState.isWorking || pendingWorkingCommand != null,
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
