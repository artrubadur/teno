package com.artrubadur.teno.ui.overlay

import com.artrubadur.teno.agent.controller.AgentControllerEvent

data class OverlayState(
    val isOverlayVisible: Boolean = false,
    val isIslandVisible: Boolean = true,

    val input: String = "",
    val focusInput: Boolean = false,

    val activeConnectionName: String? = null, val isReady: Boolean = false,
    val isWorking: Boolean = false,
    val isLoading: Boolean = false,
    val controllerEvents: List<AgentControllerEvent> = emptyList(),
) {
    val canSend: Boolean
        get() = input.isNotBlank() && isReady && !isWorking && !isLoading
}
