package com.artrubadur.teno.agent.tools.integrations.screen

import android.view.accessibility.AccessibilityNodeInfo

class ScreenEnterPresser(
    private val reader: ScreenTreeReader,
) {
    fun press() {
        val node = reader.read().nodes
            .flatten()
            .firstOrNull { it.node.isFocused && it.node.isEditable }
            ?: error("No focused editable field found")

        if (!node.node.performAction(
                AccessibilityNodeInfo.AccessibilityAction.ACTION_IME_ENTER.id
            )
        ) {
            error("Enter action failed")
        }
    }
}
