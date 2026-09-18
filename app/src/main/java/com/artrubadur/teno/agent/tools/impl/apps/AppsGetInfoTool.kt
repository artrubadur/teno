package com.artrubadur.teno.agent.tools.impl.apps

import android.content.Context
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.apps.appMetadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

class AppsGetInfoTool(private val context: Context) : Tool<AppsGetInfoTool.Args> {
    override val name = "apps_get_info"
    override val title = "Get app info"
    override val description = "Returns information about an installed app"
    override val group = ToolGroup.APPS
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val argsSerializer = Args.serializer()

    override suspend fun executeTyped(args: Args): JsonObject =
        context.appMetadata(args.packageName)

    @Serializable
    data class Args(
        @SerialName("package_name")
        val packageName: String,
    )
}
