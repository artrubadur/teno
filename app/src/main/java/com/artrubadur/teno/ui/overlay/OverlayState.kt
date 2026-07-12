package com.artrubadur.teno.ui.overlay

data class OverlayState(
    val isOverlayVisible: Boolean = false,
    val isIslandVisible: Boolean = true,

    val input: String = "",
    val focusInput: Boolean = false,

    val activeConnectionName: String? = null, val isReady: Boolean = false,
    val isWorking: Boolean = false,
    val isLoading: Boolean = false,
    val latestEvent: String? = null,
) {
    val isActivated: Boolean
        get() = activeConnectionName != null

    val canSend: Boolean
        get() = input.isNotBlank() && isReady && !isWorking && !isLoading
}