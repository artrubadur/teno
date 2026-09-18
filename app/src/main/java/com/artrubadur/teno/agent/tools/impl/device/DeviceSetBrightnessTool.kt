package com.artrubadur.teno.agent.tools.impl.device

import android.content.Context
import android.provider.Settings
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolPermission
import com.artrubadur.teno.agent.tools.ToolRisk
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.roundToInt

class DeviceSetBrightnessTool(
    private val context: Context
) : Tool<DeviceSetBrightnessTool.Args> {

    override val name = "device_set_brightness"

    override val title = "Set brightness"

    override val description = "Sets system screen brightness as a value from 0.0 to 1.0"
    override val group = ToolGroup.DEVICE

    override val risk = ToolRisk.SAFE

    override val enabled = true

    override val requiredPermissions = setOf(ToolPermission.WRITE_SETTINGS)

    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
        require(args.value in 0f..1f) { "value must be between 0.0 and 1.0" }

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

    @Serializable
    data class Args(
        val value: Float,
    )
}

