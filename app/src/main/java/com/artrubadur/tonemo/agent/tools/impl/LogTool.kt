package com.artrubadur.tonemo.agent.tools.impl

import android.util.Log
import com.artrubadur.tonemo.agent.tools.AgentTool
import com.artrubadur.tonemo.agent.tools.ToolResult
import com.artrubadur.tonemo.agent.tools.ToolRisk
import com.artrubadur.tonemo.agent.tools.toJsonSchema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class LogTool : AgentTool<LogToolArgs> {

    override val name = "log"

    override val description = "Writes a debug message to the app log for internal diagnostics."

    override val risk = ToolRisk.SAFE

    override val argsSerializer = LogToolArgs.serializer()

    override val argsSchema = argsSerializer.toJsonSchema()

    override suspend fun execute(args: LogToolArgs): ToolResult {
        when (args.level) {
            LogLevel.DEBUG -> Log.d("TonemoToolCall", args.message)
            LogLevel.INFO -> Log.i("TonemoToolCall", args.message)
            LogLevel.WARNING -> Log.w("TonemoToolCall", args.message)
            LogLevel.ERROR -> Log.e("TonemoToolCall", args.message)
        }

        return ToolResult.Success("""{"logged":true}""")
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
