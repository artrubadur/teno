package com.artrubadur.teno.agent.tools.impl.device

import android.content.Context
import android.media.AudioManager
import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolRisk
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.roundToInt

class DeviceGetVolumeTool(
    private val context: Context
) : Tool<NoArgs> {

    override val name = "device_get_volume"

    override val title = "Get volume"

    override val description = "Returns current system media volume"

    override val group = ToolGroup.DEVICE

    override val risk = ToolRisk.SAFE

    override val enabled = true

    override val argsSerializer = NoArgs.serializer()

    override suspend fun executeTyped(args: NoArgs): JsonObject {
        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val current =
            audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        val max =
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        return buildJsonObject {
            put("value", "${roundValue(current, max)}%")
        }
    }

    private fun roundValue(
        value: Int,
        max: Int
    ): Int {
        return (value.toFloat() / max * 100).roundToInt()
    }
}
