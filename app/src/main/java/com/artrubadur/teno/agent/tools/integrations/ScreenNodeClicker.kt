package com.artrubadur.teno.agent.tools.integrations

import android.view.accessibility.AccessibilityNodeInfo

class ScreenNodeClicker(
    private val reader: ScreenTreeReader
) {
    fun click(reference: ScreenNodeReference) {
        val node = reader.find(reference)
        if (!node.node.isClickable) {
            error("Node is not clickable")
        }
        if (!node.enabled) {
            error("Node disabled")
        }

        if (!node.node.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
            error("Click failed")
        }
    }
}
