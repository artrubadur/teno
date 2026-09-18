package com.artrubadur.teno.agent.tools.impl.screen

import android.accessibilityservice.AccessibilityService
import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenAccessibilityBridge
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ScreenCollapseNotificationShadeTool : Tool<NoArgs> {
    override val name = "screen_collapse_notification_shade"
    override val title = "Collapse notification shade"
    override val description = "Collapses the notification shade"
    override val group = ToolGroup.SCREEN
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)
    override val argsSerializer = NoArgs.serializer()

    override suspend fun executeTyped(args: NoArgs): JsonObject {
        val service = ScreenAccessibilityBridge.service
            ?: error("Accessibility service is unavailable")

        return buildJsonObject {
            put(
                "ok",
                service.performGlobalAction(
                    AccessibilityService.GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE
                )
            )
        }
    }
}
