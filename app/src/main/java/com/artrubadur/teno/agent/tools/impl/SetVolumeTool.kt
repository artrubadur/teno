package com.artrubadur.teno.agent.tools.impl

import android.content.Context
import android.media.AudioManager
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolRisk
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.roundToInt

class SetVolumeTool(
    private val context: Context
) : Tool<SetVolumeToolArgs> {

    override val name = "set_volume"

    override val title = "Set volume"

    override val description =
        "Sets system media volume as a value from 0.0 to 1.0"

    override val group = ToolGroup.SYSTEM

    override val risk = ToolRisk.SAFE

    override val argsSerializer = SetVolumeToolArgs.serializer()

    override suspend fun executeTyped(
        args: SetVolumeToolArgs
    ): JsonObject {
        require(args.value in 0f..1f) {
            "value must be between 0.0 and 1.0"
        }

        val audioManager =
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val max =
            audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        val volume =
            (args.value * max)
                .roundToInt()
                .coerceIn(0, max)

        audioManager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            volume,
            0
        )

        return buildJsonObject {
            put("ok", true)
        }
    }
}

@Serializable
data class SetVolumeToolArgs(
    val value: Float
)