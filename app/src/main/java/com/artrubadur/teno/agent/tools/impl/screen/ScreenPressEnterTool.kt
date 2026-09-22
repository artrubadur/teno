package com.artrubadur.teno.agent.tools.impl.screen

import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenEnterPresser
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.milliseconds

class ScreenPressEnterTool(
    private val presser: ScreenEnterPresser,
) : Tool<NoArgs> {
    override val name = "screen_press_enter"
    override val title = "Press Enter"
    override val description = "Presses Enter on the focused editable field"
    override val group = ToolGroup.SCREEN
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)
    override val argsSerializer = NoArgs.serializer()

    override suspend fun executeTyped(args: NoArgs): JsonObject {
        presser.press()
        delay(500.milliseconds)

        return buildJsonObject { put("ok", true) }
    }
}
