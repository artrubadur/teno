package com.artrubadur.teno.agent.tools.integrations.screen

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

    fun longClick(reference: ScreenNodeReference) {
        val node = reader.find(reference)
        if (!node.node.isLongClickable) {
            error("Node is not long clickable")
        }
        if (!node.enabled) {
            error("Node disabled")
        }

        if (!node.node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)) {
            error("Long click failed")
        }
    }
}
