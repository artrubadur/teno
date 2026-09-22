package com.artrubadur.teno.agent.tools.impl.screen

import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeStore
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeTextInputter
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.milliseconds

class ScreenSetTextTool(
    private val store: ScreenNodeStore,
    private val inputter: ScreenNodeTextInputter,
) : Tool<ScreenSetTextTool.Args> {
    override val name = "screen_set_text"
    override val title = "Set screen text"
    override val description =
        "Replaces text in an editable field using its numeric node_id from get_screen_tree"
    override val group = ToolGroup.SCREEN
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        val nodeId = args.nodeId.extractNodeId()
            ?: error("Invalid node_id. Call get_screen_tree again and use its numeric id")
        val reference = store.reference(nodeId)
            ?: error("Node id is unavailable. Call get_screen_tree again")

        inputter.setText(reference, args.text)
        delay(500.milliseconds)

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
