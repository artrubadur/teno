package com.artrubadur.teno.agent.tools.impl.diagnostics

import android.util.Log
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolRisk
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class DiagnosticsWriteDebugLogTool : Tool<DiagnosticsWriteDebugLogTool.Args> {

    override val name = "diagnostics_write_debug_log"

    override val title = "Write debug log"

    override val description =
        "Writes a diagnostic entry to the app's developer log. " +
                "Use only when the user explicitly requests a log entry."
    override val group = ToolGroup.DIAGNOSTICS

    override val risk = ToolRisk.SAFE

    override val enabled = false

    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject {
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

    @Serializable
    data class Args(
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
}