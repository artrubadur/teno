package com.artrubadur.teno.agent.tools.impl.apps

import android.content.Context
import com.artrubadur.teno.agent.tools.NoArgs
import com.artrubadur.teno.agent.tools.Tool
import com.artrubadur.teno.agent.tools.ToolGroup
import com.artrubadur.teno.agent.tools.ToolRisk
import com.artrubadur.teno.agent.tools.integrations.apps.launchableApplications
import com.artrubadur.teno.agent.tools.integrations.apps.summaryJson
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject

class AppsListInstalledTool(private val context: Context) : Tool<NoArgs> {
    override val name = "apps_list_installed"
    override val title = "List installed apps"
    override val description = "Lists installed apps available to open"
    override val group = ToolGroup.APPS
    override val risk = ToolRisk.SAFE
    override val enabled = true
    override val argsSerializer = NoArgs.serializer()

    override suspend fun executeTyped(args: NoArgs): JsonObject {
        val apps = context.launchableApplications()
        return kotlinx.serialization.json.buildJsonObject {
            put("apps", JsonArray(apps.map { context.summaryJson(it) }))
        }
    }
}
