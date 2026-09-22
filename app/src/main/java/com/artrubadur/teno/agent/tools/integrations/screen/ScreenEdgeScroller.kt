package com.artrubadur.teno.agent.tools.integrations.screen

import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.milliseconds

@Serializable
enum class ScreenScrollEdge {
    @SerialName("top")
    TOP,

    @SerialName("bottom")
    BOTTOM,

    @SerialName("left")
    LEFT,

    @SerialName("right")
    RIGHT,
}

class ScreenEdgeScroller(
    private val reader: ScreenTreeReader,
) {
    suspend fun scrollTo(edge: ScreenScrollEdge) {
        var count = 0
        while (count < MAX_SCROLLS) {
            val scrollable = reader.read().nodes
                .flatten()
                .asSequence()
                .filter { it.node.isScrollable }
                .maxByOrNull { it.bounds.width().toLong() * it.bounds.height() }
                ?: error("No scrollable node found")

            val action = when (edge) {
                ScreenScrollEdge.TOP -> AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
                ScreenScrollEdge.BOTTOM -> AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                ScreenScrollEdge.LEFT ->
                    AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_LEFT.id

                ScreenScrollEdge.RIGHT ->
                    AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_RIGHT.id
            }
            if (!scrollable.node.performAction(action)) return

            count++
            delay(100.milliseconds)
        }
    }

    private companion object {
        const val MAX_SCROLLS = 100
    }
}
