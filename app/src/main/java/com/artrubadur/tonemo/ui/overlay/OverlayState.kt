package com.artrubadur.tonemo.ui.overlay

import com.artrubadur.tonemo.connection.Connection

data class OverlayState(
    val isOverlayVisible: Boolean = false,
    val isIslandVisible: Boolean = true,

    val input: String = "",
    val focusInput: Boolean = false,

    val activeConnection: Connection? = null,
    val isReady: Boolean = false,
    val isWorking: Boolean = false,
    val isLoading: Boolean = false,
    val latestEvent: String? = null,
) {
    val canSend: Boolean
        get() = input.isNotBlank() && activeConnection != null && isReady && !isWorking && !isLoading
}
