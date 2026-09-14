package com.artrubadur.teno.agent.tools.impl

import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.ScreenNodeClicker
import com.artrubadur.teno.agent.tools.integrations.ScreenNodeStore
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ClickScreenNodeTool(
    private val store: ScreenNodeStore,
    private val clicker: ScreenNodeClicker
) : Tool<ClickScreenNodeTool.Args> {

    override val name = "click_screen_node"

    override val title = "Click screen node"

    override val description =
        "Clicks a visible element on the screen using its node_id."

    override val group = ToolGroup.SCREEN

    override val risk = ToolRisk.SAFE

    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)

    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        val nodeId = args.nodeId.extractNodeId()
            ?: error("Invalid node id")

        val fingerprint = store.fingerprint(nodeId)
            ?: error("Call get_screen_tree first")

        clicker.click(fingerprint)
        return buildJsonObject {
            put("ok", true)
            put("node_id", nodeId)
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
