package com.artrubadur.teno.agent.tools.impl.apps

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.apps.openApplication
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class AppsOpenTool(private val context: Context) : Tool<AppsOpenTool.Args> {
    override val name = "apps_open"
    override val title = "Open app"
    override val description = "Opens an installed app"
    override val group = ToolGroup.APPS
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject =
        context.openApplication(args.packageName)

    @Serializable
    data class Args(
        @SerialName("package_name")
        val packageName: String,
    )
}
