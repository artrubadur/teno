package com.artrubadur.teno.agent.tools.impl.screen

import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeClicker
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeStore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ScreenLongClickScreenNodeTool(
    private val store: ScreenNodeStore,
    private val clicker: ScreenNodeClicker,
) : Tool<ScreenLongClickScreenNodeTool.Args> {
    override val name = "screen_long_click_screen_node"
    override val title = "Long click screen node"
    override val description = "Long clicks a visible long-clickable node using its node_id"
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

        clicker.longClick(reference)
        return buildJsonObject {
            put("ok", true)
            put("node_id", nodeId)
        }
    }

    @Serializable
    data class Args(
        @SerialName("node_id")
        val nodeId: String,
    )

    private fun String.extractNodeId(): String? = Regex("\\d+").find(this)?.value
}
