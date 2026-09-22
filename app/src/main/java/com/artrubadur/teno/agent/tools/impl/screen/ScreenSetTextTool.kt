package com.artrubadur.teno.agent.tools.impl.screen

import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeStore
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeTextInputter
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ScreenSetTextTool(
    private val store: ScreenNodeStore,
    private val inputter: ScreenNodeTextInputter,
) : Tool<ScreenSetTextTool.Args> {
    override val name = "screen_set_text"
    override val title = "Set screen text"
    override val description =
        "Replaces text in an editable field using the exact node_id from get_screen_tree"
    override val group = ToolGroup.SCREEN
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        val nodeId = args.nodeId.extractNodeId()
            ?: error("Invalid node_id. Use the exact id returned by get_screen_tree")
        val reference = store.reference(nodeId)
            ?: error("Call get_screen_tree first")

        inputter.setText(reference, args.text)
        return buildJsonObject {
            put("ok", true)
        }
    }

    @Serializable
    data class Args(
        @SerialName("node_id")
        val nodeId: String,
        val text: String,
    )

    private fun String.extractNodeId(): String? = Regex("\\d+").find(this)?.value
}
