package com.artrubadur.teno.agent.tools.impl.screen

import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeStore
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenNodeSwiper
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenSwipeDirection
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ScreenSwipeTool(
    private val store: ScreenNodeStore,
    private val swiper: ScreenNodeSwiper,
) : Tool<ScreenSwipeTool.Args> {
    override val name = "screen_swipe"
    override val title = "Swipe screen"
    override val description =
        "Swipes the screen in the specified direction; an empty node_id swipes at the screen center"
    override val group = ToolGroup.SCREEN
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        val nodeId = args.nodeId.trim().takeIf(String::isNotEmpty)?.let {
            it.extractNodeId() ?: error("Invalid node id")
        }
        val reference = nodeId?.let { store.reference(it) ?: error("Call get_screen_tree first") }
        swiper.swipe(reference, args.direction)
        return buildJsonObject {
            put("ok", true)
            nodeId?.let { put("node_id", it) }
        }
    }

    @Serializable
    data class Args(
        @SerialName("direction")
        val direction: ScreenSwipeDirection,
        @SerialName("node_id")
        val nodeId: String = "",
    )

    private fun String.extractNodeId(): String? = Regex("\\d+").find(this)?.value
}
