package com.artrubadur.teno.agent.tools.impl.screen

import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeStore
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenTreeReader
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ScreenGetScreenNodeTool(
    private val store: ScreenNodeStore,
    private val reader: ScreenTreeReader
) : Tool<ScreenGetScreenNodeTool.Args> {

    override val name = "screen_get_screen_node"

    override val title = "Get screen node"

    override val description =
        "Returns complete details for a node by its node_id."

    override val group = ToolGroup.SCREEN

    override val risk = ToolRisk.SAFE

    override val enabled = true

    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)

    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        val nodeId = args.nodeId.extractNodeId()
            ?: error("Invalid node id")
        val reference = store.reference(nodeId)
            ?: error("Call get_screen_tree first")
        val node = reader.find(reference)
        val source = node.node

        return buildJsonObject {
            put("ok", true)
            put("node_id", nodeId)
            put("role", node.role)
            source.text?.let { put("text", it.toString()) }
            source.contentDescription?.let { put("content_description", it.toString()) }
            source.hintText?.let { put("hint", it.toString()) }
            put(
                "bounds",
                buildJsonObject {
                    put("left", node.bounds.left)
                    put("top", node.bounds.top)
                    put("right", node.bounds.right)
                    put("bottom", node.bounds.bottom)
                }
            )
            put("clickable", node.clickable)
            put("long_clickable", node.longClickable)
            put("enabled", node.enabled)
            put("focused", node.focused)
            node.checked?.let { put("checked", it) }
            put("selected", node.selected)
        }
    }

    @Serializable
    data class Args(
        @SerialName("node_id")
        val nodeId: String
    )

    private fun String.extractNodeId(): String? =
        Regex("\\d+").find(this)?.value
}
