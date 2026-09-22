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
        "Returns screen nodes with numeric ids in top-to-bottom order"

    override val group = ToolGroup.SCREEN

    override val risk = ToolRisk.SAFE

    override val enabled = true

    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)

    override val argsSerializer = NoArgs.serializer()

    override suspend fun executeTyped(args: NoArgs): JsonObject {
        val capture = reader.read()
        if (capture.nodes.isEmpty()) {
            error("Screen tree is empty. Wait for the screen to finish loading, then call get_screen_tree again.")
        }

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
            put("clickable", clickable)
            put("long_clickable", longClickable)
            put("enabled", enabled)
            if (focused) put("focused", true)
            checked?.let { put("checked", it) }
            if (selected) put("selected", true)
            if (children.isNotEmpty()) {
                put("children", JsonArray(children.map { it.toJson() }))
            }
        }
}
