package com.artrubadur.teno.agent.tools.impl.system

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolRisk
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class SystemSetClipboardTool(
    private val context: Context
) : Tool<SystemSetClipboardTool.Args> {

    override val name = "system_set_clipboard"

    override val title = "Set clipboard text"

    override val description =
        "Sets clipboard text"

    override val group = ToolGroup.SYSTEM

    override val risk = ToolRisk.SAFE

    override val enabled = true

    override val argsSerializer =
        Args.serializer()

    override suspend fun executeTyped(
        args: Args
    ): JsonObject {

        val clipboard =
            context.getSystemService(
                Context.CLIPBOARD_SERVICE
            ) as ClipboardManager

        val clip =
            ClipData.newPlainText(
                "agent",
                args.text
            )

        clipboard.setPrimaryClip(clip)

        return buildJsonObject {
            put("ok", true)
        }
    }

    @Serializable
    data class Args(
        val text: String
    )
}
