package com.artrubadur.tonemo.agent.tools.impl

import android.util.Log
import com.artrubadur.tonemo.agent.tools.Tool
import com.artrubadur.tonemo.agent.tools.ToolRisk
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
            LogLevel.DEBUG -> Log.d("TonemoToolCall", args.message)
            LogLevel.INFO -> Log.i("TonemoToolCall", args.message)
            LogLevel.WARNING -> Log.w("TonemoToolCall", args.message)
            LogLevel.ERROR -> Log.e("TonemoToolCall", args.message)
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
