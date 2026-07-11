package com.artrubadur.tonemo.agent.tools.impl

import android.content.Context
import android.provider.Settings
import com.artrubadur.tonemo.agent.tools.Tool
import com.artrubadur.tonemo.agent.tools.ToolPermission
import com.artrubadur.tonemo.agent.tools.ToolRisk
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.roundToInt

class SetBrightnessTool(
    private val context: Context
) : Tool<SetBrightnessToolArgs> {

    override val name = "set_brightness"

    override val title = "Set brightness"

    override val description = "Sets system screen brightness as a value from 0.0 to 1.0"

    override val risk = ToolRisk.SAFE

    override val requiredPermissions = setOf(ToolPermission.WRITE_SETTINGS)

    override val argsSerializer = SetBrightnessToolArgs.serializer()

    override suspend fun executeTyped(args: SetBrightnessToolArgs): JsonObject {
        require(args.value in 0f..1f) { "value must be between 0.0 and 1.0" }
        require(Settings.System.canWrite(context)) { "permission is required" }

        val value = (args.value * MAX_BRIGHTNESS).roundToInt().coerceIn(0, MAX_BRIGHTNESS)
        Settings.System.putInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            value
        )

        return buildJsonObject {
            put("ok", true)
        }
    }

    private companion object {
        const val MAX_BRIGHTNESS = 255
    }
}

@Serializable
data class SetBrightnessToolArgs(
    val value: Float,
)
