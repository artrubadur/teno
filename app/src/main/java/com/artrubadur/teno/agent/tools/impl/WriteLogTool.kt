package com.artrubadur.teno.agent.tools.impl

import android.util.Log
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolRisk
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class LogTool : Tool<WriteLogToolArgs> {

    override val name = "write_log"

    override val title = "Write log"

    override val description =
        "Writes a message to the internal app log"

    override val risk = ToolRisk.SAFE

    override val argsSerializer = WriteLogToolArgs.serializer()

    override suspend fun executeTyped(args: WriteLogToolArgs): JsonObject {
        when (args.level) {
            LogLevel.DEBUG -> Log.d("TenoToolCall", args.message)
            LogLevel.INFO -> Log.i("TenoToolCall", args.message)
            LogLevel.WARNING -> Log.w("TenoToolCall", args.message)
            LogLevel.ERROR -> Log.e("TenoToolCall", args.message)
        }

        return buildJsonObject {
            put("ok", true)
        }
    }
}

@Serializable
data class WriteLogToolArgs(
    val message: String,
    val level: LogLevel = LogLevel.DEBUG
)

@Serializable
enum class LogLevel {
    @SerialName("debug")
    DEBUG,

    @SerialName("info")
    INFO,

    @SerialName("warning")
    WARNING,

    @SerialName("error")
    ERROR
}
