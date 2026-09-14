package com.artrubadur.teno.agent.tools.integrations

import android.view.accessibility.AccessibilityNodeInfo

class ScreenNodeClicker(
    private val reader: ScreenTreeReader
) {
    fun click(fingerprint: String) {
        val node = reader.find(fingerprint)
        if (!node.enabled) {
            error("Node disabled")
        }

        if (!node.node.clickSelfOrParent()) {
            error("Click failed")
        }
    }

    private fun AccessibilityNodeInfo.clickSelfOrParent(): Boolean {
        var current: AccessibilityNodeInfo? = this
        while (current != null) {
            if (
                current.isEnabled &&
                current.isClickable &&
                current.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            ) {
                return true
            }
            current = current.parent
        }
        return false
    }
}
