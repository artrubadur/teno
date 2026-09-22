package com.artrubadur.teno.agent.tools.impl.screen

import android.accessibilityservice.AccessibilityService
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.screen.ScreenAccessibilityBridge
import kotlinx.coroutines.delay
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.time.Duration.Companion.milliseconds

class ScreenPressNavigationButtonTool : Tool<ScreenPressNavigationButtonTool.Args> {
    override val name = "screen_press_navigation_button"
    override val title = "Press navigation button"
    override val description = "Presses a system navigation button"
    override val group = ToolGroup.SCREEN
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val requiredPermissions = setOf(ToolPermission.ACCESSIBILITY_SERVICE)
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        val service = ScreenAccessibilityBridge.service
            ?: error("Accessibility service is unavailable")
        val action = when (args.button) {
            Button.BACK -> AccessibilityService.GLOBAL_ACTION_BACK
            Button.HOME -> AccessibilityService.GLOBAL_ACTION_HOME
            Button.RECENTS -> AccessibilityService.GLOBAL_ACTION_RECENTS
        }
        delay(500.milliseconds)
        
        return buildJsonObject {
            put("ok", service.performGlobalAction(action))
        }
    }

    @Serializable
    data class Args(
        val button: Button,
    )

    @Serializable
    enum class Button {
        @SerialName("back")
        BACK,

        @SerialName("home")
        HOME,

        @SerialName("recents")
        RECENTS,
    }
}
