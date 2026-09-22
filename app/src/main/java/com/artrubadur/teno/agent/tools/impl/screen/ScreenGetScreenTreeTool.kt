package com.artrubadur.teno.agent.tools.impl.screen

import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNode
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeStore
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenTreeReader
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ScreenGetScreenTreeTool(
    private val reader: ScreenTreeReader,
    private val store: ScreenNodeStore
) : Tool<NoArgs> {

    override val name = "screen_get_screen_tree"

    override val title = "Get screen tree"

    override val description =
        "Returns visible and off-screen text, buttons, input fields, and other available screen elements."

    override val group = ToolGroup.SCREEN

    override val risk = ToolRisk.SAFE

    override val enabled = true

    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)

    override val argsSerializer = NoArgs.serializer()

    override suspend fun executeTyped(args: NoArgs): JsonObject {
        val capture = reader.read()

        store.replace(capture)

        return buildJsonObject {
            put("ok", true)
            capture.packageName?.let { put("package_name", it) }
            // put(
            //     "screen",
            //     buildJsonObject {
            //         put("width", capture.width)
            //         put("height", capture.height)
            //     }
            // )
            put("nodes", JsonArray(capture.nodes.map { it.toJson() }))
        }
    }

    private fun ScreenNode.toJson(): JsonObject =
        buildJsonObject {
            put("id", id)
            put("role", role)
            text?.let { put("text", it) }
            hint?.let { put("hint", it) }
            put("visible", visible)
            // put(
            //     "bounds",
            //     JsonArray(
            //         listOf(
            //             JsonPrimitive(bounds.left),
            //             JsonPrimitive(bounds.top),
            //             JsonPrimitive(bounds.right),
            //             JsonPrimitive(bounds.bottom)
            //         )
            //     )
            // )
            if (clickable) put("clickable", true)
            if (longClickable) put("long_clickable", true)
            if (!enabled) put("enabled", false)
            if (focused) put("focused", true)
            checked?.let { put("checked", it) }
            if (selected) put("selected", true)
            if (children.isNotEmpty()) {
                put("children", JsonArray(children.map { it.toJson() }))
            }
        }
}
