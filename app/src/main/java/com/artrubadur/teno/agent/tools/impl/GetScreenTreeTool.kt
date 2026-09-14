package com.artrubadur.teno.agent.tools.impl

import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.ScreenNode
import com.artrubadur.teno.agent.tools.integrations.ScreenNodeStore
import com.artrubadur.teno.agent.tools.integrations.ScreenTreeReader
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GetScreenTreeTool(
    private val reader: ScreenTreeReader,
    private val store: ScreenNodeStore
) : Tool<NoArgs> {

    override val name = "get_screen_tree"

    override val title = "Get screen tree"

    override val description =
        "Returns nodes for visible text, buttons, input fields, and other screen elements."

    override val group = ToolGroup.SCREEN

    override val risk = ToolRisk.SAFE

    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)

    override val argsSerializer = NoArgs.serializer()

    override suspend fun executeTyped(args: NoArgs): JsonObject {
        val capture = reader.read()

        store.replace(capture.nodes)

        return buildJsonObject {
            put("ok", true)
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
            if (!enabled) put("enabled", false)
            if (focused) put("focused", true)
            checked?.let { put("checked", it) }
            if (selected) put("selected", true)
            if (children.isNotEmpty()) {
                put("children", JsonArray(children.map { it.toJson() }))
            }
        }
}
