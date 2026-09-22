package com.artrubadur.teno.agent.tools.integrations.screen

import android.view.accessibility.AccessibilityNodeInfo

class ScreenNodeScroller(
    private val reader: ScreenTreeReader,
) {
    fun scrollTo(reference: ScreenNodeReference) {
        val node = reader.find(reference)
        if (!node.node.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN.id)) {
            error("Failed to scroll to node")
        }
    }
}
