package com.artrubadur.teno.agent.tools.integrations.screen

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import java.security.MessageDigest

class ScreenNodeStore {
    private var referencesById: Map<String, ScreenNodeReference> = emptyMap()

    fun replace(capture: ScreenCapture) {
        referencesById = capture.nodes.flatten().associate { node ->
            node.id to ScreenNodeReference(
                fingerprint = node.fingerprint,
                packageName = capture.packageName,
                windowType = capture.windowType
            )
        }
    }

    fun clear() {
        referencesById = emptyMap()
    }

    fun reference(nodeId: String): ScreenNodeReference? = referencesById[nodeId]
}

class ScreenTreeReader(
    private val context: Context
) {
    fun read(): ScreenCapture {
        val service = ScreenAccessibilityBridge.service
            ?: error("Accessibility unavailable. Enable Teno accessibility.")
        val windows = service.windows
            ?: error("Window list unavailable. Call get_screen_tree again.")
        if (windows.isEmpty()) {
            error("No screen windows. Call get_screen_tree again.")
        }

        val prioritizedWindows = windows
            .filter { window ->
                window.type == AccessibilityWindowInfo.TYPE_APPLICATION ||
                        window.type == AccessibilityWindowInfo.TYPE_SYSTEM
            }
            .sortedBy { window ->
                window.type == AccessibilityWindowInfo.TYPE_SYSTEM
            }

        if (prioritizedWindows.isEmpty()) {
            error("No readable application or system windows.")
        }

        val (window, root) = prioritizedWindows.firstNotNullOfOrNull { window ->
            window.root?.let { root -> window to root }
        } ?: error("Window content unavailable. Call get_screen_tree again.")

        val nodes = simplify(
            node = root,
            path = "0"
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
            nodes = indexed,
            packageName = root.packageName?.toString(),
            windowType = window.type
        )
    }

    fun find(reference: ScreenNodeReference): ScreenNode {
        val capture = read()
        if (capture.packageName != reference.packageName) {
            error("Screen changed: package changed. Call get_screen_tree.")
        }
        if (capture.windowType != reference.windowType) {
            error("Screen changed: window type changed. Call get_screen_tree.")
        }

        val matches = capture.nodes.flatten().filter { node ->
            node.fingerprint == reference.fingerprint
        }

        if (matches.isEmpty()) error("Node not found")
        if (matches.size > 1) error("Ambiguous node")
        return matches.single()
    }

    private fun simplify(
        node: AccessibilityNodeInfo,
        path: String
    ): List<ScreenNode> {
        val bounds = Rect()
        node.getBoundsInScreen(bounds)

        val nodeClickable = node.isClickable || node.isLongClickable
        val children = (0 until node.childCount)
            .mapNotNull { index ->
                node.getChild(index)?.let { child ->
                    simplify(
                        node = child,
                        path = "$path.$index"
                    )
                }
            }
            .flatten()
            .sortedWith(nodeOrder)

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
                visible = node.isVisibleToUser,
                clickable = node.isClickable,
                longClickable = node.isLongClickable,
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
    val nodes: List<ScreenNode>,
    val packageName: String?,
    val windowType: Int
)

data class ScreenNodeReference(
    val fingerprint: String,
    val packageName: String?,
    val windowType: Int
)

data class ScreenNode(
    val id: String,
    val fingerprint: String,
    val role: String,
    val text: String?,
    val hint: String?,
    val bounds: Rect,
    val visible: Boolean,
    val clickable: Boolean,
    val longClickable: Boolean,
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
