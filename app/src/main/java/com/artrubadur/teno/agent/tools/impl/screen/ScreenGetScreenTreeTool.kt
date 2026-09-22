package com.artrubadur.teno.agent.tools.impl.screen

import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNode
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeStore
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenTreeReader
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenTreeScope
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ScreenGetScreenTreeTool(
    private val reader: ScreenTreeReader,
    private val store: ScreenNodeStore
) : Tool<ScreenGetScreenTreeTool.Args> {

    override val name = "screen_get_screen_tree"

    override val title = "Get screen tree"

    override val description =
        "Returns screen nodes with numeric ids; optional scopes filter by focusable, clickable, long_clickable, or visible"

    override val group = ToolGroup.SCREEN

    override val risk = ToolRisk.SAFE

    override val enabled = true

    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)

    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        val capture = reader.read(args.scopes)
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

    @Serializable
    data class Args(
        val scopes: List<ScreenTreeScope> = emptyList(),
    )

    private fun ScreenNode.toJson(): JsonObject =
        buildJsonObject {
            put("id", id)
            put("role", role)
            if (visible) put("visible", true)
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
            if (focusable) put("focusable", true)
            if (clickable) put("clickable", true)
            if (longClickable) put("long_clickable", true)
            if (enabled) put("enabled", true)
            if (focused) put("focused", true)
            checked?.let { put("checked", it) }
            if (selected) put("selected", true)
            if (children.isNotEmpty()) {
                put("children", JsonArray(children.map { it.toJson() }))
            }
        }
}
