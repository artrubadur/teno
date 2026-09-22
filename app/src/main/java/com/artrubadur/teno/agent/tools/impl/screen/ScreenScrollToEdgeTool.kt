package com.artrubadur.teno.agent.tools.impl.screen

import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenEdgeScroller
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenScrollEdge
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ScreenScrollToEdgeTool(
    private val scroller: ScreenEdgeScroller,
) : Tool<ScreenScrollToEdgeTool.Args> {
    override val name = "screen_scroll_to_edge"
    override val title = "Scroll to screen edge"
    override val description =
        "Scrolls the page to an edge; use bottom for scrolling to the end"
    override val group = ToolGroup.SCREEN
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        scroller.scrollTo(args.edge)
        return buildJsonObject {
            put("ok", true)
        }
    }

    @Serializable
    data class Args(
        val edge: ScreenScrollEdge,
    )
}
