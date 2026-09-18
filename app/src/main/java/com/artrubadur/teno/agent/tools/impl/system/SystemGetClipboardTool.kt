package com.artrubadur.teno.agent.tools.impl.system

import android.content.ClipboardManager
import android.content.Context
import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolRisk
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SystemGetClipboardTool(
    private val context: Context
) : Tool<NoArgs> {

    override val name = "system_get_clipboard"

    override val title = "Get clipboard"

    override val description =
        "Returns current clipboard text"

    override val group = ToolGroup.SYSTEM

    override val risk = ToolRisk.SAFE

    override val enabled = true

    override val argsSerializer =
        NoArgs.serializer()

    override suspend fun executeTyped(
        args: NoArgs
    ): JsonObject {

        val clipboard =
            context.getSystemService(
                Context.CLIPBOARD_SERVICE
            ) as ClipboardManager

        val text =
            clipboard.primaryClip
                ?.getItemAt(0)
                ?.coerceToText(context)
                ?.toString()

        return buildJsonObject {
            put(
                "text",
                text ?: ""
            )
        }
    }
}
