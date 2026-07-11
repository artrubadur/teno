package com.artrubadur.tonemo.agent.tools.impl

import android.content.Context
import android.provider.Settings
import com.artrubadur.tonemo.agent.tools.NoArgs
import com.artrubadur.tonemo.agent.tools.Tool
import com.artrubadur.tonemo.agent.tools.ToolRisk
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.math.roundToInt

class GetBrightnessTool(
    private val context: Context
) : Tool<NoArgs> {

    override val name = "get_brightness"

    override val title = "Get brightness"

    override val description = "Returns current system brightness"

    override val risk = ToolRisk.SAFE

    override val argsSerializer = NoArgs.serializer()

    override suspend fun executeTyped(args: NoArgs): JsonObject {
        val systemValue =
            Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS)
                .coerceIn(0, MAX_BRIGHTNESS)

        return buildJsonObject {
            put("value", "${roundValue(systemValue)}%")
        }
    }

    private fun roundValue(systemValue: Int): Int =
        (systemValue.toFloat() / MAX_BRIGHTNESS * 100).roundToInt()

    private companion object {
        const val MAX_BRIGHTNESS = 255
    }
}
