package com.artrubadur.teno.agent.tools.integrations.screen

import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo

class ScreenNodeTextInputter(
    private val reader: ScreenTreeReader,
) {
    fun setText(reference: ScreenNodeReference, text: String) {
        val node = reader.find(reference)
        if (!node.node.isEditable) {
            error("Node is not an editable field")
        }

        val arguments = Bundle().apply {
            putCharSequence(
                AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                text,
            )
        }
        if (!node.node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)) {
            error("Failed to set text")
        }
    }
}
