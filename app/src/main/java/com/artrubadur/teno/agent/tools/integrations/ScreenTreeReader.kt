package com.artrubadur.teno.agent.tools.integrations

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import java.security.MessageDigest

class ScreenNodeStore {
    private var fingerprintsById: Map<String, String> = emptyMap()

    fun replace(nodes: List<ScreenNode>) {
        fingerprintsById = nodes.flatten().associate { node ->
            node.id to node.fingerprint
        }
    }

    fun clear() {
        fingerprintsById = emptyMap()
    }

    fun fingerprint(nodeId: String): String? = fingerprintsById[nodeId]
}

class ScreenTreeReader(
    private val context: Context
) {
    fun read(): ScreenCapture {
        val service = ScreenAccessibilityBridge.service
            ?: error("Accessibility service is not connected. Enable accessibility access for Teno and try again.")
        val windows = service.windows
            ?: error("Screen windows are unavailable. Wait for the screen to finish changing, then call get_screen_tree again.")
        if (windows.isEmpty()) {
            error("No screen windows are available. Wait for the screen to finish changing, then call get_screen_tree again.")
        }

        val applicationWindows = windows.filter {
            it.type == AccessibilityWindowInfo.TYPE_APPLICATION
        }
        if (applicationWindows.isEmpty()) {
            error("No app screen is available to read. Close any system dialog or wait for the screen to finish changing, then call get_screen_tree again.")
        }

        val root = applicationWindows.firstNotNullOfOrNull { it.root }
            ?: error("The app screen is present, but its content is not available yet. Wait for it to finish loading, then call get_screen_tree again.")

        val nodes = simplify(
            node = root,
            path = "0",
            ancestorClickable = false
        )
            .sortedWith(nodeOrder)

        val ids = Ids()
        val occurrences = mutableMapOf<String, Int>()
        val indexed = nodes.map { node ->
            node.withIds(ids, occurrences)
        }

        val metrics = context.resources.displayMetrics
        return ScreenCapture(
            width = metrics.widthPixels,
            height = metrics.heightPixels,
            nodes = indexed
        )
    }

    fun find(fingerprint: String): ScreenNode {
        val matches = read().nodes.flatten().filter { node ->
            node.fingerprint == fingerprint
        }

        if (matches.isEmpty()) error("Node not found")
        if (matches.size > 1) error("Ambiguous node")
        return matches.single()
    }

    private fun simplify(
        node: AccessibilityNodeInfo,
        path: String,
        ancestorClickable: Boolean
    ): List<ScreenNode> {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val nodeClickable = node.isClickable || node.isLongClickable
        val children = (0 until node.childCount)
            .mapNotNull { index ->
                node.getChild(index)?.let { child ->
                    simplify(
                        node = child,
                        path = "$path.$index",
                        ancestorClickable = ancestorClickable || nodeClickable
                    )
                }
            }
            .flatten()
            .sortedWith(nodeOrder)

        if (!node.isVisibleToUser || bounds.isEmpty) return children

        val role = node.role()
        val text = node.cleanText()
        val hint = node.hintText?.clean()
        val useful =
            text != null ||
                    hint != null ||
                    nodeClickable ||
                    node.isCheckable ||
                    node.isFocused ||
                    role in containerRoles

        if (!useful) return children

        val baseFingerprint = listOf(
            role,
            text.orEmpty(),
            hint.orEmpty(),
            node.viewIdResourceName?.substringAfterLast('/').orEmpty(),
            path
        ).joinToString(separator = "|")

        if (
            role == "unknown" &&
            text == null &&
            hint == null &&
            children.size == 1 &&
            !node.isCheckable &&
            !node.isFocused &&
            !node.isSelected
        ) {
            return children
        }

        return listOf(
            ScreenNode(
                id = "",
                fingerprint = baseFingerprint,
                role = role,
                text = text,
                hint = hint,
                bounds = bounds,
                clickable = nodeClickable || ancestorClickable,
                enabled = node.isEnabled,
                focused = node.isFocused,
                checked = node.checkedOrNull(),
                selected = node.isSelected,
                node = node,
                children = children
            )
        )
    }

    private fun ScreenNode.withIds(
        ids: Ids,
        occurrences: MutableMap<String, Int>
    ): ScreenNode {
        val occurrence = occurrences.getOrDefault(fingerprint, 0)
        occurrences[fingerprint] = occurrence + 1
        val stableFingerprint = hash("$fingerprint|$occurrence")

        return copy(
            id = ids.next(),
            fingerprint = stableFingerprint,
            children = children.map { child ->
                child.withIds(ids, occurrences)
            }
        )
    }

    private class Ids {
        private var next = 1

        fun next(): String = (next++).toString()
    }

    private companion object {
        val containerRoles = setOf("list")

        val nodeOrder = compareBy<ScreenNode>(
            { it.bounds.top },
            { it.bounds.left },
            { it.bounds.bottom },
            { it.bounds.right }
        )
    }
}

data class ScreenCapture(
    val width: Int,
    val height: Int,
    val nodes: List<ScreenNode>
)

data class ScreenNode(
    val id: String,
    val fingerprint: String,
    val role: String,
    val text: String?,
    val hint: String?,
    val bounds: Rect,
    val clickable: Boolean,
    val enabled: Boolean,
    val focused: Boolean,
    val checked: Boolean?,
    val selected: Boolean,
    val node: AccessibilityNodeInfo,
    val children: List<ScreenNode>
)

fun List<ScreenNode>.flatten(): List<ScreenNode> =
    flatMap { node -> listOf(node) + node.children.flatten() }

private fun AccessibilityNodeInfo.role(): String {
    val className = className?.toString().orEmpty().lowercase()
    return when {
        "button" in className -> "button"
        "edittext" in className -> "input"
        "checkbox" in className -> "checkbox"
        "switch" in className -> "switch"
        "image" in className -> "image"
        "recyclerview" in className || "listview" in className -> "list"
        "textview" in className -> "text"
        else -> "unknown"
    }
}

private fun AccessibilityNodeInfo.cleanText(): String? {
    return listOf(text, contentDescription)
        .firstNotNullOfOrNull { value -> value?.clean() }
}

private fun AccessibilityNodeInfo.checkedOrNull(): Boolean? {
    if (!isCheckable) return null

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        checked == AccessibilityNodeInfo.CHECKED_STATE_TRUE
    } else {
        @Suppress("DEPRECATION")
        isChecked
    }
}

private fun CharSequence.clean(): String? {
    val value = toString()
        .replace(Regex("\\s+"), " ")
        .trim()
        .ifBlank { null }

    return value?.let {
        if (it.length > 80) {
            it.take(80) + "... [truncated by get_screen_tree]"
        } else {
            it
        }
    }
}

private fun hash(value: String): String {
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(value.toByteArray())

    return bytes
        .take(8)
        .joinToString(separator = "") { byte ->
            "%02x".format(byte)
        }
}
