package com.artrubadur.tonemo.agent.tools.impl

import android.util.Log
import com.artrubadur.tonemo.agent.tools.Tool
import com.artrubadur.tonemo.agent.tools.ToolRisk
import com.artrubadur.tonemo.agent.tools.toJsonSchema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class LogTool : Tool<LogToolArgs> {

    override val name = "write_log"

    override val description =
        "Use when the user explicitly asks to record a diagnostic or debug message. Writes the provided message to the internal app log."

    override val risk = ToolRisk.SAFE

    override val argsSerializer = LogToolArgs.serializer()

    override val argsSchema = argsSerializer.toJsonSchema()

    override suspend fun executeTyped(args: LogToolArgs): Map<String, Boolean> {
        when (args.level) {
            LogLevel.DEBUG -> Log.d("TonemoToolCall", args.message)
            LogLevel.INFO -> Log.i("TonemoToolCall", args.message)
            LogLevel.WARNING -> Log.w("TonemoToolCall", args.message)
            LogLevel.ERROR -> Log.e("TonemoToolCall", args.message)
        }

        return mapOf("ok" to true)
    }
}

@Serializable
data class LogToolArgs(
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
